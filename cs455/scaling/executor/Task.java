package cs455.scaling.executor;

import cs455.scaling.util.HashCodeCompute;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class Task implements Runnable {

    private SelectionKey selectionKey;

    // The buffer into which we'll read data when it's available
    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

    private HashCodeCompute hashCodeCompute;

    private static ArrayList<SelectionKey> keyArrayList;

    public Task(SelectionKey key) {
        this.selectionKey = key;
        this.hashCodeCompute = new HashCodeCompute();
        keyArrayList = new ArrayList<>();
    }

    private void read() throws IOException {


        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        long nbytes = 0;
        readBuffer.clear();
        while (readBuffer.hasRemaining() && nbytes != -1) {
            nbytes = socketChannel.read(readBuffer);
        }

        if (nbytes == -1) {
            selectionKey.channel().close();
            selectionKey.cancel();
            return;
        }
        else
        {
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void write(byte[] hash) throws  IOException {

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        ByteBuffer buffer = ByteBuffer.wrap(hash);

        try {
            while (buffer.hasRemaining())
            {
                socketChannel.write(buffer);
            }
        }
        catch (ClosedChannelException cce) {
        }
        catch (Exception e) {
        }

        //System.out.println("Outgoing - " + Arrays.toString(hash));

        selectionKey.interestOps(SelectionKey.OP_READ);
    }

    private byte[] compute() {

        byte[] arr = readBuffer.array();
        //System.out.println("Incoming - " + Arrays.toString(arr));
        byte[] hash = hashCodeCompute.SHA1FromBytes(arr);
        return hash;

    }

    @Override
    public void run() {
        //System.out.println("Task Execution Started!");
        try
        {
            this.read();
            byte[] data = this.compute();
            this.write(data);

            selectionKey.attach(null);
            keyArrayList.remove(selectionKey);

        }
        catch (IOException io) {
            System.out.println("Error in Task- run()");
            io.printStackTrace();
        }
        //System.out.println("Task Execution Completed!");
    }
}
