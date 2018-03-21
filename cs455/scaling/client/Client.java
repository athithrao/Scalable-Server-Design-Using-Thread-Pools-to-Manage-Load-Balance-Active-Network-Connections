package cs455.scaling.client;

import cs455.scaling.util.DataList;
import cs455.scaling.util.StatisticsCollectorAndDisplay;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class Client {

    private final InetSocketAddress serverSocketAddress;
    private final int messageRate;
    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(20);
    private DataList dataHashMap;
    private ClientSenderThread clientSenderThread;
    private Thread senderThread;
    private Thread collectorAndDisplay;

    private static StatisticsCollectorAndDisplay statisticsCollectorAndDisplay;

    public Client (String ip, int portno, int rate) {
        this.serverSocketAddress = new InetSocketAddress(ip,portno);
        this.messageRate = rate;
        statisticsCollectorAndDisplay = new StatisticsCollectorAndDisplay(2,20,1);
    }


    public void initialize() throws IOException {
        selector = SelectorProvider.provider().openSelector();
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_CONNECT);
        channel.connect(serverSocketAddress);
        dataHashMap = new DataList();
        collectorAndDisplay = new Thread(statisticsCollectorAndDisplay, "statisticsCollectorAndDisplay");
    }

    public void run() throws IOException {

        while(true) {

            int readyChannels = selector.select();

            if(readyChannels == 0) continue;

            Set<SelectionKey> selectedKeys = selector.selectedKeys();

            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                //other operations
                if (key.isConnectable()) {
                    this.connect(key);
                }
                else if (key.isReadable()) {
                    this.read(key);
                }

            }

        }

    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.finishConnect();

        key.interestOps(SelectionKey.OP_WRITE);

        clientSenderThread = new ClientSenderThread(key,messageRate, dataHashMap, statisticsCollectorAndDisplay);
        senderThread = new Thread(clientSenderThread, "clientSenderThread");
        collectorAndDisplay.start();
        senderThread.start();
    }

    private void read(SelectionKey key) throws IOException {

        SocketChannel socketChannel = (SocketChannel) key.channel();

        statisticsCollectorAndDisplay.incrementReceiveMessage();

        this.readBuffer.clear();
        readBuffer.rewind();
        int numRead = 0;
        try {

            while(this.readBuffer.hasRemaining() && numRead != -1)
            {
                numRead = socketChannel.read(this.readBuffer);
                //System.out.println("Number of bytes read: " + numRead + "with thread id: " + Thread.currentThread().getName());
                //this.readBuffer.clear();
            }


        } catch (IOException e) {
            key.cancel();
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
            key.channel().close();
            key.cancel();
            return;
        }
        else
        {
            readBuffer.flip();
            byte[] arr = readBuffer.array();
            //System.out.println("Incoming - " + Arrays.toString(arr));
            dataHashMap.removeFromList(arr);
        }
    }

    public void close(){

        clientSenderThread.close();

    }

    public static void main(String[] args) {
        System.out.println("Welcome to Client 1.0");

        if (args.length <= 2 || args.length > 3) {
            System.out.println("Syntax to run client - java cs455.scaling.client.Client server-host server-port message-rate");
        }
        else {
            try {
                Client client = new Client(args[0],Integer.parseInt(args[1]),Integer.parseInt(args[2]));
                client.initialize();

                client.run();

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        System.out.println("Client shutting down!");
                        client.close();
                    }
                });
            }
            catch(IOException io)
            {
                System.out.println("Error in main() - Client");
                io.printStackTrace();
            }
        }
    }


}
