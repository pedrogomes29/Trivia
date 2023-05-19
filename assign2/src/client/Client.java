package client;
import server.Game;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;


public class Client {

    private Socket socket;
    private PrintStream out;
    private BufferedReader in;
    private String token;

    private String host;


    private int port;
    private GameState gameState;

    enum AuthenticationOption{
        LOG_IN,
        REGISTER,
        EXIT
    }

    enum GameState{
        ESTABLISHING_CONNECTION,
        PLAYING,
        WAITING,
        GAME_OVER,
        QUIT
    }

    enum ServerResponse{
        CLIENT_ERROR,
        SERVER_ERROR,
        CONNECTION_ESTABLISHED,
        WRONG_TOKEN,
        INVALID_LOGIN,
        INVALID_REGISTER,
        ERROR
    }
    public Client(String host,int port){
        try
        {
            this.host = host;
            this.port = port;
            // Connect to the server
            socket = new Socket( host, port );

            // Create input and output streams to read from and write to the server
            out = new PrintStream( socket.getOutputStream(),true);
            in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            gameState = GameState.ESTABLISHING_CONNECTION;
            token = null;

        }
        catch( Exception e )
        {
            System.out.println("Couldn't establish a connection to the server");
            gameState = GameState.QUIT;
        }
    }

    public void closeConnection(){
        try
        {
            in.close();
            out.close();
            socket.close();

        }
        catch( Exception e )
        {
            System.out.println("Couldn't close the connection to the server");
        }
    }


    public static String[] getUserCredentials(){
        System.out.print("Username: ");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        return new String[]{username, password};
    }


    public AuthenticationOption chooseAuthenticationOption(){
        Scanner scanner = new Scanner(System.in);
        String input;
        do {
            System.out.println("Choose an option:");
            System.out.println("1: Log in");
            System.out.println("2: Register");
            System.out.println("3. Close");
            input = scanner.nextLine();
        } while (!input.equals("1") && !input.equals("2") && !input.equals("3"));

        switch (input) {
            case "1" -> {
                return AuthenticationOption.LOG_IN;
            }
            case "2" -> {
                return AuthenticationOption.REGISTER;
            }
            default -> {
                gameState = GameState.QUIT;
                return AuthenticationOption.EXIT;
            }
        }
    }


    private ServerResponse authenticate_user(String username,String password)throws Exception{
        out.println("USERNAME "+ username);
        out.println("PASSWORD " + password);
        String serverResponse = in.readLine();
        if(serverResponse==null)
            throw new SocketException();
        if(serverResponse.startsWith("TOKEN")){
            return receive_token(serverResponse);
        }
        else {
            switch (serverResponse) {
                case "CLIENT_ERROR" -> {
                    return ServerResponse.CLIENT_ERROR;
                }
                case "INTERNAL_SERVER_ERROR" -> {
                    return ServerResponse.SERVER_ERROR;
                }
                case "INVALID_LOG_IN" -> {
                    return ServerResponse.INVALID_LOGIN;
                }
                case "INVALID_REGISTER" -> {
                    return ServerResponse.INVALID_REGISTER;
                }
                default -> {
                    return ServerResponse.ERROR;
                }
            }
        }

    }

    private ServerResponse log_in(String username,String password) throws Exception {
        out.println("LOG_IN");
        return authenticate_user(username,password);
    }

    private ServerResponse register_user(String username,String password) throws Exception {
        out.println("REGISTER");
        return authenticate_user(username,password);
    }


