package server;

import java.util.Objects;
import java.util.Queue;
import java.util.UUID;

public class MessageProcessor extends Thread {
    private final Server server;
    private final Message request;

    public MessageProcessor(Server server,Message request){
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
                synchronized (server.tokenToUsername) {
                    server.tokenToUsername.put(token, player.getUsername());
                }
                player.sendMessage("TOKEN " + token);
                return AuthenticationState.TOKEN;
            }
            else {
                player.sendMessage("INVALID_LOG_IN");
                return AuthenticationState.INITIAL_STATE;
            }
        }
        else if(messageList.length==2 && Objects.equals(messageList[0], "USERNAME")){
            player.setUsername(messageList[1]);
            return AuthenticationState.LOG_IN;
        }
        else {
            player.sendMessage("INVALID_RESPONSE");
            return AuthenticationState.INITIAL_STATE;
        }
    }
    public AuthenticationState dealWithRegister(String clientMessage,Player player){
        String[] messageList = clientMessage.split(" ");
        if(player.getUsername()!=null && messageList.length==2 && Objects.equals(messageList[0], "PASSWORD")){
            if (server.db.addUser(player.getUsername(), messageList[1])) {
                String token = generateToken();
                synchronized (server.tokenToUsername) {
                    server.tokenToUsername.put(token, player.getUsername());
                }
                player.sendMessage("TOKEN " + token);
                return AuthenticationState.TOKEN;
            }
            else {
                player.sendMessage("INVALID_REGISTER");
                return AuthenticationState.INITIAL_STATE;
            }
        }
        else if(messageList.length==2 && Objects.equals(messageList[0], "USERNAME")){
            player.setUsername(messageList[1]);
            return AuthenticationState.REGISTER;
        }
        else {
            player.sendMessage("INVALID_RESPONSE");
            return AuthenticationState.INITIAL_STATE;
        }
    }


    public AuthenticationState dealWithToken(String clientMessage,Player player){
        String[] messageList = clientMessage.split(" ");
        if(messageList.length==2 && Objects.equals(messageList[0], "TOKEN")){
            String token = messageList[1];
            String username;
            synchronized (server.tokenToUsername){
                username = server.tokenToUsername.get(token);
            }

            if(username!=null){
                player.sendMessage("CONNECTION_ESTABLISHED");
                player.setUsername(username);
                player.authenticate();
                if (!server.playerIsPlaying(player) && !server.playerIsWaiting(player)) //function already replaces player if it was playing
                {
                    player.setSkillLevel(server.db.getSkillLevel(username));
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
                player.sendMessage("INVALID_TOKEN");
                return AuthenticationState.INITIAL_STATE;
            }

        }
        else {
            player.sendMessage("INVALID_RESPONSE");
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
                    player.sendMessage("INVALID_RESPOSNE");
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

