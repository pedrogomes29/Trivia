package server;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Objects;
import java.util.UUID;

public class ConnectionEstablisher extends Thread{
    private final Socket socket;
    private final PlayerDatabase db;

    ConnectionEstablisher( Socket socket ,PlayerDatabase db)
    {
        this.socket = socket;
        this.db = db;
    }

    private boolean is_token_valid(String token){
        return true;
    }

    private String generateToken(){
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }


    @Override
    public void run()
    {
        try
        {

            // Get input and output streams
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            PrintStream out = new PrintStream( socket.getOutputStream(),true);


            String clientMessage = in.readLine();
            if(Objects.equals(clientMessage, "LOG_IN")){
                String[] usernameMessage = in.readLine().split(" ");
                String[] passwordMessage = in.readLine().split(" ");

                if(usernameMessage.length!=2 || !Objects.equals(usernameMessage[0], "USERNAME")  ||
                        passwordMessage.length!=2 || !Objects.equals(passwordMessage[0], "PASSWORD") )
                    out.println("INVALID_LOG_IN");
                else {
                    String username = usernameMessage[1];
                    String password = passwordMessage[1];
                    if (db.authenticateUser(username, password)) {
                        String token = generateToken();
                        out.println("TOKEN " + token);
                        clientMessage = in.readLine();
                        if (!Objects.equals(clientMessage, "RECEIVED_TOKEN " + token))
                            out.println("WRONG_TOKEN");
                        else {
                            out.println("CONNECTION_ESTABLISHED");
                        }
                    } else {
                        out.println("INVALID_LOG_IN");
                    }
                }
            }
            else if(Objects.equals(clientMessage, "REGISTER")){
                String[] usernameMessage = in.readLine().split(" ");
                String[] passwordMessage = in.readLine().split(" ");

                if(usernameMessage.length!=2 || !Objects.equals(usernameMessage[0], "USERNAME")  ||
                        passwordMessage.length!=2 || !Objects.equals(passwordMessage[0], "PASSWORD") )
                    out.println("INVALID_REGISTER");
                else {
                    String username = usernameMessage[1];
                    String password = passwordMessage[1];
                    if (db.addUser(username, password)) {
                        String token = generateToken();
                        out.println("TOKEN " + token);
                        clientMessage = in.readLine();
                        if (!Objects.equals(clientMessage, "RECEIVED_TOKEN " + token))
                            out.println("WRONG_TOKEN");
                        else {
                            out.println("CONNECTION_ESTABLISHED");
                        }
                    } else {
                        out.println("INVALID_REGISTER");
                    }
                }
            }
            else if(clientMessage.startsWith("TOKEN")){
                String token = clientMessage.split(" ") [1];
                if(is_token_valid(token)){
                    out.println("CONNECTION_ESTABLISHED");

                }
                else{
                    out.println("INVALID_TOKEN");
                }
            }
            else{
                out.println("INVALID_RESPONSE");
            }

        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}

