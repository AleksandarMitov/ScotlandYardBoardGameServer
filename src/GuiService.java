import swing.view.*;
import swing.application.*;
import scotlandyard.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

/**
 * A class to start the application.
 *
 * To start the GUI, run the following:
 * <pre>
 *   ant gui
 * </pre>
 */

public class GuiService implements WindowListener, ActionListener, Runnable {

    private ScotlandYardGame game;
    private GameView gameView;
    private SetUpView setUpView;
    private FileAccess fileAccess;
    private ThreadCommunicator threadCom;
    private JFrame window;

    /**
     * Is the entry point for the game.
     *
     * @param args the arguments from the command line.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }
        GuiService application = new GuiService();

        SwingUtilities.invokeLater(application);
    }

    /**
     * Starts the window and adds the SetUpView.
     */
    public void run() {
        fileAccess = new FileAccess();
        window = new JFrame();
        window.setMinimumSize(new Dimension(1000, 835));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setOpaque(false);
        setUpView = new SetUpView(fileAccess);
        setUpView.setActionListener(this);
        window.addComponentListener(gameView);
        window.add(setUpView);
        setUpView.setVisible(true);
        window.pack();
        window.setTitle("Scotland Yard");
        window.setLocationByPlatform(true);
        window.addWindowListener(this);
        window.setVisible(true);
    }

    /**
     * Called when the window is being closed. This then tells the
     * game to save it's current state.
     *
     * @param e the WindowEvent sent when the window closes.
     */
    public void windowClosing(WindowEvent e) {
        if (gameView != null) {
            game.saveGame();
        }
    }

    /**
     * Called when the buttons in the SetUpView are clicked by the user.
     *
     * @param e the ActionEvent containing information about which button
     * has been clicked.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("startGame")) {
            if(validGameName(setUpView.selectedGameName())) {
                setUpView.setVisible(false);
                beginGame();
                newGame();
            } else {
                JOptionPane.showMessageDialog(null, "Game name must be at least one character long and not contain any of #, /, \\. \nPlease choose another", "Invalid Name", JOptionPane.WARNING_MESSAGE, fileAccess.getWarningIcon());
            }
        } else if (e.getActionCommand().equals("loadGame")) {
            if (setUpView.selectedFilePath() != null) {
                setUpView.setVisible(false);
                beginGame();
                loadGame();
            }
        }
    }

    // Returns true if  the given string is a valid game
    // name.
    // @param name the string to be checked.
    private boolean validGameName(String name) {
        if (name == null) return false;
        boolean containsHash = name.indexOf("#") != -1;
        boolean containsBackslash = name.indexOf("/") != -1;
        boolean containsForwardSlash = name.indexOf("\\") != -1;
        return (!containsHash) && (!containsBackslash) && (!containsForwardSlash);
    }

    // Starts a new ScotlandYardGame.
    // Starts the game and the views in new threads.
    private void beginGame() {
        threadCom = new ThreadCommunicator();

        gameView = new GameView(fileAccess, threadCom);
        window.addComponentListener(gameView);
        window.add(gameView);
        window.pack();
        new Thread(new QueueHandler()).start();
    }

    // Starts a new game in a new Thread.
    private void newGame() {
        int playerNo = setUpView.selectedPlayers();
        String gameName = setUpView.selectedGameName();
        game = new ScotlandYardGame(playerNo, gameName, "graph.txt", threadCom);
        new Thread(game).start();
    }

    // Loads a previously played game in a new Thread.
    private void loadGame() {
        game = new ScotlandYardGame(setUpView.selectedFilePath(), threadCom);
        new Thread(game).start();
    }

    // Removes the gameview and shows the setup view
    private void endGame() {
        window.remove(gameView);
        setUpView.setVisible(true);
        setUpView.refreshSaves();
        threadCom.clearEvents();
        threadCom.clearUpdates();
    }

    private class QueueHandler implements Runnable {

        /**
         * Takes items off the queue and updates the views appropriately.
         */
        public void run() {
            while (true) {
                try {
                    ThreadCommunicator.Packet packet = threadCom.takeUpdate();
                    decodeUpdate(packet.getId(), packet.getObject());
                } catch (Exception e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
            }
        }

        // Decodes the id of the update and acts accordingly.
        // @param id the id of the update.
        // @param object the object associated with the id.
        private void decodeUpdate(String id, Object object) {
            if (id.equals("init_views")) {
                @SuppressWarnings("unchecked")
                List<PlayerData> players = (List<PlayerData>) object;
                for (PlayerData player : players);
                gameView.initialise(players);
            } else if (id.equals("update_board")) {
                Move move = (Move) object;
                gameView.updateBoard(move);
            } else if (id.equals("update_moves")) {
                Move move = (Move) object;
                gameView.updateMoves(move);
            } else if (id.equals("update_players")) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) object;
                Colour colour = (Colour) list.get(0);
                Ticket ticket = (Ticket) list.get(1);
                Integer ticketNo = (Integer) list.get(2);
                gameView.updatePlayers(colour, ticket, ticketNo);
            } else if (id.equals("update_current_player")) {
                Colour player = (Colour) object;
                gameView.setCurrentPlayer(player);
            } else if (id.equals("send_notification")) {
                String message = (String) object;
                gameView.setNotification(message);
            } else if (id.equals("show_location")) {
                JLabel label = (JLabel) object;
                gameView.showLocation(label);
            }  else if (id.equals("zoom_in")) {
                Integer location = (Integer) object;
                gameView.zoomToNode(location);
            } else if (id.equals("zoom_out")) {
                gameView.zoomOut();
            } else if (id.equals("reset_timer")) {
                gameView.resetTimer();
            } else if (id.equals("stop_timer")) {
                gameView.stopTimer();
            } else if (id.equals("show_route")) {
                @SuppressWarnings("unchecked")
                List<Integer> hints = (List<Integer>) object;
                for (Integer hint : hints);
                gameView.setRouteHint(hints);
            } else if (id.equals("select_ticket")) {
                @SuppressWarnings("unchecked")
                List<Integer> hints = (List<Integer>) object;
                for (Integer hint : hints);
                gameView.setRouteHint(hints);
            } else if (id.equals("end_game")) {
                endGame();
            } else if (id.equals("highlight_node")) {
                Integer location = (Integer) object;
                gameView.highlightNode(location);
            } else if (id.equals("valid_moves")) {
                @SuppressWarnings("unchecked")
                List<Move> moves = (List<Move>) object;
                for (Move move : moves);
                gameView.updateValidMoves(moves);
            }

        }

    }

    /**
     * Unused method from the WindowListener interface.
     *
     * @param e the WindowEvent from the listener.
     */
    public void windowOpened(WindowEvent e) {}
    /**
     * Unused method from the WindowListener interface.
     *
     * @param e the WindowEvent from the listener.
     */
    public void windowClosed(WindowEvent e) {}
    /**
     * Unused method from the WindowListener interface.
     *
     * @param e the WindowEvent from the listener.
     */
    public void windowIconified(WindowEvent e) {}
    /**
     * Unused method from the WindowListener interface.
     *
     * @param e the WindowEvent from the listener.
     */
    public void windowDeiconified(WindowEvent e) {}
    /**
     * Unused method from the WindowListener interface.
     *
     * @param e the WindowEvent from the listener.
     */
    public void windowActivated(WindowEvent e) {}
    /**
     * Unused method from the WindowListener interface.
     *
     * @param e the WindowEvent from the listener.
     */
    public void windowDeactivated(WindowEvent e) {}

}
