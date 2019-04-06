package view;

import model.Song;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * SongListView class is to show either the library or a playlist.
 * This contains a table with column headers for songs.
 * Table related listeners are attached.
 */
public class SongListView extends JPanel {
    //components for table
    private JScrollPane tableScrollPane;
    private JTable table;
    private DefaultTableModel tableModel;
    private String[] columnHeader;
    private int tableRowHeight = 24;

    /**
     * Constructs a panel to show a list of songs
     * with an empty table view.
     */
    public SongListView(){
        // Table setup
        columnHeader = new String[]{"Path", "Title", "Artist", "Album", "Year", "Comment", "Genre"};
        table = new JTable(){
            @Override   //block table contents editing
            public boolean isCellEditable(int row, int column) { return false; }
        };

        //for dynamic row addition
        tableModel = new DefaultTableModel(columnHeader,0);
        table.setModel(tableModel);

        //table behavior setups
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        //ui setups
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFont(MusicPlayerGUI.FONT);
        table.getTableHeader().setFont(MusicPlayerGUI.FONT);
        table.setRowHeight(tableRowHeight);
        //table.setShowGrid(false);

//        //change the look of the header
//        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
//        renderer.setBorder(BorderFactory.createEmptyBorder());
//        table.getTableHeader().setDefaultRenderer(renderer);

        //put table in place
        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        this.setLayout(new BorderLayout());
        this.add(tableScrollPane, BorderLayout.CENTER);
    }

    /**
     * Constructs a panel to show a list of songs
     * with a table view from a list of songs.
     * @param songList list of Songs to be reflected in table view,
     *                 which can be from the library or a playlist.
     */
    public SongListView (ArrayList<Song> songList){
        this();
        updateTableView(songList);
    }

    /**
     * Updates the table view.
     * @param songList list of Songs to be reflected in table view,
     *                 which can be from the library or a playlist.
     */
    public void updateTableView(ArrayList<Song> songList) {
        table.removeAll();
        for (Song song : songList) {
            tableModel.addRow(song.toArray());
        }
        tableModel.fireTableDataChanged();
    }

    /**
     * Returns the table of this SongListView
     * @return JTable containing songs
     */
    public JTable getSongTable(){
        return table;
    }

    /**
     * Set color theme on the table view panel.
     */
    public void setColorTheme(ColorTheme colorTheme){
        Color[] bgColor = colorTheme.bgColor;
        Color[] fgColor = colorTheme.fgColor;
        Color[] pointColor = colorTheme.pointColor;

        //table
        table.setBackground(bgColor[0]);
        table.setForeground(fgColor[1]);
        table.getTableHeader().setBackground(bgColor[1]);
        table.getTableHeader().setForeground(fgColor[2]);

        //horizontal line of the table
        table.setGridColor(bgColor[2]);

        //table row selection
        table.setSelectionBackground(pointColor[0]);
        table.setSelectionForeground(pointColor[1]);
    }
}
