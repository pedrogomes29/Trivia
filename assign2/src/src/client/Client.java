package client;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;


public class Client {

    private Socket socket;
    private PrintStream out;
    private BufferedReader in;
    private String token;

    enum AuthenticationOption{
        LOG_IN,
        REGISTER,
        EXIT
    }

    public Client(String host,int port){
        try
        {
            // Connect to the server
            socket = new Socket( host, port );

            // Create input and output streams to read from and write to the server
            out = new PrintStream( socket.getOutputStream(),true);
            in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

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


    public static AuthenticationOption chooseAuthenticationOption(){
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
                return AuthenticationOption.EXIT;
            }
        }
    }


    public boolean authenticate_user(String username,String password) throws Exception {
        out.println("LOG_IN");
        out.println("USERNAME "+ username);
        out.println("PASSWORD " + password);
        String serverResponse;
        serverResponse = in.readLine();
        String[] tokenMessage = serverResponse.split(" ");
        if(tokenMessage.length!=2 || !Objects.equals(tokenMessage[0], "TOKEN"))
            return false;
        token = tokenMessage[1];
        out.println("RECEIVED_TOKEN " + token);
        serverResponse = in.readLine();
        return Objects.equals(serverResponse, "CONNECTION_ESTABLISHED");
    }

    public boolean register_user(String username,String password) throws Exception {
        out.println("REGISTER");
        out.println("USERNAME "+ username);
        out.println("PASSWORD " + password);
        String serverResponse;
        serverResponse = in.readLine();
        String[] tokenMessage = serverResponse.split(" ");
        if(tokenMessage.length!=2 || !Objects.equals(tokenMessage[0], "TOKEN"))
            return false;
        token = tokenMessage[1];
        out.println("RECEIVED_TOKEN " + token);
        serverResponse = in.readLine();
        return Objects.equals(serverResponse, "CONNECTION_ESTABLISHED");
    }
    public void establishConnection(){
        try
        {
            AuthenticationOption option = chooseAuthenticationOption();
            if(option==AuthenticationOption.LOG_IN) {
                String username,password;
                do {
                    String[] userCredentials = getUserCredentials();
                    username = userCredentials[0];
                    password = userCredentials[1];
                }while(!this.authenticate_user(username,password));
            }
            if(option==AuthenticationOption.REGISTER) {
                String username,password;
                do {
                    String[] userCredentials = getUserCredentials();
                    username = userCredentials[0];
                    password = userCredentials[1];
                }while(!this.register_user(username,password));
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
    public static void main( String[] args )
    {
        Client client = new Client ("localhost", 8080 );

        client.establishConnection();

        System.out.println("Succesfully established connection: "+ client.token);

        client.closeConnection();
    }
}
