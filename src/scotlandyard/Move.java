package scotlandyard;

public abstract class Move {
    public final Colour colour;

    protected Move(Colour colour) {
        this.colour = colour;
    }

    @Override
    public String toString() {
        return this.colour.toString();
    }
}
