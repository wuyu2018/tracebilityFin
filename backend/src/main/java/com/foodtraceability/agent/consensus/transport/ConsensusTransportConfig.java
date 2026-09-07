package com.foodtraceability.agent.consensus.transport;

import com.foodtraceability.agent.consensus.grpc.ConsensusPeerConfig;
import com.foodtraceability.agent.contract.DataOnChainContract;
import com.foodtraceability.agent.contract.PermissionControlContract;
import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.service.BlockchainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class ConsensusTransportConfig {

    private final ConsensusPeerConfig peerConfig;
    private final PermissionControlContract permissionControlContract;
    private final DataOnChainContract dataOnChainContract;
    private final BlockchainService blockchainService;

    public ConsensusTransportConfig(ConsensusPeerConfig peerConfig,
                                     PermissionControlContract permissionControlContract,
                                     DataOnChainContract dataOnChainContract,
                                     BlockchainService blockchainService) {
        this.peerConfig = peerConfig;
        this.permissionControlContract = permissionControlContract;
        this.dataOnChainContract = dataOnChainContract;
        this.blockchainService = blockchainService;
    }

    @Bean
    public ConsensusTransport consensusTransport(@Lazy MultiAgentCoordinator agentCoordinator) {
        if (peerConfig.isGrpcEnabled()) {
            return new GrpcConsensusTransport(
                    peerConfig, permissionControlContract, dataOnChainContract,
                    null, null, null);
        }
        return new InProcessConsensusTransport(
                () -> agentCoordinator.getAllAgents().stream().toList(),
                permissionControlContract,
                dataOnChainContract,
                blockchainService);
    }
}
