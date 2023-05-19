package server;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;



public class Server
{
    final int NUMBER_OF_PLAYERS_PER_GAME = 4;
    final int NUMBER_OF_ROUNDS = 5;
    public final int port;


    public final HashMap<String,String> tokenToUsername;
    public final List<Player> players_waiting;

    public final List<Game> games;

    public PlayerDatabase db;
    public boolean running = true;
    public final ReadWriteLock playerQueueLock;

    public Server( int port )
    {
        this.port = port;
        this.players_waiting = new ArrayList<>();
        this.db = new PlayerDatabase("database.txt");
        this.tokenToUsername = new HashMap<>();
        this.games = new ArrayList<>();
        this.playerQueueLock = new ReentrantReadWriteLock();
    }

    public boolean playerIsWaiting(Player player){
        boolean isPlayerWaiting = false;
        boolean needToReleaseLock = true;
        playerQueueLock.readLock().lock();
        try {
            int nrPlayers =  players_waiting.size();
            for (int i = 0; i < nrPlayers; i++) {
                Player playerWaiting = players_waiting.get(i);
                if (Objects.equals(player.getUsername(), playerWaiting.getUsername())) {
                    player.setMaxSkillGap(playerWaiting.getMaxSkillGap());
                    playerQueueLock.readLock().unlock();
                    needToReleaseLock = false;
                    playerQueueLock.writeLock().lock(); //player could have been removed between releasing the read lock and obtaining the write lock
                                                        //another player could have been added at the end of the array or removed from anywhere in the list
                                                        //new index can only be lower than before
                    try {
                        for(;i>=0;i--){ //search starting from index where we found the player with the read lock
                            if (players_waiting.get(i) == playerWaiting) {
                                isPlayerWaiting = true;
                                break;
                            }
                        }
                        if(isPlayerWaiting)
                            players_waiting.set(i, player);
                    }
                    finally{
                        playerQueueLock.writeLock().unlock();
                    }
                    if(isPlayerWaiting) {
                        player.sendMessage("QUEUE_POSITION_" + (i + 1));
                        player.sendMessage("PLAYERS_WAITING_" + nrPlayers);
                        playerWaiting.unauthenticate();
                        playerWaiting.authenticationState = AuthenticationState.INITIAL_STATE;
                        playerWaiting.sendMessage("REMOTE_LOG_IN");
                    }
                    break;
                }
            }
        }
        finally {
            if(needToReleaseLock)
                playerQueueLock.readLock().unlock();
        }
        return isPlayerWaiting;
    }

    public void addPlayerToQueue(Player player){
        int nrPlayersWaiting;
        playerQueueLock.writeLock().lock();
        try{
            players_waiting.add(player);
            nrPlayersWaiting = players_waiting.size();
        }
        finally{
            playerQueueLock.writeLock().unlock();
        }
        player.sendMessage("QUEUE_POSITION_" + nrPlayersWaiting);
        player.sendMessage("PLAYERS_WAITING_" + nrPlayersWaiting);
    }
    public boolean playerIsPlaying(Player player){
        for(Game game:games){
            if(game.gameHasPlayer(player)) {
                return true;
            }
        }
        return false;
    }
    public void stopServer()
    {
        running = false;
    }

