package scotlandyard;

import graph.*;

import java.util.*;

public class ScotlandYardGraph extends UndirectedGraph<Integer, Transport> {
	/**
	 * Generates all possible moves with a single ticket for the player
	 * @param player the player that has to make the move
	 * @param location the location the player is starting from
	 * @param forbiddenLocations the locations which the player explicitly cannot go to
	 * @return a list containing all the legal moves
	 */
    public List<MoveTicket> generateMoves(PlayerData player, Integer location, Set<Integer> forbiddenLocations) {
    	List<MoveTicket> possibleLocations = new ArrayList<MoveTicket>();
    	List<Edge<Integer, Transport>> edges = getEdgesFrom(getNode(location)); //getting all the edges starting from the given location
    	if(edges == null) return possibleLocations;//getEdges from returns null when there are no edges so we gotta handle that case
    	//see if each edge represents a legal move for the player
    	for(Edge<Integer, Transport> edge : edges)
    	{
    		Transport whatKindOfTransport = edge.getData();//What kind of transport we must use following this edge
    		Ticket whatKindOfTicket = Ticket.fromTransport(whatKindOfTransport);//What kind of ticket we must use
    		Integer endLocation = edge.getTarget().getIndex(); //the end location of the ticket
    		MoveTicket moveTicket = MoveTicket.instance(player.getColour(), whatKindOfTicket, endLocation);//end result
    		//if we got that kind of ticket and location is not forbidden, we can make the move
    		boolean validityCriteria = player.hasTickets(moveTicket) && !forbiddenLocations.contains(endLocation);
    		if(validityCriteria) possibleLocations.add(moveTicket);
    	}
    	/*
    	 Now, if the player is MrX and has a black ticket, then he can use it
    	 to go to any of the locations he can using other kinds of tickets
    	 so we should generate those possibilities as well.
    	 */
    	if(player.hasTicket(Ticket.Secret))
    	{
    		List<MoveTicket> secretMoves = new ArrayList<MoveTicket>();
    		for(MoveTicket move : possibleLocations)
    		{
    			//Skip the moves by boat which already require a secret ticket
    			if(move.ticket != Ticket.Secret)
    			{
    				secretMoves.add(MoveTicket.instance(player.getColour(), Ticket.Secret, move.target));
    			}
    		}
    		//merge the two lists
    		possibleLocations.addAll(secretMoves);
    	}
        return possibleLocations;
    }
}
