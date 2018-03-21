package cs455.scaling.util;

import java.util.LinkedList;

public class BlockingQueue {

    private LinkedList<Runnable> queue;

    public BlockingQueue(){
        queue = new LinkedList<>();
    }

    public synchronized void enqueue(Runnable task) {
        this.queue.add(task);
        notify();
    }

    public synchronized Runnable dequeue()
            throws InterruptedException{
        while(this.queue.size() == 0){
            wait();
        }
        return this.queue.remove(0);
    }

}
