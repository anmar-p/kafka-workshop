# kafka-workshop

Note: when talking about Apache image, it is meant version 3.8.1

1. Go to `docker` folder and run the docker compose file
```bash
cd docker
docker compose up -d
```
This will spin up 3 containers: zookeeper, kafka and kowl  
Zookeeper is there to:  
- help the kafka brokers stay in sync (ensures brokers know each other's state)
- elect which broker will be the controller (broker that manages which partitions will be leaders and which followers)
- track brokers' status (and reelects controller if fails)
- store cluster metadata (broker info, partition leader/followers, topics names/partitions/replicas)  

Kafka server:
- stores topic-partition messages (in log files)
- handles message distribution across partitions
- handles data replication (across multiple brokers ensuring availability even when broker fails)
- handles message retention
- elects partition leaders and distributes them across brokers
- handles communication with clients (producers/consumers)
- is responsible for authentication/authorization
- coordinates the consumer groups (assigns partitions, handles rebalancing, commits offsets, monitors consumers' health)

Kowl:
- offers a UI to visualise the cluster and messages

2. You can see the logs in Docker Dashboard, but you can also exec into the container:
```bash
docker exec -it <container_id> /bin/sh
cd /
```

Here we can see the `bin` folder with all the command scripts (`opt/kafka/bin` if Apache image)
Inside `etc/kafka`, there are the different properties files (`opt/kafka/config` for Apache)
Inside `/var/lib/kafka/data`, kafka saves the messages (`/tmp/kraft-combined-logs` for Apache)


3. Create a topic
```bash
bin/kafka-topics --bootstrap-server localhost:9092 --create --topic kafka-workshop-topic --partitions 1 --replication-factor 1   
(Apache) 
bin/kafka-topics.sh --create --topic kafka-workshop-topic --bootstrap-server localhost:9092
```
The max replication factor we can define, is the number of brokers we have

4. List all topics and describe the one we just created
```bash
bin/kafka-topics --bootstrap-server localhost:9092 --list
bin/kafka-topics --bootstrap-server localhost:9092 --describe --topic kafka-workshop-topic

(Apache)
bin/kafka-topics.sh --list --bootstrap-server localhost:9092
bin/kafka-topics.sh --describe --topic kafka-workshop-topic --bootstrap-server localhost:9092
```
If you look at the logs of Kafka, you will see that kafka has created something in
/var/lib/kafka/data. If you go there, you will see a folder with the name of our topic and a number, like
kafka-workshop-topic-0. This number is the partition. Partitions start from 0.
If you go in this folder, you will see a 00000000000000000.log file. This is where the events we send will be saved.

5. Let's send some data
To do that we need to create a producer. Open a new terminal and write this command:
```bash
bin/kafka-console-producer --broker-list localhost:9092 --topic kafka-workshop-topic
(Apache)
bin/kafka-console-producer.sh --topic kafka-workshop-topic --bootstrap-server localhost:9092
```
Now write anything you want and pres Enter. Send as many messages as you want

6. Where did they go?
Let's create a consumer (in a new terminal) and read them
```bash
bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic kafka-workshop-topic --from-beginning
(Apache)
bin/kafka-console-consumer.sh --topic kafka-workshop-topic --from-beginning --bootstrap-server localhost:9092
```
If you now go back to the 00000.log file of /var/lib/kafka/data, you will be able to see the events we sent.
They won't be fully readable, due to Kafka's internal storage format.

7. Now let's create a new Producer that sends Key-Value pairs
```bash
bin/kafka-console-producer --broker-list localhost:9092 --topic kafka-workshop-topic --property "parse.key=true" --property "key.separator=:"
(Apache)
bin/kafka-console-producer.sh --topic kafka-workshop-topic --bootstrap-server localhost:9092 --property "parse.key=true" --property "key.separator=:"
```
and an example event
```bash
key1: value1
```
and let's create a new consumer to read the messages:
```bash
bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic kafka-workshop-topic --from-beginning  --property "print.key=true" --property "key.separator=:"
(Apache)
bin/kafka-console-consumer.sh --topic kafka-workshop-topic --bootstrap-server localhost:9092  --property "print.key=true" --property "key.separator=:"
```
Send multiple events like the above, and use different keys, but make sure you send multiple events with the same key.

Example:
```bash
key1: value1_1
key2: value2_1
key3: value3_1
key2: value2_2
...
```
9. Oh wow, we are getting so many messages. Let's make an 'inspired' change and alter the topic to have more partitions. 
```bash
bin/kafka-topics --bootstrap-server localhost:9092 --alter --topic kafka-workshop-topic --partitions 3
bin/kafka-topics --describe --topic kafka-workshop-topic --bootstrap-server localhost:9092
(Apache)
bin/kafka-topics.sh --bootstrap-server localhost:9092 --alter --topic kafka-workshop-topic --partitions 3
bin/kafka-topics.sh --describe --topic kafka-workshop-topic --bootstrap-server localhost:9092

```
10. Now create 3 different consumers to monitor all these partitions
```bash
bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic kafka-workshop-topic --partition 0 --from-beginning  --property "print.key=true" --property "key.separator=:"
(Apache)
bin/kafka-console-consumer.sh --topic kafka-workshop-topic --partition 0 --bootstrap-server localhost:9092  --property "print.key=true" --property "key.separator=:" --from-beginnning
```
and let's keep sending messages
You will see that now the keys are distributed to the 3 partitions, but even though from now on same keys end in the same partition, 
the old keys remain in the first one. There goes the order (¬_¬”)

Missing offsets, consumer groups, high/low watermarks, reset


###Running Kafka Locally without Docker image

1. Download Kafka and extract it
```bash
https://dlcdn.apache.org/kafka/3.8.1/kafka_2.13-3.8.1.tgz
tar -xzf kafka_2.13-3.8.1.tgz
cd kafka_2.13-3.8.1
```
In the `bin` folder are all the scripts that we can use to directly interact with Kafka

2. Start Zookeeper in a new terminal (you can start it in the background, but keep it on if you want to see how zookeeper's logs look like)
```bash
bin/zookeeper-server-start.sh config/zookeeper.properties
```
3. In another terminal, start Kafka:
```bash
bin/kafka-server-start.sh config/server.properties
```
The config here define all of kafka's properties, like the address to zookeeper, log retention props,
whether to allow automatic creation of topics, and other general management properties

###Running Kafka Locally from a Docker image (best option when you don't have Java installed locally)

1.Download the latest Docker image of Kafka
```bash
docker pull apache/kafka:3.8.1
```
2. Start Kafka
```bash
docker run -p 9092:9092 apache/kafka:3.8.1
```
