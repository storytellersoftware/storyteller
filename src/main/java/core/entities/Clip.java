package core.entities;

import core.services.json.DeJSONiffy;
import core.services.json.JSONiffy;
import org.json.JSONException;
import org.json.JSONObject;
import playback.PlaybackFilter;

import java.util.Date;


public class Clip extends StorytellerEntity {
    private String name;
    private String description;
    private PlaybackFilter filter;
    private String playbackNodeId;

    public Clip(String id, Date timestamp, String createdUnderNodeIdId,
                String devGroupId, String name, String description,
                PlaybackFilter filter, String playbackNodeId) throws JSONException {
        super(id, timestamp, createdUnderNodeIdId, devGroupId);
        setName(name);
        setDescription(description);
        setFilter(filter);
        setPlaybackNodeId(playbackNodeId);
    }

    public Clip(String nodeID, String devGroupID, String name, String description, PlaybackFilter filter) {
        super(new Date(), nodeID, devGroupID);

        setName(name);
        setDescription(description);
        setFilter(filter);
        setPlaybackNodeId(nodeID);
    }

    public Clip(String ID, String nodeID, String devGroupID, String name, String description, PlaybackFilter filter) {
        super(ID, new Date(), nodeID, devGroupID);

        setName(name);
        setDescription(description);
        setFilter(filter);
        setPlaybackNodeId(nodeID);
    }

    /**
     * ******************
     * GETTERS and SETTERS
     * *******************
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Clip name: ");
        builder.append(getName());
        builder.append("Clip description: ");
        builder.append(getDescription());
        builder.append("Filter string: ");
        builder.append(getFilterString());
        builder.append("Node id: ");
        builder.append(getPlaybackNodeId());
        builder.append(" ");
        builder.append(super.toString());

        return builder.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilterString() {
        try {
            return JSONiffy.toJSON(filter).toString();
        } catch (JSONException e) {    // as string, because it goes in the database
            return "null";
        }
    }

    public PlaybackFilter getFilter() {
        return filter;
    }

    public void setFilter(JSONObject filterJSON) {
        try {
            filter = DeJSONiffy.playbackFilter(filterJSON);
        } catch (JSONException e) {
            filter = null;
        }
    }

    public void setFilter(PlaybackFilter filter) {
        this.filter = filter;
    }

    public void setFilterFromString(String filterString) throws JSONException {
        setFilter(new JSONObject(filterString));
    }

    public String getPlaybackNodeId() {
        return playbackNodeId;
    }

    public void setPlaybackNodeId(String playbackNodeId) {
        this.playbackNodeId = playbackNodeId;
    }

    public void update(Clip updateClip) {
        this.update((StorytellerEntity) updateClip);

        this.setDescription(updateClip.getDescription());
        this.setFilter(updateClip.getFilter());
        this.setName(updateClip.getName());
        this.setPlaybackNodeId(updateClip.getPlaybackNodeId());
    }
}
