package com.consumerlw.core.serviceImpl;

import cn.hutool.json.JSONObject;
import com.consumerlw.core.service.ConsumerExeService;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConsumerExeServiceImpl implements ConsumerExeService {

    public boolean dealMsg(List<MessageExt> params){

        //执行消费逻辑对 收到的消息进行消费
//          Boolean reqFlag = hutoolHttpUtil.postReq( new JSONObject());

          return true;
    }

}
