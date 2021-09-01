package com.consumerlw.core.mq.consumer;

import java.util.List;

import com.consumerlw.core.mq.bean.MessageEvent;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 监听消息进行消费
 */
@Component
public class ConsumerService {
    @EventListener(condition = "#event.msgs[0].topic=='Test_topic' ")
    public void rocketmqMsgListener(MessageEvent event) {
        try {
            List<MessageExt> msgs = event.getMsgs();
            for (MessageExt msg : msgs) {
                System.err.println("消费消息:"+new String(msg.getBody()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}