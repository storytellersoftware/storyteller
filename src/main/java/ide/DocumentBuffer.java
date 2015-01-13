package ide;

import core.Constants;
import core.events.InsertEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A class used to help mirror the text in files on the IDE. We do this so IDE
 * plugins can be relatively dumb (they only need to know the file ids and the
 * positions in the files)
 * <p>
 * Contains a "miniclass" called SimplifiedTextEvent
 * <p>
 * Helps answer the questions:
 * If I add a text event at index x, what is the ID of the text event of the
 * previous neighbor (Interpreting IDE inputs)
 * <p>
 * If I delete a text event at index x, what is the ID of that event (Interpreting
 * IDE inputs)
 */

//TODO should a doc buffer hold the current name of the doc???
public class DocumentBuffer implements Serializable {
    private static final long serialVersionUID = 5080777642119002949L;

    //list of simple text events (storyteller id and text)
    private List<SimplifiedTextEvent> listOfSimplifiedTextEvents;

    public DocumentBuffer() {
        //TODO experiment what changing this to an ArrayList will do to performance
        listOfSimplifiedTextEvents = new LinkedList<SimplifiedTextEvent>();
    }

    public DocumentBuffer(List<InsertEvent> insertEvents) {
        //set up this class's data
        this();

        //go through all the passed in insert events
        for (InsertEvent insertEvent : insertEvents) {
            //create a new SimplifiedTextEvent with each event's data
            SimplifiedTextEvent event = new SimplifiedTextEvent(insertEvent.getId(), insertEvent.getEventData());

            //add the SimplifiedTextEvent to the list
            listOfSimplifiedTextEvents.add(event);
        }
    }

    /**
     * Creates a DocumentBuffer from the results of another DocumentBuffer.toJSONArray()
     *
     * @param docTextJarr
     */
    public DocumentBuffer(JSONArray docTextJarr) throws JSONException {
        //set up this class's data
        this();

        //go through all the JSON objects
        for (int i = 0; i < docTextJarr.length(); i++) {
            //get the JSON representation of an event
            JSONObject SimplifiedTextEventObject = docTextJarr.getJSONObject(i);

            //create a new SimplifiedTextEvent with each event's data
            SimplifiedTextEvent event = new SimplifiedTextEvent(SimplifiedTextEventObject.getString(Constants.ID), SimplifiedTextEventObject.getString(Constants.EVENT_DATA));

            //add the SimplifiedTextEvent to the list
            listOfSimplifiedTextEvents.add(event);
        }
    }

    /**
     * For use with paste events.  Returns the ID of the textEvent at a given index
     *
     * @param index
     * @return
     */
//	public String getTextEvent(int index)
//	{
//		//if this is a bad index
//		if (index == -1)
//		{
//			//return an empty string
//			return "";
//		}
//		
//		return listOfSimplifiedTextEvents.get(index).getId();
//	}
    public String getTextAtIndex(int index) {
        String retVal = null;

        //verify that the index is good
        if (listOfSimplifiedTextEvents.size() > 0 &&
                index >= 0 &&
                index < listOfSimplifiedTextEvents.size()) {
            retVal = listOfSimplifiedTextEvents.get(index).getText();
        }

        return retVal;
    }

    /**
     * Gets the index of the insertEvent that has the id given
     *
     * @param thisId
     * @return
     */
    public int getIndexOfInsertEvent(String id) {
        //to walk through the events
        ListIterator<SimplifiedTextEvent> iterator = listOfSimplifiedTextEvents.listIterator();

        //position of the event
        int index = 0;

        while (iterator.hasNext()) {
            //get the next event
            SimplifiedTextEvent event = iterator.next();

            //if we compare a simplified event with a string, the comparison
            //is between the id and the string
            if (event.equals(id)) {
                return index;
            }
            //else- we haven't found it yet, keep looking
            index++;
        }

        //we haven't found it, return a known bad value
        return -1;
    }

