package driver;

import core.StorytellerCore;
import core.data.DBAbstractionException;
import core.data.DBFactory;
import core.data.SQLiteDBFactory;
import core.data.SQLiteDatabase;
import core.entities.Developer;
import core.entities.DeveloperGroup;
import core.entities.Project;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


/**
 * This class is used as a driver program for a GUI based playback viewer. If
 * you run this class (it does have a main) a GUI will pop and allow a user to
 * select a storyteller database file (it will default to the last db file
 * used). This starts the server and playbacks can then be viewed in a web
 * browser. There is an option in the GUI to open the browser directly when a
 * playback is started or the user can click the status label to open the
 * browser.
 * <p>
 * The user has to select a file from the file system. Then, they select a
 * project from within that database file (currently, a single database can have
 * multiple projects in them). Next, they select a developer group to be
 * associated with in the playback. The viewer of the playback has to choose a
 * developer group because any clips or storyboards created during the playback
 * will be associated with that dev group (TODO maybe allow a dev group to be
 * created from the browser OR have an anonymous dev group that can be used by
 * anyone interested in adding clips and storyboards to the database).
 */
public class GUIDriver extends JFrame implements ActionListener, WindowListener {
    private static final long serialVersionUID = 2492983411332371033L;

    //reference to the selected storyteller database file
    private File selectedDatabaseFile;

    //the server that will be serving playbacks
    private StorytellerCore storytellerCore;

    //lists of entities from the database, all the projects and dev groups in
    //the database
    private final List<Project> projectsFromDatabase = new ArrayList<Project>();
    private final List<DeveloperGroup> devGroupsFromDatabase = new ArrayList<DeveloperGroup>();

    //GUI components
    private JButton selectFileButton;
    private JLabel selectedFileLabel;
    private JLabel stateOfServerLabel;
    private JButton startStopServerButton;
    private JComboBox<String> projectsComboBox;
    private JComboBox<String> devNamesComboBox;
    private JCheckBox openPlaybackInBrowserCheckbox;

    //some initial text for the dropdowns
    private final String[] selectADBString = {"Select a database first"};

    //constants for the gui
    private final String START_SERVER_BUTTON_TEXT = "Start Server";
    private final String STOP_SERVER_BUTTON_TEXT = "Stop Server";
    //constant for the path of the preferences file
    private final String PREFS_FILE_PATH = ".guiDriverPrefs";

    public GUIDriver() {
        super("Playback Driver");

        //set up the initial GUI
        setUpGUI();

        //add a window listener to close the project when the window is closed
        addWindowListener(this);
    }

    private void setUpGUI() {
        //topmost layout will be a border layout
        getContentPane().setLayout(new BorderLayout());

        //center panel (project and developer dropdown lists, and state of
        //server label)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(3, 2));

        centerPanel.add(new JLabel("Choose a project"));
        projectsComboBox = new JComboBox<String>(selectADBString);
        centerPanel.add(projectsComboBox);

        centerPanel.add(new JLabel("Choose a developer group"));
        devNamesComboBox = new JComboBox<String>(selectADBString);
        centerPanel.add(devNamesComboBox);

        stateOfServerLabel = new JLabel("The server is NOT running");
        centerPanel.add(stateOfServerLabel);

        getContentPane().add(centerPanel, BorderLayout.CENTER);

        //south panel (start the server)
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new FlowLayout());

        startStopServerButton = new JButton(START_SERVER_BUTTON_TEXT);
        startStopServerButton.addActionListener(this);

        openPlaybackInBrowserCheckbox = new JCheckBox("Open the playback in the browser?");

        southPanel.add(startStopServerButton);
        southPanel.add(openPlaybackInBrowserCheckbox);

        getContentPane().add(southPanel, BorderLayout.SOUTH);

        //create the north panel by looking for the last used db file
        JPanel northPanel = createNorthPanel();

        getContentPane().add(northPanel, BorderLayout.NORTH);

