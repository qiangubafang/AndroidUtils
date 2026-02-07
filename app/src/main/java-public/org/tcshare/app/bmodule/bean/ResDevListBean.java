package org.tcshare.app.bmodule.bean;


import java.util.List;
import java.util.Objects;

public class ResDevListBean {


    private String code;
    private String time; // 服务器时间
    private List<DataBean> data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public static class DataBean{
        private String id;
        private String imei;
        private String name;
        private String code;
        private String type;
        private String lng;
        private String lat;
        private String addr;
        private String state02;
        private String state03;
        private String state04;
        private String state12;
        private String state13;
        private String state14;
        private String state22;
        private String state23;
        private String state24;
        private String state31;
        private String state33;
        private String state34;
        private String state42;
        private String state43;
        private String state44;
        private String data0;
        private String data1;
        private String data2;
        private String data3;
        private String data4;
        private String time;
        private String time_a;
        private String time_b;
        private String time_c;
        private String time_d;
        private String four;
        private String addr1;
        private String addr2;
        private String addr3;
        private String addr4;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DataBean dataBean = (DataBean) o;
            return id.equals(dataBean.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getAddr() {
            return addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        public String getState02() {
            return state02;
        }

        public void setState02(String state02) {
            this.state02 = state02;
        }

        public String getState03() {
            return state03;
        }

        public void setState03(String state03) {
            this.state03 = state03;
        }

        public String getState04() {
            return state04;
        }

        public void setState04(String state04) {
            this.state04 = state04;
        }

        public String getState12() {
            return state12;
        }

        public void setState12(String state12) {
            this.state12 = state12;
        }

        public String getState13() {
            return state13;
        }

        public void setState13(String state13) {
            this.state13 = state13;
        }

        public String getState14() {
            return state14;
        }

        public void setState14(String state14) {
            this.state14 = state14;
        }

        public String getState22() {
            return state22;
        }

        public void setState22(String state22) {
            this.state22 = state22;
        }

        public String getState23() {
            return state23;
        }

        public void setState23(String state23) {
            this.state23 = state23;
        }

        public String getState24() {
            return state24;
        }

        public void setState24(String state24) {
            this.state24 = state24;
        }

        public String getState31() {
            return state31;
        }

        public void setState31(String state31) {
            this.state31 = state31;
        }

        public String getState33() {
            return state33;
        }

        public void setState33(String state33) {
            this.state33 = state33;
        }

        public String getState34() {
            return state34;
        }

        public void setState34(String state34) {
            this.state34 = state34;
        }

        public String getState42() {
            return state42;
        }

        public void setState42(String state42) {
            this.state42 = state42;
        }

        public String getState43() {
            return state43;
        }

        public void setState43(String state43) {
            this.state43 = state43;
        }

        public String getState44() {
            return state44;
        }

        public void setState44(String state44) {
            this.state44 = state44;
        }

        public String getData0() {
            return data0;
        }

        public void setData0(String data0) {
            this.data0 = data0;
        }

        public String getData1() {
            return data1;
        }

        public void setData1(String data1) {
            this.data1 = data1;
        }

        public String getData2() {
            return data2;
        }

        public void setData2(String data2) {
            this.data2 = data2;
        }

        public String getData3() {
            return data3;
        }

        public void setData3(String data3) {
            this.data3 = data3;
        }

        public String getData4() {
            return data4;
        }

        public void setData4(String data4) {
            this.data4 = data4;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getTime_a() {
            return time_a;
        }

        public void setTime_a(String time_a) {
            this.time_a = time_a;
        }

        public String getTime_b() {
            return time_b;
        }

        public void setTime_b(String time_b) {
            this.time_b = time_b;
        }

        public String getTime_c() {
            return time_c;
        }

        public void setTime_c(String time_c) {
            this.time_c = time_c;
        }

        public String getTime_d() {
            return time_d;
        }

        public void setTime_d(String time_d) {
            this.time_d = time_d;
        }

        public String getFour() {
            return four;
        }

        public void setFour(String four) {
            this.four = four;
        }

        public String getAddr1() {
            return addr1;
        }

        public void setAddr1(String addr1) {
            this.addr1 = addr1;
        }

        public String getAddr2() {
            return addr2;
        }

        public void setAddr2(String addr2) {
            this.addr2 = addr2;
        }

        public String getAddr3() {
            return addr3;
        }

        public void setAddr3(String addr3) {
            this.addr3 = addr3;
        }

        public String getAddr4() {
            return addr4;
        }

        public void setAddr4(String addr4) {
            this.addr4 = addr4;
        }


        private int selfID = -1; // 图标分类缓存用


        private int getSelfID(){
            int ret = -1;
            //g: 1 , r:2, y:3
            if ("0".equals(getType())) {// 有一项不一样，黄色， 全取下0 绿色， 全挂上1 红色
                if ("1".equals(getFour())) { // 是四相电
                    if ("0".equals(getState02()) && "0".equals(getState12()) && "0".equals(getState22()) && "0".equals(getState42())) {
                        ret = 410000;
                    } else if ("1".equals(getState02()) && "1".equals(getState12()) && "1".equals(getState22()) && "1".equals(getState42())) {
                        ret = 420000;
                    } else {
                        ret = 430000;
                    }
                } else {
                    if ("0".equals(getState02()) && "0".equals(getState12()) && "0".equals(getState22())) {
                        ret = 310000;
                    } else if ("1".equals(getState02()) && "1".equals(getState12()) && "1".equals(getState22())) {
                        ret = 320000;
                    } else {
                        ret = 330000;
                    }
                }

            } else if ("1".equals(getType())) {
                if ("0".equals(getState02())) {
                    ret = 110000;
                } else if ("1".equals(getState02())) {
                    ret = 120000;
                } else {
                    ret = 130000;
                }
            } else if ("2".equals(getType())) {
                if ("0".equals(getState02())) {
                    ret = 110000;
                } else if ("1".equals(getState02())) {
                    ret = 120000;
                } else {
                    ret = 130000;
                }
            } else if ("3".equals(getType())) {
                if ("0".equals(getState02()) && "0".equals(getState12())) {
                    ret = 210000;
                } else if ("1".equals(getState02()) && "1".equals(getState12())) {
                    ret = 220000;
                } else {
                    ret = 230000;
                }
            }
            return ret;
        }

    }
}
