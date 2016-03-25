package scotlandyard;

import graph.*;

import java.util.*;

public class ScotlandYardGraph extends UndirectedGraph<Integer, Transport> {

    public List<MoveTicket> generateMoves(Colour player, Integer location, Map<Ticket, Integer> tickets) {
    	List<MoveTicket> possibleLocations = new ArrayList<MoveTicket>();
    	List<Edge<Integer, Transport>> edges = getEdgesFrom(getNode(location)); //getting all the edges starting from the given location
    	if(edges == null) return possibleLocations;//getEdges from returns null when there are no edges so we gotta handle that case
    	for(Edge<Integer, Transport> edge : edges)
    	{
    		Transport whatKindOfTransport = edge.getData();//What kind of transport we must use following this edge
    		Ticket whatKindOfTicket = Ticket.fromTransport(whatKindOfTransport);//What kind of ticket we must use
    		Integer endLocation = edge.getTarget().getIndex(); //the end location of the ticket
    		MoveTicket moveTicket = MoveTicket.instance(player, whatKindOfTicket, endLocation);//end result
    		if(tickets.get(whatKindOfTicket) != null && tickets.get(whatKindOfTicket) > 0) possibleLocations.add(moveTicket);//if we got that kind of ticket, we can make the move
    	}
        return possibleLocations;
    }

}
