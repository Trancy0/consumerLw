package com.consumerlw.core.service;

import cn.hutool.json.JSONObject;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ConsumerExeService {

    public boolean dealMsg(List<MessageExt> params);
}
