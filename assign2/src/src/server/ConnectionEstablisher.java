package server;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

public class ConnectionEstablisher extends Thread{
    private final Socket socket;
    ConnectionEstablisher( Socket socket )
    {
        this.socket = socket;
    }

    private boolean is_token_valid(String token){
        return true;
    }

    private String generateToken(){
        return "TOKEN_EXAMPLE";
    }

    private boolean authenticateUser(String userName,String password){

    }

    @Override
    public void run()
    {
        try
        {
            System.out.println( "Received a connection" );

            // Get input and output streams
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            PrintStream out = new PrintStream( socket.getOutputStream(),true);


            String clientMessage = in.readLine();
            if(Objects.equals(clientMessage, "LOG_IN")){
                String username = in.readLine();
                String password = in.readLine();
            }
            else if(Objects.equals(clientMessage, "REGISTER")){

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

            in.close();
            out.close();

            System.out.println( "Connection closed" );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}

