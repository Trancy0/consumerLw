package com.consumerlw.core.mq.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.List;

@PropertySource(value = "classpath:rocketmq.properties")
@ConfigurationProperties(prefix = "lw.rocketmq")
@Configuration
@Setter
@Getter
@ToString
@Accessors(chain = true)
public class RocketMqProperties {

    private String namesrvAddr;
    private String producerGroupName;
    private String transactionProducerGroupName;
    private String consumerGroupName;
    private String producerInstanceName;//指定不同集群
    private String consumerInstanceName;
    private  String producerTranInstanceName;
    private int comsumerBatchMaxSize;
    private boolean consumerBroadcasting;
    private boolean enableHistroryConsumer;
    private boolean enableOrderConsumer;
    private List<String> subscribe = new ArrayList<String>();

}
