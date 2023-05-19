package server;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Team {
    private final int id;
    private final List<Player> players;
    public boolean noMoreQuestions;
    private int score;
    private int skillLevel;
    private int questionIndex;
    private final Game game;

    public ReadWriteLock lock;

    private List<String> currentQuestion;

    public Team(List<Player> players, int id,Game game) {
        this.players = players;
        this.score = 0;
        this.skillLevel = 0;
        for (Player player : players) {
            this.skillLevel += player.getSkillLevel();
        }
        this.skillLevel /= players.size();
        this.id = id;
        this.questionIndex = 0;
        this.currentQuestion = null;
        this.game = game;
        this.lock = new ReentrantReadWriteLock();
    }

    public boolean teamHasPlayer(Player newPlayer){
        boolean hasPlayer = false;
        this.lock.readLock().lock();
        try {
            for (int i = 0; i < players.size(); i++) {
                Player playerWaiting = players.get(i);
                if (Objects.equals(players.get(i).getUsername(), newPlayer.getUsername())) {
                    this.lock.readLock().unlock();
                    newPlayer.setSkillLevel(playerWaiting.getSkillLevel());
                    playerWaiting.unauthenticate();
                    playerWaiting.authenticationState = AuthenticationState.INITIAL_STATE;
                    playerWaiting.sendMessage("REMOTE_LOG_IN");
                    this.lock.writeLock().lock(); //Only chance for someone to have changed this array
                                                  //between releasing the read lock and obtaining the write lock
                                                  //would be if someone else was logging in to the same account simmultaneously
                                                  //either way, the index doesn't change
                    try {
                        players.set(i, newPlayer);
                    } finally {
                        this.lock.writeLock().unlock();
                    }
                    newPlayer.setGame(this.game);
                    newPlayer.setTeam(this);
                    hasPlayer = true;
                    break;
                }
            }
        }
        finally{
            if(!hasPlayer)
                this.lock.readLock().unlock();
        }
        return hasPlayer;
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

    public int getTeamId() {return id;}

    public int getQuestionIndex() {return questionIndex;}

    public void increaseQuestionIndex() {questionIndex++;}

    public List<String> getCurrentQuestion() {return currentQuestion;}

    public void setCurrentQuestion(List<String> currentQuestion) {this.currentQuestion = currentQuestion;}

    public String teamMembersToString(){
        StringBuilder teamMembers = new StringBuilder();
        this.lock.readLock().lock();
        try {
            for (Player player : players) {
                teamMembers.append(player.getUsername()).append("-");
            }
        }
        finally{
            this.lock.readLock().unlock();
        }
        return teamMembers.substring(0, teamMembers.length()-1);
    }
}
