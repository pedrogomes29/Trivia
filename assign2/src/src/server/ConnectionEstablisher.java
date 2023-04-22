package server;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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

    @Override
    public void run()
    {
        try
        {
            System.out.println( "Received a connection" );

            // Get input and output streams
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            PrintWriter out = new PrintWriter( socket.getOutputStream() );

            out.println("REQUEST_TOKEN\n");
            out.flush();

            String answer = in.readLine();
            if(Objects.equals(answer, "NO_TOKEN")){
                System.out.println("TOKEN_"+generateToken()+"\n");
            }
            else if(answer.startsWith("TOKEN")){
                String token = answer.split(" ") [1];
                if(is_token_valid(token)){
                    out.println("CONNECTION_ESTABLISHED");
                }
                else{
                    out.println("INVALID_TOKEN");
                    run();
                }
            }
            else{
                out.println("INVALID_RESPONSE");
                run();
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

