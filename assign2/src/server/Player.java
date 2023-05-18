package server;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Player {
    private final long socketId;
    private int skillLevel;
    private String username;

    private final Queue<Message> writeQueue;
    private int maxSkillGap;
    private Team team;

    private boolean isAuthenticated;
    public AuthenticationState authenticationState;

    private Game game;

    private boolean canBeProcessed;
    private final Lock lock;
    private final Condition processingCondition;

    private long disconnectTime;

    public Player(long socketId,Queue<Message> writeQueue) {
        this.socketId = socketId;
        this.writeQueue = writeQueue;
        this.authenticationState = AuthenticationState.INITIAL_STATE;
        this.maxSkillGap = 5;
        this.isAuthenticated = false;
        this.lock = new ReentrantLock();
        this.processingCondition = lock.newCondition();
        this.canBeProcessed = true;
        this.disconnectTime = -1;
    }


    public void obtainLock() {
        lock.lock();
        try {
            while (!canBeProcessed) { //while because of spurious wake-ups
                processingCondition.await();
            }
            canBeProcessed = false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public void freeLock(){
        lock.lock();
        try {
            canBeProcessed = true;
            processingCondition.signal(); // Signal that the processing condition is true
        } finally {
            lock.unlock();
        }
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


    public void setSkillLevel(int elo){
        skillLevel = elo;
    }

    public void increaseSkillLevel(int elo) { skillLevel += elo;}

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


    public void disconnect(){
        this.disconnectTime = System.currentTimeMillis();
    }
    public long timeSinceDisconnect(){
        if(disconnectTime==-1)
            return -1;
        else
            return System.currentTimeMillis() - disconnectTime;
    }
}