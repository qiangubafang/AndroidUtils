package org.tcshare.app.bmodule.bean;


import android.text.TextUtils;


public class EquipListBean {
    private final ResDevListBean.DataBean item;
    public String name;
    public String type;

    public EquipListBean(ResDevListBean.DataBean d) {
        this.item = d;
        name = "[ " + d.getAddr4() + " ] : " + (TextUtils.isEmpty(d.getName()) ? d.getCode() : d.getName());

        if("0".equals(d.getType())){
            if("1".equals(d.getFour())){ // 是四相电
               type = "普通接地线(4相)";
            }else{
                type = "普通接地线(3相)";
            }

        }else if("1".equals(d.getType())){
            type = "个人安保线";
        }else if("2".equals(d.getType())){
            type = "地线接地线";
        }else if("3".equals(d.getType())){
            type = "直流接地线";
        }

    }

    public ResDevListBean.DataBean getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public ResDevListBean.DataBean getRawData() {
        return item;
    }
}