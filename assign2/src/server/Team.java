package server;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Team {
    private final int id;
    private final List<Player> players;
    public boolean noMoreQuestions;
    private int score;
    private int skillLevel;

    private int questionIndex;

    private List<String> currentQuestion;


    ExecutorService playersThread;

    public Team(List<Player> players, int id) {
        this.players = players;
        this.score = 0;
        this.skillLevel = 0;
        for (Player player : players) {
            this.skillLevel += player.getSkillLevel();
        }
        this.skillLevel /= players.size();
        this.playersThread = Executors.newFixedThreadPool(players.size());
        this.id = id;
        this.questionIndex = 0;
        this.currentQuestion = null;
    }

    public boolean teamHasPlayer(Player newPlayer){
        for(int i=0;i<players.size();i++) {
            if (Objects.equals(players.get(i).getUsername(), newPlayer.getUsername())) {
                newPlayer.setSkillLevel(players.get(i).getSkillLevel());
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

    public int getTeamId() {return id;}

    public int getQuestionIndex() {return questionIndex;}

    public void increaseQuestionIndex() {questionIndex++;}

    public List<String> getCurrentQuestion() {return currentQuestion;}

    public void setCurrentQuestion(List<String> currentQuestion) {this.currentQuestion = currentQuestion;}

    public String teamMembersToString(){
        StringBuilder teamMembers = new StringBuilder();
        for (Player player: players){
            teamMembers.append(player.getUsername()).append("-");
        }
        return teamMembers.substring(0, teamMembers.length()-1);
    }
}
