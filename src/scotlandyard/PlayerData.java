package scotlandyard;

import java.util.Map;

/**
 * A class that contains all the information about a particular player.
 */

public class PlayerData {

    private Player player;
    private Colour colour;
    private Integer location;
    public Map<Ticket, Integer> tickets;

    /**
     * Constructs a new PlayerData object.
     *
     * @param player the Player object associated with the player.
     * @param colour the colour of the player.
     * @param location the location of the player.
     * @param tickets the tickets associated with the player.
     */
    public PlayerData(Player player, Colour colour, Integer location, Map<Ticket, Integer> tickets) {
        this.player = player;
        this.colour = colour;
        this.location = location;
        this.tickets = tickets;
    }

    /**
     * Returns the Player object associated with the player.
     *
     * @return the Player object associated with the player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the Colour object associated with the player.
     *
     * @return the Colour object associated with the player.
     */
    public Colour getColour() {
        return colour;
    }

    /**
     * Sets the player's current location.
     *
     * @param location the player's current location.
     */
    public void setLocation(Integer location) {
        this.location = location;
    }

    /**
     * Returns the player's current location.
     *
     * @return the player's current location.
     */
    public Integer getLocation() {
        return location;
    }

    /**
     * Returns the player's current tickets.
     *
     * @return the player's current tickets.
     */
    public Map<Ticket, Integer> getTickets() {
        return tickets;
    }

    /**
     * Sets the player's current tickets.
     *
     * @param tickets the player's current tickets.
     */
    public void setTickets(Map<Ticket, Integer> tickets) {
        this.tickets = tickets;
    }

    /**
     * Adds a ticket to the player's current tickets.
     *
     * @param ticket the ticket to be added.
     */
    public void addTicket(Ticket ticket) {
        incTicket(true, ticket);
    }

    /**
     * Removes a ticket to the player's current tickets.
     *
     * @param ticket the ticket to be removed.
     */
    public void removeTicket(Ticket ticket) {
        incTicket(false, ticket);
    }

    // Increments the ticket count for a particular Ticket.
    // @param inc the boolean to decide whether to increment (true) or decrement (false).
    // @param ticket the Ticket whose number is to be changed.
    private void incTicket(boolean inc, Ticket ticket) {
        Integer ticketCount = tickets.get(ticket);
        if (inc) ticketCount++;
        else ticketCount--;
        tickets.remove(ticket);
        tickets.put(ticket, ticketCount);
    }

    public boolean hasTickets(Move move) {
        if (move instanceof MoveTicket) return hasTickets((MoveTicket) move);
        else if (move instanceof MoveDouble) return hasTickets((MoveDouble) move);
        else return true;
    }

    private boolean hasTickets(MoveTicket move) {
        if (tickets.get(move.ticket) > 0) return true;
        else return false;
    }

    private boolean hasTickets(MoveDouble move) {
        if (tickets.get(Ticket.Double) > 0) {
            MoveTicket move1 = (MoveTicket) move.move1;
            MoveTicket move2 = (MoveTicket) move.move2;
            if (move1.ticket.equals(move2.ticket)) {
                return tickets.get(move1.ticket) > 1;
            } else {
                return hasTickets(move1) && hasTickets(move2);
            }
        }
        return false;
    }
    
    /**
     * Tells if the player has a ticket of a given kind
     * 
     * @param ticket the ticket type to check for
     * @return true if he has at least 1 ticket of the given type, false otherwise
     */
    public boolean hasTicket(Ticket ticket)
    {
    	return tickets.containsKey(ticket) && tickets.get(ticket) > 0;
    }
}
