package com.gameex.dw.justtalk;

import android.annotation.SuppressLint;

import com.gameex.dw.justtalk.jiguangIM.GlobalEventListener;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.SharedPreferenceUtil;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.multidex.MultiDexApplication;
import cn.jiguang.share.android.api.JShareInterface;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Message;

public class FlayChatApplication extends MultiDexApplication {
    private static final String TAG = "JGAPPLICATION";
    /**
     * 极光AppKey
     */
    public static final String APP_KEY = "fa964c46085d5543e75797c0";
    /**
     * 微信appId
     */
    public static final String WE_CHAT_APP_ID = "wx440625ae89c4244d";
    /**
     * 各大银行对应编号
     */
    public static final HashMap<String, String> bankNumMap = new HashMap<>();
    /**
     * 银行类型对应编号
     */
    public static final HashMap<String, String> bankTypeMap = new HashMap<>();

    public static final String CONV_TITLE = "conv_title";
    public static final int IMAGE_MESSAGE = 1;
    public static final int TAKE_PHOTO_MESSAGE = 2;
    public static final int TAKE_LOCATION = 3;
    public static final int FILE_MESSAGE = 4;
    public static final int TACK_VIDEO = 5;
    public static final int TACK_VOICE = 6;
    public static final int BUSINESS_CARD = 7;
    public static final int REQUEST_CODE_SEND_FILE = 26;

    public static final int RESULT_CODE_ALL_MEMBER = 22;
    @SuppressLint("UseSparseArrays")
    public static Map<Long, Boolean> isAtMe = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    public static Map<Long, Boolean> isAtAll = new HashMap<>();
    public static List<Message> forWardMsg = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i("IMDebugApplication", "init");
//        JMessageClient.setDebugMode(true);  //极光im调式模式
        JMessageClient.init(getApplicationContext(), true); //极光im sdk初始化，并启动消息漫游
        JPushInterface.init(getApplicationContext());   //极光推送sdk初始化
        JShareInterface.init(getApplicationContext());  //极光社会化分享sdk初始化
//        JShareInterface.setDebugMode(true); //极光社会化分享调试模式
        JMessageClient.registerEventReceiver(
                new GlobalEventListener(getApplicationContext()));

        EmojiManager.install(new TwitterEmojiProvider());

        //初始化缓存工具类
        SharedPreferenceUtil.getInstance(this, null);

