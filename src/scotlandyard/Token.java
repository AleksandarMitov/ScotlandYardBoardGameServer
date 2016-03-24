package scotlandyard;

public class Token {

    private Integer token;
    private Colour colour;
    private long timestamp;

    public Token(Integer token, Colour colour, long timestamp) {
        this.token = token;
        this.colour = colour;
        this.timestamp = timestamp;
    }

    public Integer getToken() {
        return token;
    }

    public Colour getColour() {
        return colour;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
