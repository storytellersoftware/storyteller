package playback;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the filters to hide things that are not relevant to
 * the viewer of a playback. Basic playbacks begin with a user choosing what
 * they want to see in a filter menu. Another type of playback is one where
 * the user has selected some text and they want to see events related to them.
 * <p>
 * The viewer of a basic playback can choose to limit playback to a
 * certain time (yesterday, last week, etc.), only changes made by certain
 * developers, only changes in a certain set of documents, to ignore
 * characters that were added and then deleted under a certain number of
 * seconds, and whether the user would just like to see the end state a playback.
 * In addition, this filter class keeps track of whether to show the history
 * of pastes and the type of 'blocks' to show during playback (individual
 * characters, words, or lines).
 */
public class PlaybackFilter {
    //id of the oldest node in a node lineage, used to limit what gets filtered
    private String nodeID;

    //the node sequence number (index of the last relevant event in the node)
    private int nodeSequenceNumber;

    //for selected text playback:
    //- list of selected and relevant events
    private List<String> selectedAndRelevantEventIds;

    //for user selected filter playback:
    //- earliest and latest acceptable timestamps for the events in a node lineage
    private long startTime;
    private long endTime;

    //all the ids of the acceptable documents in the filter
    private List<String> acceptableDocumentIDs;

    //all the developer group ids of acceptable developers who contributed in this lineage
    private List<String> acceptableDeveloperGroupIDs;

    //number of seconds between an insert and delete to make them both unneeded
    private int hideDeleteLimit;

    //should we show pasted events' origin. In other words, if the text was pasted
    //and relevant should we make the copied text relevant too
    private boolean showPasteOrigin;

    //should we show only the end result of the playback. This is useful if the
    //viewer doesn't need to see the code evolve, they only want to see the final
    //result with relevant events highlighted but not animated.
    private boolean showOnlyEndResult;

    //the type of blocks of characters to show up
    private String relevantBlockType;

    //these define the playback block types- chars (show all individual characters),
    //words (show only white space), lines (show only newlines)
    public static final String CHARACTER_BLOCK_TYPE = "chars";
    public static final String WORD_BLOCK_TYPE = "words";
    public static final String LINE_BLOCK_TYPE = "lines";

    public PlaybackFilter(String nodeID, int nodeSequenceNumber, long startTime,
                          long endTime, int hideDeleteLimit,
                          boolean showPasteOrigin, boolean showOnlyEndResult,
                          String relevantBlockType) {
        //set the node info
        setNodeID(nodeID);
        setNodeSequenceNumber(nodeSequenceNumber);
        //set the filter info
        setStartTime(startTime);
        setEndTime(endTime);
        setHideDeleteLimit(hideDeleteLimit);
        setShowPasteOrigin(showPasteOrigin);
        setShowOnlyEndResult(showOnlyEndResult);
        setRelevantBlockType(relevantBlockType);
        setAcceptableDocumentIDs(new ArrayList<String>());
        setAcceptableDeveloperGroupIDs(new ArrayList<String>());
        //set the selected text info
        setSelectedAndRelevantEventIds(new ArrayList<String>());
    }

    public PlaybackFilter() {
        //default data for an empty filter
        this(null, 0, 0, 0, 0, false, false, CHARACTER_BLOCK_TYPE);
    }

    /**
     * if there are any events in the selected and relevant collection then
     * this is a selected text filter and not one created using filter parameters
     * from a menu
     */
    public boolean isASelectedTextFilter() {
        //return true if there are one or more selected events
        return getSelectedAndRelevantEventIds().size() > 0;
    }

    //getters/setters
    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public int getNodeSequenceNumber() {
        return nodeSequenceNumber;
    }

    public void setNodeSequenceNumber(int nodeSequenceNumber) {
        this.nodeSequenceNumber = nodeSequenceNumber;
    }

    public List<String> getSelectedAndRelevantEventIds() {
        return selectedAndRelevantEventIds;
    }

    public void setSelectedAndRelevantEventIds(List<String> selectedAndRelevantEventIds) {
        this.selectedAndRelevantEventIds = selectedAndRelevantEventIds;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public List<String> getAcceptableDocumentIDs() {
        return acceptableDocumentIDs;
    }

    public void setAcceptableDocumentIDs(List<String> documentIDs) {
        this.acceptableDocumentIDs = documentIDs;
    }

    public List<String> getAcceptableDeveloperGroupIDs() {
        return acceptableDeveloperGroupIDs;
    }

    public void setAcceptableDeveloperGroupIDs(List<String> developerGroupIDs) {
        this.acceptableDeveloperGroupIDs = developerGroupIDs;
    }

    public int getHideDeleteLimit() {
        return hideDeleteLimit;
    }

    public void setHideDeleteLimit(int hideDeleteLimit) {
        this.hideDeleteLimit = hideDeleteLimit;
    }

    public boolean isShowPasteOrigin() {
        return showPasteOrigin;
    }

    public void setShowPasteOrigin(boolean showPasteOrigin) {
        this.showPasteOrigin = showPasteOrigin;
    }

    public boolean isShowOnlyEndResult() {
        return showOnlyEndResult;
    }

    public void setShowOnlyEndResult(boolean showOnlyEndResult) {
        this.showOnlyEndResult = showOnlyEndResult;
    }

    public String getRelevantBlockType() {
        return relevantBlockType;
    }

    public void setRelevantBlockType(String relevantBlockType) {
        this.relevantBlockType = relevantBlockType;
    }
}