    /**
     * Returns the id of the event in the document buffer at a selected index
     *
     * @param index
     * @return
     */
    public String getIdAtIndex(int index) {
        String retVal = null;

        //verify that the index is good
        if (listOfSimplifiedTextEvents.size() > 0 &&
                index >= 0 &&
                index < listOfSimplifiedTextEvents.size()) {
            //get the id of the event
            retVal = listOfSimplifiedTextEvents.get(index).getId();
        }

        return retVal;
    }

    /**
     * Adds a text event to the buffer at a given location and returns what the previous neighbor id is.
     *
     * @param newID ID of the event being added
     * @param index The index at which the event was added
     * @param text  What character is here (really for debugging and seeing what the buffer is)
     * @return the ID of the previous neighbor to the event added
     */
    public String addTextEvent(String newID, int index, String text) {
        String returnVal = null;

        //if this is the first event in the document buffer
        if (listOfSimplifiedTextEvents.size() == 0 || index == 0) {
            //add to the beginning of the buffer
            listOfSimplifiedTextEvents.add(0, new SimplifiedTextEvent(newID, text));
        } else //the event comes somewhere after the first position
        {
            //using a list iterator to improve performance by not having to
            //trudge through the list twice. Start at the passed in index
            ListIterator<SimplifiedTextEvent> iterator = listOfSimplifiedTextEvents.listIterator(index - 1);

            //get the next event
            SimplifiedTextEvent foundEvent = iterator.next();

            //add the new event
            iterator.add(new SimplifiedTextEvent(newID, text));

            //return the id of the event immediately before the new event
            returnVal = foundEvent.getId();
        }

        return returnVal;
    }

    /**
     * Takes the index of a delete event and then returns the id of the actual event referenced.
     *
     * @param index location that a delete event references
     * @return ID of the text event that was there
     */
    public String addDeleteEvent(int index) {
        //using a list iterator to improve performance by not having to
        //trudge through the list twice. Start at the passed in index
        ListIterator<SimplifiedTextEvent> iterator = listOfSimplifiedTextEvents.listIterator(index);

        //find the event
        SimplifiedTextEvent foundEvent = iterator.next();

        //remove it
        iterator.remove();

        //return the id of the removed event
        return foundEvent.getId();
    }

    /**
     * Adds a text event to the buffer after a given previous neighbor id and
     * returns what index that is.
     *
     * @param newID              ID of the event being added
     * @param previousNeighborID The ID of the text event that the text should be inserted after
     * @param text               What character is here (really for debugging and seeing what the buffer is)
     */
    public void addTextEvent(String newID, String previousNeighborID, String text) {
        //using a list iterator to improve performance by not having to trudge
        //through the list twice.
        ListIterator<SimplifiedTextEvent> iterator = listOfSimplifiedTextEvents.listIterator();

        //if they passed in a good prev neighbor
        if (previousNeighborID != null) {
            //move through the events
            while (iterator.hasNext()) {
                //grab the next event
                SimplifiedTextEvent event = iterator.next();

                //if we have found the event that should back up to the new one
                if (event.equals(previousNeighborID)) {
                    //stop looking
                    break;
                }
            }
        }

        //TODO should this be up in the 'if'? what if the id is not found,
        //do we really want to add to the end??
        //updates the linked list to show the correct state (either in the buffer
        //if the previous neighbor was found or at the end of the document if not)
        iterator.add(new SimplifiedTextEvent(newID, text));
    }