        initMap();
    }

    /**
     * 初始化银行卡map数据
     */
    private void initMap() {
        bankNumMap.put("SRCB", "深圳农村商业银行");
        bankNumMap.put("BGB", "广西北部湾银行");
        bankNumMap.put("SHRCB", "上海农村商业银行");
        bankNumMap.put("BJBANK", "北京银行");
        bankNumMap.put("WHCCB", "威海市商业银行");
        bankNumMap.put("BOZK", "周口银行");
        bankNumMap.put("KORLABANK", "库尔勒市商业银行");
        bankNumMap.put("SPABANK", "平安银行");
        bankNumMap.put("SDEB", "顺德农商银行");
        bankNumMap.put("HURCB", "湖北省农村信用社");
        bankNumMap.put("WRCB", "无锡农村商业银行");
        bankNumMap.put("BOCY", "朝阳银行");
        bankNumMap.put("CZBANK", "浙商银行");
        bankNumMap.put("HDBANK", "邯郸银行");
        bankNumMap.put("BOC", "中国银行");
        bankNumMap.put("BOD", "东莞银行");
        bankNumMap.put("CCB", "中国建设银行");
        bankNumMap.put("ZYCBANK", "遵义市商业银行");
        bankNumMap.put("SXCB", "绍兴银行");
        bankNumMap.put("GZRCU", "贵州省农村信用社");
        bankNumMap.put("ZJKCCB", "张家口市商业银行");
        bankNumMap.put("BOJZ", "锦州银行");
        bankNumMap.put("BOP", "平顶山银行");
        bankNumMap.put("HKB", "汉口银行");
        bankNumMap.put("SPDB", "上海浦东发展银行");
        bankNumMap.put("NXRCU", "宁夏黄河农村商业银行");
        bankNumMap.put("NYNB", "广东南粤银行");
        bankNumMap.put("GRCB", "广州农商银行");
        bankNumMap.put("BOSZ", "苏州银行");
        bankNumMap.put("HZCB", "杭州银行");
        bankNumMap.put("HSBK", "衡水银行");
        bankNumMap.put("HBC", "湖北银行");
        bankNumMap.put("JXBANK", "嘉兴银行");
        bankNumMap.put("HRXJB", "华融湘江银行");
        bankNumMap.put("BODD", "丹东银行");
        bankNumMap.put("AYCB", "安阳银行");
        bankNumMap.put("EGBANK", "恒丰银行");
        bankNumMap.put("CDB", "国家开发银行");
        bankNumMap.put("TCRCB", "江苏太仓农村商业银行");
        bankNumMap.put("NJCB", "南京银行");
        bankNumMap.put("ZZBANK", "郑州银行");
        bankNumMap.put("DYCB", "德阳商业银行");
        bankNumMap.put("YBCCB", "宜宾市商业银行");
        bankNumMap.put("SCRCU", "四川省农村信用");
        bankNumMap.put("KLB", "昆仑银行");
        bankNumMap.put("LSBANK", "莱商银行");
        bankNumMap.put("YDRCB", "尧都农商行");
        bankNumMap.put("CCQTGB", "重庆三峡银行");
        bankNumMap.put("FDB", "富滇银行");
        bankNumMap.put("JSRCU", "江苏省农村信用联合社");
        bankNumMap.put("JNBANK", "济宁银行");
        bankNumMap.put("CMB", "招商银行");
        bankNumMap.put("JINCHB", "晋城银行JCBANK");
        bankNumMap.put("FXCB", "阜新银行");
        bankNumMap.put("WHRCB", "武汉农村商业银行");
        bankNumMap.put("HBYCBANK", "湖北银行宜昌分行");
        bankNumMap.put("TZCB", "台州银行");
        bankNumMap.put("TACCB", "泰安市商业银行");
        bankNumMap.put("XCYH", "许昌银行");
        bankNumMap.put("CEB", "中国光大银行");
        bankNumMap.put("NXBANK", "宁夏银行");
        bankNumMap.put("HSBANK", "徽商银行");
        bankNumMap.put("JJBANK", "九江银行");
        bankNumMap.put("NHQS", "农信银清算中心");
        bankNumMap.put("MTBANK", "浙江民泰商业银行");
        bankNumMap.put("LANGFB", "廊坊银行");
        bankNumMap.put("ASCB", "鞍山银行");
        bankNumMap.put("KSRB", "昆山农村商业银行");
        bankNumMap.put("YXCCB", "玉溪市商业银行");
        bankNumMap.put("DLB", "大连银行");
        bankNumMap.put("DRCBCL", "东莞农村商业银行");
        bankNumMap.put("GCB", "广州银行");
        bankNumMap.put("NBBANK", "宁波银行");
        bankNumMap.put("BOYK", "营口银行");
        bankNumMap.put("SXRCCU", "陕西信合");
        bankNumMap.put("GLBANK", "桂林银行");
        bankNumMap.put("BOQH", "青海银行");
        bankNumMap.put("CDRCB", "成都农商银行");
        bankNumMap.put("QDCCB", "青岛银行");
        bankNumMap.put("HKBEA", "东亚银行");
        bankNumMap.put("HBHSBANK", "湖北银行黄石分行");
        bankNumMap.put("WZCB", "温州银行");
        bankNumMap.put("TRCB", "天津农商银行");
        bankNumMap.put("QLBANK", "齐鲁银行");
        bankNumMap.put("GDRCC", "广东省农村信用社联合社");
        bankNumMap.put("ZJTLCB", "浙江泰隆商业银行");
        bankNumMap.put("GZB", "赣州银行");
        bankNumMap.put("GYCB", "贵阳市商业银行");
        bankNumMap.put("CQBANK", "重庆银行");
        bankNumMap.put("DAQINGB", "龙江银行");
        bankNumMap.put("CGNB", "南充市商业银行");
        bankNumMap.put("SCCB", "三门峡银行");
        bankNumMap.put("CSRCB", "常熟农村商业银行");
        bankNumMap.put("SHBANK", "上海银行");
        bankNumMap.put("JLBANK", "吉林银行");
        bankNumMap.put("CZRCB", "常州农村信用联社");
        bankNumMap.put("BANKWF", "潍坊银行");
        bankNumMap.put("ZRCBANK", "张家港农村商业银行");
        bankNumMap.put("FJHXBC", "福建海峡银行");
        bankNumMap.put("ZJNX", "浙江省农村信用社联合社");
        bankNumMap.put("LZYH", "兰州银行");
        bankNumMap.put("JSB", "晋商银行");
        bankNumMap.put("BOHAIB", "渤海银行");
        bankNumMap.put("CZCB", "浙江稠州商业银行");
        bankNumMap.put("YQCCB", "阳泉银行");
        bankNumMap.put("SJBANK", "盛京银行");
        bankNumMap.put("XABANK", "西安银行");
        bankNumMap.put("BSB", "包商银行");
        bankNumMap.put("JSBANK", "江苏银行");
        bankNumMap.put("FSCB", "抚顺银行");
        bankNumMap.put("HNRCU", "河南省农村信用");
        bankNumMap.put("COMM", "交通银行");
        bankNumMap.put("XTB", "邢台银行");
        bankNumMap.put("CITIC", "中信银行");
        bankNumMap.put("HXBANK", "华夏银行");
        bankNumMap.put("HNRCC", "湖南省农村信用社");
        bankNumMap.put("DYCCB", "东营市商业银行");
        bankNumMap.put("ORBANK", "鄂尔多斯银行");
        bankNumMap.put("BJRCB", "北京农村商业银行");
        bankNumMap.put("XYBANK", "信阳银行");
        bankNumMap.put("ZGCCB", "自贡市商业银行");
        bankNumMap.put("CDCB", "成都银行");
        bankNumMap.put("HANABANK", "韩亚银行");
        bankNumMap.put("CMBC", "中国民生银行");
        bankNumMap.put("LYBANK", "洛阳银行");
        bankNumMap.put("GDB", "广东发展银行");
        bankNumMap.put("ZBCB", "齐商银行");
        bankNumMap.put("CBKF", "开封市商业银行");
        bankNumMap.put("H3CB", "内蒙古银行");
        bankNumMap.put("CIB", "兴业银行");
        bankNumMap.put("CRCBANK", "重庆农村商业银行");
        bankNumMap.put("SZSBK", "石嘴山银行");
        bankNumMap.put("DZBANK", "德州银行");
        bankNumMap.put("SRBANK", "上饶银行");
        bankNumMap.put("LSCCB", "乐山市商业银行");
        bankNumMap.put("JXRCU", "江西省农村信用");
        bankNumMap.put("ICBC", "中国工商银行");
        bankNumMap.put("JZBANK", "晋中市商业银行");
        bankNumMap.put("HZCCB", "湖州市商业银行");
        bankNumMap.put("NHB", "南海农村信用联社");
        bankNumMap.put("XXBANK", "新乡银行");
        bankNumMap.put("JRCB", "江苏江阴农村商业银行");
        bankNumMap.put("YNRCC", "云南省农村信用社");
        bankNumMap.put("ABC", "中国农业银行");
        bankNumMap.put("GXRCU", "广西省农村信用");
        bankNumMap.put("PSBC", "中国邮政储蓄银行");
        bankNumMap.put("BZMD", "驻马店银行");
        bankNumMap.put("ARCU", "安徽省农村信用社");
        bankNumMap.put("GSRCU", "甘肃省农村信用");
        bankNumMap.put("LYCB", "辽阳市商业银行");
        bankNumMap.put("JLRCU", "吉林农信");
        bankNumMap.put("URMQCCB", "乌鲁木齐市商业银行");
        bankNumMap.put("XLBANK", "中山小榄村镇银行");
        bankNumMap.put("CSCB", "长沙银行");
        bankNumMap.put("JHBANK", "金华银行");
        bankNumMap.put("BHB", "河北银行");
        bankNumMap.put("NBYZ", "鄞州银行");
        bankNumMap.put("LSBC", "临商银行");
        bankNumMap.put("BOCD", "承德银行");
        bankNumMap.put("SDRCU", "山东农信");
        bankNumMap.put("NCB", "南昌银行");
        bankNumMap.put("TCCB", "天津银行");
        bankNumMap.put("WJRCB", "吴江农商银行");
        bankNumMap.put("CBBQS", "城市商业银行资金清算中心");
        bankNumMap.put("HBRCU", "河北省农村信用社");


        bankTypeMap.put("DC", "储蓄卡");
        bankTypeMap.put("CC", "信用卡");
        bankTypeMap.put("SCC", "准贷记卡");
        bankTypeMap.put("PC", "预付费卡");
    }


}
