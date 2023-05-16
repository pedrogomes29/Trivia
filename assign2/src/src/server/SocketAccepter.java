package server;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;

public class SocketAccepter implements Runnable{
    private Server server;
    private ServerSocketChannel serverSocket;

    private Queue<Socket> socketQueue;

    public SocketAccepter(Server server, Queue socketQueue)  {
        this.server     = server;
        this.socketQueue = socketQueue;
    }



    public void run() {
        try{
            this.serverSocket = ServerSocketChannel.open();
            this.serverSocket.bind(new InetSocketAddress(server.port));
        } catch(IOException e){
            e.printStackTrace();
            return;
        }


        while(server.running){
            try{
                SocketChannel socketChannel = this.serverSocket.accept();

                System.out.println("Socket accepted: " + socketChannel);

                this.socketQueue.add(new Socket(socketChannel));

            } catch(IOException e){
                e.printStackTrace();
            }

        }

    }
}
