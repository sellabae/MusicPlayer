import javazoom.jlgui.basicplayer.BasicPlayer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This controller is a Supervising Controller or messenger
 * between Model(data,entity) and View(interface,gui)
 * based on the MVC design pattern.
 * It prevents direct interactions between data and interfaces.
 */
public class MainController {

    //View
    private MusicPlayerGUI playerView;

    //Models
    private SongLibrary library;
    private DatabaseHandler db;

    //Other Controllers
    private BasicPlayer player;
    //private PlayerController playerController;

    /**
     * Construct a main controller and initialize all modules
     */
    public MainController(){
        playerView = new MusicPlayerGUI("MainController Testing");
        library = new SongLibrary();
        db = new DatabaseHandler();
        player = new BasicPlayer();

        playerView.updateTableView(db.getSongLibrary());

        playerView.setVisible(true);

        addListeners();
        addListenersToTable();

    }

    //TODO add inner classes of ActionListener for Table, Buttons, Slider

    /**
     * Add Listeners to buttons, volume slider
     */
    public void addListeners(){
        //Buttons
        playerView.startSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("PLAY button is pressed.");
            }
        });
        playerView.stopSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("STOP button is pressed.");
            }
        });
        playerView.prevSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("PREVIOUS button is pressed.");
            }
        });
        playerView.nextSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("NEXT button is pressed.");
            }
        });

        //Slider
        playerView.scrollVolume.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                System.out.println("Slider tick: " + source.getValue());

                if (!source.getValueIsAdjusting()) {
                    int volume = source.getValue();
                    System.out.println("Volume: " + volume);
                }

            }
        });
    }

    /**
     * Add Listeners for table related actions
     */
    public void addListenersToTable(){
        //Table row selected
        playerView.songTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            JTable table = playerView.songTable;

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()) {
                    int selectedRow = playerView.songTable.getSelectedRow();
                    String title = table.getValueAt(selectedRow, 1).toString();
                    String artist = table.getValueAt(selectedRow, 2).toString();

                    System.out.println("ROW " + selectedRow + " '" + title + " - " + artist + "' is selected.");
                }
            }
        });
    }
}
