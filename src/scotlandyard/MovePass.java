package scotlandyard;

import java.util.HashMap;
import java.util.Map;

public class MovePass extends Move {
    private MovePass(Colour colour) {
        super(colour);
    }

    private static Map<Colour, MovePass> cachePass = new HashMap<Colour, MovePass>();

    public static MovePass instance(Colour colour) {
        MovePass move = cachePass.get(colour);
        if (move == null) {
            move = new MovePass(colour);
            cachePass.put(colour, move);
        }
        return move;
    }

    @Override
    public String toString() {
        return "Move Pass " + super.toString();
    }

}