    /**
     * Takes the id of a text event and deletes it.
     *
     * @param idOfTextEventToDelete
     */
    public void addDeleteEvent(String idOfTextEventToDelete) {
        //using a list iterator to improve performance by not having to trudge
        //through the list twice.
        ListIterator<SimplifiedTextEvent> iterator = listOfSimplifiedTextEvents.listIterator();

        //while there are more events
        while (iterator.hasNext()) {
            //grab the next event
            SimplifiedTextEvent event = iterator.next();

            //If we compare an ID block with a string, the comparison is
            //between the id block's id and the string
            if (event.equals(idOfTextEventToDelete)) {
                break;
            }
        }
        //TODO should this be up in the 'if'? what if the id is not found,
        //do we really want to remove the last element???
        iterator.remove();
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        for (SimplifiedTextEvent event : listOfSimplifiedTextEvents) {
            buffer.append(event.getText());
        }

        return buffer.toString();
    }

    /**
     * A miniclass that holds a very basic text event.  Just the text and the id.
     *
     * @author Kevin
     */
    private class SimplifiedTextEvent implements Serializable {
        private static final long serialVersionUID = 2640802508710968763L;

        //storyteller id of the text event
        private String id;

        //the character of text
        private String text;

        private SimplifiedTextEvent(String id, String text) {
            this.id = id;
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        @Override
        public boolean equals(Object obj) {
            //if they pass in a string
            if (obj instanceof String) {
                //compare this event's id to the string
                return id.equals(obj);
            }
            //else its another SimplifiedTextEvent
            else if (obj instanceof SimplifiedTextEvent) {
                //get the other's id
                SimplifiedTextEvent otherID = (SimplifiedTextEvent) obj;

                //compare
                return this.hashCode() == otherID.hashCode();
            }
            //else- sent in the wrong type

            return false;
        }

        @Override
        public int hashCode() {
            return (getId() + getText()).hashCode();
        }
    }

    /**
     * Converts this DocumentBuffer into a portable JSONArray type.  If the JSONArray is
     * passed into the constructor, it should be recreated exactly as it was before the
     * "compression".
     *
     * @return
     */
    public JSONArray toJSONArray() throws JSONException {
        JSONArray jarr = new JSONArray();
        for (SimplifiedTextEvent event : listOfSimplifiedTextEvents) {
            JSONObject textJobj = new JSONObject();
            textJobj.put(Constants.EVENT_DATA, event.getText());
            textJobj.put(Constants.ID, event.getId());
            jarr.put(textJobj);
        }
        return jarr;
    }

