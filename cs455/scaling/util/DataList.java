package cs455.scaling.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DataList {

    private List<String> list;
    private HashCodeCompute compute;

    public DataList() {
        list = new ArrayList<>();
        compute = new HashCodeCompute();
    }

    public synchronized void addToList(byte[] data) {

        byte[] hash = compute.SHA1FromBytes(data);
        BigInteger hashInt = new BigInteger(1, hash);
        list.add(hashInt.toString(16));
    }

    public synchronized void removeFromList(byte[] hash) {

        BigInteger hashInt = new BigInteger(1, hash);
        String hashString = hashInt.toString(16);

        if(list.contains(hashString))
        {
            list.remove(hashString);
            //System.out.println("Found and removed.");
        }
        //System.out.println("Data List size - " + list.size());
    }


}
