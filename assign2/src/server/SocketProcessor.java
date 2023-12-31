package server;
import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Iterator;
import java.util.List;

public class SocketProcessor implements Runnable{
    private final Queue<Socket> inboundSocketQueue;
    private long nextSocketId = 16 * 1024;
    private final Selector readSelector;
    private final Selector writeSelector;
    private final Server server;

    private final ByteBuffer readByteBuffer  = ByteBuffer.allocate(1024 * 1024);
    private final ByteBuffer writeByteBuffer = ByteBuffer.allocate(1024 * 1024);
    private final Set<Socket> emptyToNonEmptySockets = new HashSet<>();
    private final Set<Socket> nonEmptyToEmptySockets = new HashSet<>();
    private final Queue<Message> outboundMessageQueue = new LinkedList<>();
    private final Map<Long, Socket> socketMap = new HashMap<>();
    private final ExecutorService processorThreadPool;

    public SocketProcessor(Server server,Queue<Socket> inboundSocketQueue) throws IOException{
        this.inboundSocketQueue = inboundSocketQueue;
        this.readSelector = Selector.open();
        this.writeSelector = Selector.open();
        this.server = server;
        this.processorThreadPool = Executors.newFixedThreadPool(10);
    }

    @Override
    public void run() {
        while(server.running){
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
        writeToSockets();
    }

    public void takeNewSockets() throws IOException {
        Socket newSocket;
        synchronized (this.inboundSocketQueue) {
            newSocket = this.inboundSocketQueue.poll();
        }
        while(newSocket != null){
            newSocket.player = new Player(this.nextSocketId++,outboundMessageQueue);
            newSocket.socketChannel.configureBlocking(false);

            this.socketMap.put(newSocket.player.getSocketId(), newSocket);

            SelectionKey key = newSocket.socketChannel.register(this.readSelector, SelectionKey.OP_READ);
            key.attach(newSocket);

            synchronized (this.inboundSocketQueue) {
                newSocket = this.inboundSocketQueue.poll();
            }
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

    private void closeSocket(SelectionKey key) throws IOException{
        Socket socket = (Socket) key.attachment();

        System.out.println("Socket closed: " + socket.player.getSocketId());
        this.socketMap.remove(socket.player.getSocketId());
        socket.player.disconnect();
        key.attach(null);
        key.cancel();
        key.channel().close();
    }

    private void readFromSocket(SelectionKey key) throws IOException {
        try {
            Socket socket = (Socket) key.attachment();
            socket.read(this.readByteBuffer);
            socket.messageReader.read(this.readByteBuffer);
            List<Message> fullMessages = socket.messageReader.messages;
            if (fullMessages.size() > 0) {
                for (Message message : fullMessages) {
                    message.player.obtainLock();
                    MessageProcessor messageProcessor = new MessageProcessor(server, message);
                    processorThreadPool.execute(messageProcessor);  //the message processor will eventually push outgoing messages into a MessageWriter for this socket.
                }
                fullMessages.clear();
            }


            if (socket.endOfStreamReached)
                closeSocket(key);
        }
        catch(SocketException e){
            closeSocket(key);
        }
    }

    public void writeToSockets() throws IOException {

        // Take all new messages from outboundMessageQueue
        takeNewOutboundMessages();

        // Cancel all sockets which have no more data to write.
        cancelEmptySockets();

        // Register all sockets that *have* data and which are not yet registered.
        registerNonEmptySockets();

        // Select from the Selector.
        int writeReady = this.writeSelector.selectNow();

        if(writeReady > 0){
            Set<SelectionKey>      selectionKeys = this.writeSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator   = selectionKeys.iterator();

            while(keyIterator.hasNext()){
                SelectionKey key = keyIterator.next();

                Socket socket = (Socket) key.attachment();

                socket.messageWriter.write(this.writeByteBuffer);

                if(socket.messageWriter.isEmpty()){
                    this.nonEmptyToEmptySockets.add(socket);
                }

                keyIterator.remove();
            }

            selectionKeys.clear();

        }
    }

    private void registerNonEmptySockets() throws ClosedChannelException {
        for(Socket socket : emptyToNonEmptySockets){
            socket.socketChannel.register(this.writeSelector, SelectionKey.OP_WRITE, socket);
        }
        emptyToNonEmptySockets.clear();
    }

    private void cancelEmptySockets() {
        for(Socket socket : nonEmptyToEmptySockets){
            SelectionKey key = socket.socketChannel.keyFor(this.writeSelector);

            key.cancel();
        }
        nonEmptyToEmptySockets.clear();
    }

    private void takeNewOutboundMessages() {
        Message outMessage;
        synchronized (outboundMessageQueue) {
            outMessage = this.outboundMessageQueue.poll();
        }
        while(outMessage != null){
            Socket socket = this.socketMap.get(outMessage.player.getSocketId());

            if(socket != null){
                MessageWriter messageWriter = socket.messageWriter;
                if(messageWriter.isEmpty()){
                    messageWriter.enqueue(outMessage);
                    nonEmptyToEmptySockets.remove(socket);
                    emptyToNonEmptySockets.add(socket);
                } else{
                    messageWriter.enqueue(outMessage);
                }
            }
            synchronized (outboundMessageQueue) {
                outMessage = this.outboundMessageQueue.poll();
            }
        }
    }

}

