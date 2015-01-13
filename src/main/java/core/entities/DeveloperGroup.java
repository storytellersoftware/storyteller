package core.entities;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

public class DeveloperGroup extends StorytellerEntity {
//	public DeveloperGroup(Date timestamp, String createdUnderNodeId, String createdDevGroupId)
//	{
//		super(timestamp, createdUnderNodeId, createdDevGroupId);
//	}

    public DeveloperGroup(List<Developer> loggedInDevs, Date timestamp, String createdUnderNodeId, String devGroupId) {
        //make the id of a dev group the MD5 hash of all the developers that will make up the group
        super(generateID(loggedInDevs), timestamp, createdUnderNodeId, devGroupId);
    }

    public DeveloperGroup(String id, Date timestamp, String createdUnderNodeId, String devGroupId) {
        super(id, timestamp, createdUnderNodeId, devGroupId);
    }

    /**
     * Generate an MD5 hash of a set of emails.
     */
    private static String generateID(List<Developer> loggedInDevs) {
        String Id = null;
        try {
            //MD5 utility
            MessageDigest md = MessageDigest.getInstance("MD5");

            //for each email address
            for (Developer dev : loggedInDevs) {
                //turn the email address into bytes and update the MD5 utility
                md.update(dev.getEmail().getBytes("UTF-8"));
            }

            //make a digest
            byte[] thedigest = md.digest();

            //convert back to bytes and a string
            BigInteger bigInt = new BigInteger(1, thedigest);
            Id = bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return Id;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Developer Group");
        builder.append(" ");
        builder.append(super.toString());
        return builder.toString();
    }

    public void update(DeveloperGroup updateDevGroup) {
        this.update((StorytellerEntity) updateDevGroup);
    }
}
