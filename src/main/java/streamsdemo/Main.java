package streamsdemo;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        final KafkaStreams streams = setupStreams();
        final Health health = setupHealthEndpoint(streams);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread("shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                health.stop();
                latch.countDown();
            }
        });

        try {
            streams.start();
            health.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    private static KafkaStreams setupStreams() throws Exception {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streamsdemo");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, getEnvOr("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        final StreamsBuilder builder = new StreamsBuilder();
        builder.<String, String>stream(getEnvOr("INPUT_TOPIC_NAME", "streamsdemo-input"))
                .flatMapValues(Main::convert)
                .to(getEnvOr("OUTPUT_TOPIC_NAME", "streamsdemo-output"));

        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        StreamsStatus.register(streams);

        return streams;
    }

    private static List<String> convert(String input) {
        return Arrays
                .stream(input.split("\\W+"))
                .map(s -> s + "!")
                .collect(Collectors.toList());
    }

    private static Health setupHealthEndpoint(KafkaStreams streams) throws Exception {
        String healthPortStr = getEnvOr("HEALTH_PORT", "");
        if (healthPortStr.isEmpty()) {
            return new Health.NoHealth();
        }

        String host = getEnvOr("HEALTH_HOST", "0.0.0.0");
        int port = Integer.parseInt(healthPortStr);
        InetSocketAddress address = new InetSocketAddress(host, port);
        return new SunHttpServerHealth(streams, address);
    }

    private static String getEnvOr(String name, String alternative) {
        final String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            return alternative;
        }
        return value;
    }
}
