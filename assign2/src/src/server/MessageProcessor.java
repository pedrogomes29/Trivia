package server;

import java.util.Objects;
import java.util.Queue;

public class MessageProcessor {
    private Queue<Message> writeQueue;

    public MessageProcessor(Queue<Message> writeQueue){
        this.writeQueue = writeQueue;
    }

    public void process(Message request){
        System.out.println("Message Received from socket" + request.player.getSocketId() + " :" + new String(request.bytes));


        Message response = new Message("Ola velho",request.player);

        writeQueue.offer(response);
        response = new Message("Tudo bem?",request.player);

        writeQueue.offer(response);
        response = new Message("Es ganda rei",request.player);

        writeQueue.offer(response);
        response = new Message("Meu menino",request.player);

        writeQueue.offer(response);
    }
}

