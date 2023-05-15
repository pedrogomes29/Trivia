package server;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.*;

public class SocketProcessor implements Runnable{
    private Queue<Socket> inboundSocketQueue;
    private long nextSocketId = 16 * 1024;
    private Selector readSelector;
    private Selector writeSelector;
    private ByteBuffer readByteBuffer  = ByteBuffer.allocate(1024 * 1024);

    private Map<Long, Socket> socketMap = new HashMap<>();

    public SocketProcessor(Queue<Socket> inboundSocketQueue) throws IOException{
        this.inboundSocketQueue = inboundSocketQueue;
        this.readSelector         = Selector.open();
        this.writeSelector        = Selector.open();
    }

    @Override
    public void run() {
        while(true){
            try{
                executeCycle();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }


    public void executeCycle() throws IOException {
        takeNewSockets();
        readFromSockets();
        //writeToSockets();
    }

    public void takeNewSockets() throws IOException {
        Socket newSocket = this.inboundSocketQueue.poll();

        while(newSocket != null){
            newSocket.socketId = this.nextSocketId++;
            newSocket.socketChannel.configureBlocking(false);

            this.socketMap.put(newSocket.socketId, newSocket);

            SelectionKey key = newSocket.socketChannel.register(this.readSelector, SelectionKey.OP_READ);
            key.attach(newSocket);

            newSocket = this.inboundSocketQueue.poll();
        }
    }

    public void readFromSockets() throws IOException {
        int readReady = this.readSelector.selectNow();

        if(readReady > 0){
            Set<SelectionKey> selectedKeys = this.readSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while(keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                readFromSocket(key);

                keyIterator.remove();
            }
            selectedKeys.clear();
        }
    }
    private void readFromSocket(SelectionKey key) throws IOException {
        Socket socket = (Socket) key.attachment();
        socket.read(this.readByteBuffer);
        System.out.println(new String(this.readByteBuffer.array()));
        /*
        socket.messageReader.
        if(fullMessages.size() > 0){
            for(Message message : fullMessages){
                message.socketId = socket.socketId;
                this.messageProcessor.process(message);  //the message processor will eventually push outgoing messages into a MessageWriter for this socket.
            }
            fullMessages.clear();
        }

         */

        if(socket.endOfStreamReached){
            System.out.println("Socket closed: " + socket.socketId);
            this.socketMap.remove(socket.socketId);
            key.attach(null);
            key.cancel();
            key.channel().close();
        }
    }

}
