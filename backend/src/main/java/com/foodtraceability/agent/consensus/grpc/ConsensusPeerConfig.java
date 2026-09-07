package com.foodtraceability.agent.consensus.grpc;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "consensus")
public class ConsensusPeerConfig {

    private boolean grpcEnabled = false;

    private boolean grpcServerStart = false;

    private int grpcServerPort = 50051;

    private String agentId = "ca-agent-0";

    private String agentRole = "PRIMARY";

    private List<Peer> peers = new ArrayList<>();

    @Getter
    @Setter
    public static class Peer {
        private String id;
        private String host;
        private int grpcPort;

        public String address() {
            return host + ":" + grpcPort;
        }
    }
}
