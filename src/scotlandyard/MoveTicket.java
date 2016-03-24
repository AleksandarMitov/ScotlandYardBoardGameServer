package scotlandyard;

import java.util.HashMap;
import java.util.Map;

public class MoveTicket extends Move {
    private static Map<Colour, Map<Ticket, Map<Integer, MoveTicket>>> cacheTicket = new HashMap<Colour, Map<Ticket, Map<Integer, MoveTicket>>>();

    private MoveTicket(Colour colour, Ticket ticket, int target) {
        super(colour);
        this.target = target;
        this.ticket = ticket;
    }

    public static MoveTicket instance(Colour colour, Ticket ticket, int target) {
        Map<Ticket, Map<Integer, MoveTicket>> ticketMap = cacheTicket.get(colour);
        if (ticketMap == null) {
            ticketMap = new HashMap<Ticket, Map<Integer, MoveTicket>>();
            cacheTicket.put(colour, ticketMap);
        }
        Map<Integer, MoveTicket> targetMap = ticketMap.get(ticket);
        if (targetMap == null) {
            targetMap = new HashMap<Integer, MoveTicket>();
            ticketMap.put(ticket, targetMap);
        }
        MoveTicket move = targetMap.get(target);
        if (move == null) {
            move = new MoveTicket(colour, ticket, target);
            targetMap.put(target, move);
        }
        return move;
    }

    public final int target;
    public final Ticket ticket;

    @Override
    public String toString() {
        return super.toString() + " " + this.target + " " + this.ticket;
    }

}
