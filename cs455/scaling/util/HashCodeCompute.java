package cs455.scaling.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCodeCompute {

    public byte[] SHA1FromBytes(byte[] data) {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            byte[] hash = digest.digest(data);
            return hash;
        }
        catch (NoSuchAlgorithmException ns)
        {
            ns.printStackTrace();
        }
        return null;
       }
}
