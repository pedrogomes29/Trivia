package server;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ConnectionEstablisher extends Thread{
    private final Socket socket;
    private final PlayerDatabase db;
    private HashMap<String,String> tokenToUsername;
    private List<Player> players_waiting;

    private static final int MAX_NR_ATTEMPTS = 10;

    ConnectionEstablisher(Socket socket , PlayerDatabase db, HashMap<String,String> tokenToUsername,List<Player> players_waiting)
    {
        this.socket = socket;
        this.db = db;
        this.tokenToUsername = tokenToUsername;
        this.players_waiting = players_waiting;
    }


    private String generateToken(){
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }



    @Override
    public void run()
    {
        BufferedReader in=null;
        PrintStream out=null;
        int nrAttempts = 0;
        boolean connectionEstablished = false;
        String username = null;

        while(!connectionEstablished && nrAttempts<MAX_NR_ATTEMPTS) {
            try {
                // Get input and output streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintStream(socket.getOutputStream(), true);
                connectionEstablished = true;
            } catch (Exception e) {
                e.printStackTrace();
                nrAttempts++;
            }
        }

        if(nrAttempts==MAX_NR_ATTEMPTS) {
            try {
                socket.close();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        connectionEstablished = false;


        while (!connectionEstablished && nrAttempts < MAX_NR_ATTEMPTS) {
            try
            {
                String clientMessage = in.readLine();
                if(Objects.equals(clientMessage, "LOG_IN")){
                    String[] usernameMessage = in.readLine().split(" ");
                    String[] passwordMessage = in.readLine().split(" ");

                    if(usernameMessage.length!=2 || !Objects.equals(usernameMessage[0], "USERNAME")  ||
                            passwordMessage.length!=2 || !Objects.equals(passwordMessage[0], "PASSWORD") )
                        out.println("CLIENT_ERROR");
                    else {
                        username = usernameMessage[1];
                        String password = passwordMessage[1];
                        if (db.authenticateUser(username, password)) {
                            String token = generateToken();
                            tokenToUsername.put(username,token);
                            out.println("TOKEN " + token);
                            clientMessage = in.readLine();
                            if (!Objects.equals(clientMessage, "RECEIVED_TOKEN " + token))
                                out.println("WRONG_TOKEN");
                            else {
                                out.println("CONNECTION_ESTABLISHED");
                                connectionEstablished = true;
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
                        out.println("CLIENT_ERROR");
                    else {
                        username = usernameMessage[1];
                        String password = passwordMessage[1];
                        if (db.addUser(username, password)) {
                            String token = generateToken();
                            tokenToUsername.put(username,token);
                            out.println("TOKEN " + token);
                            clientMessage = in.readLine();
                            if (!Objects.equals(clientMessage, "RECEIVED_TOKEN " + token))
                                out.println("WRONG_TOKEN");
                            else {
                                out.println("CONNECTION_ESTABLISHED");
                                connectionEstablished = true;
                            }
                        } else {
                            out.println("INVALID_REGISTER");
                        }
                    }
                }
                else if(clientMessage.startsWith("TOKEN")){
                    String token = clientMessage.split(" ") [1];
                    username = tokenToUsername.get(token);

                    if(username!=null){
                        out.println("CONNECTION_ESTABLISHED");
                        connectionEstablished = true;
                    }
                    else{
                        out.println("INVALID_TOKEN");
                    }
                }
                else if(Objects.equals(clientMessage, "EXIT")){
                    nrAttempts = MAX_NR_ATTEMPTS - 1;
                }
                else
                    out.println("INVALID_RESPONSE");


                nrAttempts++;
            }
            catch( Exception e )
            {
                nrAttempts = MAX_NR_ATTEMPTS;
                out.println("INTERNAL_SERVER_ERROR");
                e.printStackTrace();
            }

        }

        if(nrAttempts==MAX_NR_ATTEMPTS) {
            try {
                if(in!=null)
                    in.close();
                if(out!=null)
                    out.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {

            int skilLevel = db.getSkillLevel(username);

            Player player = new Player(this.socket, skilLevel);

            players_waiting.add(player);
        }

    }
}

