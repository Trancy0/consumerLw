package com.consumerlw.core.mq.producer;

import com.alibaba.fastjson.JSON;
import com.consumerlw.core.mq.bean.SxInfo;
import com.consumerlw.core.mq.bean.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class ProducerController {
    @Autowired
    private DefaultMQProducer defaultProducer;
//    @Autowired
//    private TransactionMQProducer transactionMQProducer;

    /**
     * 发送普通消息
     */
    @GetMapping("/sendMessage")
    public void sendMsg() {

        for(int i=0;i<10;i++){
//            User user = new User();
//            user.setId(String.valueOf(i));
//            user.setUsername("jhp"+i);
            SxInfo sxInfo = new SxInfo();
            sxInfo.setSourceReferNo("事项库唯一编码"+i);
            sxInfo.setMatterCode("事项码");
            sxInfo.setAppUrl("appurl");
            sxInfo.setOfflineUrl("线下办理地址");
            sxInfo.setPadUrl("pad办理地址");
            sxInfo.setPcUrl("pcUrl");
            String json = JSON.toJSONString(sxInfo);

            Message msg = new Message("Test_topic","TagA",json.getBytes());
            try {
                SendResult result = defaultProducer.send(msg);
                log.info("发送消息id:"+result.getMsgId()+":"+","+"发送状态:"+result.getSendStatus());
                log.info("同步事项:"+sxInfo.getSourceReferNo());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 发送事务消息
     * @return
     */
//    @GetMapping("/sendTransactionMess")
//    public String sendTransactionMsg() {
//        SendResult sendResult = null;
//        try {
//            // a,b,c三个值对应三个不同的状态
//            String ms = "c";
//            Message msg = new Message("user-topic","white",ms.getBytes());
            // 发送事务消息
//            sendResult = transactionProducer.sendMessageInTransaction(msg, (Message msg1, Object arg) {
//                String value = "";
//                if (arg instanceof String) {
//                    value = (String) arg;
//                }
//                if (value == "") {
//                    throw new RuntimeException("发送消息不能为空...");
//                } else if (value =="a") {
//                    return LocalTransactionState.ROLLBACK_MESSAGE;
//                } else if (value =="b") {
//                    return LocalTransactionState.COMMIT_MESSAGE;
//                }
//                return LocalTransactionState.ROLLBACK_MESSAGE;
//            }, 4);
//            System.out.println(sendResult);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return sendResult.toString();
//    }

    /**
     * 支持顺序发送消息
     */
    @GetMapping("/sendMessOrder")
    public void sendMsgOrder() {
        for(int i=0;i<10;i++) {
            User user = new User();
            user.setId(String.valueOf(i));
//            user.setUsername("jhp" + i);
            String json = JSON.toJSONString(user);
            Message msg = new Message("Test_topic","TagA", json.getBytes());
            try{
                defaultProducer.send(msg, new MessageQueueSelector() {
                    @Override
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                        int index = ((Integer) arg) % mqs.size();
                        return mqs.get(index);
                    }
                },i);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
