import scotlandyard.*;

import java.util.*;

import org.junit.*;
import static org.junit.Assert.*;

public class SpectatorTests {

    public class TestSpectator implements Spectator {

        Move move;

        public void notify(Move move) {
            this.move = move;
        }

    }

    public class TestPlayer implements Player {

        Move chosenMove;
        List<Move> moves;

        public void notify(int location, List<Move> moves, Integer token, Receiver receiver) {
            chosenMove = moves.iterator().next();
            this.moves = moves;
            receiver.playMove(chosenMove, token);
        }

    }

    public class TestPlayer2 implements Player {

        Move chosenMove;
        List<Move> movesToUse;
        int count;

        TestPlayer2(List<Move> movesToUse) {
            this.movesToUse = movesToUse;
            count = 0;
        }

        public void notify(int location, List<Move> moves, Integer token, Receiver receiver) {
            chosenMove = movesToUse.get(count);
            count++;
            receiver.playMove(chosenMove, token);
        }

    }

    @Test
    public void testSpectatorIsNotifiedOfADetectiveMove() throws Exception {
        ScotlandYard game = TestHelper.getStoppedGame(1, "test_resources/small_map.txt");
        TestHelper.addMrxToGame(game, 3);

        TestPlayer player = new TestPlayer();

        TestHelper.addDetectiveToGame(game, player, Colour.Blue, 7);

        TestSpectator spectator = new TestSpectator();
        game.spectate(spectator);
        game.turn();
        game.turn();

        assertNotNull("The spectator should have received a move", spectator.move);
    }

    @Test
    public void testSpectatorIsNotifiedOfADetectiveMove2() throws Exception {
        ScotlandYard game = TestHelper.getStoppedGame(1, "test_resources/small_map.txt");
        TestHelper.addMrxToGame(game, 3);

        TestPlayer player = new TestPlayer();

        TestHelper.addDetectiveToGame(game, player, Colour.Blue, 7);

        TestSpectator spectator = new TestSpectator();
        game.spectate(spectator);
        game.turn();
        game.turn();

        assertEquals("The spectators move should be the same as the detectives chosen move",
                    player.chosenMove, spectator.move);
    }

    @Test
    public void testSpectatorIsNotifiedOfAMrXMove() throws Exception {
        ScotlandYard game = TestHelper.getStoppedGame(1, "test_resources/small_map.txt");
        TestHelper.addMrxToGame(game, 3);

        TestPlayer player = new TestPlayer();

        TestHelper.addDetectiveToGame(game, player, Colour.Blue, 7);

        TestSpectator spectator = new TestSpectator();
        game.spectate(spectator);
        game.turn();

        assertNotNull("The spectator should have received a mr X move", spectator.move);
    }

    @Test
    public void testSpectatorIsGivenMrXsLastKnowLocationIfHeIsHidden() throws Exception {
        List<Boolean> rounds = Arrays.asList(false, false, false);
        ScotlandYard game = TestHelper.getStoppedGame(1, rounds, "test_resources/small_map.txt");

        List<Move> movesToUse = new ArrayList<Move>();
        movesToUse.add(MoveTicket.instance(Colour.Black, Ticket.Bus, 2));
        movesToUse.add(MoveTicket.instance(Colour.Black, Ticket.Bus, 1));
        movesToUse.add(MoveTicket.instance(Colour.Black, Ticket.Taxi, 6));
        TestPlayer2 player = new TestPlayer2(movesToUse);

        TestHelper.addMrxToGame(game, player, 3);

        TestHelper.addDetectiveToGame(game, Colour.Blue, 7);

        TestSpectator spectator = new TestSpectator();
        game.spectate(spectator);
        game.turn();
        game.turn();
        game.turn();

        MoveTicket moveTicket = (MoveTicket) spectator.move;

        assertEquals("If Mr X is not currently visible, the location in the move should be " +
                    "his last known location", moveTicket.target, game.getPlayerLocation(Colour.Black));
        assertEquals("If Mr X is not currently visible, the location in the move should be " +
                    "his last known location", 0, moveTicket.target);
        assertEquals("If Mr X is not currently visible, the location in the move should be " +
                    "his last known location", 0, game.getPlayerLocation(Colour.Black));
    }

    @Test
    public void testSpectatorIsGivenCorrectLocationWhenMrXIsVisible() throws Exception {
        List<Boolean> rounds = Arrays.asList(false, true, true);
        ScotlandYard game = TestHelper.getStoppedGame(1, rounds, "test_resources/small_map.txt");

        List<Move> movesToUse = new ArrayList<Move>();
        movesToUse.add(MoveTicket.instance(Colour.Black, Ticket.Bus, 2));
        movesToUse.add(MoveTicket.instance(Colour.Black, Ticket.Bus, 1));
        movesToUse.add(MoveTicket.instance(Colour.Black, Ticket.Taxi, 6));
        TestPlayer2 player = new TestPlayer2(movesToUse);

        TestHelper.addMrxToGame(game, player, 3);

        TestHelper.addDetectiveToGame(game, Colour.Blue, 7);

        TestSpectator spectator = new TestSpectator();
        game.spectate(spectator);
        assertEquals("1 If Mr X is not currently visible, the location in the move should be " +
                    "his last known location", 0, game.getPlayerLocation(Colour.Black));
        game.turn();

        assertEquals("2 If Mr X is not currently visible, the location in the move should be " +
                    "his last known location", 2, game.getPlayerLocation(Colour.Black));
        game.turn();

        assertEquals("3 If Mr X is not currently visible, the location in the move should be " +
                    "his last known location", 2, game.getPlayerLocation(Colour.Black));
        game.turn();

        assertEquals("4 If Mr X is not currently visible, the location in the move should be " +
                    "his last known location", 1, game.getPlayerLocation(Colour.Black));
        MoveTicket moveTicket = (MoveTicket) spectator.move;

        assertEquals("5 If Mr X is currently visible, the location in the move should be " +
                    "his current location", moveTicket.target, ((MoveTicket)player.chosenMove).target);
    }

}
