package com.foodtraceability.agent.consensus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class PbftMessageQueue implements MessageListener {
    
    private static final Logger log = LoggerFactory.getLogger(PbftMessageQueue.class);
    private static final String CONSENSUS_CHANNEL = "consensus:queue";
    private static final String CONSENSUS_TOPIC = "consensus:*";
    
    private final BlockingQueue<PbftMessage> messageQueue;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final ObjectMapper objectMapper;
    
    public PbftMessageQueue(RedisTemplate<String, Object> redisTemplate,
                           RedisMessageListenerContainer listenerContainer) {
        this.messageQueue = new LinkedBlockingQueue<>(1000);
        this.redisTemplate = redisTemplate;
        this.listenerContainer = listenerContainer;
        this.objectMapper = new ObjectMapper();
        
        listenerContainer.addMessageListener(this, new PatternTopic(CONSENSUS_TOPIC));
        log.info("PBFT Message Queue initialized");
    }
    
    public void publishMessage(PbftMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(CONSENSUS_CHANNEL, json);
            log.debug("Published PBFT message: type={}, seq={}", 
                     message.getType(), message.getSequenceNumber());
        } catch (Exception e) {
            log.error("Failed to publish PBFT message", e);
        }
    }
    
    public PbftMessage takeMessage() throws InterruptedException {
        return messageQueue.poll(5, TimeUnit.SECONDS);
    }
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            byte[] body = message.getBody();
            if (body != null) {
                String json = new String(body);
                PbftMessage pbftMessage = objectMapper.readValue(json, PbftMessage.class);
                messageQueue.offer(pbftMessage);
                log.debug("Received PBFT message: type={}, seq={}", 
                         pbftMessage.getType(), pbftMessage.getSequenceNumber());
            }
        } catch (Exception e) {
            log.error("Failed to process PBFT message", e);
        }
    }
    
    public int getQueueSize() {
        return messageQueue.size();
    }
}
