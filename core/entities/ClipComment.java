package core.entities;

import java.util.Date;

public class ClipComment extends StorytellerEntity
{
	private String text;
	private String displayCommentEventId;
	private String clipId;
	private String startHighlightEventId;
	private String endHighlightEventId;

	public ClipComment(Date timestamp, String createdUnderNodeIdId,
			String devGroupId, String text, String displayCommentEventId,
			String clipId, String startHighlightEventId, String endHighlightEventId)
	{
		super(timestamp, createdUnderNodeIdId, devGroupId);
		setText(text);
		setDisplayCommentEventId(displayCommentEventId);
		setClipId(clipId);
		setStartHighlightEventId(startHighlightEventId);
		setEndHighlightEventId(endHighlightEventId);
	}

	public ClipComment(String id, Date timestamp, String createdUnderNodeIdId,
			String devGroupId, String text, String displayCommentEventId,
			String clipId, String startHighlightEventId, String endHighlightEventId)
	{
		super(id, timestamp, createdUnderNodeIdId, devGroupId);
		setText(text);
		setDisplayCommentEventId(displayCommentEventId);
		setClipId(clipId);
		setStartHighlightEventId(startHighlightEventId);
		setEndHighlightEventId(endHighlightEventId);
	}

	public ClipComment(Clip parent, String developerGroupID, String commentText,
			String eventID, String firstHighlightedEvent, String lastHighlightedEvent)
	{
		this(
				new Date(),
				parent.getCreatedUnderNodeId(),
				developerGroupID,
				commentText,
				eventID,
				parent.getId(),
				firstHighlightedEvent,
				lastHighlightedEvent);
	}

	/*********************
	  GETTERS and SETTERS
	 *********************/
	@Override
	public String toString()
	{
		return null;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getDisplayCommentEventId()
	{
		return displayCommentEventId;
	}

	public void setDisplayCommentEventId(String displayCommentEventId)
	{
		this.displayCommentEventId = displayCommentEventId;
	}

	public String getClipId()
	{
		return clipId;
	}

	public void setClipId(String clipId)
	{
		this.clipId = clipId;
	}

	public String getStartHighlightEventId()
	{
		return startHighlightEventId;
	}

	public void setStartHighlightEventId(String startHighlightEventId)
	{
		this.startHighlightEventId = startHighlightEventId;
	}

	public String getEndHighlightEventId()
	{
		return endHighlightEventId;
	}

	public void setEndHighlightEventId(String endHighlightEventId)
	{
		this.endHighlightEventId = endHighlightEventId;
	}
	
	
	public void update(ClipComment updateComment)
	{
		//Calls on super class to update base information about StorytellerEntity
		this.update((StorytellerEntity)updateComment);
		
		this.setClipId(updateComment.getClipId());
		this.setDisplayCommentEventId(updateComment.getDisplayCommentEventId());
		this.setEndHighlightEventId(updateComment.getEndHighlightEventId());
		this.setStartHighlightEventId(updateComment.getStartHighlightEventId());
		this.setText(updateComment.getText());
	}
}
