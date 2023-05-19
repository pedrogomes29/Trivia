package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Game {
    private final List<List<String>> questions;

    private final int numberOfRounds;

    private final List<Team> teams;

    private final Server server;
    private final Lock answerLock;

    public Game(List<Player> players, int numberOfRounds, Server server){
        this.teams = new ArrayList<>();
        this.numberOfRounds = numberOfRounds;
        this.server = server;
        this.answerLock = new ReentrantLock();
        this.separatePlayersIntoTeams(players);
        this.questions = new ArrayList<>();
        this.readQuestionsFromFile();
    }

    private void separatePlayersIntoTeams(List<Player> players) {
        players.sort(Comparator.comparingInt(Player::getSkillLevel));
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
        Team teamA = new Team(teamAPlayers, 0,this);
        Team teamB = new Team(teamBPlayers, 1,this);
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

    private String removeStartingAndEndingLetter(String s){
        return s.substring(1, s.length() - 1);
    }
    private void readQuestionsFromFile() {

        try (BufferedReader br = new BufferedReader(new FileReader("trivia.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = removeStartingAndEndingLetter(line); //remove []
                String[] parts = line.split(",");
                List<String> question = new ArrayList<>();
                String part1 = String.join(",",Arrays.copyOfRange(parts, 0, parts.length-1));
                String part2 = parts[parts.length-1];
                question.add(removeStartingAndEndingLetter(part1.trim())); //remove ""
                question.add(removeStartingAndEndingLetter(part2.trim())); //remove ""
                questions.add(question);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void start() {
        this.sendMessageToTeam(teams.get(0), "CONCLUSIONS_Game started, you are team 0. \n" +
                "CONCLUSIONS_Your team members are: " + teams.get(0).teamMembersToString() + "\n" +
                "CONCLUSIONS_Your enemy team members are: " + teams.get(1).teamMembersToString() + "\n" +
                "CONCLUSIONS_Your team skill level is: " + teams.get(0).getSkillLevel() + "\n" +
                "CONCLUSIONS_Your enemy team skill level is: " + teams.get(1).getSkillLevel());
        this.sendMessageToTeam(teams.get(1), "CONCLUSIONS_Game started, you are team 1. \n" +
                "CONCLUSIONS_Your team members are: " + teams.get(1).teamMembersToString() + "\n" +
                "CONCLUSIONS_Your enemy team members are: " + teams.get(0).teamMembersToString() + "\n" +
                "CONCLUSIONS_Your team skill level is: " + teams.get(1).getSkillLevel() + "\n" +
                "CONCLUSIONS_Your enemy team skill level is: " + teams.get(0).getSkillLevel());
        sendQuestionToTeam(teams.get(0));
        sendQuestionToTeam(teams.get(1));
    }

    private void gameOver() {
        System.out.println("Game over");
        for (Team team : teams) {
            System.out.println("Team " + team.getTeamId() + " score: " + team.getScore());
            this.sendMessageToTeam(team, "CONCLUSIONS_Your Team score: " + team.getScore());
            this.sendMessageToTeam(team, "CONCLUSIONS_Enemy Team score: " + teams.get((team.getTeamId() + 1) % 2).getScore());
        }
        if (teams.get(0).getScore() > teams.get(1).getScore()) {
            System.out.println("Team 0 wins");
            this.sendMessageToTeam(teams.get(0), "CONCLUSIONS_You win");
            this.sendMessageToTeam(teams.get(1), "CONCLUSIONS_You lose");

            teams.get(0).lock.readLock().lock();
            try {
                for (Player player : teams.get(0).getPlayers()) {
                    player.increaseSkillLevel(10);
                }
            }
            finally{
                teams.get(0).lock.readLock().unlock();
            }

            teams.get(1).lock.readLock().lock();
            try {

                for (Player player : teams.get(1).getPlayers()) {
                    player.increaseSkillLevel(-10);
                }
            }
            finally{
                teams.get(1).lock.readLock().unlock();
            }

        } else if (teams.get(0).getScore() < teams.get(1).getScore()) {
            System.out.println("Team 1 wins");
            this.sendMessageToTeam(teams.get(1), "CONCLUSIONS_You win");
            this.sendMessageToTeam(teams.get(0), "CONCLUSIONS_You lose");

            teams.get(1).lock.readLock().lock();
            try {

                for (Player player : teams.get(1).getPlayers()) {
                    player.increaseSkillLevel(10);
                }
            }
            finally{
                teams.get(1).lock.readLock().unlock();
            }

            teams.get(0).lock.readLock().lock();
            try {
                for (Player player : teams.get(0).getPlayers()) {
                    player.increaseSkillLevel(-10);
                }
            }
            finally{
                teams.get(0).lock.readLock().unlock();
            }
        } else {
            System.out.println("Draw");
            this.sendMessageToTeam(teams.get(0), "CONCLUSIONS_Draw");
            this.sendMessageToTeam(teams.get(1), "CONCLUSIONS_Draw");
        }
        this.sendMessageToTeam(teams.get(0), "GAME OVER");
        this.sendMessageToTeam(teams.get(1), "GAME OVER");

        teams.get(0).lock.readLock().lock();
        try {
            for (Player player : teams.get(0).getPlayers()) {
                server.db.setPlayerSkillLevel(player.getUsername(), player.getSkillLevel());
            }
        }
        finally{
            teams.get(0).lock.readLock().unlock();
        }

        teams.get(1).lock.readLock().lock();
        try {
            for (Player player : teams.get(1).getPlayers()) {
                server.db.setPlayerSkillLevel(player.getUsername(), player.getSkillLevel());
            }
        }
        finally{
            teams.get(1).lock.readLock().unlock();
        }


        synchronized (server.games) {
            server.games.remove(this);
        }
    }

    public boolean gameHasPlayer(Player player){
        for(Team team:teams){
            if(team.teamHasPlayer(player)) {
                player.sendMessage("RECONNECTED");
                player.sendQuestion(team.getQuestionIndex(),team.getCurrentQuestion());
                return true;
            }
        }
        return false;
    }


    public void sendQuestionToTeam(Team team){
        if (team.getQuestionIndex() < numberOfRounds) {
            //get random question
            int questionIndex = new Random().nextInt(questions.size());
            List<String> question = questions.get(questionIndex);
            questions.remove(questionIndex);
            List<Player> teamPlayers = team.getPlayers();
            team.lock.readLock().lock();
            try {
                for (Player player : teamPlayers) {
                    player.sendQuestion(team.getQuestionIndex(), question);
                }
            }
            finally{
                team.lock.readLock().unlock();
            }
            team.setCurrentQuestion(question);
        }
        else {
            sendMessageToTeam(team, "CONCLUSIONS_You have no more questions");
            team.noMoreQuestions = true;
        }
        if (teams.get(0).noMoreQuestions && teams.get(1).noMoreQuestions){
            gameOver();
        }
    }
    public void sendMessageToTeam(Team team, String message){
        List<Player> teamPlayers = team.getPlayers();
        team.lock.readLock().lock();
        try {
            for (Player player : teamPlayers)
                player.sendMessage(message);
        }
        finally{
            team.lock.readLock().unlock();
        }
    }

    public void receivedAnswer(Player player, int round, String clientMessage){
        answerLock.lock();
        boolean firstAnswer;
        try {
            if(player.getTeam().getQuestionIndex()==round){
                player.getTeam().increaseQuestionIndex();
                firstAnswer = true;
            }
            else
                firstAnswer = false;
        }
        finally{
            answerLock.unlock();
        }
        if(firstAnswer) {
            if (player.getTeam().getCurrentQuestion().get(1).equals(clientMessage)) {
                player.increaseSkillLevel(1);
                player.getTeam().increaseScore(1);
                this.sendMessageToTeam(player.getTeam(), "ANSWER_Correct answer, " + clientMessage + ", answered by: " + player.getUsername()
                        + ", Team Score: " + player.getTeam().getScore() + ", Player's Skill Level: " + player.getSkillLevel());

            } else {
                player.increaseSkillLevel(-1);
                player.getTeam().increaseScore(-1);
                this.sendMessageToTeam(player.getTeam(), "ANSWER_Wrong answer, " + clientMessage + ", answered by: " + player.getUsername() + ", right answer was: " + questions.get(player.getTeam().getQuestionIndex()).get(1)
                        + ", Team Score: " + player.getTeam().getScore() + ", Player's Skill Level: " + player.getSkillLevel());
            }
            sendQuestionToTeam(player.getTeam());
        }
    }
}