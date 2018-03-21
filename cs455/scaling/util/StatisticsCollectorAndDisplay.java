package cs455.scaling.util;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class StatisticsCollectorAndDisplay implements Runnable {

    private final HashMap<SocketChannel,Integer> messagesPerSampleMap = new HashMap<>();
    private final LinkedList<Integer> sampleList = new LinkedList<>();
    private double numberMessagesRead;
    private int activeClientConnections;
    private double meanPerClientThroughput;
    private double stdevPerClientThroughput;
    private int sentMessages;
    private int receiveMessages;
    private int displayInterval = 0;
    private int samplesInterval = 0;
    private int program = 0;
    private volatile boolean exitFlag;
    private int secondsCounter = 0;

    private Object Lock = new Object();

    public StatisticsCollectorAndDisplay(int program, int displayInterval, int samplesInterval) {

        this.program = program;
        this.displayInterval = displayInterval;
        this.samplesInterval = samplesInterval;
        this.numberMessagesRead = 0;
        this.activeClientConnections = 0;
        this.meanPerClientThroughput = 0;
        this.stdevPerClientThroughput = 0;
        this.sentMessages = 0;
        this.receiveMessages = 0;

    }

    public void run() {

        while(!exitFlag) {

            try
            {
                TimeUnit.MILLISECONDS.sleep(1000);

                secondsCounter++;
                updateSampleList();

                if(secondsCounter%displayInterval == 0) {
                    calculateStatistics();
                    printStatistics();
                    clearStatistics();
                }

            }
            catch (InterruptedException ie) {
                System.out.println("Error in StatisticsCollectorAndDisplay - Run()");
                ie.printStackTrace();
                break;
            }

        }
    }

    private void printStatistics() {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if (program == 1) {
            System.out.println("[" + timestamp + "] Server Throughtput: " + numberMessagesRead + "/s, " + "Active Client Connectionctions: " + activeClientConnections + ", Mean Client Throughtput: " + meanPerClientThroughput + "/s, Std. Dev. of Per-Client Throughput: " + stdevPerClientThroughput + "/s" );
        }
        else {
            synchronized (Lock) {
                System.out.println("[" + timestamp + "] Total Sent Messages: " + sentMessages + ", Total Receive Messages: " + receiveMessages);
            }
        }
    }

    private void calculateStatistics() {

        double sum1 = 0.0;
        double sum2 = 0.0;
        int count = sampleList.size();

        if(program == 1) {
            for (Integer i : sampleList) {
                sum1 += i;
                sum2 += i * i;
            }
            numberMessagesRead = sum1 / displayInterval;
            activeClientConnections = messagesPerSampleMap.size();
            meanPerClientThroughput = sum1 / count;
            stdevPerClientThroughput = Math.sqrt((count * sum2 - sum1 * sum1) / (count * count));
        }
    }

    private void clearStatistics() {
        numberMessagesRead = 0;
        activeClientConnections = 0;
        meanPerClientThroughput = 0;
        stdevPerClientThroughput = 0;
        sampleList.clear();
        synchronized (Lock) {

            sentMessages = 0;
            receiveMessages = 0;

        }
    }

    public void addToStatisticsCollector(SocketChannel channel) {

        synchronized (Lock) {
            messagesPerSampleMap.putIfAbsent(channel,0);
        }
    }

    public void incrementMessagePerSample(SelectionKey key) {

        SocketChannel socketChannel = (SocketChannel) key.channel();

        synchronized (Lock) {
            messagesPerSampleMap.computeIfPresent(socketChannel, (k,v) -> v+1);
        }
    }

    public void incrementSentMessages() {

        synchronized (Lock) {

            sentMessages++;

        }

    }

    public void incrementReceiveMessage() {

        synchronized (Lock) {

            receiveMessages++;

        }

    }

    public void updateSampleList()
    {
        HashMap<SocketChannel,Integer> copy;
        if ( program == 1) {
            synchronized (Lock) {
                copy = deepCopy(messagesPerSampleMap);
                //messagesPerSampleMap.resetAll(0);
                for (SocketChannel key : messagesPerSampleMap.keySet())
                    messagesPerSampleMap.computeIfPresent(key, (k,v) -> v=0);
            }
            for (SocketChannel key : copy.keySet()) {
                sampleList.add(copy.get(key));
            }
        }

    }

    private HashMap<SocketChannel,Integer> deepCopy(HashMap<SocketChannel,Integer> m) {
        HashMap<SocketChannel,Integer> result = new HashMap<>();
        for (SocketChannel id: m.keySet())
            result.put(id, new Integer(m.get(id)) );
        return result;
    }

    public void close() throws IOException {
        exitFlag = true;
    }
}
