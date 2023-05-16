package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Player {
    private long socketId;
    private int skillLevel;
    private String username;
    private int maxSkillGap;

    private boolean isAuthenticated;
    public AuthenticationState authenticationState;


    public Player(long socketId) {
        this.socketId = socketId;
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
/*
    public void sendQuestion(List<String> question) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);

            for (String line : question) {
                writer.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error sending question to player: " + e.getMessage());
        }
    }
    public String receiveAnswer() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            String answer = dataInputStream.readUTF();
            System.out.println(answer);
            return answer;
        } catch (IOException e) {
            System.err.println("Error receiving answer from player: " + e.getMessage());
        }
        return null;
    }
*/

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