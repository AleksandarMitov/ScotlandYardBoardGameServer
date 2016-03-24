import scotlandyard.*;

import java.util.*;

import org.junit.*;
import static org.junit.Assert.*;

public class ValidMovesHelper {

    TestHelper.TestPlayer player1;
    TestHelper.TestPlayer player2;
    TestHelper.TestPlayer player3;

    public static ScotlandYard simpleGame(int numDetectives) throws Exception {
        List<Boolean> rounds = new ArrayList<Boolean>();
        rounds.add(true);
        rounds.add(true);
        rounds.add(true);
        rounds.add(true);
        return TestHelper.getStoppedGame(numDetectives, rounds, "test_resources/small_map.txt");
    }

}
