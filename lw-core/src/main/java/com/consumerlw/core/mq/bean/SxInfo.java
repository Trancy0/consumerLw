package com.consumerlw.core.mq.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SxInfo {
    private String sourceReferNo;//事项库事项唯一码
    private String matterCode;//事项码
    private String pcUrl;//pc办理地址
    private String appUrl;//无线办理地址
    private String padUrl;//pad办理地址
    private String offlineUrl;//线下大厅办理地址


}
