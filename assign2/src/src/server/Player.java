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

    private boolean isAuthenticated;
    public AuthenticationState authenticationState;


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
        for (String line : question) {
            writeQueue.offer(new Message(line,this));
        }
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
}