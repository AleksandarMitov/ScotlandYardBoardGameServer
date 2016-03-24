package scotlandyard;

import java.util.HashMap;
import java.util.Map;

public class MoveDouble extends Move {
    public final MoveTicket move1;
    public final MoveTicket move2;
    private static Map<Colour, Map<Ticket, Map<Integer, Map<Ticket, Map<Integer, MoveDouble>>>>> cacheDoubleMove = new HashMap<Colour, Map<Ticket, Map<Integer, Map<Ticket, Map<Integer, MoveDouble>>>>>();

    private MoveDouble(Colour player, MoveTicket move1, MoveTicket move2) {
        super(player);
        this.move1 = move1;
        this.move2 = move2;
    }

    public static MoveDouble instance(Colour colour, MoveTicket move1, MoveTicket move2) {
        return instance(colour, move1.ticket, move1.target, move2.ticket, move2.target);

    }

    public static MoveDouble instance(Colour colour, Ticket ticket1, int target1, Ticket ticket2, int target2) {
        Map<Ticket, Map<Integer, Map<Ticket, Map<Integer, MoveDouble>>>> ticketMap1 = cacheDoubleMove.get(colour);
        if (ticketMap1 == null) {
            ticketMap1 = new HashMap<Ticket, Map<Integer, Map<Ticket, Map<Integer, MoveDouble>>>>();
            cacheDoubleMove.put(colour, ticketMap1);
        }
        Map<Integer, Map<Ticket, Map<Integer, MoveDouble>>> targetMap1 = ticketMap1.get(ticket1);
        if (targetMap1 == null) {
            targetMap1 = new HashMap<Integer, Map<Ticket, Map<Integer, MoveDouble>>>();
            ticketMap1.put(ticket1, targetMap1);
        }
        Map<Ticket, Map<Integer, MoveDouble>> ticketMap2 = targetMap1.get(target1);
        if (ticketMap2 == null) {
            ticketMap2 = new HashMap<Ticket, Map<Integer, MoveDouble>>();
            targetMap1.put(target1, ticketMap2);
        }
        Map<Integer, MoveDouble> targetMap2 = ticketMap2.get(ticket2);
        if (targetMap2 == null) {
            targetMap2 = new HashMap<Integer, MoveDouble>();
            ticketMap2.put(ticket2, targetMap2);
        }
        MoveDouble move = targetMap2.get(target2);
        if (move == null) {
            MoveTicket move1 = MoveTicket.instance(colour, ticket1, target1);
            MoveTicket move2 = MoveTicket.instance(colour, ticket2, target2);
            move = new MoveDouble(colour, move1, move2);
            targetMap2.put(target2, move);
        }
        return move;
    }

    @Override
    public String toString() {
        return "Move Double " + super.toString() + ": " + move1.target + " " + move1.ticket + " -> " + move2.target + " " + move2.ticket;
    }

}
