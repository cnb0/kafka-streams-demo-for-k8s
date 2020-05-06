package streamsdemo;

import org.apache.kafka.streams.KafkaStreams;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public interface StreamsStatusMBean {
    String getStatus();
    boolean getIsReady();
}

class StreamsStatus implements StreamsStatusMBean {

    private final KafkaStreams streams;

    private StreamsStatus(KafkaStreams streams) {
        this.streams = streams;
    }

    public String getStatus() {
        return this.streams.state().name();
    }

    public boolean getIsReady() {
        return this.streams.state().isRunning();
    }

    public static void register(KafkaStreams streams) throws Exception {
        ObjectName objectName = new ObjectName("streamsdemo:type=status");
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        StreamsStatusMBean statusMBean = new StreamsStatus(streams);
        server.registerMBean(statusMBean, objectName);
    }
}
