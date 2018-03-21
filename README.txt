CS455 - HW2 - Programming Component
SCALABLE SERVER DESIGN: USING THREAD POOLS TO MANAGE AND LOAD BALANCE ACTIVE NETWORK 

Name - Athith Amarnath
CSUID - 830715061

To make the project please use the following command

	make

To clean the project please use the following command 

	make clean

To run the Server, please run the following command 
	
	java cs455.scaling.server.Server <portno> <number of threads in the thread pool>
	
	The portno arguement is the command line arguement to give the port number for the server to start its operations.

To run the clients, I used the following script in start.sh. This script takes in a arguement - the number of clients per node.

    CLASSES=/s/chopin/b/grad/athitha/CS455/CS455_P2/src
    SCRIPT="cd $CLASSES; java cs455.scaling.client.Client santa-fe 5000 5"

    #$1 is the command-line argument
    for ((j = 1; j <= $1; j++));
    do
        COMMAND='gnome-terminal'
        for i in `cat machine_list`
        do
           echo 'logging into '$i
           OPTION='--tab -e "ssh -t '$i' '$SCRIPT'"'
        COMMAND+=" $OPTION"
        done
    eval $COMMAND&
    done

to stop the clients, I used the following script in stop.sh

for i in `cat machine_list`
do
    ssh $i "killall -u $USER java"
done


Assumptions - 
1. With 100 clients and 5 messages per client per message, the server throughput is about 499.xx/s. This is because of the threads recording the statistics and the server/ client thread itself is not in sync.
2. Also, the load of the clients should be taken into consideration. Sometimes, the clients sents 1 less than the required messages per second. More number of clients per 20 seconds would add to the difference.
3. The system starts incrementally and this design does not assume that all clients start their operations at the same time.




