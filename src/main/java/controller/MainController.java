package controller;

import javazoom.jlgui.basicplayer.BasicPlayer;
import model.Playlist;
import model.Song;
import model.SongLibrary;
import view.ColorTheme;
import view.MusicPlayerGUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

/**
 * This controller is a Supervising Controller or messenger
 * between Model(data,entity) and View(interface,gui)
 * based on the MVC design pattern.
 * It makes data and interfaces are independent from each other.
 * ActionListeners are here.
 */
public class MainController {

    //View
    private MusicPlayerGUI playerView;
    //Models
    private SongLibrary library;
    //Other Controllers
    private PlayerController playerControl;

    private Song selectedSong;  //different from currentSong

    /**
     * Construct a main controller and initialize all modules
     */
    public MainController() {
        //assign modules
        playerView = new MusicPlayerGUI("MyTunes1.0");
        library = new SongLibrary(); //should always be up-to-date with db
        Playlist.setLibrary(library);

        playerControl = new PlayerController(library, playerView);
        selectedSong = null;

        //setup presentation
        playerView.updateTableView(library);
        playerView.setVisible(true);

        //Add listeners to buttons and slider
        playerView.addPlayerControlButtonListener(new PlayerControlButtonListener());
        playerView.addVolumeSliderListener(new VolumeSliderListener());

        //Add listeners to standard menu bar / popup menu items
        playerView.addMenuItemListener(new MenuItemListener());

        //Add listener to table
        playerView.addSelectionListenerForTable(new SelectionListenerForTable());
        playerView.addMouseListenerForTable(new MouseListenerForTable());

        //Add drop target to scroll pane
        playerView.addDragDropToScrollPane(new DragDropToScrollPane());

        //Extra feature, add listener to extra menus on menu bar
        playerView.addOptionalMenuItemListener(new OptionalMenuItemListener());

    }

    //Listeners

    /**
     * PlayerControlButtonListener class implements
     * the actions of Play/Stop/Prev/Next buttons
     * by the name of the components.
     */
    class PlayerControlButtonListener implements ActionListener {
        String btnName;
        int playerStatus;

        @Override
        public void actionPerformed(ActionEvent e) {
            // Get the name of event source component
            btnName = ((JButton)e.getSource()).getName();
            playerStatus = playerControl.getPlayerStatus();

            if (btnName.equals("play")) {
                //PLAY button actions
                System.out.println("[BUTTON] Play/Pause/Resume button is pressed.");

                switch (playerStatus) {
                    //Pause Action
                    case BasicPlayer.PLAYING :
                        playerControl.pauseSong();
                        break;
                    //Resume Action
                    case BasicPlayer.PAUSED :
                        playerControl.resumeSong();
                        break;
                    //Play Action
                    case BasicPlayer.STOPPED :
                    default:
                        if(playerView.isAnyRowSelected()) {
                            playerControl.setCurrentSong(selectedSong);
                            playerControl.playSong();
                        }else{
                            System.out.println("nothing selected, playing the first song in the library..");
                            playerControl.playSong();
                        }
                        break;
                }
            } else if (btnName.equals("stop")) {
                //STOP button action
                System.out.println("[BUTTON] STOP button is pressed.");
                playerControl.stopSong();
            }
            else if (btnName.equals("prev")) {
                //PREV button action
                System.out.println("[BUTTON] PREV button is pressed.");
                playerControl.playPrevSong();
            }
            else if (btnName.equals("next")) {
                //NEXT button action
                System.out.println("[BUTTON] NEXT button is pressed.");
                playerControl.playNextSong();
            }
            else {
                System.out.println("none of play/stop/prev/next buttons");
            }
        }
    }

