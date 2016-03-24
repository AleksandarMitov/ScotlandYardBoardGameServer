import scotlandyard.*;

import java.io.*;
import java.util.*;

public class TestHelper {

    public final static Colour[] colours = {
        Colour.Black,
        Colour.Blue,
        Colour.Green,
        Colour.Red,
        Colour.White,
        Colour.Yellow
    };

    public final static Ticket[] tickets = {
        Ticket.Taxi,
        Ticket.Bus,
        Ticket.Underground,
        Ticket.Double,
        Ticket.Secret
    };

    public final static int[] locations = {
        5,
        15,
        63,
        67,
        98,
        121,
        140
    };

    public final static int[] mrXTicketNumbers = {
        4,
        3,
        3,
        2,
        5
    };

    public final static int[] detectiveTicketNumbers = {
        11,
        8,
        4,
        0,
        0
    };

    public static ScotlandYardGraph makeGraph(String graphFileName) {
        try {
            ScotlandYardGraphReader reader = new ScotlandYardGraphReader();
            return reader.readGraph(graphFileName);
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public static class SingleMovePlayer implements  Player {

        public MoveTicket move;
        public int location;

        public void notify(int location, List<Move> moves, Integer token, Receiver receiver) {
            this.location = location;
            for(Move move : moves) {
                if(move instanceof MoveTicket) {
                    this.move = (MoveTicket) move;
                    receiver.playMove(move, token);
                    return;
                }
            }
            receiver.playMove(null, token);
        }

    }

    public static class DoubleMovePlayer implements  Player {

        public MoveDouble chosenMove;

        public void notify(int location, List<Move> moves, Integer token, Receiver receiver) {
            for(Move move : moves) {
                if(move instanceof MoveDouble) {
                    chosenMove = (MoveDouble) move;
                    receiver.playMove(move, token);
                    return;
                }
            }
            receiver.playMove(null, token);
        }

    }

    public static SingleMovePlayer getSingleMovePlayer() {
        return new SingleMovePlayer();
    }

    public static DoubleMovePlayer getDoubleMovePlayer() {
        return new DoubleMovePlayer();
    }

    public static List<Boolean> getRounds() {
        List<Boolean> rounds = new ArrayList<Boolean>();
        rounds.add(false);
        rounds.add(false);
        rounds.add(true);
        rounds.add(false);
        rounds.add(false);
        return rounds;
    }

    public static void addMrxToGame(ScotlandYard game, int location) {
        addMrxToGame(game, TestHelper.getPlayer(), location);
    }

    public static void addMrxToGame(ScotlandYard game, Player player, int location) {
        game.join(player, Colour.Black, location, TestHelper.getTickets(true));
    }

    public static void addDetectiveToGame(ScotlandYard game, Player player, Colour colour, int location) {
        game.join(player, colour, location, TestHelper.getTickets(false));
    }

    public static void addDetectiveToGame(ScotlandYard game, Colour colour, int location) {
        addDetectiveToGame(game, TestHelper.getPlayer(), colour, location);
    }

    public static Map<Ticket, Integer> getTickets(boolean mrX) {
        Map<Ticket, Integer> tickets = new HashMap<Ticket, Integer>();
        for (int i = 0; i < TestHelper.tickets.length; i++) {
            if(mrX)
                tickets.put(TestHelper.tickets[i], TestHelper.mrXTicketNumbers[i]);
            else
                tickets.put(TestHelper.tickets[i], TestHelper.detectiveTicketNumbers[i]);
        }
        return tickets;
    }

    public static ScotlandYard getStoppedGame(int numDetectives) {
        return getStoppedGame(numDetectives, TestHelper.getRounds(), "graph.txt");
    }

    public static ScotlandYard getStoppedGame(int numDetectives, List<Boolean> rounds) {
        return getStoppedGame(numDetectives, rounds, "graph.txt");
    }

    public static ScotlandYard getStoppedGame(int numDetectives, String mapFilename) {
        return getStoppedGame(numDetectives, TestHelper.getRounds(), mapFilename);
    }

    public static ScotlandYard getStoppedGame(int numDetectives, List<Boolean> rounds, String graphFilename) {
        ScotlandYardGraph graph = makeGraph(graphFilename);
        return new StopTurnModel(numDetectives, rounds, graph, new ScotlandYardMapQueue<Integer, Token>(), 0);
    }

    public static ScotlandYard subscribedGame(int numDetectives, String graphFilename) {
        ScotlandYard game = TestHelper.getStoppedGame(numDetectives, TestHelper.getRounds(), graphFilename);
        for (int i = 0; i < numDetectives+1; i++) {
            game.join(TestHelper.getPlayer(),
                      colours[i], locations[i],
                      TestHelper.getTickets(colours[i] == Colour.Black));
        }
        return game;
    }

    public static ScotlandYard subscribedGame(int numDetectives) {
        return subscribedGame(numDetectives, "graph.txt");
    }

    public static class TestPlayer implements Player {

        public List<Move> moves;
        public Move move;
        public int location;

        public void notify(int location, List<Move> moves, Integer token, Receiver receiver) {
            this.moves = moves;
            this.location = location;
            Iterator<Move> it = moves.iterator();
            move = null;
            if(it.hasNext()) {
              move = it.next();
            }
            receiver.playMove(move, token);
        }

    }

    public static class StopTurnModel extends ScotlandYard {

        public StopTurnModel(int numberOfDetectives, List<Boolean> rounds, ScotlandYardGraph graph, MapQueue<Integer, Token> queue, Integer gameId) {
            super(numberOfDetectives, rounds, graph, queue,  gameId);
        }

        @Override
        public void playMove(Move move, Integer token) {
            //Get data off queue
            Token secretToken = queue.get(gameId);
            if (secretToken != null && token == secretToken.getToken()) {
                queue.remove(gameId);
                play(move);
                nextPlayer();
            }
        }

    }

    public static Player getPlayer() {
        return new SingleMovePlayer();
    }

}
