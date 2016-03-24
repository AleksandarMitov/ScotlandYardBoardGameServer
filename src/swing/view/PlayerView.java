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
 * A view to display the players Tickets.
 */

public class PlayerView extends JPanel implements ActionListener{

    private Map<Colour, PlayerTile>tiles;
    private PlayerTile currentTile;
    private FileAccess fileAccess;
    private JButton hintButton;
    private ActionListener listener = null;
    private JPanel viewHolder;

    /**
     * Constructs a new PlayerView to display all players Tickets.
     *
     * @param fileAccess the FileAccess object that contains the images.
     */
    public PlayerView(FileAccess fileAccess) {
        this.tiles = new HashMap<Colour, PlayerTile>();
        this.fileAccess = fileAccess;
        setPreferredSize(new Dimension(300, 749));
        setBackground(new Color(255, 255, 255, 240));
        setLayout(new BorderLayout());

        hintButton = new JButton("Hint");
        hintButton.setBackground(new Color(251, 68, 60, 255));
        hintButton.setContentAreaFilled(false);
        hintButton.setOpaque(true);
        hintButton.setBorderPainted(false);
        hintButton.setContentAreaFilled(false);
        hintButton.setForeground(Color.WHITE);
        hintButton.setFont(new Font("Helvetica Neue", Font.BOLD, 18));
        hintButton.setPreferredSize(new Dimension(120, 40));
        hintButton.addActionListener(this);
        add(hintButton, BorderLayout.NORTH);

        viewHolder = new JPanel(new GridLayout(6, 1));
        viewHolder.setOpaque(false);
        viewHolder.setBorder(BorderFactory.createEmptyBorder(15, 23, 15, 22));
        add(viewHolder, BorderLayout.CENTER);

    }

    /**
     * Action listener method for the hint button
     *
     * @param e the action produced by the hint button.
     */
    public void actionPerformed(ActionEvent e) {
        if (listener != null) {
            listener.actionPerformed(new ActionEvent(true, 0, "hint"));
        }
    }

    /**
     * Sets the new current player.
     *
     * @param colour the color of the new current player.
     */
    public void setCurrentPlayer(Colour colour) {
        PlayerTile tile = tiles.get(colour);
        currentTile.setCurrent(false);
        tile.setCurrent(true);
        currentTile = tile;
    }

    /**
     * Updates the tickets for a specified player.
     *
     * @param colour the player to be updated.
     * @param ticket the Ticket whose number is to be updated.
     * @param ticketNo the new number of Tickets for the player.
     */
    public void update(Colour colour, Ticket ticket, Integer ticketNo) {
        PlayerTile tile = tiles.get(colour);
        tile.update(ticket, ticketNo);
    }

    /**
     * Initialises the displayed players with the players in the List.
     *
     * @param players the List of players to be added to the view.
     * @param listener the listener to receive events from the view.
     */
    public void initialise(List<PlayerData> players, ActionListener listener) {
        for (PlayerData player : players) {
            PlayerTile tile = new PlayerTile(player);
            if (player.getColour().equals(Colour.Black)) currentTile =  tile;
            tiles.put(player.getColour(), tile);
            viewHolder.add(tile);
            setActionListener(listener);
            validate();
        }
    }

    /**
     * Forwards the specified ActionListener to each of the
     * tiles.
     * @param listener the listener to be added to the views.
     */
    public void setActionListener(ActionListener listener) {
        this.listener = listener;
        for (Map.Entry<Colour, PlayerTile> entry : tiles.entrySet()) {
            PlayerTile tile = entry.getValue();
            tile.setActionListener(listener);
        }
    }

     // A class for displaying a players tickets and current
     // player indicator.
     private class PlayerTile extends JLabel {

        private PlayerData player;
        private MouseListener mListener;
        private Map<Colour, BufferedImage> playerImages;
        private Map<Ticket, TicketView> ticketViews;
        private boolean current;

