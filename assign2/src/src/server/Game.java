package server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game {
    private final List<List<String>> questions;

    private final int numberOfRounds;
    private final List<Player> players;
    private final List<Player> teamA;
    private final List<Player> teamB;
    public Game(List<Player> players, int numberOfRounds) {
        this.players = players;
        this.numberOfRounds = numberOfRounds;
        this.teamA = new ArrayList<>();
        this.teamB = new ArrayList<>();
        this.separatePlayersIntoTeams();
        this.questions = new ArrayList<>();
        this.readQuestionsFromFile();
        this.answerQuestion();
    }

    private void separatePlayersIntoTeams() {
        for (int i = 0; i < players.size(); i++) {
            if (i % 2 == 0) {
                teamA.add(players.get(i));
            } else {
                teamB.add(players.get(i));
            }
        }
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
    public boolean answerQuestion(){
        //Get a random question
        int random = (int) (Math.random() * questions.size());
        List<String> question = questions.get(random);
        Scanner scanner = new Scanner(System.in);
        System.out.println(question.get(0));
        System.out.print("Your answer:");
        String answer = scanner.nextLine();
        if(answer.equalsIgnoreCase(question.get(1))){
            System.out.println("Correct!");
            return true;}
        else{
            System.out.println("Incorrect!");
            System.out.println("The correct answer was: " + question.get(1));
            return false;
        }
    }
}