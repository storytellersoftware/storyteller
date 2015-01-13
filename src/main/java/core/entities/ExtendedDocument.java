package core.entities;

import core.events.InsertEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * This class is an extension of the storyteller entity, called Document. It
 * is extended by having a name, the text in the document, and parent directory.
 * This class is used when recreating the state of the file system. It allows
 * easy access and manipulation of documents since it has some more information
 * that is only gathered from going through certain events.
 */

public class ExtendedDocument extends Document {
    //the text in the document
    private String text;

    //the name of the document
    private String name;

    //the parent directory
    private ExtendedDirectory parentDirectory;

    //indicates if this document was deleted
    private boolean wasDeleted;

    //list of the insert events that go along with the text
    private List<InsertEvent> insertEvents;

    public ExtendedDocument(Document doc) {
        //use the base class ctor
        super(doc.getId(), doc.getTimestamp(), doc.getCreatedUnderNodeId(), doc.getCreatedByDevGroupId(), doc.getParentDirectoryId());

        //create the list of event ids
        insertEvents = new ArrayList<InsertEvent>();

        //default to not deleted
        setWasDeleted(false);
    }

    public ExtendedDocument(String docId, Date timestamp, String createdUnderNodeId, String createdByDevGroupId, String parentDirId, String name) {
        //use the base class ctor
        super(docId, timestamp, createdUnderNodeId, createdByDevGroupId, parentDirId);

        //store the name of the doc
        setName(name);

        //default to not deleted
        setWasDeleted(false);

        //create the list of event ids
        insertEvents = new ArrayList<InsertEvent>();
    }

    public void addText(String t) {
        text = t;
    }

    public String getText() {
        return text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addInsertEvents(List<InsertEvent> eventIds) {
        //add all the insert events
        insertEvents.addAll(eventIds);
    }

    public List<InsertEvent> getInsertEvents() {
        return insertEvents;
    }

    public String getPath() {
        return getParentDirectory().getPath() + File.separator + getName();
    }

    public ExtendedDirectory getParentDirectory() {
        return parentDirectory;
    }

    public void setParentDirectory(ExtendedDirectory parentDirectory) {
        this.parentDirectory = parentDirectory;
    }

    public boolean getWasDeleted() {
        return wasDeleted;
    }

    public void setWasDeleted(boolean d) {
        wasDeleted = d;
    }

    @Override
    public String toString() {
        return this.getText();
    }
}