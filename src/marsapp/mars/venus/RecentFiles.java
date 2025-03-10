package mars.venus;

import mars.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class RecentFiles {
    private static String settingsKey = "recentFiles";
    private static String delimiter = "; ";
    private List<String> recentFiles;
    private JMenu menu;
    private VenusUI mainUI;

    public RecentFiles( VenusUI appFrame ) {

        recentFiles = new ArrayList<String>();

        // Read from global settings
        String str = Globals.getSettings().getSetting(settingsKey, "");
        if ( ! str.isEmpty() )
            for( String file : str.split(delimiter) ) {
                recentFiles.add( file );
            }
        
        this.mainUI = appFrame;
        menu = new JMenu("Open Recent");
        createMenuItems();
    }

    /**
     * Creates the "Open Recent" menu items
     */
    private void createMenuItems() {
        for( String f : recentFiles ) {
            File file = new File(f);
            JMenuItem r = new JMenuItem( file.getName() );
            r.addActionListener(evt -> { mainUI.editor.open( file ); });
            menu.add( r );
        }
        if ( ! recentFiles.isEmpty() ) {
            menu.addSeparator();
        }

        JMenuItem menuClear = new JMenuItem("Clear recently opened...");
        menuClear.addActionListener(evt -> { this.clear( ); });
        menuClear.setEnabled(! recentFiles.isEmpty());
        menu.add( menuClear );
    }

    /**
     * Updates the menu items
     */
    private void updateMenuItems() {
        menu.removeAll();
        createMenuItems();
    }

    /**
     * 
     * @return The recent files menu
     */
    public JMenu getMenu() {
        return menu;
    }

    /**
    * Clears the recent files list
    */
    public void clear() {
        recentFiles.clear();
        save();
        updateMenuItems();
    }

    /**
    * Adds a new filename to the recent files list
    * @note if the filename already exists, it is moved to the top of the list
    * @param filename
    */
    public void add( String filename ) {

        // Look for existing file or last entry
        if ( recentFiles.contains(filename)) {
           int idx = recentFiles.indexOf(filename);
           recentFiles.remove(idx);
           recentFiles.add( 0, filename );
        } else {
           recentFiles.add( 0, filename );
           // Max. list size is 8
           int size = recentFiles.size();
           if(size > 8)
              recentFiles.remove(recentFiles.size() - 1);
        }

        // Save the list to the property file
        save();
        updateMenuItems();
    }

    /**
     * Save recent files preference
     */
    private void save() {
        String list = String.join( delimiter, recentFiles );
        Globals.getSettings().putSetting(settingsKey,list);
    }

    /**
     * Gets the path of the most recent file. If it does not exist default 
     * to user home.
     * 
     * @return
     */
    public String getPath() {
        String path;

        if ( recentFiles.size() > 0 ) {
            File f = new File( recentFiles.get(0) );
            path = f.getParentFile().getPath();
        } else {
            path = System.getProperty("user.home");
        }
        return path;
    }
}
