package client;
import server.Game;

import javax.imageio.IIOException;
import java.io.*;
import java.net.Socket;
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

        CONNECTION_ERROR,

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
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        try
        {
            out.println("EXIT");
            in.close();
            out.close();
            socket.close();

        }
        catch( Exception e )
        {
            e.printStackTrace();
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
            FileWriter myWriter = new FileWriter("../../../token.txt");
            myWriter.write(this.token);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private String get_token(){
        File f = new File("../../../token.txt");
        if(f.exists() && !f.isDirectory()) {
            Scanner myReader = null;
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

    private ServerResponse receive_token(String serverResponse) throws Exception {
        String[] tokenMessage = serverResponse.split(" ");
        if (tokenMessage.length != 2 || !Objects.equals(tokenMessage[0], "TOKEN"))
            return ServerResponse.ERROR;
        token = tokenMessage[1];
        save_token();
        out.println("TOKEN " + token);
        serverResponse = in.readLine();
        if (Objects.equals(serverResponse, "CONNECTION_ESTABLISHED"))
            return ServerResponse.CONNECTION_ESTABLISHED;
        else if (Objects.equals(serverResponse, "WRONG_TOKEN"))
            return ServerResponse.WRONG_TOKEN;
        else
            return ServerResponse.ERROR;
    }


    public void play(){
        System.out.println("Waiting for enough players to start a game...");
        try {
            while (true) {
                String serverText = in.readLine();
                if (serverText.startsWith("ANSWER_") || serverText.startsWith("CONCLUSIONS_")) {
                    String[] answerMessage = serverText.split("_");
                    System.out.println(answerMessage[1]);
                } else {
                    System.out.println(serverText);
                    if (serverText.equals("GAME OVER")) {
                        break;
                    }
                    System.out.print("Enter your answer:");
                    BufferedReader user = new BufferedReader(new InputStreamReader(System.in));
                    while (!user.ready() && !in.ready()) {}

                    if (user.ready()) {
                        out.println(user.readLine());
                        break;
                    }
                }
            }
        }

        catch( Exception e )
        {
            System.out.println("Lost connection..");
            e.printStackTrace();
            reestablishConnection();
            if(gameState == GameState.PLAYING) {
                System.out.println("Restablished connection");
                //voltar a jogar
            }
        }
    }

    public void reestablishConnection(){
        try {
            // Connect to the server
            socket = new Socket(host, port);
            out = new PrintStream(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if(token==null) {
                gameState = GameState.ESTABLISHING_CONNECTION;
                establishConnection();
            }
            else {
                out.println("TOKEN " + token);
                String serverResponse = in.readLine();
                if (Objects.equals(serverResponse, "CONNECTION_ESTABLISHED")) {
                    gameState = GameState.PLAYING;
                }
                else if (Objects.equals(serverResponse, "INVALID_TOKEN")) {
                    token=null;
                    if(gameState != GameState.ESTABLISHING_CONNECTION) {
                        gameState = GameState.ESTABLISHING_CONNECTION;
                        System.out.println("We regret to inform that you lost the connection to the game and it can't be reconnected");
                    }
                    establishConnection();
                }
                else{
                    reestablishConnection();
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
            reestablishConnection();
        }

    }

    public void establishConnection(){
        while(gameState == GameState.ESTABLISHING_CONNECTION) {
            try {
                AuthenticationOption option = chooseAuthenticationOption();
                if (option == AuthenticationOption.LOG_IN) {
                    String username, password;
                    String[] userCredentials = getUserCredentials();
                    username = userCredentials[0];
                    password = userCredentials[1];
                    ServerResponse serverResponse = this.log_in(username, password);
                    switch (serverResponse) {
                        case CLIENT_ERROR -> {
                            System.out.println("Unexpected client error");
                            break;
                        }
                        case SERVER_ERROR -> {
                            System.out.println("Unexpected server error");
                            break;
                        }
                        case ERROR -> {
                            System.out.println("Something unexpected happened");
                            break;
                        }
                        case INVALID_LOGIN -> {
                            System.out.println("Invalid username/password");
                            break;
                        }
                        case WRONG_TOKEN -> {
                            do {
                                serverResponse = receive_token(in.readLine());
                            } while (serverResponse != ServerResponse.CONNECTION_ESTABLISHED);
                            gameState = GameState.PLAYING;
                            System.out.println("Logged in succesfully");
                            break;
                        }
                        case CONNECTION_ESTABLISHED -> {
                            gameState = GameState.PLAYING;
                            System.out.println("Logged in succesfully");
                            break;
                        }
                    }
                } else if (option == AuthenticationOption.REGISTER) {
                    String username, password;
                    String[] userCredentials = getUserCredentials();
                    username = userCredentials[0];
                    password = userCredentials[1];
                    ServerResponse serverResponse = this.register_user(username, password);
                    switch (serverResponse) {
                        case CLIENT_ERROR -> {
                            System.out.println("Unexpected client error");
                            break;
                        }
                        case SERVER_ERROR -> {
                            System.out.println("Unexpected server error");
                            break;
                        }
                        case ERROR -> {
                            System.out.println("Something unexpected happened");
                            break;
                        }
                        case INVALID_REGISTER -> {
                            System.out.println("Username already taken");
                            break;
                        }
                        case WRONG_TOKEN -> {
                            do {
                                serverResponse = receive_token(in.readLine());
                            } while (serverResponse != ServerResponse.CONNECTION_ESTABLISHED);
                            gameState = GameState.PLAYING;
                            System.out.println("Registered succesfully");
                            break;
                        }
                        case CONNECTION_ESTABLISHED -> {
                            gameState = GameState.PLAYING;
                            System.out.println("Registered succesfully");
                            break;
                        }
                    }
                } else if (option == AuthenticationOption.EXIT) {
                    gameState = GameState.QUIT;
                }

            } catch (Exception e) {
                e.printStackTrace();
                reestablishConnection();
            }
        }
    }
    public static void main( String[] args ) throws IOException
    {
        if(args.length>0){
            if(args[0].equals("clear_cookies")){
                File myObj = new File("../../../token.txt");
                myObj.delete();
            }

        }

        Client client = new Client ("localhost", 8080 );
        String token = client.get_token();
        if(token!=null){
            client.token = token;
            client.reestablishConnection();
        }
        while (client.gameState != GameState.QUIT) {
            if (client.gameState == GameState.ESTABLISHING_CONNECTION)
                client.establishConnection();
            if (client.gameState == GameState.PLAYING)
                client.play();
        }

        client.closeConnection();
    }
}
