package driver;

import core.StorytellerCore;
import core.data.DBFactory;
import core.data.SQLiteDBFactory;

public class DriverForPlugin {
    public static void main(String[] args) {
        try {
            System.out.println("Storyteller Driver");

            //create a factory for any databases that will be created in this run of the program
            DBFactory sqliteDbFactory = new SQLiteDBFactory();

            //create a server that handles playbacks, ide events, and merging and pass in
            //the db factory so it knows how to create databases
            StorytellerCore testServer = new StorytellerCore(sqliteDbFactory, true, true, false);

            System.out.println("Red October - Fox Three");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
