package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Server extends Thread
{
    private ServerSocket serverSocket;
    private final int port;


    private HashMap<String,Integer> tokenToQueuePosition;
    private List<Socket> clients_waiting;

    private boolean running = false;

    public Server( int port )
    {
        this.port = port;
        this.clients_waiting = new ArrayList<>();
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
        while( running )
        {
            try
            {
                System.out.println( "Listening for a connection" );

                // Call accept() to receive the next connection
                Socket socket = serverSocket.accept();

                // Pass the socket to the RequestHandler thread for processing
                ConnectionEstablisher connectionEstablisher = new ConnectionEstablisher(socket);
                connectionEstablisher.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
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