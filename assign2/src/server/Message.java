package server;

public class Message {
    public Player player;
    public byte[] bytes;
    public Message(byte[] bytes,Player player) {
        this.bytes = bytes;
        this.player = player;
    }

    public Message(String message,Player player) {
        this.bytes = (message+System.lineSeparator()).getBytes();
        this.player = player;
    }
}