        //window controls
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 250);
        setVisible(true);
    }

    /*
     * This creates the north panel which holds a button to select a db file and
     * a label with the path of the selected file. This methods checks to see if
     * there is a preferences file on the machine and attempts to read the last
     * used db file from the preferences file. If it finds a previously used
     * database file it sets it so the user doesn't have to repeatedly select a
     * file over and over again every time they run this program. If there is
     * not a previously used db file then the user must select one by clicking
     * the button to bring up a file chooser.
     */
    private JPanel createNorthPanel() {
        JPanel northPanel = null;
        try {
            //north panel (file name and file selector)
            northPanel = new JPanel();
            northPanel.setLayout(new FlowLayout());

            //look for the preferences file
            File prefsFile = new File(PREFS_FILE_PATH);

            //if the prefs file exists
            if (prefsFile.exists()) {
                //open and read the one line from the file with the path of the
                //last file opened
                BufferedReader reader = new BufferedReader(new FileReader(prefsFile.getAbsolutePath()));
                String lastDBFilePath = reader.readLine();
                reader.close();

                //if there was some text read in from the prefs file
                if (lastDBFilePath != null) {
                    //set the database file to the path from the prefs file
                    selectedDatabaseFile = new File(lastDBFilePath);

                    //if the database file is there, the prefs file was good
                    if (selectedDatabaseFile.exists()) {
                        //now that there is a db file fill the drop downs
                        setUpDropDowns();

                        //set the label
                        selectedFileLabel = new JLabel("Selected DB File: " + selectedDatabaseFile.getAbsolutePath());
                    } else
                    //the file path in the prefs file is bad for some reason
                    {
                        //don't let the user start the server
                        startStopServerButton.setEnabled(false);

                        //preferences file holds a bad file name, null it out
                        selectedDatabaseFile = null;

                        //set the label
                        selectedFileLabel = new JLabel("No file selected");
                    }
                }
            } else
            //the prefs file does not exist
            {
                selectedFileLabel = new JLabel("No file selected");

                //don't let the user start the server
                startStopServerButton.setEnabled(false);

            }

            selectFileButton = new JButton("Select a database file");
            selectFileButton.addActionListener(this);

            northPanel.add(selectFileButton);
            northPanel.add(selectedFileLabel);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return northPanel;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        //user selected a new database file
        if (event.getSource() == selectFileButton) {
            //create a file chooser (only .ali files)
            final JFileChooser fc = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Storyteller database files", "ali");
            fc.setFileFilter(filter);

            //pop it up
            int returnVal = fc.showOpenDialog(this);

            //if the user selected a file
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                //this is the selected file
                selectedDatabaseFile = fc.getSelectedFile();
                selectedFileLabel.setText("Selected DB File: " + selectedDatabaseFile.getAbsolutePath());

                //now that there is a db file fill the drop downs
                setUpDropDowns();

                startStopServerButton.setEnabled(true);
            }
            //else- they can choose another file later
        }
        //the user wants to start or stop the server
        else if (event.getSource() == startStopServerButton) {
            //if they are starting the server
            if (event.getActionCommand().equals(START_SERVER_BUTTON_TEXT)) {
                //change the text on the button to stop the server
                startStopServerButton.setText(STOP_SERVER_BUTTON_TEXT);

                //start the server
                setUpServer();
            } else
            //stopping the server
            {
                //close the server
                stopServer();

                //disable the last input
                startStopServerButton.setEnabled(false);
            }
        }
    }

    private void setUpDropDowns() {
        try {
            if (selectedDatabaseFile.exists()) {
                //open the selected file and and use the db api
                SQLiteDatabase db = new SQLiteDatabase(selectedDatabaseFile.getAbsolutePath());

                //first get all dev groups from the db

                //remove the existing dev groups in the list
                devGroupsFromDatabase.clear();

                //add the ones from this database to the list
                devGroupsFromDatabase.addAll(db.getAllDeveloperGroups());

                //remove all dev names from the drop down
                devNamesComboBox.removeAllItems();

                //populate with dev names
                for (int i = 0; i < devGroupsFromDatabase.size(); i++) {
                    //holds all the devs in a dev group
                    StringBuilder builder = new StringBuilder();

                    //get all the devs in the current dev group
                    List<Developer> devsInGroup = db.getDevelopersInADeveloperGroup(devGroupsFromDatabase.get(i).getId());

                    //build up a string of dev names
                    for (Developer dev : devsInGroup) {
                        builder.append(dev.getFirstName());
                        builder.append(" ");
                        builder.append(dev.getLastName());
                        builder.append(", ");
                    }
                    //remove the last comma
                    builder.deleteCharAt(builder.lastIndexOf(","));

                    //add the dev names to the drop down
                    devNamesComboBox.addItem(builder.toString());
                }

                //now projects
                projectsFromDatabase.clear();
                projectsFromDatabase.addAll(db.getAllProjects());
                projectsComboBox.removeAllItems();
                for (int i = 0; i < projectsFromDatabase.size(); i++) {
                    projectsComboBox.addItem(projectsFromDatabase.get(i).getProjectName());
                }
            } else
            //problem with the selected database file
            {
                JOptionPane.showMessageDialog(this, "Error opening the database file");
            }
        } catch (DBAbstractionException e) {
            e.printStackTrace();
        }
    }

    private void setUpServer() {
        try {
            //if the file has been selected and is good
            if (selectedDatabaseFile != null &&
                    selectedDatabaseFile.exists() &&
                    selectedDatabaseFile.isFile()) {
                //create a db factory so the server can create new databases (this should never happen
                //in this GUI driver)
                DBFactory sqliteDbFactory = new SQLiteDBFactory();

                //create a server that handles playbacks, NO ide events, and NO branching/merging
                storytellerCore = new StorytellerCore(sqliteDbFactory, false, true, false);

                //get the project name
                String projectName = projectsFromDatabase.get(projectsComboBox.getSelectedIndex()).getProjectName();

                storytellerCore.createDatabaseAbstraction(selectedDatabaseFile.getPath());

                //create a link to the browser
                stateOfServerLabel.setText("<html>The server is running on: <a href='http://localhost:4444/playback.html'> http://localhost:4444/playback.html</a><br>Kill the program to run a different database file</html>");
                stateOfServerLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            Desktop.getDesktop().browse(new URI("http://localhost:4444/playback.html"));
                        } catch (IOException e1) {
                        } catch (URISyntaxException e1) {
                        }
                    }
                });
                stateOfServerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

                //if the user wants to open the browser directly
                if (openPlaybackInBrowserCheckbox.isSelected() && Desktop.isDesktopSupported()) {
                    //open a browser window
                    Desktop.getDesktop().browse(new URI("http://localhost:4444/"));
                } else
                //doesn't want to or is not capable of opening browser
                {
                    //JOptionPane.showMessageDialog(this,
                    //"The server is running open your browser to view a playback");
                }

                //write the path to the selected file to the prefs file
                BufferedWriter writer = new BufferedWriter(new FileWriter(PREFS_FILE_PATH));
                writer.write(selectedDatabaseFile.getAbsolutePath());
                writer.close();

                //disable some of the inputs
                selectFileButton.setEnabled(false);
                projectsComboBox.setEnabled(false);
                devNamesComboBox.setEnabled(false);
                openPlaybackInBrowserCheckbox.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(this, "You must select a valid storyteller database file before starting the server");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {

    }

    /**
     * main to create and run this GUI
     */
    public static void main(String[] args) {
        //start the driver
        GUIDriver guiDriver = new GUIDriver();
    }

    //window listener methods
    @Override
    public void windowClosing(WindowEvent arg0) {
        //if the server object is set
        if (storytellerCore != null) {
            //close the server
            stopServer();
        }

        System.out.println("Closing the window");

        //kill the program
        System.exit(0);
    }

    @Override
    public void windowActivated(WindowEvent arg0) {
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {
    }

    @Override
    public void windowDeiconified(WindowEvent arg0) {
    }

    @Override
    public void windowIconified(WindowEvent arg0) {
    }

    @Override
    public void windowOpened(WindowEvent arg0) {
    }
}
