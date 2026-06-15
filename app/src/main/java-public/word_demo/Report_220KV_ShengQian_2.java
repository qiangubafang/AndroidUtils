package word_demo;

/**
 * @Description TODO
 * <p>
 * Created by 千古八方 on 2026/6/15.
 * Copyright (c) 2026 千古八方 All rights reserved.
 */


import org.tcshare.poi.data.PictureRenderData;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description TODO
 * <p>
 * Created by 千古八方 on 2026/4/20.
 * Copyright (c) 2026 千古八方 All rights reserved.
 *
 * 括号内为模板变化内容
 */

@Setter
@Getter
public class Report_220KV_ShengQian_2 {

    // 检测单位： [国网潍坊供电公司智能运检中心]
    private String testOrg = "国网潍坊供电公司智能运检中心";
    // 单位地址: [山东省潍坊市奎文区潍安路8000号]
    private String orgAddr = "山东省潍坊市奎文区潍安路8000号";
    // 联系人/电话: [李兰潺/19846935206]
    private String orgContact = "李兰潺/19846935206";
    // 检测内容： 输电线路[带电]绝缘子零值检测
    private String testType = "带电";


    // 发布日期，报告发布日期。 [2025年11月14日]
    private String reportDate = "2025年11月14日";

    // 公司名称
    private String companyName = "山东海恩德智能科技有限公司";
    // 公司名称英文
    private String companyNameEn = "Shandong High End Intelligent Technology Co. Ltd.";

    // 检验日期， 开始
    private String checkDateStart = "2025年11月13日";
    // 检验日期， 结束
    private String checkDateEnd = "2025年11月13日";
    // 检测绝缘子总数量(汇总后的总数量)
    private String checkTotalNum = "28";
    // 检测绝缘子劣化数量(汇总后的总数量)
    private String checkTotalErrorNum = "0";
    // 检测绝缘子外观[未]发现绝缘子外观脏污
    private String foundDirty = "未";
    // 线路名称： [220kV胜前II线]
    private String circuitName = "220kV胜前II线";

    // 3.1 检测参数
    private String paramInsulatorType = "双联"; // 绝缘子串型
    private String paramMountType = "单挂点"; // 挂点形式
    private String paramPhaseNum = "2"; // 每相/极串数量
    private String paramOneSideNum = "15"; // 每串片数
    private String paramCategory = "防污双伞"; // 普通或防污
    private String paramMaterial = "瓷质"; // 绝缘子材料
    private String paramHeight = "160"; // 结构高度(mm)
    private String paramDiameter = "280"; // 盘(伞)径(mm)
    private String paramAntiFouling = "是"; // 是否喷涂防污闪材料


    // 5.5.3无人机吊装机器人 ; 图片指定宽高
    private PictureRenderData imgDiaozhuang;
    // 机器人落串;
    private PictureRenderData imgLuoChuan;
    // 机器人安放及调试;
    private PictureRenderData imgPutDebug;
    // 机器人检测作业图片;
    private PictureRenderData imgWorkCheck;

    // 6.1 检测结果：机器人现场检测图片； 多个
    private List<PictureRenderData> imgCheckSite;
    // 6.1 检测结果；多个
    private List<CheckResult> temp_check_result;
    // 6.2 检测结果；多个表格
    private List<CheckResultTable> temp_check_result_table;



    //------------ 6.3 汇总表内容 -----------
    private List<CheckResultTableSum> checkResultSum;



    @Setter
    @Getter
    public static class BaseCheckResult {
        // 第几个检查结果
        private int checkCount = 1;
        // 线路名称： [220kV胜前II线]
        private String circuitName = "220kV胜前II线";
        // 塔杆号：[010]
        private String taganNumber = "010";
        // 检查日期
        private String checkDate = "2025.11.28";
    }
    @Setter
    @Getter
    public static class CheckResult extends BaseCheckResult{
        // 绝缘子串组装类型
        private String checkItemGroupType = "耐张双联";
        // 检查的绝缘子类型
        private String checkItemType = "防污双伞";
        // 检测结果图片； 多张照片
        private List<PictureRenderData> imgCheckResult;
        // 单次检测数量
        private String checkNum = "28";
        // 单次检测出低值片数量
        private String checkErrorNum = "0";
    }
    @Setter
    @Getter
    public static class CheckResultTable extends BaseCheckResult {
        // 检查日期,仅月份和日
        private String checkDateSimple = "11月28日";
        // 检测环境信息: 温度/℃ [4]
        private String checkTemp = "4";
        // 检测环境信息: 湿度/% [32]
        private String checkHum = "32";
        // 检查的绝缘子类型
        private String checkItemType = "防污双伞";
        // 小号侧型号
        private String checkItemModeSmall = "XWP2-120";
        // 大号侧型号
        private String checkItemModeBig = "XWP2-120";
        // 安装相别 小：上/中/下  大：上/中/下 ， 第一个格子开始，两个合并的；
        // 水平的，是左中右， 垂直的：上中下，
        private String posName0 = "上"; // 上 / 左
        private String posName2 = "下"; // 下 / 右

        // 大小侧 x 上中下 x  左右 = 12 组可能； [1-12]
        // 测量的到的数据长度
        private List<CheckValue> checkValue = new ArrayList<>();


    }
    // 12个结果
    @Setter
    @Getter
    public static class CheckValue {
        private int num; // 序号
        private String value1;
        private String value2;
        private String value3;
        private String value4;
        private String value5;
        private String value6;
        private String value7;
        private String value8;
        private String value9;
        private String value10;
        private String value11;
        private String value12;

        public CheckValue(int num) {
            this.num = num;
        }
    }


    @Setter
    @Getter
    public static class CheckResultTableSum extends CheckResultTable{
        // 按照此顺序输出List
        //1	[circuitName]	[checkDateSimple]	[taganNumber]	[checkTemp]	[checkHum]
        // [paramOneSideNum]	[checkNum]	[checkErrorNum]	[checkErrorPos]
        // 单串片数
        private String paramOneSideNum = "15";
        // 单次检测数量
        private String checkNum = "28";
        // 单次检测出低值片数量
        private String checkErrorNum = "0";
        // 低值片位置
        private String checkErrorPos = "无";

    }
}

