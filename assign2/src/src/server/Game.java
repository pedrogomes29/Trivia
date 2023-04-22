package server;

import java.net.Socket;
import java.util.List;

public class Game {
    private List<Socket> userSockets;
    public Game(int players, List<Socket> userSockets) {
        this.userSockets = userSockets;
    }
    public void start() {
        System.out.println("Starting game with " + userSockets.size() + " players");
    }
}