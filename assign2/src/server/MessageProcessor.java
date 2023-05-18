package server;

import java.util.Objects;
import java.util.Queue;
import java.util.UUID;

public class MessageProcessor extends Thread {
    private Queue<Message> writeQueue;
    private final Server server;
    private final Message request;

    public MessageProcessor(Server server,Queue<Message> writeQueue,Message request){
        this.writeQueue = writeQueue;
        this.server = server;
        this.request = request;
    }
    private String generateToken(){
        return UUID.randomUUID() + "-" + System.currentTimeMillis();
    }

    public AuthenticationState dealWithLogin(String clientMessage,Player player){
        String[] messageList = clientMessage.split(" ");
        if(player.getUsername()!=null && messageList.length==2 && Objects.equals(messageList[0], "PASSWORD")){
            if (server.db.authenticateUser(player.getUsername(), messageList[1])) {
                String token = generateToken();
                server.tokenToUsername.put(token,player.getUsername());
                writeQueue.offer(new Message("TOKEN " + token,player));
                return AuthenticationState.TOKEN;
            }
            else {
                writeQueue.offer(new Message("INVALID_LOG_IN",player));
                return AuthenticationState.INITIAL_STATE;
            }
        }
        else if(messageList.length==2 && Objects.equals(messageList[0], "USERNAME")){
            player.setUsername(messageList[1]);
            return AuthenticationState.LOG_IN;
        }
        else {
            writeQueue.offer(new Message("INVALID_RESPONSE",player));
            return AuthenticationState.INITIAL_STATE;
        }
    }
    public AuthenticationState dealWithRegister(String clientMessage,Player player){
        String[] messageList = clientMessage.split(" ");
        if(player.getUsername()!=null && messageList.length==2 && Objects.equals(messageList[0], "PASSWORD")){
            if (server.db.addUser(player.getUsername(), messageList[1])) {
                String token = generateToken();
                server.tokenToUsername.put(token,player.getUsername());
                writeQueue.offer(new Message("TOKEN " + token,player));
                return AuthenticationState.TOKEN;
            }
            else {
                writeQueue.offer(new Message("INVALID_REGISTER",player));
                return AuthenticationState.INITIAL_STATE;
            }
        }
        else if(messageList.length==2 && Objects.equals(messageList[0], "USERNAME")){
            player.setUsername(messageList[1]);
            return AuthenticationState.REGISTER;
        }
        else {
            writeQueue.offer(new Message("INVALID_RESPONSE",player));
            return AuthenticationState.INITIAL_STATE;
        }
    }


    public AuthenticationState dealWithToken(String clientMessage,Player player){
        String[] messageList = clientMessage.split(" ");
        if(messageList.length==2 && Objects.equals(messageList[0], "TOKEN")){
            String token = messageList[1];
            String username = server.tokenToUsername.get(token);

            if(username!=null){
                writeQueue.offer(new Message("CONNECTION_ESTABLISHED",player));
                player.setUsername(username);
                player.authenticate();
                player.setSkillLevel(server.db.getSkillLevel(username));
                if (!server.playerIsPlaying(player) && !server.playerIsWaiting(player)) //function already replaces player if it was playing
                {
                    server.playerQueueLock.writeLock().lock();
                    try{
                        server.players_waiting.add(player);
                    }
                    finally{
                        server.playerQueueLock.writeLock().unlock();
                    }
                }
                return AuthenticationState.END;
            }
            else {
                writeQueue.offer(new Message("INVALID_TOKEN", player));
                return AuthenticationState.INITIAL_STATE;
            }

        }
        else {
            writeQueue.offer(new Message("INVALID_RESPONSE",player));
            return AuthenticationState.INITIAL_STATE;
        }
    }


    public AuthenticationState dealWithInitialState(String clientMessage,Player player){
        switch(clientMessage){
            case "LOG_IN" ->{
                return AuthenticationState.LOG_IN;
            }
            case "REGISTER" ->{
                return AuthenticationState.REGISTER;
            }
            default -> {
                if(clientMessage.startsWith(("TOKEN"))){
                    return dealWithToken(clientMessage, player);
                }
                else{
                    writeQueue.offer(new Message("INVALID_RESPOSNE",player));
                    return AuthenticationState.INITIAL_STATE;
                }

            }
        }
    }

    @Override
    public void run()
    {
        try {
            String clientMessage = new String(request.bytes);
            Player player = request.player;
            if (player.isAuthenticated()) {
                player.getGame().receivedAnswer(player, clientMessage);
            } else {
                switch (player.authenticationState) {
                    case INITIAL_STATE -> {
                        player.authenticationState = dealWithInitialState(clientMessage, player);
                    }
                    case LOG_IN -> {
                        player.authenticationState = dealWithLogin(clientMessage, player);
                    }
                    case REGISTER -> {
                        player.authenticationState = dealWithRegister(clientMessage, player);
                    }
                    case TOKEN -> {
                        player.authenticationState = dealWithToken(clientMessage, player);
                    }
                }
            }
        }
        finally {
            request.player.freeLock();
        }

    }
}

