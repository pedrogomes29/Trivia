package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game extends Thread{
    private final List<List<String>> questions;

    private ExecutorService teamsThread;
    private final int numberOfRounds;
    private final List<Player> players;

    private final List<Team> teams;

    private int questionId;

    public Game(List<Player> players, int numberOfRounds){
        this.players = players;
        this.teams = new ArrayList<>();
        this.numberOfRounds = numberOfRounds;
        this.separatePlayersIntoTeams();
        this.questions = new ArrayList<>();
        this.readQuestionsFromFile();
    }

    private void separatePlayersIntoTeams() {
        players.sort(Comparator.comparingInt(Player::getSkillLevel));
        this.teamsThread = Executors.newFixedThreadPool(2);
        List<Player> teamAPlayers = new ArrayList<>();
        List<Player> teamBPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            player.setGame(this);
            if (i % 2 == 0) {
                teamAPlayers.add(player);
            } else {
                teamBPlayers.add(player);
            }
        }
        Team teamA = new Team(teamAPlayers, 0);
        Team teamB = new Team(teamBPlayers, 1);
        for (int i = 0; i < players.size(); i++){
            Player player = players.get(i);
            if (i % 2 == 0) {
                player.setTeam(teamA);
            } else {
                player.setTeam(teamB);
            }
        }
        this.teams.add(teamA);
        this.teams.add(teamB);
    }

    private void readQuestionsFromFile() {

        try (BufferedReader br = new BufferedReader(new FileReader("trivia.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("[", "");
                line = line.replace("]", "");
                String[] parts = line.split(",");
                List<String> question = new ArrayList<>();
                question.add(parts[0].substring(1, parts[0].length() - 1));
                question.add(parts[1].substring(2, parts[1].length() - 1));
                questions.add(question);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Starting game with " + players.size() + " players");
    }

    public static void main(String[] args) {
        List<Player> players = new ArrayList<>();
        Game game = new Game(players, 10);
    }

    public void run() {
        sendQuestionToTeam(teams.get(0));
        sendQuestionToTeam(teams.get(1));
    }

    private void gameOver() {
        System.out.println("Game over");
        for (Team team : teams) {
            System.out.println("Team " + team.getTeamId() + " score: " + team.getScore());
        }
        if (teams.get(0).getScore() > teams.get(1).getScore()) {
            System.out.println("Team 0 wins");
        } else if (teams.get(0).getScore() < teams.get(1).getScore()) {
            System.out.println("Team 1 wins");
        } else {
            System.out.println("Draw");
        }
    }

    public boolean gameHasPlayer(Player player){
        for(Team team:teams){
            if(team.teamHasPlayer(player))
                return true;
        }
        return false;
    }


    public void sendQuestionToTeam(Team team){
        if (questionId < 12) {
            List<String> question = questions.get(questionId);
            List<Player> teamPlayers = team.getPlayers();
            for (Player player: teamPlayers){
                player.sendQuestion(question);
            }
            questionId++;
        } else {
            gameOver();
        }
    }
    public void receivedAnswer(Player player, String clientMessage){
        sendQuestionToTeam(player.getTeam());
    }
}