    private void save_token(){
        try {
            FileWriter myWriter = new FileWriter("token.txt");
            myWriter.write(this.token);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private String get_token(){
        File f = new File( "token.txt");
        if(f.exists() && !f.isDirectory()) {
            Scanner myReader;
            try {
                myReader = new Scanner(f);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            if(myReader.hasNextLine())
                return myReader.nextLine();
            else
                return null;
        }
        else
            return null;
    }
    private ServerResponse send_token()throws IOException{
        out.println("TOKEN " + token);
        String serverResponse = in.readLine();
        if(serverResponse==null)
            throw new SocketException();
        if (Objects.equals(serverResponse, "CONNECTION_ESTABLISHED"))
            return ServerResponse.CONNECTION_ESTABLISHED;
        else if (Objects.equals(serverResponse, "WRONG_TOKEN"))
            return ServerResponse.WRONG_TOKEN;
        else
            return ServerResponse.ERROR;
    }

    private ServerResponse receive_token(String serverResponse) throws Exception {
        if(serverResponse==null)
            throw new SocketException();
        String[] tokenMessage = serverResponse.split(" ");
        if (tokenMessage.length != 2 || !Objects.equals(tokenMessage[0], "TOKEN"))
            return ServerResponse.ERROR;
        token = tokenMessage[1];
        save_token();
        return send_token();
    }



    public void play(){
        try {
            while (true) {
                String serverText = in.readLine();
                if(serverText==null)
                    throw new SocketException();
                if (serverText.startsWith("ANSWER_") || serverText.startsWith("CONCLUSIONS_")) {
                    String[] answerMessage = serverText.split("_");
                    System.out.println(answerMessage[1]);
                } else {
                    if (serverText.equals("GAME OVER")) {
                        this.gameState = GameState.GAME_OVER;
                        break;
                    }
                    if (serverText.equals("REMOTE_LOG_IN")) {
                        System.out.println(System.lineSeparator()+"Someone has logged in to your account from a different location");
                        this.gameState = GameState.ESTABLISHING_CONNECTION;
                        break;
                    }
                    String[] questionMessage = serverText.split("_");
                    int round = Integer.parseInt(questionMessage[1]);
                    String question = questionMessage[2];
                    System.out.println(question);
                    System.out.print("Enter your answer:");
                    BufferedReader user = new BufferedReader(new InputStreamReader(System.in));
                    while (!user.ready() && !in.ready()) {}

                    if (user.ready()) {
                        out.println("ANSWER_" + round + "_" + user.readLine());
                    }
                }
            }
        }
        catch (SocketException e) {
            reestablishConnection();
        }
        catch(Exception e){
            System.out.println("Something unexpected happened");
            e.printStackTrace(); //would remove in production;
            gameState = GameState.QUIT;
        }

    }

    public void waiting(){
        try {
            String serverText = in.readLine();
            if(serverText==null)
                throw new SocketException();
            if(Objects.equals(serverText, "RECONNECTED")){
                this.gameState = GameState.PLAYING;
                return;
            }
            else if(serverText.startsWith("QUEUE_POSITION")){
                String[] serverTextSplit = serverText.split("_");
                System.out.println("You have been placed in the queue in position " + serverTextSplit[2]);
                serverText = in.readLine();
                if(serverText==null)
                    throw new SocketException();
                serverTextSplit = serverText.split("_");
                if (Objects.equals(serverTextSplit[0], "PLAYERS") && Objects.equals(serverTextSplit[1], "WAITING")){
                    System.out.println("There are " + serverTextSplit[2] + " players waiting");
                    System.out.println("Waiting for enough players to start the game...");
                    this.gameState = GameState.PLAYING;
                    return;
                }
            }

        }
        catch (SocketException e) {
            reestablishConnection();
        }
        catch(Exception e){
            System.out.println("Something unexpected happened");
            e.printStackTrace(); //would remove in production;
            this.gameState = GameState.QUIT;
        }

    }


    public void reestablishConnection(){
        int num_attempts = 1;
        System.out.println("Lost connection to the server, trying to reestablish it");
        while(num_attempts<8) {
            try {
                if(num_attempts>1)
                    Thread.sleep(1000*(long) Math.pow(2,num_attempts));

                socket.close();
                out.close();
                in.close();
                // Connect to the server
                socket = new Socket(host, port);
                out = new PrintStream(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                if (token == null) {
                    gameState = GameState.ESTABLISHING_CONNECTION;
                    establishConnection();
                } else {
                    out.println("TOKEN " + token);
                    String serverResponse = in.readLine();
                    if(serverResponse==null)
                        throw new SocketException();
                    if (Objects.equals(serverResponse, "CONNECTION_ESTABLISHED")) {
                        gameState = GameState.WAITING;
                    } else if (Objects.equals(serverResponse, "INVALID_TOKEN")) {
                        token = null;
                        if (gameState != GameState.ESTABLISHING_CONNECTION) {
                            gameState = GameState.ESTABLISHING_CONNECTION;
                            System.out.println("We regret to inform you that your queue position/game progress was lost");
                        }
                        establishConnection();
                        if(gameState == GameState.WAITING){
                            return;
                        }
                    } else {
                        num_attempts++;
                    }
                }
            } catch (Exception e) {
                num_attempts++;
                System.out.println("Couldn't reestablish connection, trying again in " + (long) Math.pow(2,num_attempts) + " seconds");
            }
        }
        System.out.println("Maximum number of retries reached, quitting");
    }

    public void establishConnection(){
        while(gameState == GameState.ESTABLISHING_CONNECTION) {
            try {
                AuthenticationOption option = chooseAuthenticationOption();
                switch (option) {
                    case LOG_IN -> {
                        String username, password;
                        String[] userCredentials = getUserCredentials();
                        username = userCredentials[0];
                        password = userCredentials[1];
                        ServerResponse serverResponse = this.log_in(username, password);
                        switch (serverResponse) {
                            case CLIENT_ERROR -> System.out.println("Unexpected client error");
                            case SERVER_ERROR -> System.out.println("Unexpected server error");
                            case ERROR -> System.out.println("Something unexpected happened");
                            case INVALID_LOGIN -> System.out.println("Invalid username/password");
                            case WRONG_TOKEN -> {
                                gameState = GameState.ESTABLISHING_CONNECTION;
                                System.out.println("Something unexpected happened");
                            }
                            case CONNECTION_ESTABLISHED -> {
                                gameState = GameState.WAITING;
                                System.out.println("Logged in succesfully");
                            }
                        }
                    }
                    case REGISTER -> {
                        String username, password;
                        String[] userCredentials = getUserCredentials();
                        username = userCredentials[0];
                        password = userCredentials[1];
                        ServerResponse serverResponse = this.register_user(username, password);
                        switch (serverResponse) {
                            case CLIENT_ERROR -> System.out.println("Unexpected client error");
                            case SERVER_ERROR -> System.out.println("Unexpected server error");
                            case ERROR -> System.out.println("Something unexpected happened");
                            case INVALID_REGISTER -> System.out.println("Username already taken");
                            case WRONG_TOKEN -> {
                                gameState = GameState.ESTABLISHING_CONNECTION;
                                System.out.println("Something unexpected happened");
                            }
                            case CONNECTION_ESTABLISHED -> {
                                gameState = GameState.WAITING;
                                System.out.println("Registered succesfully");
                            }
                        }
                    }
                    case EXIT -> gameState = GameState.QUIT;
                }

            }
            catch (SocketException e) {
                reestablishConnection();
            }
            catch(Exception e){
                System.out.println("Something unexpected happened");
                e.printStackTrace(); //would remove in production;
                gameState = GameState.QUIT;
            }
        }
    }
    public static void main( String[] args ) {
        if(args.length>0){
            if(args[0].equals("clear_cookies")){
                try {
                    FileWriter fileWriter = new FileWriter("token.txt");
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write("");
                    bufferedWriter.close();
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        Client client = new Client ("localhost", 8080 );
        String token = client.get_token();
        if(token!=null && client.gameState!=GameState.QUIT){
            client.token = token;
            try {
                if (Objects.requireNonNull(client.send_token()) == ServerResponse.CONNECTION_ESTABLISHED) {
                    client.gameState = GameState.WAITING;
                } else {
                    client.gameState = GameState.ESTABLISHING_CONNECTION;
                    client.token = null;
                }
            }
            catch (SocketException e) {
                client.reestablishConnection();
            }
            catch(Exception e){
                System.out.println("Something unexpected happened");
                e.printStackTrace(); //would remove in production;
                client.gameState = GameState.QUIT;
            }
        }
        while (client.gameState != GameState.QUIT) {
            switch (client.gameState) {
                case ESTABLISHING_CONNECTION -> client.establishConnection();
                case PLAYING -> client.play();
                case WAITING -> client.waiting();
                case GAME_OVER -> client.dealWithGameOver();
                default -> System.out.println("Something unexpected happened");
            }
        }
        client.closeConnection();
    }

    private void dealWithGameOver() {
        Scanner scanner = new Scanner(System.in);
        String input;
        do {
            System.out.println("Game Over");
            System.out.println("1: Play again");
            System.out.println("2: Exit");
            input = scanner.nextLine();
        } while (!input.equals("1") && !input.equals("2"));
        if (input.equals("1")) {
            out.println("PLAY_AGAIN");
            this.gameState = GameState.WAITING;
        }else {
            this.gameState = GameState.QUIT;
        }
    }
}
