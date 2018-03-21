package cs455.scaling.threadpool;

import cs455.scaling.util.BlockingQueue;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;

public class PoolThread extends Thread {


   private int poolThreadId;
   private BlockingQueue taskqueue;
   private volatile boolean exitFlag;

   public PoolThread(int id, BlockingQueue queue) {
       this.poolThreadId = id;
       this.taskqueue = queue;
       exitFlag = false;
   }

   public void run() {

       while(!exitFlag) {
           try
           {
               Runnable task = taskqueue.dequeue();
               task.run();
           }
           catch (InterruptedException ie)
           {
               System.out.println("Exception on Thread" + poolThreadId + "- run()");
               ie.printStackTrace();
               break;
           }

       }

   }

   public void close() throws IOException {
       exitFlag = true;
   }

}
