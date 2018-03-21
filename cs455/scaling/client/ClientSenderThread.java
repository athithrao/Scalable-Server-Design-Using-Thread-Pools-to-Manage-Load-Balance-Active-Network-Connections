package cs455.scaling.client;

import cs455.scaling.util.DataList;
import cs455.scaling.util.GenerateData;
import cs455.scaling.util.StatisticsCollectorAndDisplay;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ClientSenderThread implements Runnable
{
    private final SelectionKey selectionKey;
    private final  int messageRate;
    private boolean exitFlag;
    private final GenerateData generateData;
    private final DataList dataHashMap;
    private StatisticsCollectorAndDisplay statisticsCollectorAndDisplay;

    public ClientSenderThread(SelectionKey selectionKey, int rate, DataList map, StatisticsCollectorAndDisplay statisticsCollectorAndDisplay) {
        this.selectionKey = selectionKey;
        this.messageRate = rate;
        this.exitFlag = false;
        generateData = new GenerateData();
        dataHashMap = map;
        this.statisticsCollectorAndDisplay = statisticsCollectorAndDisplay;
    }

    public void close() {
        this.exitFlag = true;
    }

    @Override
    public void run() {

        SocketChannel channel = (SocketChannel) selectionKey.channel();

        while (!exitFlag) {

            try
            {

                byte[] data = generateData.getPacket();
                dataHashMap.addToList(data);
                statisticsCollectorAndDisplay.incrementSentMessages();

                ByteBuffer buffer = ByteBuffer.wrap(data);

                try {
                    while (buffer.hasRemaining())
                    {
                        //System.out.println("Outgoing - " + Arrays.toString(buffer.array()));
                        channel.write(buffer);
                    }
                }
                catch (ClosedChannelException cce) {
                }
                catch (Exception e) {
                }

                selectionKey.interestOps(SelectionKey.OP_READ);
                buffer.clear();
                TimeUnit.MILLISECONDS.sleep(1000/messageRate);

            }
            catch (InterruptedException ie) {
                System.out.println("InterruptedException on ClientSenderThread - Run()");
                break;
            }
        }

    }
}
