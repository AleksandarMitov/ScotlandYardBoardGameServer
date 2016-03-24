import scotlandyard.*;

import java.util.*;

import org.junit.*;
import static org.junit.Assert.*;

public class DetectiveValidMovesTests extends ValidMovesHelper {

    @Test
    public void testDetectiveValidMoves() throws Exception {
        ScotlandYard game = ValidMovesHelper.simpleGame(1);
        player2 = new TestHelper.TestPlayer();

        TestHelper.addMrxToGame(game, TestHelper.getSingleMovePlayer(), 1);
        TestHelper.addDetectiveToGame(game, player2, Colour.Blue, 3);
        game.turn();
        game.turn();

        List<Move> moves = player2.moves;
        List<Move> expected = new ArrayList<Move>();
        expected.add(MoveTicket.instance(Colour.Blue, Ticket.Taxi, 4));
        expected.add(MoveTicket.instance(Colour.Blue, Ticket.Bus, 2));

        for (Move move : expected) {
            assertTrue("The returned set of valid moves should contain " + move, moves.contains(move));
        }
    }

    @Test
    public void testDetectiveValidMoves2() throws Exception {
        ScotlandYard game = ValidMovesHelper.simpleGame(1);
        player1 = new TestHelper.TestPlayer();
        player2 = new TestHelper.TestPlayer();
        TestHelper.addMrxToGame(game, player1, 1);
        TestHelper.addDetectiveToGame(game, player2, Colour.Blue, 2);
        game.turn();
        game.turn();

        List<Move> moves = player2.moves;
        List<Move> expected = new ArrayList<Move>();
        expected.add(MoveTicket.instance(Colour.Blue, Ticket.Bus, 3));
        expected.add(MoveTicket.instance(Colour.Blue, Ticket.Underground, 5));
        expected.add(MoveTicket.instance(Colour.Blue, Ticket.Taxi, 6));

        for (Move move : expected) {
            assertTrue("The returned set of valid moves should contain " + move, moves.contains(move));
        }
    }

    @Test
    public void testDetectiveInTargetLocationMakesInvalidTransport() throws Exception {
        ScotlandYard game = ValidMovesHelper.simpleGame(2);
        player1 = new TestHelper.TestPlayer();
        player2 = new TestHelper.TestPlayer();
        player3 = new TestHelper.TestPlayer();
        TestHelper.addMrxToGame(game, player1, 1);
        TestHelper.addDetectiveToGame(game, player2, Colour.Blue, 3);
        TestHelper.addDetectiveToGame(game, player3, Colour.Yellow, 2);
        game.turn();
        game.turn();

        List<Move> moves = player2.moves;
        Move testMove = MoveTicket.instance(Colour.Blue, Ticket.Bus, 2);

        assertFalse("If another detective is on an adjacent location, that location shouldn't be" +
                    " a valid route", moves.contains(testMove));
    }

    @Test
    public void testMrXInTargetLocationMakesValidTransport() throws Exception {
        ScotlandYard game = ValidMovesHelper.simpleGame(1);
        player1 = new TestHelper.TestPlayer();
        player2 = new TestHelper.TestPlayer();
        TestHelper.addMrxToGame(game, player1, 2);
        TestHelper.addDetectiveToGame(game, player2, Colour.Blue, 3);
        game.turn();
        game.turn();

        Move testMove = MoveTicket.instance(Colour.Blue, Ticket.Bus, 2);

        assertTrue("If Mr X is on an adjacent location, that location should be" +
                  " a valid route", player2.moves.contains(testMove));
    }

    @Test
    public void testMovesNotIncludedIfNotEnoughTickets() throws Exception {
        ScotlandYard game = ValidMovesHelper.simpleGame(1);
        player1 = new TestHelper.TestPlayer();
        player2 = new TestHelper.TestPlayer();
        TestHelper.addMrxToGame(game, player1, 2);

        Map<Ticket, Integer> tickets = TestHelper.getTickets(false);
        tickets.put(Ticket.Taxi, 0);
        game.join(player2, Colour.Blue, 4, tickets);

        game.turn();
        game.turn();

        List<Move> moves = player2.moves;
        Move testMove1 = MoveTicket.instance(Colour.Blue, Ticket.Taxi, 3);
        Move testMove2 = MoveTicket.instance(Colour.Blue, Ticket.Taxi, 5);

        assertFalse("If a detective is short on tickets the valid routes must not contain " +
                    "routes of this type", moves.contains(testMove1));
        assertFalse("If a detective is short on tickets the valid routes must not contain " +
                    "routes of this type", moves.contains(testMove2));
    }

    @Test
    public void testSameTransportWithDifferentTicketsShouldBeInValidTransports() throws Exception {
        ScotlandYard game = ValidMovesHelper.simpleGame(1);
        player1 = new TestHelper.TestPlayer();
        player2 = new TestHelper.TestPlayer();
        TestHelper.addMrxToGame(game, player1, 2);

        Map<Ticket, Integer> tickets = TestHelper.getTickets(false);
        tickets.put(Ticket.Taxi, 0);
        game.join(player2, Colour.Blue, 1, tickets);
        

        game.turn();
        game.turn();

        List<Move> moves = player2.moves;
        Move testMove1 = MoveTicket.instance(Colour.Blue, Ticket.Taxi, 6);
        Move testMove2 = MoveTicket.instance(Colour.Blue, Ticket.Bus , 6);

        assertFalse("If a detective is short on tickets the valid routes must not contain " +
                    "routes of this type", moves.contains(testMove1));
        assertTrue("If to routes connect the same stations but on of the routes has a ticket type " +
                  "that the detective has run out of, the other route should still be included",
                  moves.contains(testMove2));
    }

    @Test
    public void testDetectiveHasNoValidMoves() throws Exception {
        ScotlandYard game = ValidMovesHelper.simpleGame(1);
        player1 = new TestHelper.TestPlayer();
        player2 = new TestHelper.TestPlayer();
        TestHelper.addMrxToGame(game, player1, 2);
        TestHelper.addDetectiveToGame(game, player2, Colour.Blue, 7);
        game.turn();
        game.turn();

        List<Move> moves = player2.moves;

        Move testMove = MovePass.instance(Colour.Blue);

        assertEquals("If there are no valid moves only a single move should be returned", 1, moves.size());
        assertTrue("If there are no valid moves only a pass move should be returned", moves.contains(testMove));
    }

}
