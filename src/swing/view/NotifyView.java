package swing.view;

import scotlandyard.*;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;

/**
 * A view to display a messages to the players.
 */

public class NotifyView extends JPanel implements ActionListener {

    private BufferedImage background;
    private boolean visible = false;
    private Timer timer;
    private JLabel label;
    private Queue<String> queue;

    private final int kNotifyTime = 3000;

    /**
     * Constructs a new NotifyView to display notifications to the user.
     *
     * @param background the background image of the notification panel.
     */
    public NotifyView(BufferedImage background) {
        setPreferredSize(new Dimension(500, 100));
        setOpaque(false);
        setBackground(Color.blue);
        setLayout(new GridBagLayout());
        this.background = background;
        queue = new ArrayDeque<String>();
        timer = new Timer(kNotifyTime, this);
        timer.setRepeats(false);
        label = new JLabel("");
        label.setFont(new Font("SansSerif", Font.PLAIN, 18));
        add(label);
    }

    /**
     * Draws the view.
     *
     * @param g0 the Graphics object to draw to.
     */
    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        if (visible) g.drawImage(background, 0, 0, 500, 100, null);
    }

    /**
     * Adds a message to the queue to be displayed to the user.
     *
     * @param text the message to be shown to the user.
     */
    public void notify(String text) {
        queue.add(text);
        pollQueue();
    }

    // Polls the queue and updates the message if one is not currently
    // displayed or the timer for the last message has gone off.
    private void pollQueue() {
        String head = queue.peek();
        if (head != null && !visible) setText(queue.poll());
    }

    // Sets the message in the JLabel.
    // @param text the message to be shown in the JLabel.
    private void setText(String text) {
        visible = true;
        label.setText(text);
        timer.stop();
        timer.start();
        repaint();
    }

    /**
     * Is called when the timer runs out. Timer is for
     * 5 seconds and determines how long the messages are visible for.
     *
     * @param e the ActionEvent from the timer.
     */
    public void actionPerformed(ActionEvent e) {
        //Timer has gone off
        visible = false;
        label.setText("");
        timer.stop();
        repaint();
        pollQueue();
    }

}
