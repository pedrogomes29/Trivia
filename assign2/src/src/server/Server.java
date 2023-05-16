package server;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread
{
    final int NUMBER_OF_PLAYERS_PER_GAME = 2;
    final int NUMBER_OF_ROUNDS = 5;
    private ServerSocketChannel serverSocketChannel;
    private final int port;


    public HashMap<String,String> tokenToUsername;
    public List<Player> players_waiting;

    public List<Game> games;

    public PlayerDatabase db;

    public SecureRandom saltGenerator;

    private final ExecutorService connectionThreadPool;
    private final ExecutorService gameThreadPool;

    private boolean running = false;

    public Server( int port )
    {
        this.port = port;
        this.players_waiting = new ArrayList<>();
        this.saltGenerator = new SecureRandom();
        this.db = new PlayerDatabase("database.txt");
        this.tokenToUsername = new HashMap<>();
        this.games = new ArrayList<>();
        this.connectionThreadPool = Executors.newFixedThreadPool(50);
        this.gameThreadPool = Executors.newFixedThreadPool(10);
    }


    public boolean playerIsWaiting(Player player){
        for(int i=0;i<players_waiting.size();i++){
            Player playersWaiting = players_waiting.get(i);
            if(Objects.equals(player.getUsername(), playersWaiting.getUsername())) {
                player.setMaxSkillGap( playersWaiting.getMaxSkillGap());
                players_waiting.set(i,player);
                return true;
            }
        }
        return false;
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

            SocketAccepter socketAccepter = new SocketAccepter(this.port, socketQueue);
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

        private final long CHECK_INTERVAL_MS = 5000;

        private boolean playersAreConnected(List<Player> players){
            for(Player player:players){
                if(!player.isConnected())
                    return false;
            }
            return true;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(CHECK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (players_waiting) {
                    /*
                    if (players_waiting.size() >= NUMBER_OF_PLAYERS_PER_GAME) {
                        List<Player> matchedPlayers = new ArrayList<>();

                        for (Player player : players_waiting) {
                            matchedPlayers.clear();
                            matchedPlayers.add(player);

                            for (Player otherPlayer : players_waiting) {
                                if (player.getSocket().equals(otherPlayer.getSocket())) {
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
                                players_waiting.removeAll(matchedPlayers);
                                Game game = new Game(matchedPlayers,NUMBER_OF_ROUNDS);
                                games.add(game);
                                gameThreadPool.execute(game);
                            }
                        }
                        for (Player player : players_waiting)
                            player.increaseSkillGap();
                    }

                     */
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