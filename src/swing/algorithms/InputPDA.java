package swing.algorithms;

import scotlandyard.*;

import java.util.*;

/**
 * A class to act as a DPDA to work out when there is enough information for a move.
 */

public class InputPDA {

    private Stack<String> stack;
    private Map<String, Integer> nodes;
    private Map<String, Ticket> tickets;
    private int currentState = 1;

    /**
     * Constructs a new InputPDA object.
     */
    public InputPDA() {
        stack = new Stack<String>();
        nodes = new HashMap<String, Integer>();
        tickets = new HashMap<String, Ticket>();
    }

    // Returns the top of the stack if it is an Integer.
    // @return the top of the stack.
    private Integer popStackInteger() {
        String nodeStr = stack.pop();
        Integer node = nodes.get(nodeStr);
        return node;
    }

    // Returns the top of the stack if it is an Ticket.
    // @return the top of the stack.
    private Ticket popStackTicket() {
        String ticketStr = stack.pop();
        Ticket ticket = tickets.get(ticketStr);
        return ticket;
    }

    // Pushes an Integer onto the top of the stack.
    // @param node the Integer to be pushed to the top of the stack.
    private void pushStack(Integer node) {
        String nodeStr = node.toString();
        nodes.put(nodeStr, node);
        stack.push(nodeStr);
    }

    // Pushes a Ticket onto the top of the stack.
    // @param node the Ticket to be pushed to the top of the stack.
    private void pushStack(Ticket ticket) {
        String ticketStr = ticket.toString();
        tickets.put(ticketStr, ticket);
        stack.push(ticketStr);
    }

    /**
     * Transitions the DPDA according to the input and what
     * the stack contains.
     * This handles transitions for Integer inputs.
     *
     * @param input the letter on the input 'tape'.
     */
    public void transition(Integer input) {
        switch(currentState) {
            case 1:
                pushStack(input);
                currentState = 2;
                break;
            case 2:
                popStackInteger();
                pushStack(input);
                currentState = 2;
                break;
            case 4:
                pushStack(input);
                currentState = 5;
                break;
            case 5:
                popStackInteger();
                pushStack(input);
                currentState = 5;
                break;
            case 6:
                pushStack(input);
                currentState = 7;
                break;
            case 7:
                popStackInteger();
                pushStack(input);
                currentState = 7;
                break;
            default:
                stack.clear();
                currentState = 1;
                break;
        }
    }

    /**
     * Transitions the DPDA according to the input and what
     * the stack contains.
     * This handles transitions for Ticket inputs.
     *
     * @param input the letter on the input 'tape'.
     */
    public void transition(Ticket input) {
        switch(currentState) {
            case 1:
                if (input.equals(Ticket.Double)) {
                    currentState = 4;
                }
                break;
            case 2:
                pushStack(input);
                currentState = 3;
                break;
            case 5:
                pushStack(input);
                currentState = 6;
                break;
            case 6:
                popStackTicket();
                pushStack(input);
                currentState = 6;
                break;
            case 7:
                pushStack(input);
                currentState = 8;
                break;
            default:
                stack.clear();
                currentState = 1;
                break;
        }
    }

    /**
     * Returns true if the DPDA is in an accept state.
     *
     * @return true if the DPDA is in an accept state.
     */
    public boolean isAccepted() {
        if (currentState == 3 || currentState == 8) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Resets the DPDA to its initial state and empties the stack.
     */
    public void reset() {
        stack.clear();
        nodes.clear();
        tickets.clear();
        currentState = 1;
    }

    /**
     * Creates either a MoveTicket or a MoveDouble from the contents
     * of the stack.
     *
     * @param colour the colour of the player whose Move is being made.
     * @return the Move for the specified player.
     */
    public Move createMove(Colour colour) {
        if (stack.size() == 2) {
            Ticket ticket = popStackTicket();
            Integer node = popStackInteger();
            return MoveTicket.instance(colour, ticket, node);
        } else if (stack.size() == 4) {
            Ticket ticketTwo = popStackTicket();
            Integer nodeTwo = popStackInteger();
            Ticket ticketOne = popStackTicket();
            Integer nodeOne = popStackInteger();
            MoveTicket moveOne = MoveTicket.instance(colour, ticketOne, nodeOne);
            MoveTicket moveTwo = MoveTicket.instance(colour, ticketTwo, nodeTwo);
            return MoveDouble.instance(colour, moveOne, moveTwo);
        }
        return MovePass.instance(colour);
    }

}
