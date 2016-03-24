package swing.view;

import swing.application.*;
import scotlandyard.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

/**
 * A view to allow players to set up a new game or load an existing one.
 */

public class SetUpView extends JPanel {

    private FileAccess fileAccess;
    private JButton replay;
    private JButton load;
    private JButton start;
    private JList<String> loadList;
    private JTextField gameNameField;
    private JComboBox<Integer> playerDropDown;
    private List<String> savedGames;
    private BufferedImage background;
    private BufferedImage backgroundImage;

    /**
     * Constructs a new SetUpView object.
     *
     * @param fileAccess the FileAccess object to get the images.
     */
    public SetUpView(FileAccess fileAccess) {
        this.fileAccess = fileAccess;
        background = fileAccess.getSetupBackground();

        setPreferredSize(new Dimension(1272, 809));
        setBackground(new Color(51,135,253));
        setLayout(new GridBagLayout());

        LoadPanel loadPanel = new LoadPanel();
        loadPanel.setPreferredSize(new Dimension(400, 400));
        add(loadPanel);

        NewPanel newPanel = new NewPanel();
        newPanel.setPreferredSize(new Dimension(400, 400));
        add(newPanel);

        backgroundImage = fileAccess.getSetupImage(new Dimension(1272, 809));

    }

    /**
     * Draws the background image of the view.
     *
     * @param g0 the Graphics object to draw to.
     */
    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(backgroundImage, 0, 0, null);
        Dimension panelSize = getSize();
        g.drawImage(background, (panelSize.width / 2) - 415, (panelSize.height / 2) - 215, null);
    }

    /**
     * Returns the file path of the selected item in the load list.
     *
     * @return the file path of the selected item in the load list.
     */
    public String selectedFilePath() {
        int index = loadList.getSelectedIndex();
        if (index >= 0) return savedGames.get(index);
        return null;
    }

    /**
     * Returns the name of the selected item in the load list.
     *
     * @return the name of the selected item in the load list.
     */
    public String selectedGameName() {
        String gameName = gameNameField.getText();
        if (gameName.length() == 0) return null;
        return gameName;
    }

    /**
     * Returns the number of players selected by the user.
     *
     * @return the number of players selected by the user.
     */
    public Integer selectedPlayers() {
        return (Integer)playerDropDown.getSelectedItem();
    }

    /**
     * Sets the specified ActionListener to receive click events.
     *
     * @param listener the listener to be added.
     */
    public void setActionListener(ActionListener listener) {
        load.addActionListener(listener);
        start.addActionListener(listener);
    }

    /**
     * Refreshes the list of saved games and clears the
     * text field.
     */
    public void refreshSaves() {
        gameNameField.setText("");

        savedGames = fileAccess.savedGames();
        loadList.setListData(savedGames.toArray(new String[savedGames.size()]));
    }

    // A view to draw the load list and button.
    private class LoadPanel extends JPanel {

        /**
         * Constructs a new LoadPanel object.
         */
        public LoadPanel() {
            setBorder(new EmptyBorder(35, 40, 40, 40));
            setOpaque(false);

            savedGames = fileAccess.savedGames();

            loadList = Formatter.list(savedGames);
            JScrollPane scrollPane = new JScrollPane(loadList);
            scrollPane.setPreferredSize(new Dimension(320, 260));
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220, 255), 1));
            add(scrollPane);

            Component spacer = Box.createRigidArea(new Dimension(400,10));
            add(spacer);

            load = Formatter.button("Load");
            load.setActionCommand("loadGame");
            add(load);
        }

    }

    // A view to draw the new game panel.
    private class NewPanel extends JPanel {

        /**
         * Constructs a new NewPanel object.
         */
        public NewPanel() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(35, 40, 40, 40));
            setOpaque(false);

            JPanel box = new JPanel();
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setOpaque(false);

            JComponent nameLabel = new JLabel("Game Name");
            nameLabel = styleComponent(nameLabel);
            box.add(nameLabel);

            gameNameField = new JTextField(10);
            gameNameField = (JTextField)styleComponent(gameNameField);
            gameNameField.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220, 255), 1));
            box.add(gameNameField);

            Component spacer = Box.createRigidArea(new Dimension(0, 10));
            box.add(spacer);

            JComponent playerLabel = new JLabel("Players");
            playerLabel = styleComponent(playerLabel);
            box.add(playerLabel);

            Integer[] numPlayers = {2,3,4,5,6};
            playerDropDown  = new JComboBox<Integer>(numPlayers);
            playerDropDown.setLightWeightPopupEnabled(true);
            playerDropDown.setAlignmentX(Component.LEFT_ALIGNMENT);
            playerDropDown.setMaximumSize(new Dimension(80,30));
            box.add(playerDropDown);

            add(box, BorderLayout.CENTER);

            start = Formatter.button("Start");
            start.setActionCommand("startGame");
            start.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(start, BorderLayout.SOUTH);
        }

        // Returns the styled version of the element.
        // @param component the element to be styled.
        // @return the styled version of the element.
        private JComponent styleComponent(JComponent component) {
            component.setFont(new Font("Helvetica Neue", 0, 18));
            component.setMaximumSize(new Dimension(200,30));
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            component.setBorder(new EmptyBorder(0, 0, 10, 0));
            return component;
        }

    }

}
