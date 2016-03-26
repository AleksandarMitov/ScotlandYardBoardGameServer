package scotlandyard;

import graph.*;

import java.util.*;

public class ScotlandYardGraph extends UndirectedGraph<Integer, Transport> {

    public List<MoveTicket> generateMoves(Colour player, Integer location, Map<Ticket, Integer> tickets, Set<Integer> forbiddenLocations) {
    	List<MoveTicket> possibleLocations = new ArrayList<MoveTicket>();
    	List<Edge<Integer, Transport>> edges = getEdgesFrom(getNode(location)); //getting all the edges starting from the given location
    	if(edges == null) return possibleLocations;//getEdges from returns null when there are no edges so we gotta handle that case
    	for(Edge<Integer, Transport> edge : edges)
    	{
    		Transport whatKindOfTransport = edge.getData();//What kind of transport we must use following this edge
    		Ticket whatKindOfTicket = Ticket.fromTransport(whatKindOfTransport);//What kind of ticket we must use
    		Integer endLocation = edge.getTarget().getIndex(); //the end location of the ticket
    		MoveTicket moveTicket = MoveTicket.instance(player, whatKindOfTicket, endLocation);//end result
    		boolean validityCriteria = tickets.get(whatKindOfTicket) != null && tickets.get(whatKindOfTicket) > 0 && !forbiddenLocations.contains(endLocation);
    		if(validityCriteria) possibleLocations.add(moveTicket);//if we got that kind of ticket, we can make the move
    	}
    	/*
    	 Now, if the player is MrX and has a black ticket, then he can use it
    	 to go to any of the locations he can using other kinds of tickets
    	 so we should generate those possibilities as well.
    	 */
    	if(player == Colour.Black && tickets.get(Ticket.Secret) != null && tickets.get(Ticket.Secret) > 0)
    	{
    		List<MoveTicket> secretMoves = new ArrayList<MoveTicket>();
    		for(MoveTicket move : possibleLocations)
    		{
    			//Skip the moves by boat which already require a secret ticket
    			if(move.ticket != Ticket.Secret)
    			{
    				secretMoves.add(MoveTicket.instance(player, Ticket.Secret, move.target));
    			}
    		}
    		possibleLocations.addAll(secretMoves);
    	}
        return possibleLocations;
    }

}
