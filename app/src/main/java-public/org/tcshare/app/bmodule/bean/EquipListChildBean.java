package org.tcshare.app.bmodule.bean;



public class EquipListChildBean{
    private final boolean isBtn;
    private final ResDevListBean.DataBean rawData;
    private String name;

    /**
     * state02	字符串	A相状态
     * 0——取下
     * 1——挂线
     * state03	字符串	A相危险警告
     * 1——警告
     * 0——报警取消
     * state04	字符串	A相电压低报警
     * 1——警告
     * 0——报警取消
     * data0	字符串	A相电池电压（单位mv）
     * time_a	字符串	A相上报时间
     * @param rawData
     * @param phase
     * @param state02
     * @param state03
     * @param state04
     * @param data0
     * @param time_a
     */
    public EquipListChildBean(ResDevListBean.DataBean rawData, String phase, String state02, String state03, String state04, String data0, String time_a) {
        this.rawData = rawData;
        if ("btn".equals(phase)) {
            isBtn = true;
        }else if("主机".equals(phase)){
            name = phase + ": " + data0 + " mV " + ("1".equals(state02) ? "出库" : "入库");
            isBtn = false;
        }else if("时间".equals(phase)){
            name = phase + ": " + time_a;
            isBtn = false;
        }else{
            name = phase + ": " + data0 + " mV " + ("1".equals(state02) ? "挂线" : "取下");
            isBtn = false;
        }
    }

    public String getName() {
        return name;
    }

    public boolean isBtn() {
        return isBtn;
    }

    public ResDevListBean.DataBean getRawData() {
        return rawData;
    }
}