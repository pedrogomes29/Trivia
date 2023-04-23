package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.IllegalBlockingModeException;
import java.util.List;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Player {
    private Socket socket;
    private int skillLevel;
    private String username;
    private int maxSkillGap;

    public Player(Socket socket, int skillLevel,String username) {
        this.socket = socket;
        this.skillLevel = skillLevel;
        this.maxSkillGap = 5;
        this.username = username;
    }


    public String getUsername(){
        return username;
    }
    public Socket getSocket() {
        return socket;
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


    public void increaseSkillLevel(int elo) { skillLevel += elo;}

    public void decreaseSkillLevel(int elo) { skillLevel += elo;}

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
}