        /**
         * Constructs a new PlayerTile that displays the player's Tickets.
         * A MouseListener listens for clicks on the Tickets.
         *
         * @param listener the MouseListener to be attached to the view.
         * @param player the player whose information is to be displayed.
         */
        public PlayerTile(PlayerData player) {
            playerImages = fileAccess.getPlayers();
            setPreferredSize(new Dimension(255, 100));
            setLayout(null);
            setOpaque(false);
            this.player = player;
            this.ticketViews = new HashMap<Ticket, TicketView>();

            if (player.getColour().equals(Colour.Black)) {
                drawMrX();
            } else {
                drawDetective(player.getColour());
            }
        }

        /**
         * Sets whether this player tile is current or not.
         *
         * @param isCurrent boolean value representing whether
         * the player tile should be current.
         */
        public void setCurrent(boolean isCurrent) {
            current = isCurrent;
            repaint();
        }

        /**
         * Forwards the specified ActionListener to each of the
         * ticket views.
         *
         * @param listener the listener to be added to the views.
         */
        public void setActionListener(ActionListener listener) {
            for (Map.Entry<Ticket, TicketView> entry : ticketViews.entrySet()) {
                TicketView view = entry.getValue();
                view.setActionListener(listener);
            }
        }

        /**
         * Updates a ticketview with a new value
         *
         * @param ticket the ticket that represents the ticketview
         * @param value the newm value to be updated
         */
        public void update(Ticket ticket, int value) {
            TicketView view = ticketViews.get(ticket);
            if (view != null) {
                view.setValue(value);
            }
        }

