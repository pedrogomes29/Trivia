package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Queue;

public class Player {
    private long socketId;
    private int skillLevel;
    private String username;

    private Queue<Message> writeQueue;
    private int maxSkillGap;
    private Team team;

    private boolean isAuthenticated;
    public AuthenticationState authenticationState;

    private Game game;


    public Player(long socketId,Queue<Message> writeQueue) {
        this.socketId = socketId;
        this.writeQueue = writeQueue;
        this.authenticationState = AuthenticationState.INITIAL_STATE;
        this.maxSkillGap = 5;
        this.isAuthenticated = false;
    }

    public boolean isAuthenticated(){
        return isAuthenticated;
    }

    public String getUsername(){
        return username;
    }
    public long getSocketId() {
        return socketId;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public int getMaxSkillGap(){
        return maxSkillGap;
    }

    public void increaseSkillGap(){
        maxSkillGap++;
    }


    public void setMaxSkillGap(int maxSkillGap){this.maxSkillGap=maxSkillGap;}

    public void increaseSkillLevel(int elo) { skillLevel += elo;}

    public void decreaseSkillLevel(int elo) { skillLevel += elo;}

    public void sendQuestion(List<String> question) {
            writeQueue.offer(new Message(question.get(0),this));
    }

    public void sendMessage(String message){
        writeQueue.offer(new Message(message, this));
    }
    public void setUsername(String username){
        this.username = username;
    }

    public void authenticate(){
        this.isAuthenticated = true;
    }

    public boolean isConnected(){
        return true;
    }

    public Game getGame(){ return game;}

    public void setGame(Game game){ this.game = game;}

    public void setTeam(Team team){ this.team = team;}

    public Team getTeam(){ return team;}
}