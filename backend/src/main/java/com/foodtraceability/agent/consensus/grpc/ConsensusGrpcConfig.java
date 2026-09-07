package com.foodtraceability.agent.consensus.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
public class ConsensusGrpcConfig {

    private static final Logger log = LoggerFactory.getLogger(ConsensusGrpcConfig.class);

    private final ConsensusPeerConfig peerConfig;
    private final ConsensusServiceGrpcImpl consensusGrpcService;

    public ConsensusGrpcConfig(ConsensusPeerConfig peerConfig,
                                ConsensusServiceGrpcImpl consensusGrpcService) {
        this.peerConfig = peerConfig;
        this.consensusGrpcService = consensusGrpcService;
    }

    private Server grpcServer;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (peerConfig.isGrpcServerStart()) {
            try {
                grpcServer = ServerBuilder.forPort(peerConfig.getGrpcServerPort())
                        .addService(consensusGrpcService)
                        .build()
                        .start();
                log.info("gRPC server started on port {} (agent: {}, role: {})",
                        peerConfig.getGrpcServerPort(), peerConfig.getAgentId(), peerConfig.getAgentRole());

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (grpcServer != null) {
                        grpcServer.shutdown();
                        try {
                            grpcServer.awaitTermination(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }));
            } catch (IOException e) {
                log.error("Failed to start gRPC server", e);
            }
        }
    }
}
