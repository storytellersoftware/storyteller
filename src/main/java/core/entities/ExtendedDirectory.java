package core.entities;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * This class is an extension of the storyteller entity, called Directory. It
 * is extended by having a name, parent directory, list of subdirectories, and
 * list of documents in the directory. This class is used when recreating the
 * state of the file system. It allows easy access and manipulation of
 * documents and directories since it has some more information that is only
 * gathered from going through certain events.
 */
public class ExtendedDirectory extends Directory {
    //this is the name of the directory (plain old Directories don't have names)
    private String name;

    //reference to the parent directory
    private ExtendedDirectory parentDirectory;

    //indicates if this document was deleted
    private boolean wasDeleted;

    //list of all directories inside this one
    private List<ExtendedDirectory> subdirectories;

    //list of all documents inside this directory
    private List<ExtendedDocument> documents;

    public ExtendedDirectory(Directory dir) {
        //use the base class ctor
        super(dir.getId(), dir.getTimestamp(), dir.getCreatedUnderNodeId(), dir.getCreatedByDevGroupId(), dir.getParentDirectoryId());

        //create the lists
        subdirectories = new ArrayList<ExtendedDirectory>();
        documents = new ArrayList<ExtendedDocument>();

        //defalt to not deleted
        setWasDeleted(false);
    }

    public ExtendedDirectory(String dirId, Date timestamp, String createdUnderNodeId, String createdByDevGroupId, String parentDirId, String name) {
        //use the base class ctor
        super(dirId, timestamp, createdUnderNodeId, createdByDevGroupId, parentDirId);

        //set the name
        setName(name);

        //create the lists
        subdirectories = new ArrayList<ExtendedDirectory>();
        documents = new ArrayList<ExtendedDocument>();

        //defalt to not deleted
        setWasDeleted(false);
    }

    public void getAllDirectories(List<ExtendedDirectory> dirs) {
        dirs.add(this);

        //go through this directory's sub-directories
        for (ExtendedDirectory subDir : getSubdirectories()) {
            //recurse
            subDir.getAllDirectories(dirs);
        }
    }

    public void getAllDocuments(List<ExtendedDocument> docs) throws JSONException {
        //gather all of this directory's documents
        docs.addAll(getDocuments());

        //go through this directory's sub-directories
        for (ExtendedDirectory subDir : getSubdirectories()) {
            //recurse
            subDir.getAllDocuments(docs);
        }
    }

    public void addDirectory(ExtendedDirectory dir) {
        //add a directory
        subdirectories.add(dir);
    }

    public List<ExtendedDirectory> getSubdirectories() {
        return subdirectories;
    }

    public void addDocument(ExtendedDocument doc) {
        //add a document
        documents.add(doc);
    }

    public List<ExtendedDocument> getDocuments() {
        return documents;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExtendedDirectory getParentDirectory() {
        return parentDirectory;
    }

    public void setParentDirectory(ExtendedDirectory parentDirectory) {
        this.parentDirectory = parentDirectory;
    }

    public String getPath() {
        //start with a slash (only the root directory will keep this slash)
        String previousPath = File.separator;

        //if there is a parent
        if (getParentDirectory() != null) {
            //write over the previous path with the parent's path and a slash
            // /path/to/parent
            previousPath = getParentDirectory().getPath() + File.separator;
        }
        //else- no parent, this is the root "/"

        //add the name, /path/to/parent/thisdir
        return previousPath + getName();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Path: ");
        builder.append(getPath());
        builder.append("\n");

        builder.append("Subdirectories:\n");
        for (ExtendedDirectory dir : subdirectories) {
            builder.append(dir.getPath());
            builder.append("\n");
            builder.append(dir.toString());
            builder.append("\n");
        }

        builder.append("Documents\n");
        for (ExtendedDocument doc : documents) {
            builder.append(doc.getPath());
            builder.append("\n");
            builder.append(doc.getText());
            builder.append("\n");
        }

        return builder.toString();
    }

    public boolean getWasDeleted() {
        return wasDeleted;
    }

    public void setWasDeleted(boolean d) {
        wasDeleted = d;
    }
}