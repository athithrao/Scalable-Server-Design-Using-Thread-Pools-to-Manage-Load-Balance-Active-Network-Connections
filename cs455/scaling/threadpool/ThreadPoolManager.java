package cs455.scaling.threadpool;

import cs455.scaling.util.BlockingQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ThreadPoolManager {

    private final int MAX_THREADS;

    private final List<PoolThread> poolThreads;

    private final BlockingQueue taskQueue;

    public ThreadPoolManager(int numThreads) {

        this.MAX_THREADS = numThreads;
        this.poolThreads = new ArrayList<>();
        this.taskQueue = new BlockingQueue();

    }

    public void initialize() {

        for(int i = 0; i < MAX_THREADS; i++)
        {
            PoolThread thread = new PoolThread(i+1,taskQueue);
            poolThreads.add(thread);
            thread.start();
        }
    }

    public void close()
    {
        try
        {
            for(PoolThread thread : poolThreads)
            {
                thread.close();
            }
        }
        catch (IOException io)
        {
            System.out.println("Exception in ThreadPoolManager - close()");
            io.printStackTrace();
        }

    }

    public void execute(Runnable task)
    {
        taskQueue.enqueue(task);
    }

}