        /**
         * Draws the view in the JLabel.
         *
         * @param g0 the Graphics object to draw to.
         */
        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            BufferedImage background = playerImages.get(player.getColour());
            g.drawImage(background, null, 0, 0);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.white);
            if (current) {
                g.fillOval(235, 10, 10, 10);
            }
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            if (player.getColour().equals(Colour.Black)) {
                g.drawString("Mr X", 16, 21);
            } else {
                g.drawString(player.getColour().toString() + " Detective", 16, 21);
            }
        }

        // Draws a detective's Tickets.
        // @param colour the colour of the detective to be drawn.
        private void drawDetective(Colour colour) {
            Map<Ticket, BufferedImage> ticketImages = fileAccess.getTickets();
            int taxiNo = player.getTickets().get(Ticket.Taxi);
            int busNo = player.getTickets().get(Ticket.Bus);
            int trainNo = player.getTickets().get(Ticket.Underground);

            TicketView taxiTicket = new TicketView(ticketImages.get(Ticket.Taxi), Ticket.Taxi, colour);
            taxiTicket.setBounds(16, 31, 65, 55);
            taxiTicket.setValue(taxiNo);
            add(taxiTicket);
            ticketViews.put(Ticket.Taxi, taxiTicket);

            TicketView busTicket = new TicketView(ticketImages.get(Ticket.Bus), Ticket.Bus, colour);
            busTicket.setBounds(96, 31, 65, 55);
            busTicket.setValue(busNo);
            add(busTicket);
            ticketViews.put(Ticket.Bus, busTicket);

            TicketView trainTicket = new TicketView(ticketImages.get(Ticket.Underground), Ticket.Underground, colour);
            trainTicket.setBounds(176, 31, 65, 55);
            trainTicket.setValue(trainNo);
            add(trainTicket);
            ticketViews.put(Ticket.Underground, trainTicket);
        }

        // Draws Mr X's Tickets.
        private void drawMrX() {
            Map<Ticket, BufferedImage> ticketImages = fileAccess.getTickets();
            int taxiNo = player.getTickets().get(Ticket.Taxi);
            int busNo = player.getTickets().get(Ticket.Bus);
            int trainNo = player.getTickets().get(Ticket.Underground);
            int secretNo = player.getTickets().get(Ticket.Secret);
            int doubleNo = player.getTickets().get(Ticket.Double);

            TicketView taxiTicket = new TicketView(ticketImages.get(Ticket.Taxi), Ticket.Taxi, Colour.Black);
            taxiTicket.setBounds(16, 31, 65, 55);
            taxiTicket.setValue(taxiNo);
            add(taxiTicket);
            ticketViews.put(Ticket.Taxi, taxiTicket);

            TicketView busTicket = new TicketView(ticketImages.get(Ticket.Bus), Ticket.Bus, Colour.Black);
            busTicket.setBounds(56, 31, 65, 55);
            busTicket.setValue(busNo);
            add(busTicket);
            ticketViews.put(Ticket.Bus, busTicket);

            TicketView trainTicket = new TicketView(ticketImages.get(Ticket.Underground), Ticket.Underground, Colour.Black);
            trainTicket.setBounds(96, 31, 65, 55);
            trainTicket.setValue(trainNo);
            add(trainTicket);
            ticketViews.put(Ticket.Underground, trainTicket);

            TicketView doubleTicket = new TicketView(ticketImages.get(Ticket.Secret), Ticket.Secret, Colour.Black);
            doubleTicket.setBounds(136, 31, 65, 55);
            doubleTicket.setValue(secretNo);
            add(doubleTicket);
            ticketViews.put(Ticket.Secret, doubleTicket);

            TicketView secretTicket = new TicketView(ticketImages.get(Ticket.Double), Ticket.Double, Colour.Black);
            secretTicket.setBounds(176, 31, 65, 55);
            secretTicket.setValue(doubleNo);
            add(secretTicket);
            ticketViews.put(Ticket.Double, secretTicket);

        }

        // A class for displaying a ticket and number badge;
        private class TicketView extends JLabel implements MouseListener {

            private BufferedImage image;
            private TicketBadge badge;
            protected ActionListener aListener = null;
            private Ticket ticket;
            private Colour colour;

            /**
             * Constructs a new TicketView object.
             *
             * @param image the background image of the view.
             * @param ticket the Ticket that this view represents.
             * @param colour the Colour of the player to whom this
             * Ticket belongs.
             */
            public TicketView(BufferedImage image, Ticket ticket, Colour colour) {
                this.image = image;
                this.addMouseListener(this);
                this.ticket = ticket;
                this.colour = colour;

                badge = new TicketBadge();
                badge.setBounds(35, 0, 30, 30);
                add(badge);
            }

            /**
             * Draws the view.
             *
             * @param g0 the Graphics object to draw to.
             */
            public void paintComponent(Graphics g0) {
                super.paintComponent(g0);
                Graphics2D g = (Graphics2D) g0;
                g.setStroke(new BasicStroke(4));
                g.drawImage(image, null, 0, 15);

                g.setColor(Color.BLACK);
            }

            /**
             * Sets the badge value.
             *
             * @param value the value to be displayed
             */
            public void setValue(int value) {
                badge.setValue(value);
            }

            /**
             * Sets the specified ActionListener to recieve click events.
             *
             * @param listener the listener to be added.
             */
            public void setActionListener(ActionListener listener) {
                aListener = listener;
            }

            /**
             * Returns the Ticket associated with this view.
             *
             * @return the Ticket associated with this view.
             */
            public Ticket getTicket(){
                return ticket;
            }

            /**
             * Returns the Colour of the player to whom this view
             * belongs.
             *
             * @return the Colour of the player to whom this view
             * belongs.
             */
            public Colour getColor(){
                return colour;
            }

            /**
             * Is called when a Move is clicked on. Sends an ActionEvent to the
             * added ActionListener.
             *
             * @param e the MouseEvent containing the JLabel of the Move clicked on.
             */
            public void mouseReleased(MouseEvent e) {
                if (aListener != null) {
                    aListener.actionPerformed(new ActionEvent(ticket, 0, "ticket"));
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

            // Class for displaying the number of tickets in
            // a badge.
            private class TicketBadge extends JLabel {

                private BufferedImage badgeImage;
                private JLabel label;

                /**
                 * Constructs a new TicketBadge object.
                 */
                public TicketBadge() {
                    badgeImage = fileAccess.getCircle();
                    label = new JLabel("0", SwingConstants.CENTER);
                    label.setForeground(Color.white);
                    label.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
                    label.setBounds(0, 0, 30, 30);
                    add(label);
                }

                /**
                 * Sets the number in the view.
                 *
                 * @param value the number for the view to display.
                 */
                public void setValue(int value) {
                    label.setText("" + value);
                }

                /**
                 * Draws the view.
                 *
                 * @param g0 the Graphics object to draw to.
                 */
                public void paintComponent(Graphics g0) {
                    super.paintComponent(g0);
                    Graphics2D g = (Graphics2D) g0;
                    g.drawImage(badgeImage, null, 0, 0);

                }

            }

        }

    }

}
