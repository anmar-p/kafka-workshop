package com.workshops.kafka.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;


@Component
@Slf4j
@RequiredArgsConstructor
public class Consumer implements ApplicationRunner {

    @Autowired
    private final KafkaConsumer<String, String> consumer;
    private static final String TOPIC = "kafka-workshop-topic";

    public void start(){
        log.info("Starting to poll");
        consumer.subscribe(Collections.singleton(TOPIC));
        while(true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            records.forEach(record -> System.out.println("Headers: " + record.headers() + "\nKey: " + record.key() + "\nValue: " + record.value()));
        }

    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        start();
    }
}
