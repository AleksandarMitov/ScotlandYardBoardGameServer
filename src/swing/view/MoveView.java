package swing.view;

import swing.application.*;
import scotlandyard.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

/**
 * A view to display Mr X's previous moves.
 */

public class MoveView extends JPanel implements MouseListener {

    private Map<JLabel, Tuple<Integer, Ticket>> locations;
    private List<Move> moves;
    private Map<Ticket, BufferedImage> tickets;
    private Map<Ticket, BufferedImage> ticketsBlank;
    private List<BufferedImage> startTickets;
    private ActionListener aListener;

    /**
     * Constructs a new MoveView which displays a list of all of Mr X's
     * previous moves.
     *
     * @param fileAccess the FileAccess object to get images.
     */
    public MoveView(FileAccess fileAccess) {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        setBackground(new Color(51, 51, 51));
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        this.aListener = null;
        this.tickets = fileAccess.getTickets();
        this.ticketsBlank = fileAccess.getTicketsBlank();
        this.startTickets = fileAccess.getStartTickets();
        this.moves = new ArrayList<Move>();
        this.locations = new HashMap<JLabel, Tuple<Integer, Ticket>>();
    }

    // Adds a new move to the view.
    // @param move the move to be added.
    private void addMove(Move move) {
        if (move instanceof MoveTicket) {
            insert((MoveTicket) move);
        } else if (move instanceof MoveDouble) {
            MoveDouble moveDouble = (MoveDouble) move;
            insert((MoveTicket) moveDouble.move1);
            insert((MoveTicket) moveDouble.move2);
        }
    }

    // Redraws all the moves when an update occurs.
    private void redrawMoves() {
        removeAll();
        for (Move move : moves) {
            addMove(move);
        }
    }

    // Creates the JLabel and adds it to the view for a Move.
    // @param move the Move to be inserted
    private void insert(MoveTicket move) {
        BufferedImage image = null;
        if (move.ticket == null) image = startTickets.get(0);
        else image = tickets.get(move.ticket);
        JLabel label = new JLabel(new ImageIcon(image));
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 30, 0));
        label.setForeground(Color.white);
        label.setIconTextGap(-60);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.CENTER);
        locations.put(label, new Tuple<Integer, Ticket>(move.target, move.ticket));
        label.addMouseListener(this);
        add(label);
    }

    /**
     * Updates the view with the specified move if it belongs
     * to Mr X.
     *
     * @param move the move to be checked and added.
     */
    public void update(Move move) {
        if (move.colour.equals(Colour.Black)) {
            moves.add(move);
            addMove(move);
        }
    }

    /**
     * Shows the location of a Move contained in a JLabel.
     *
     * @param label the JLabel containing the move whose location is
     * to be displayed.
     */
    public void showLocation(JLabel label) {
        Tuple<Integer, Ticket> tuple = locations.get(label);
        BufferedImage image = null;
        if (tuple.ticket == null) image = startTickets.get(1);
        else image = ticketsBlank.get(tuple.ticket);
        label.setIcon(new ImageIcon(image));
        label.setText("" + tuple.location);
    }

    /**
     * Hides all previously shown locations. This is so detectives can't
     * see Mr X's location.
     */
    public void hideLocations() {
        redrawMoves();
    }

    /**
     * Clears all of the Moves out of a MoveView, ready for a new game.
     */
    public void clearMoves() {
        removeAll();
        repaint();
    }

    /**
     * Adds the specified ActionListener to recieve when a Move is clicked on.
     * If listener listener is null, no action is performed.
     *
     * @param listener the listener to be added to the view.
     */
    public void setActionListener(ActionListener listener) {
        aListener = listener;
    }

    /**
     * Is called when a Move is clicked on. Sends an ActionEvent to the
     * added ActionListener.
     *
     * @param e the MouseEvent containing the JLabel of the Move clicked on.
     */
    public void mouseReleased(MouseEvent e) {
        if (e.getSource() instanceof JLabel) {
            if (aListener != null) {
                aListener.actionPerformed(new ActionEvent((JLabel) e.getSource(), 0, "move"));
            }
        }
    }

    /**
     * Unused method from the MouseListener interface.
     * @param e the MouseEvent containing the cursor location.
     */
    public void mouseClicked(MouseEvent e) {}
    /**
     * Unused method from the MouseListener interface.
     * @param e the MouseEvent containing the cursor location.
     */
    public void mousePressed(MouseEvent e) {}
    /**
     * Unused method from the MouseListener interface.
     * @param e the MouseEvent containing the cursor location.
     */
    public void mouseDragged(MouseEvent e) {}
    /**
     * Unused method from the MouseListener interface.
     * @param e the MouseEvent containing the cursor location.
     */
    public void mouseEntered(MouseEvent e) {}
    /**
     * Unused method from the MouseListener interface.
     * @param e the MouseEvent containing the cursor location.
     */
    public void mouseExited(MouseEvent e) {}
    /**
     * Unused method from the MouseListener interface.
     * @param e the MouseEvent containing the cursor location.
     */
    public void mouseMoved(MouseEvent e) {}

    // A class to represent a 2-Tuple of objects.
    private class Tuple<X, Y> {

        public final X location;
        public final Y ticket;

        /**
         * Constructs a new 2 entry tuple.
         *
         * @param location the first item in the tuple.
         * @param ticket the second item in the tuple.
         */
        public Tuple(X location, Y ticket) {
            this.location = location;
            this.ticket = ticket;
        }
    }

}
