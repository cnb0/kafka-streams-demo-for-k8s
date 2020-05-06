package streamsdemo;

import com.sun.net.httpserver.HttpServer;
import org.apache.kafka.streams.KafkaStreams;

import java.io.IOException;
import java.net.InetSocketAddress;

class SunHttpServerHealth implements Health {
    private static final int OK_CODE = 201;
    private static final int FAIL_CODE = 500;
    private final KafkaStreams streams;
    private final InetSocketAddress serverAddress;
    private HttpServer server;

    SunHttpServerHealth(KafkaStreams streams, InetSocketAddress serverAddress) {
        this.streams = streams;
        this.serverAddress = serverAddress;
    }

    public void start() {
        try {
            this.server = HttpServer.create(this.serverAddress, 0);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to set up a health endpoint HTTP server", ex);
        }
        this.server.createContext("/health/live", exchange -> {
            exchange.sendResponseHeaders(OK_CODE, 0);
            exchange.close();
        });
        this.server.createContext("/health/ready", exchange -> {
            int responseCode  = this.streams.state().isRunning() ? OK_CODE : FAIL_CODE;
            exchange.sendResponseHeaders(responseCode, 0);
            exchange.close();
        });
        this.server.start();
    }

    public void stop() {
        if (this.server != null) {
            this.server.stop(0);
        }
    }
}
