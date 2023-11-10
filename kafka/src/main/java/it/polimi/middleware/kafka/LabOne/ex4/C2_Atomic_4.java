package it.polimi.middleware.kafka.LabOne.ex4;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class C2_Atomic_4 {
    private static final String defaultConsumerGroupId = "g4";
    private static final String defaultInputTopic = "ex4";
    private static final String defaultOutputTopic = "ex4_result";

    private static final String serverAddr = "localhost:9092";
    private static final String producerTransactionalId = "forwarderTransactionalId";

    // Default is "latest": try "earliest" instead
    private static final String offsetResetStrategy = "earliest";

    // Save the number of messages received for each key
    private static final Map<String, Integer> mess_per_key = new HashMap<>();

    public static void main(String[] args) {
        // If there are arguments, use the first as group, the second as input topic, the third as output topic.
        // Otherwise, use default group and topics.
        String consumerGroupId = args.length >= 1 ? args[0] : defaultConsumerGroupId;
        String inputTopic = args.length >= 2 ? args[1] : defaultInputTopic;
        String outputTopic = args.length >= 3 ? args[2] : defaultOutputTopic;

        // Consumer
        final Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        // The consumer does not commit automatically, but within the producer transaction
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(false));
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(inputTopic));

        // Producer
        final Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, producerTransactionalId);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));

        final KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);
        producer.initTransactions();

        while (true) {
            final ConsumerRecords<String, String> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            producer.beginTransaction();

            Map<String, Integer> modifiedKeys = new HashMap<>();

            for (final ConsumerRecord<String, String> record : records) {
                System.out.println("Partition: " + record.partition() +
                        "\tOffset: " + record.offset() +
                        "\tKey: " + record.key() +
                        "\tValue: " + record.value()
                );
                if (modifiedKeys.containsKey(record.key())) {
                    modifiedKeys.put(record.key(), modifiedKeys.get(record.key()) + 1);
                } else {
                    modifiedKeys.put(record.key(), 1);
                }
            }

            List<Future<RecordMetadata>> futures = new ArrayList<>();
            for (String key : modifiedKeys.keySet()) {
                int value = mess_per_key.get(key) + modifiedKeys.get(key);
                futures.add(producer.send(new ProducerRecord<>(outputTopic, key, Integer.toString(value))));
            }
            // wait for all the futures to be completed
            for (Future<RecordMetadata> future : futures) {
                try {
                    RecordMetadata ack = future.get();
                    System.out.println("Ack for topic " + ack.topic() + ", partition " + ack.partition() + ", offset " + ack.offset());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    producer.flush();
                    /*! Aborts */
                    producer.abortTransaction();
                }
            }

            // The producer manually commits the offsets for the consumer within the transaction
            final Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
            for (final TopicPartition partition : records.partitions()) {
                final List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                final long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                map.put(partition, new OffsetAndMetadata(lastOffset + 1));
            }

            // update the number of messages received for each key
            for (String key : modifiedKeys.keySet()) {
                if (mess_per_key.containsKey(key)) {
                    mess_per_key.put(key, mess_per_key.get(key) + modifiedKeys.get(key));
                } else {
                    mess_per_key.put(key, modifiedKeys.get(key));
                }
            }

            producer.sendOffsetsToTransaction(map, consumer.groupMetadata());
            producer.commitTransaction();
        }
    }

}