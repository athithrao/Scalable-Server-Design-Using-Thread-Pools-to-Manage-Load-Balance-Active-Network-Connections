# Scalable-Server-Design-Using-Thread-Pools-to-Manage-Load-Balance-Active-Network-Connections

As part of this assignment you will be developing a server to handle network traffic by designing and building your own thread pool. This thread pool will have a configurable number of threads that will be used to perform tasks relating to network communications. Specifically, you will use this thread pool to manage all tasks relating to network communications. This includes:
  1. Managing incoming network connections
  2. Receiving data over these network connections 3. Sending data over any of these links
Unlike the previous assignment where we had a receiver thread associated with each socket, we will be managing a collection of connections using a fixed thread pool. A typical set up for this assignment involves a server with a thread pool size of 10 and 100 active clients that send data over their connections.
## 1 Components
There are two components that you will be building as part of this assignment: a server and a client.
### 1.1 Server Node:
There is exactly one server node in the system. The server node provides the following functions:
- Accepts incoming network connections from the clients.
- Accepts incoming traffic from these connections
- Replies to clients by sending back a hash code for each message received.
- The server performs all the above functions byrelying on the thread pool.
## 1.2 The Clients
Unlike the server node, there are multiple Clients (minimum of 100) in the system. A client provides the following functionalities:
(1) Connect and maintain an active connection to the server.
(2) Regularly send data packets to the server. The payloads for these data packets are 8 KB and
the values for these bytes are randomly generated. The rate at which each connection will generate packets is R per-second; include a Thread.sleep(1000/ R) in the client which ensures that you achieve the targeted production rate. The typical value of R is between 2-4.
(3) The client should track hashcodes of the data packets that it has sent to the server. A server will acknowledge every packet that it has received by sending the computed hash code back to the client.

## 2 Interactions between the components
The client is expected to send messages at the rate specified during start-up. The client sends a byte[] to the server. The size of this array is 8 KB and the contents of this array are randomly generated. The client generates a new byte array for every transmission and also tracks the hash codes associated with the data that it transmits. Hashes will be generated with the SHA-1 algorithm. The following code snippet computes the SHA-1 hash of a byte array, and returns its representation as a hex string.
A client maintains these hash codes in a linked list. For every data packet that is published, the client adds the corresponding hashcode to the linked list. Upon receiving the data, the server will compute the hash code for the data packet and send this back to the client. When an acknowledgement is received from the server, the client checks the hashcode in the acknowledgement by scanning through the linked list. Once the hashcode has been verified, it can be removed from the linked list.
The server relies on the thread pool to perform all tasks. The threads within the thread pool should be created just once. Care must be taken to ensure that you are not inadvertently creating a new thread every time a task needs to be performed. There is a steep deduction (see Section 4) if you are doing so. The thread pool needs methods that allow: (1) a spare worker thread to be retrieved and
(2) a worker thread to return itself to the pool after it has finished it task.
The thread pool manager also maintains a list of the work that it needs to perform. It maintains these work units in a FIFO queue implemented using the linked list data structure. Work units are added to the tail of this work queue and when spare workers are available, they are assigned work from the top of the queue.
Every 20 seconds, the server should print its current throughput (number of messages processed per second during last 20 seconds), the number of active client connections, and mean and standard deviation of per-client throughput to the console. In order to calculate the per-client throughput statistics (mean and standard deviation), you need to maintain the throughputs for individual clients for last 20 seconds (number of messages processed per second sent by a particular client during last 20 seconds) and calculate the mean and the standard deviation of those throughput values. This message should look like the following.
  [timestamp] Server Throughput: x messages/s, Active Client Connections: y, Mean Per- client Throughput: p messages/s, Std. Dev. Of Per-client Throughput: q messages/s
 You can use these statistics to evaluate the correctness of your program.
If your server is functioning correctly (assuming it is adequately provisioned), the server throughput should remain approximately constant throughout its operation. Note that it will take some time to reach a stable value due to initialization overheads at both server and client ends. Mean per-client throughput multiplied by the number of active connections should be approximately equal to the sever throughput. Furthermore, if every client is sending messages at the same rate and the server’s scheduling algorithm is fair, the standard deviation of the per-client throughput should be low.
  Similarly, once every 20 seconds after starting up, every client should print the number of messages it has sent and received during the last 20 seconds. This log message should look similar to the following.
[timestamp] Total Sent Count: x, Total Received Count: y
# 3 Command line arguments for the two components
Your classes should be organized in a package called cs455.scaling. The command-line arguments and the order in which they should be specified for the Server and the Client are listed below
  
     java cs455.scaling.server.Server portnum thread-pool-size 

     java cs455.scaling.client.Client server-host server-port message-rate