    public void startThreads()
    {
        try {
            Matchmaker matchmaker = new Matchmaker(this);
            matchmaker.start();
            Queue<Socket> socketQueue = new ArrayBlockingQueue<>(1024); //move 1024 to ServerConfig

            SocketAccepter socketAccepter = new SocketAccepter(this, socketQueue);
            SocketProcessor socketProcessor = new SocketProcessor(this,socketQueue);
            Thread accepterThread = new Thread(socketAccepter);
            Thread processorThread = new Thread(socketProcessor);

            accepterThread.start();
            processorThread.start();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }


    private class Matchmaker extends Thread {

        private final Server server;
        public Matchmaker(Server server){
            this.server = server;
        }

        @Override
        public void run() {
            boolean startedGame=false;
            while (running) {
                if(!startedGame) {
                    try {
                        long CHECK_INTERVAL_MS = 5000;
                        Thread.sleep(CHECK_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    startedGame = false;
                }


                playerQueueLock.readLock().lock();
                if (players_waiting.size() >= NUMBER_OF_PLAYERS_PER_GAME) {
                    List<Player> matchedPlayers = new ArrayList<>();
                    List<Player> idlePlayers = new ArrayList<>();
                    for (Player player : players_waiting) {
                        if(player.timeSinceDisconnect()>=0){
                            idlePlayers.add(player);
                            continue;
                        }

                        matchedPlayers.clear();
                        matchedPlayers.add(player);

                        for (Player otherPlayer : players_waiting) {
                            if (player.getSocketId() == otherPlayer.getSocketId() || otherPlayer.timeSinceDisconnect()>=0) {
                                continue;
                            }

                            int skillDifference = Math.abs(player.getSkillLevel() - otherPlayer.getSkillLevel());
                            if (skillDifference <= player.getMaxSkillGap() && skillDifference <= otherPlayer.getMaxSkillGap()) {
                                matchedPlayers.add(otherPlayer);

                                if (matchedPlayers.size() == NUMBER_OF_PLAYERS_PER_GAME) {
                                    break;
                                }
                            }
                        }

                        if (matchedPlayers.size() == NUMBER_OF_PLAYERS_PER_GAME) {
                            startedGame = true;
                            break;
                        }
                    }

                    playerQueueLock.readLock().unlock();
                    playerQueueLock.writeLock().lock();
                    try {
                        for (Player player : players_waiting) {
                            boolean playerWillBeRemoved = false;

                            if(startedGame) {
                                for (int i = 0; i < matchedPlayers.size(); i++) {
                                    Player matchedPlayer = matchedPlayers.get(i);
                                    if (Objects.equals(player.getUsername(), matchedPlayer.getUsername())) {
                                        playerWillBeRemoved = true;
                                        if (player != matchedPlayer)
                                            matchedPlayers.set(i, player);
                                        //between releasing the read lock and obtaining the write lock
                                        //some player could have logged in and replaced the player object
                                        //in the players_waiting array, so matchedPlayers needs to be updated
                                    }
                                }
                            }

                            for (Player idlePlayer : idlePlayers) {
                                if (Objects.equals(player.getUsername(), idlePlayer.getUsername()) && player == idlePlayer &&
                                        player.timeSinceDisconnect() > 2 * 60 * 1000) { //2 minutes
                                    players_waiting.remove(player);
                                    System.out.println("Player " + player.getUsername() + " has been idle for too long, so he was removed from the queue");
                                }
                            }

                            if(!playerWillBeRemoved)
                                player.increaseSkillGap();
                        }

                        if(startedGame)
                            players_waiting.removeAll(matchedPlayers);

                    }

                    finally {
                        playerQueueLock.writeLock().unlock();
                    }

                    if(startedGame) {
                        Game game = new Game(matchedPlayers, NUMBER_OF_ROUNDS, this.server);
                        games.add(game);
                        game.start();
                    }
                }
                else
                    playerQueueLock.readLock().unlock();



            }
        }
    }
    public static void main( String[] args )
    {
        Server server = new Server( 8080);
        server.startThreads();
        Scanner scanner = new Scanner(System.in);
        String input;

        do {
            System.out.println("Server is running");
            System.out.println("Type 'queue' to see the players in queue");
            System.out.println("Type 'close' to exit");
            input = scanner.nextLine();
            if(input.equals("queue")){
                server.playerQueueLock.readLock().lock();
                try {
                    if(server.players_waiting.size()==0){
                        System.out.println("No players in queue");
                    }
                    else {
                        for (Player player : server.players_waiting) {
                            System.out.println(player.getUsername() + " Skill Level: " + player.getSkillLevel() + " Current Skill Gap: " + player.getMaxSkillGap());
                            if(player.timeSinceDisconnect()>=0){
                                System.out.println("Has been disconnected for " + (player.timeSinceDisconnect() / 1000)+ " seconds");
                            }
                        }
                    }
                }
                finally {
                    server.playerQueueLock.readLock().unlock();
                }
            }
        } while (!input.equals("close"));

        System.out.println("Server closed");
        server.stopServer();
    }
}