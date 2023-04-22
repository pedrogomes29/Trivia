package server;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

public class PlayerDatabase {
    private RandomAccessFile file;

    private SecureRandom saltGenerator;
    private long numPlayers;


    private static final int MAX_USERNAME_SIZE = 20;

    private static final int PASSWORD_SIZE = 64;

    private static final int MAX_SCORE_SIZE = 4;
    private static final long MAX_RECORD_SIZE = MAX_USERNAME_SIZE + 1 + PASSWORD_SIZE + 1 + MAX_SCORE_SIZE + System.getProperty("line.separator").length();

    private int numPlayersSeekOffset;

    public PlayerDatabase(String filename){
        try {
            file = new RandomAccessFile(filename, "rw");
            numPlayers = Integer.parseInt(file.readLine());
            numPlayersSeekOffset = String.valueOf(numPlayers).length() + System.getProperty("line.separator").length();
            this.saltGenerator = new SecureRandom();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean authenticateUser(String username,String password) {
        try {
            long low = 0;
            long high = numPlayers - 1;

            while (low <= high) {
                long mid = (low + high) / 2;
                file.seek(numPlayersSeekOffset +  mid * MAX_RECORD_SIZE);
                byte[] buffer = new byte[(int) MAX_RECORD_SIZE];
                file.readFully(buffer);
                String record = new String(buffer);
                String[] fields = record.split(",");
                String recordUsername = fields[0].trim();
                int cmp = username.compareTo(recordUsername);

                if (cmp < 0) {
                    high = mid - 1;
                } else if (cmp > 0) {
                    low = mid + 1;
                } else {
                    String recordPassword = fields[1].trim();
                    return Objects.equals(password, recordPassword);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public String bcrypt(String password) throws NoSuchAlgorithmException {
        byte[] salt = new byte[16];
        saltGenerator.nextBytes(salt);

        // Hash the password with the salt using SHA-512
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        byte[] hashedPassword = md.digest(password.getBytes());

        // Encode the hashed password as a Base64-encoded string
        return Base64.getEncoder().encodeToString(hashedPassword);

    }
    private boolean insertTextAtPosition(byte[] text,long insertPos){
        try {
            long eof = file.length();
            long shiftStart = insertPos + text.length;

            file.seek(insertPos);
            byte[] shiftedText = new byte[(int) (eof-insertPos)];
            file.read(shiftedText);
            file.seek(shiftStart);
            file.write(shiftedText);


            file.seek(insertPos);
            file.write(text);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public boolean addUser(String username,String password){
        try {
            long low = 0;
            long high = numPlayers - 1;
            long mid;



            while (low <= high) {
                mid = (low + high) / 2;
                file.seek(numPlayersSeekOffset + mid * MAX_RECORD_SIZE);
                byte[] buffer = new byte[(int) MAX_RECORD_SIZE];
                file.read(buffer);
                String record = new String(buffer);
                String existingUsername = record.substring(0, 20).trim();
                int comparison = existingUsername.compareTo(username);
                if (comparison < 0) {
                    low = mid + 1;
                } else if (comparison > 0) {
                    high = mid - 1;
                } else {
                    return false;
                }
            }


            // Determine the position to insert the new record
            long insertPos = numPlayersSeekOffset + low * MAX_RECORD_SIZE;

            if (username.length() > MAX_USERNAME_SIZE) {
                return false;
            } else if (username.length() < MAX_USERNAME_SIZE) {
                username = String.format("%" + (MAX_USERNAME_SIZE - username.length()) + "s", "") + username;
            }

            if (password.length() < PASSWORD_SIZE)
                password = String.format("%" + (PASSWORD_SIZE - password.length()) + "s", "") + password;

            String record = String.format("%s,%s, %s" + System.lineSeparator(), username, password, 100);
            byte[] recordBytes = record.getBytes();

            this.insertTextAtPosition(recordBytes,insertPos);

            numPlayers++;
            file.seek(0);
            if(String.valueOf(numPlayers).length() > String.valueOf(numPlayers-1).length()){
                file.write(String.valueOf(numPlayers/10).getBytes());
                this.insertTextAtPosition( String.valueOf(numPlayers%10).getBytes() , String.valueOf(numPlayers/10).length());
                numPlayersSeekOffset++;
            }
            else
                file.write(String.valueOf(numPlayers).getBytes());
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public void close() throws IOException {
        file.close();
    }
}
