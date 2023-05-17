package server;

import java.util.Objects;
import java.util.Queue;
import java.util.UUID;

public class MessageProcessor {
    private Queue<Message> writeQueue;
    private final Server server;

    public MessageProcessor(Server server,Queue<Message> writeQueue){
        this.writeQueue = writeQueue;
        this.server = server;
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
                if (!server.playerIsPlaying(player) && !server.playerIsWaiting(player)) //function already replaces player if it was playing
                {
                    synchronized (server.players_waiting){
                        server.players_waiting.add(player);
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
            case "EXIT" ->{
                writeQueue.offer(new Message("EXIT",player));
                return AuthenticationState.INITIAL_STATE;
                //todo end socket
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

    public void process(Message request){
        String clientMessage = new String(request.bytes);
        Player player = request.player;
        if(player.isAuthenticated()){
                player.getGame().receivedAnswer(player, clientMessage);
        }
        else{
            switch(player.authenticationState){
                case INITIAL_STATE -> {
                    player.authenticationState = dealWithInitialState(clientMessage,player);
                }
                case LOG_IN -> {
                    player.authenticationState = dealWithLogin(clientMessage,player);
                }
                case REGISTER -> {
                    player.authenticationState = dealWithRegister(clientMessage,player);
                }
                case TOKEN -> {
                    player.authenticationState = dealWithToken(clientMessage,player);
                }
            }
        }

    }
}