    //TODO where is this compression being used????
//	private static int METHOD_FOR_COMPRESSION=4;
//	
//	public byte[] getAsCompressedBytes()
//	{
//		if (METHOD_FOR_COMPRESSION == 1)
//		{
//			try
//			{
//				return getAsCompressedBytesMethodOne();
//			} 
//			catch (Exception e)
//			{
//				return null;
//			}
//		}
//		else if (METHOD_FOR_COMPRESSION == 2)
//		{
//			try
//			{
//				return getAsCompressedBytesMethodTwo();
//			} 
//			catch (Exception e)
//			{
//				return null;
//			}
//		}
//		else if (METHOD_FOR_COMPRESSION == 3)
//		{
//			try
//			{
//				return getAsCompressedBytesMethodThree();
//			} 
//			catch (Exception e)
//			{
//				return null;
//			}
//		}
//		else if (METHOD_FOR_COMPRESSION == 4)
//		{
//			try
//			{
//				return getAsCompressedBytesMethodFour();
//			} 
//			catch (Exception e)
//			{
//				return null;
//			}
//		}
//		return null;
//	}
//	
//	private byte[] getAsCompressedBytesMethodOne() throws Exception
//	{
//		return compressString(toJSONArray().toString());
//	}
//
//	private byte[] getAsCompressedBytesMethodTwo() throws Exception
//	{
//
//		return toJSONArray().toString().getBytes();
//	}
//	
//	private byte[] getAsCompressedBytesMethodThree() throws Exception
//	{
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//		ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
//		
//		objectOutputStream.writeObject(this);
//		
//		byte[] barr = outputStream.toByteArray();
//		
//		return barr;
//	}
//	
//	private byte[] getAsCompressedBytesMethodFour() throws Exception
//	{
//		return compressBytes(getAsCompressedBytesMethodThree());
//	}
//
//
//	
//	public static byte[] compressString(String dataToCompress)  throws IOException
//	{		
//		byte[] barr = compressBytes(dataToCompress.getBytes());
//		return barr;
//	}
//	
//	private static byte[] compressBytes(byte[] bytesToCompress) throws IOException
//	{
//
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//		DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream);
//		
//		deflaterOutputStream.write(bytesToCompress);
//		deflaterOutputStream.finish();
//		
//		deflaterOutputStream.close();
//		
//		byte[] barr = outputStream.toByteArray();
//
//		return barr;
//	}
//	
//	
//	private static byte[] inflateToBytes(byte[] compressedData) throws IOException
//	{
//		InputStream inputStream = new ByteArrayInputStream(compressedData);
//		InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream);
//				
//		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//		
//		byte[] byteBuffer = new byte[256];
//		
//		int returnedValue = inflaterInputStream.read(byteBuffer, 0, byteBuffer.length);
//		while(returnedValue!=-1)
//		{
//			buffer.write(byteBuffer, 0, returnedValue);
//			returnedValue = inflaterInputStream.read(byteBuffer, 0, byteBuffer.length);
//		}
//
//		inflaterInputStream.close();
//		inputStream.close();
//
//		
//		return buffer.toByteArray();
//
//	}
//	
//	public static String inflateToString(byte[] compressedData) throws IOException 
//	{
//		byte[] byteBuffer = inflateToBytes(compressedData);
//		
//		String retVal = new String(byteBuffer);
//		
//		return retVal;
//	}
//
//	public static DocumentBuffer getBufferFromCompressedString(byte[] compressedBytes)
//	{
//		if (METHOD_FOR_COMPRESSION == 1)
//		{
//			try
//			{
//				return getBufferFromCompressedStringMethodOne(compressedBytes);
//			} 
//			catch (Exception e)
//			{
//				return null;
//			}
//		}
//		else if (METHOD_FOR_COMPRESSION == 2)
//		{
//			try
//			{
//				return getBufferFromCompressedStringMethodTwo(compressedBytes);
//			} 
//			catch (JSONException e)
//			{
//				return null;
//			}
//		}
//		else if (METHOD_FOR_COMPRESSION == 3)
//		{
//			try
//			{
//				return getBufferFromCompressedStringMethodThree(compressedBytes);
//			} 
//			catch (Exception e)
//			{
//				return null;
//			} 
//		}
//		else if (METHOD_FOR_COMPRESSION == 4)
//		{
//			try
//			{
//				return getBufferFromCompressedStringMethodFour(compressedBytes);
//			} 
//			catch (Exception e)
//			{
//				return null;
//			} 
//		}
//		return null;
//	}
//
//	private static DocumentBuffer getBufferFromCompressedStringMethodOne(byte[] compressedBytes) throws JSONException, IOException
//	{
//		JSONArray jarr = new JSONArray(inflateToString(compressedBytes));
//		return new DocumentBuffer(jarr);
//	}
//
//	private static DocumentBuffer getBufferFromCompressedStringMethodTwo(byte[] compressedBytes) throws JSONException
//	{
//		JSONArray jarr = new JSONArray(new String(compressedBytes));
//		return new DocumentBuffer(jarr);
//	}
//	
//	private static DocumentBuffer getBufferFromCompressedStringMethodThree(byte[] compressedBytes) throws IOException, ClassNotFoundException
//	{
//		ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedBytes);
//		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
//		
//		return (DocumentBuffer) objectInputStream.readObject();
//	}
//
//	private static DocumentBuffer getBufferFromCompressedStringMethodFour(byte[] compressedBytes) throws IOException, ClassNotFoundException
//	{
//		ByteArrayInputStream inputStream = new ByteArrayInputStream(inflateToBytes(compressedBytes));
//		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
//		
//		return (DocumentBuffer) objectInputStream.readObject();
//	}
}
