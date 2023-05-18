package server;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;



public class Server extends Thread
{
    final int NUMBER_OF_PLAYERS_PER_GAME = 4;
    final int NUMBER_OF_ROUNDS = 5;
    public final int port;


    public HashMap<String,String> tokenToUsername;
    public final List<Player> players_waiting;

    public List<Game> games;

    public PlayerDatabase db;

    public SecureRandom saltGenerator;

    private final ExecutorService gameThreadPool;

    public boolean running = true;
    public final ReadWriteLock playerQueueLock;

    public Server( int port )
    {
        this.port = port;
        this.players_waiting = new ArrayList<>();
        this.saltGenerator = new SecureRandom();
        this.db = new PlayerDatabase("database.txt");
        this.tokenToUsername = new HashMap<>();
        this.games = new ArrayList<>();
        this.gameThreadPool = Executors.newFixedThreadPool(10);
        this.playerQueueLock = new ReentrantReadWriteLock();
    }


    public boolean playerIsWaiting(Player player){
        boolean isPlayerWaiting = false;
        playerQueueLock.readLock().lock();
        try {
            for (int i = 0; i < players_waiting.size(); i++) {
                Player playerWaiting = players_waiting.get(i);
                if (Objects.equals(player.getUsername(), playerWaiting.getUsername())) {
                    player.setMaxSkillGap(playerWaiting.getMaxSkillGap());
                    players_waiting.set(i, player);
                    isPlayerWaiting = true;
                    break;
                }
            }
        }
        finally {
            playerQueueLock.readLock().unlock();
        }
        return isPlayerWaiting;
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
        this.interrupt();
    }

    @Override
    public void run()
    {
        try {
            Matchmaker matchmaker = new Matchmaker();
            matchmaker.start();
            Queue socketQueue = new ArrayBlockingQueue(1024); //move 1024 to ServerConfig

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

        private boolean playersAreConnected(List<Player> players){
            for(Player player:players){
                if(!player.isConnected())
                    return false;
            }
            return true;
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

                if (players_waiting.size() >= NUMBER_OF_PLAYERS_PER_GAME) {
                    List<Player> matchedPlayers = new ArrayList<>();
                    List<Player> idlePlayers = new ArrayList<>();

                    playerQueueLock.readLock().lock();
                    for (Player player : players_waiting) {
                        if(player.timeSinceDisconnect()>=0){
                            idlePlayers.add(player);
                            continue;
                        }

                        matchedPlayers.clear();
                        matchedPlayers.add(player);

                        for (Player otherPlayer : players_waiting) {
                            if (player.getSocketId() == otherPlayer.getSocketId() || idlePlayers.contains(otherPlayer)) {
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

                        if (matchedPlayers.size() == NUMBER_OF_PLAYERS_PER_GAME && playersAreConnected(matchedPlayers)) {
                            playerQueueLock.readLock().unlock();
                            playerQueueLock.writeLock().lock();
                            try {
                                players_waiting.removeAll(matchedPlayers);
                            }
                            finally {
                                playerQueueLock.writeLock().unlock();
                            }
                            Game game = new Game(matchedPlayers,NUMBER_OF_ROUNDS, db);
                            games.add(game);
                            game.start();
                            startedGame = true;
                            break;
                        }
                    }
                    if(startedGame) //if we started a game, we had to remove the read lock to obtain the write lock, so we need to get a new one
                        playerQueueLock.readLock().lock();
                    for (Player player : players_waiting)
                        player.increaseSkillGap();      //we could obtain a lock for the player,
                                                        //but we only change the skill gap which is only accessed/changed by this thread
                    playerQueueLock.readLock().unlock();

                    playerQueueLock.writeLock().lock();
                    try {
                        for(Player player:idlePlayers){
                            if(player.timeSinceDisconnect() > 2*60*1000){
                                players_waiting.remove(player);
                            }
                        }
                    }
                    finally {
                        playerQueueLock.writeLock().unlock();
                    }
                }



            }
        }
    }
    public static void main( String[] args )
    {
        Server server = new Server( 8080);
        server.start();
        Scanner scanner = new Scanner(System.in);
        String input;

        do {
            System.out.println("Type 'close' to exit");
            input = scanner.nextLine();
        } while (!input.equals("close"));

        System.out.println("Server closed");
        server.stopServer();
    }
}