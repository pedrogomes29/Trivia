package server;

import java.net.Socket;

public class Player {
    private Socket socket;
    private int skillLevel;

    private int maxSkillGap;

    public Player(Socket socket, int skillLevel) {
        this.socket = socket;
        this.skillLevel = skillLevel;
        this.maxSkillGap = 5;
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

}