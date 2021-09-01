package com.consumerlw.core.mq.config;

import com.consumerlw.core.mq.bean.MessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(RocketMqProperties.class)
@Slf4j
public class RocketMQConfiguration {

    @Autowired
    private RocketMqProperties rocketMqProperties;

    @Autowired
    private ApplicationEventPublisher publisher = null;


    private static boolean isFirstSub = true;
    private static  long startTime = System.currentTimeMillis();

    /**
     * 容器初始化  打印参数
     */
    @PostConstruct
    public void init(){
        log.info("rocketMq 配置信息打印:");
        rocketMqProperties.toString();
    }

    /**
     * 创建普通消息发送实例
     *
     * @return
     */
    @Bean
    public DefaultMQProducer defaultProducer() throws MQClientException{
        DefaultMQProducer producer = new DefaultMQProducer(rocketMqProperties.getProducerGroupName());
        producer.setNamesrvAddr(rocketMqProperties.getNamesrvAddr());
//        producer.setInstanceName(rocketMqProperties.getProducerInstanceName());
        producer.setVipChannelEnabled(false);
        producer.setRetryTimesWhenSendAsyncFailed(10);
        producer.start();
        log.info("ROCKET MQ Defaultproducer server 已经启动");

        return producer;
    }

    /**
     * 创建支持事务消息发送的实例
     *
     * @return
     */
    public TransactionMQProducer transactionProducer() throws MQClientException {
        TransactionMQProducer producer = new TransactionMQProducer(rocketMqProperties.getTransactionProducerGroupName());
        producer.setNamesrvAddr(rocketMqProperties.getNamesrvAddr());
//        producer.setInstanceName(rocketMqProperties.getProducerTranInstanceName());
        producer.setRetryTimesWhenSendFailed(10);
        producer.start();

        log.info("ROCKET MQ  TransactionMQProducer server 已经启动");
        return producer;
    }

    /**
     * 创建消息消费的实例
     * @return
     * @throws MQClientException
     */
    @Bean
    public DefaultMQPushConsumer pushConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(rocketMqProperties.getConsumerGroupName());
        consumer.setNamesrvAddr(rocketMqProperties.getNamesrvAddr());
//        consumer.setInstanceName(rocketMqProperties.getConsumerInstanceName());
        //判断是否是广播模式
        if (rocketMqProperties.isConsumerBroadcasting()) {
            consumer.setMessageModel(MessageModel.BROADCASTING);
        }
        //设置批量消费
        consumer.setConsumeMessageBatchMaxSize(rocketMqProperties.getComsumerBatchMaxSize() == 0 ? 1 : rocketMqProperties
                .getComsumerBatchMaxSize());

        //获取topic和tag
        List<String> subscribeList = rocketMqProperties.getSubscribe();
        for (String sunscribe : subscribeList) {
            consumer.subscribe(sunscribe.split(":")[0], sunscribe.split(":")[1]);
        }

        // 顺序消费
        if (rocketMqProperties.isEnableOrderConsumer()) {
            consumer.registerMessageListener(new MessageListenerOrderly() {
                @Override
                public ConsumeOrderlyStatus consumeMessage(
                        List<MessageExt> msgs, ConsumeOrderlyContext context) {
                    try {
                        context.setAutoCommit(true);
                        msgs = filterMessage(msgs);
                        if (msgs.size() == 0)
                            return ConsumeOrderlyStatus.SUCCESS;
                        publisher.publishEvent(new MessageEvent(msgs, consumer));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                    }
                    return ConsumeOrderlyStatus.SUCCESS;
                }
            });
        }
        // 并发消费
        else {

            consumer.registerMessageListener(new MessageListenerConcurrently() {

                @Override
                public ConsumeConcurrentlyStatus consumeMessage(
                        List<MessageExt> msgs,
                        ConsumeConcurrentlyContext context) {
                    try {
                        //过滤消息
                        msgs = filterMessage(msgs);
                        if (msgs.size() == 0)
                            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                        publisher.publishEvent(new MessageEvent(msgs, consumer));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);

                    try {
                        consumer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    log.info("rocketmq consumer server is starting....");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();

        return consumer;
    }

    /**
     * 消息过滤
     * @param msgs
     * @return
     */
    private List<MessageExt> filterMessage(List<MessageExt> msgs) {
        if (isFirstSub && !rocketMqProperties.isEnableHistroryConsumer()) {
            msgs = msgs.stream()
                    .filter(item -> startTime - item.getBornTimestamp() < 0)
                    .collect(Collectors.toList());
        }
        if (isFirstSub && msgs.size() > 0) {
            isFirstSub = false;
        }
        return msgs;
    }
}


