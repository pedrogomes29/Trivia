package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Server extends Thread
{
    final int NUMBER_OF_PLAYERS_PER_GAME = 4;
    private ServerSocket serverSocket;
    private final int port;


    private HashMap<String,String> tokenToUsername;
    private List<Player> players_waiting;

    private PlayerDatabase db;

    private SecureRandom saltGenerator;

    private boolean running = false;

    public Server( int port )
    {
        this.port = port;
        this.players_waiting = new ArrayList<>();
        this.saltGenerator = new SecureRandom();
        this.db = new PlayerDatabase("database.txt");
        this.tokenToUsername = new HashMap<>();
    }

    public void startServer()
    {
        try
        {
            serverSocket = new ServerSocket(port);
            this.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void stopServer()
    {
        running = false;
        this.interrupt();
    }

    @Override
    public void run()
    {
        running = true;

        Matchmaker matchmaker = new Matchmaker();
        matchmaker.start();

        while( running )
        {
            try
            {
                System.out.println( "Listening for a connection" );

                // Call accept() to receive the next connection
                Socket socket = serverSocket.accept();
                System.out.println(players_waiting.size());
                // Pass the socket to the RequestHandler thread for processing
                ConnectionEstablisher connectionEstablisher = new ConnectionEstablisher(socket,db,tokenToUsername,players_waiting);
                connectionEstablisher.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private class Matchmaker extends Thread {
        private final long CHECK_INTERVAL_MS = 5000;

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(CHECK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (players_waiting) {
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

                            if (matchedPlayers.size() == NUMBER_OF_PLAYERS_PER_GAME) {
                                players_waiting.removeAll(matchedPlayers);

                                // Start a game with the matched players
                                // You can replace the following line with the logic for starting a game
                                System.out.println("Starting a game with matched players");

                                break;
                            }
                        }
                        for (Player player : players_waiting)
                            player.increaseSkillGap();
                    }
                }
            }
        }
    }
    public static void main( String[] args )
    {
        Server server = new Server( 8080);
        server.startServer();
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