    /**
     * Slider value increases when slid to the right, decreases when slid to the left.
     */
    class VolumeSliderListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            double sliderVal = ((JSlider) e.getSource()).getValue();
            playerControl.setVolume(sliderVal);
        }
    }

    /**
     * MenuItemListener class implements
     * the actions of menu items in menu bar and popup menu
     * by the name of the components.
     * "open"   Open/Play A Song not in the library
     * "add"    Add Song To Library
     * "delete" Delete Song From Library
     * "about"  About
     * "exit"   Exit
     */
    class MenuItemListener implements ActionListener {
        String menuName;
        @Override
        public void actionPerformed(ActionEvent e) {
            // Get the name of event source component
            menuName = ((JMenuItem)e.getSource()).getName();

            if (menuName.equals("open")) {
                //Open Song menu actions
                System.out.println("[Menu] Open/Play Song not in library is pressed.");
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if (chooser.showOpenDialog(playerView) == JFileChooser.APPROVE_OPTION) {
                    String selectedPath = chooser.getSelectedFile().getAbsolutePath();
                    Song newSong = new Song(selectedPath);
                    if (newSong.getPath() == null) {
                        System.out.println("[FileChooser] Not a valid file.\n");
                        JOptionPane.showMessageDialog(null, "This file is not a valid " +
                                "mp3 file.");
                    } else {
                        playerControl.playSong(newSong);
                    }
                }

            } else if (menuName.equals("add")) {
                //Add A Song To Library menu actions
                System.out.println("[Menu] Add Song is pressed.");
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                if (chooser.showOpenDialog(playerView) == JFileChooser.APPROVE_OPTION) {
                    String selectedPath = chooser.getSelectedFile().getAbsolutePath();
                    Song newSong = new Song(selectedPath);
                    //Check if the chosen file is valid input
                    if (newSong.getPath() == null) {
                        System.out.println("[FileChooser] Not a valid file.\n");
                        JOptionPane.showMessageDialog(null, "This file is not a valid " +
                                "mp3 file.");
                    } else {
                        library.addSong(newSong);
                        playerView.updateTableView(library);
                        playerControl.updateLibrary(library);
                    }
                }

            } else if (menuName.equals("delete")) {
                //Delete Song From Library menu actions
                System.out.println("[Menu] Delete Song is pressed.");

                int selectedRow = playerView.getSongTable().getSelectedRow();
                boolean isRowInbound = (selectedRow >= 0) && (selectedRow < library.size());

                if ( isRowInbound ) {
                    Song selectedSong = library.get(selectedRow);
                    System.out.println("row:"+selectedRow+" is selected to delete.");
                    library.deleteSong(selectedSong);
                    playerView.updateTableView(library);
                    playerControl.updateLibrary(library);
                } else {
                    System.out.println("row:"+selectedRow+", nothing selected to delete.");
                }

            } else if (menuName.equals("about")) {
                //About menu actions
                System.out.println("[Menu] About is pressed.");
                String title = "About";
                String appName = "MyTunes1.0";
                String teamInfo = "[CECS543 Team6]\nSella Bae\nBrett Rexius\nJohanna Thiemich";
                String date = "3/14/2019";
                String msg = appName+"\n"+date+"\n\n"+teamInfo;
                JOptionPane.showMessageDialog(playerView, msg, title, JOptionPane.PLAIN_MESSAGE);

            } else if (menuName.equals("exit")) {
                //Exit menu actions
                System.exit(0);

            } else {
                System.out.println("none of the menu item action performed.");
            }

        }
    }

    /**
     * OptionalMenuItemListener class implements
     * the action of optional menu items in menu bar
     * by the name and state of the components.
     * "darkTheme"  Set or unset the dark theme to the app
     * "songInfo"   Show or hide the current song info panel at the bottom
     */
    class OptionalMenuItemListener implements ActionListener {
        JCheckBoxMenuItem checkMenu;
        String menuName;

        @Override
        public void actionPerformed(ActionEvent e) {
            // Get the name of event source component
            checkMenu = (JCheckBoxMenuItem) e.getSource();
            menuName = checkMenu.getName();

            if (menuName.equals("darkTheme")) {
                //[Dark Theme] menu actions
                if(checkMenu.getState()) {
                    System.out.println("[ViewMenu] Set Dark Theme.");
                    playerView.setColorTheme(ColorTheme.dark);
                }else{
                    System.out.println("[ViewMenu] Unset Dark Theme.");
                    playerView.setColorTheme(ColorTheme.white);
                }

            } else if (menuName.equals("songInfo")) {
                //[Current Song Info] menu actions
                if(checkMenu.getState()) {
                    System.out.println("[ViewMenu] Show Current Song Info.");
                    playerView.getControlView().showSongInfoPanel(true);
                } else {
                    System.out.println("[ViewMenu] Hide Current Song Info.");
                    playerView.getControlView().showSongInfoPanel(false);
                }
            }
        }
    }

    /**
     * SelectionListenerForTable detects
     * any row selection change of the table
     * either by mouse or keyboard arrows
     */
    class SelectionListenerForTable implements ListSelectionListener {
        private JTable table = playerView.getSongTable();
        private int row;
        private boolean isRowInbound;

        //Table row selected
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                row = table.getSelectedRow();
                isRowInbound = row >= 0 && row < table.getRowCount();

                if (isRowInbound) {
                    selectedSong = library.get(row);
                    System.out.print("[Table] selectedRow:"+row);
                    System.out.println(", [" + selectedSong.getTitleAndArtist() + "]");
                }
            }
        }
    }

    /**
     * MouseListenerForTable covers:
     * 1. popup trigger for right-click inside of table
     * 2. popup trigger for right-click outside of table
     * 3. clear selections for left-click outside of table
     * 4. double-click to play the song
     */
    class MouseListenerForTable extends MouseAdapter {
        private JTable source;
        private int row = 0, col = 0, rowCount = 0;
        private boolean isRowInbound;

        @Override
        public void mousePressed(MouseEvent e) {
            // Get the mouse position in the table
            source = (JTable)e.getSource();
            rowCount = source.getRowCount();
            row = source.rowAtPoint( e.getPoint() );
            col = source.columnAtPoint( e.getPoint() );
            isRowInbound = (row >= 0) && (row < rowCount);
            // Right-click Popup Trigger (for MacOS)
            if (e.isPopupTrigger() && library.size() > 0) {
                if ( isRowInbound ) {   //right click in table
                    System.out.println("right clicked in table. row:"+row);
                    source.changeSelection(row, col, false, false);
                    playerView.getPopUpMenu().show(e.getComponent(), e.getX(), e.getY());
                } else {                //right click out of table
                    System.out.println("right clicked outside of the table");
                    playerView.getPopUpMenuInBlankspace().show(e.getComponent(), e.getX(), e.getY());
                }
            }
            // Left-click outside of table to clear selection
            if ( ! isRowInbound && ! e.isPopupTrigger()) {
                playerView.getSongTable().clearSelection();
                System.out.println("Cleared row selections.");
            }
        }
        @Override
        public void mouseReleased(MouseEvent e)
        {
            // Right-click Popup Trigger (for Windows)
            if (e.isPopupTrigger()) {
                if ( isRowInbound ) {   //right click in table
                    System.out.println("right clicked inside of the table");
                    source.changeSelection(row, col, false, false);
                    playerView.getPopUpMenu().show(e.getComponent(), e.getX(), e.getY());
                } else {                //right click out of table
                    System.out.println("right clicked outside of the table");
                    playerView.getPopUpMenuInBlankspace().show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
        @Override
        public void mouseClicked(MouseEvent e) {
            // Double-click on a song to play
            if ( isRowInbound ) {
                if ( (e.getClickCount() == 2) && !e.isConsumed() && !e.isPopupTrigger()) {
                    System.out.println("double clicked");
                    Song selectedSong = library.get(row);
                    playerControl.playSong(selectedSong);
                }
            }
        }
    }

    /**
     * User can drag and drop mp3 files from their directory into the library.
     */
    class DragDropToScrollPane extends DropTarget {
        public synchronized void drop(DropTargetDropEvent evt) {
            boolean invalidFilesFound = false;
            String filePath;
            int successCount = 0;
            int draggedCount = 0;
            try {
                evt.acceptDrop(DnDConstants.ACTION_COPY);
                List<File> droppedFiles = (List<File>)evt.getTransferable()
                        .getTransferData(DataFlavor.javaFileListFlavor);
                draggedCount = droppedFiles.size();
                for (File file : droppedFiles) {
                    filePath = file.getAbsolutePath();
                    Song newSong = new Song(filePath);
                    if (newSong.getPath() == null) {
                        System.out.println("[DragDrop] Not a valid file. '"+filePath+"'\n");
                        invalidFilesFound = true;
                    } else {
                        //if library successfully adds the song
                        //which is valid mp3 and not present in library..
                        if( library.addSong(newSong) ) {
                            successCount++;
                            playerView.updateTableView(library);
                            playerControl.updateLibrary(library);
                        }
                    }
                }
                if (invalidFilesFound) {
                    System.out.println("[DragDrop] Added "+successCount+" songs out of "+draggedCount+" files.\n");
                    JOptionPane.showMessageDialog(playerView,
                            "Some files have not been added\n" +
                                    "since they are not valid mp3 files.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
