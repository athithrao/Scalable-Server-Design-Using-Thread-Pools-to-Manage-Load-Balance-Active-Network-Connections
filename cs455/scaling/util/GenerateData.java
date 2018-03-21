package cs455.scaling.util;

import java.util.Random;

public class GenerateData {

    private final int PACKET_SIZE = 8192;
    private Random randomGenerator;
    private byte[] datapacket;

    public GenerateData() {
        randomGenerator = new Random();
        datapacket = new byte[PACKET_SIZE];
    }

    public byte[] getPacket() {
        randomGenerator.nextBytes(datapacket);
        return datapacket;
    }

}
