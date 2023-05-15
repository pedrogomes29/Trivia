package server;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Team {
    private final List<Player> players;
    private int score;
    private int skillLevel;

    ExecutorService playersThread;

    public Team(List<Player> players) {
        this.players = players;
        this.score = 0;
        this.skillLevel = 0;
        for (Player player : players) {
            this.skillLevel += player.getSkillLevel();
        }
        this.skillLevel /= players.size();
        this.playersThread = Executors.newFixedThreadPool(players.size());
    }

    public boolean teamHasPlayer(Player newPlayer){
        for(int i=0;i<players.size();i++) {
            if (Objects.equals(players.get(i).getUsername(), newPlayer.getUsername())) {
                players.set(i, newPlayer);
                return true;
            }
        }
        return false;
    }



    public List<Player> getPlayers(){
        return players;
    }

    public int getScore() {
        return score;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public void increaseScore(int score) {
        this.score += score;
    }

    public void sendQuestion(List<String> question){
        for (Player player : players) {
            // playersThread.execute(() -> player.sendQuestion(question));
        }
    }
}
