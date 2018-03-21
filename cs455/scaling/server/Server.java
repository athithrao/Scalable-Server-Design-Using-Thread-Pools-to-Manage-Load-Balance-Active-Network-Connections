package cs455.scaling.server;

import cs455.scaling.executor.Task;
import cs455.scaling.threadpool.ThreadPoolManager;
import cs455.scaling.util.StatisticsCollectorAndDisplay;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Iterator;

public class Server {

    private int serverPortNumber = -1;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private ThreadPoolManager manager;
    private StatisticsCollectorAndDisplay statisticsCollectorAndDisplay;
    private Thread collectorAndDisplay;

    private static ArrayList<SelectionKey> keyArrayList;

    public Server (int portno, int numThreads) {
        try
        {
            this.serverPortNumber = portno;
            manager = new ThreadPoolManager(numThreads);
            keyArrayList = new ArrayList<>();
            statisticsCollectorAndDisplay = new StatisticsCollectorAndDisplay(1, 20, 1);
            this.selector = this.initSelector();
        }
        catch (IOException io)
        {
            System.out.println("Error in Server - Server ()");
            io.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Welcome to Scalable Server 1.0");

        if (args.length <= 1 || args.length > 2) {
            System.out.println("Syntax to run server - java cs455.scaling.server.Server portnum thread-pool-size");
        }
        else {
            Server server = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            server.initialize();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    System.out.println("Server shutting down!");
                    server.close();
                }
            });

            server.run();

        }
    }

    private Selector initSelector() throws IOException {
        // Create a new selector
        Selector socketSelector = SelectorProvider.provider().openSelector();

        // Create a new non-blocking server socket channel
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // Bind the server socket to the specified address and port
        InetSocketAddress isa = new InetSocketAddress(InetAddress.getLocalHost(), this.serverPortNumber);
        serverChannel.socket().bind(isa);

        System.out.println("IP Address - " + isa.getAddress().getHostAddress());
        System.out.println("Port Number - " + this.serverPortNumber);

        // Register the server socket channel, indicating an interest in
        // accepting new connections
        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        return socketSelector;
    }

    public void run() {
        while (true) {
            try {

                int readyChannels = selector.select();

                if(readyChannels == 0) continue;

                Iterator keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {

                    SelectionKey key = (SelectionKey) keyIterator.next();
                    keyIterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (key.isAcceptable()) {
                        this.accept(key);
                    }

                    if (key.isReadable() && (key.attachment() == null)) {
                        key.attach(new Object());
                        manager.execute(new Task(key));
                        statisticsCollectorAndDisplay.incrementMessagePerSample(key);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        System.out.println("Accepting incoming connection ");
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        socketChannel.register(this.selector, SelectionKey.OP_READ);

        if(!collectorAndDisplay.isAlive())
            collectorAndDisplay.start();

        statisticsCollectorAndDisplay.addToStatisticsCollector(socketChannel);


    }

    public void initialize() {

        manager.initialize();
        collectorAndDisplay = new Thread(statisticsCollectorAndDisplay, "statisticCollectorAndDisplay");
    }

    public void close(){
        manager.close();
    }
}
