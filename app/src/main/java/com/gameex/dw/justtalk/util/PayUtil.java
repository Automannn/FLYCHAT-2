package com.gameex.dw.justtalk.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.unionpay.UPPayAssistEx;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static com.gameex.dw.justtalk.FlayChatApplication.WE_CHAT_APP_ID;

public class PayUtil {
    private static final String TAG = "PayUtil";
    /**
     * 银联
     */
    private static final String SERVER_MODE = "01";

    /**
     * 调起银联支付
     *
     * @param context 上下文
     * @param data    参数
     */
    public static void toUnionPay(Context context, String data) {
        UPPayAssistEx.startPay(context, null, null, data, SERVER_MODE);
    }

    /**
     * ali_pay
     */
    private static final int SDK_PAY_FLAG = 301;

    /**
     * 调起支付宝支付
     *
     * @param activity activity
     * @param handler  接收支付结果
     * @param data     订单信息
     */
    public static void toAlipay(final Activity activity, final Handler handler
            , final String data) {
        //支付宝沙盒模式
//        EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);

        Runnable payRunnable = () -> {
            PayTask alipay = new PayTask(activity);
            Map<String, String> result = alipay.payV2(data, true);
            Message msg = new Message();
            msg.what = SDK_PAY_FLAG;
            msg.obj = result;
            handler.sendMessage(msg);
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * 微信支付api
     */
    private static IWXAPI iwxapi;

    /**
     * 调起微信支付的方法
     *
     * @param context 上下文
     * @param data    参数
     */
    public static void toWXPay(Context context, String data) {
        final HashMap<String, String> map = resolvinJson(data);
        if (map == null) {
            Toast.makeText(context, "数据解析异常", Toast.LENGTH_SHORT).show();
            return;
        }
        iwxapi = WXAPIFactory.createWXAPI(context, WE_CHAT_APP_ID, false); //初始化微信api
        iwxapi.registerApp(WE_CHAT_APP_ID); //注册appid

//       final IWXAPI api=WXAPIFactory.createWXAPI(context,WE_CHAT_APP_ID);

        //这里注意要放在子线程
        Runnable payRunnable = () -> {
            PayReq request = new PayReq(); //调起微信APP的对象
            //下面是设置必要的参数，也就是前面说的参数,这几个参数从何而来请看上面说明
            request.appId = map.get("appId"); //微信开放平台的应用id
            request.partnerId = map.get("partnerId");  //商户号
            request.prepayId = map.get("prepayId");    //预支付交易会话id
            request.packageValue = map.get("packageValue");    //扩展字段
            request.nonceStr = map.get("nonceStr");    //随机字符串
            request.timeStamp = map.get("timeStamp");  //时间戳
            request.sign = map.get("sign");    //签名
            iwxapi.sendReq(request);//发送调起微信的请求
        };
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * 解析json
     *
     * @param dataJson 要解析的json串
     * @return hashMap.string, string
     */
    private static HashMap<String, String> resolvinJson(String dataJson) {
        HashMap<String, String> map = new HashMap<>();
        try {
            JSONObject data = new JSONObject(dataJson);
            map.put("appId", data.getString("appid"));
            map.put("partnerId", data.getString("partnerid"));
            map.put("prepayId", data.getString("prepayid"));
            map.put("packageValue", data.getString("package"));
            map.put("nonceStr", data.getString("noncestr"));
            map.put("timeStamp", data.getString("timestamp"));
            map.put("sign", data.getString("sign"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 用Pull方式解析xml数据
     *
     * @param xmlData 要解析的xml
     * @return hashMap.string, string
     */
    private static HashMap<String, String> pullXml(String xmlData) {
        HashMap<String, String> dataMap = new HashMap<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            //设置输入的内容
            xmlPullParser.setInput(new StringReader(xmlData));
            //获取当前解析事件，返回的是数字
            int eventType = xmlPullParser.getEventType();
            //保存内容
            String partnerId = "";
            String prepayId = "";
            String packageValue = "";
            String nonceStr = "";
            String timeStamp = "";
            String sign = "";

            while (eventType != (XmlPullParser.END_DOCUMENT)) {
                String nodeName = xmlPullParser.getName();
                switch (eventType) {
                    //开始解析XML
                    case XmlPullParser.START_TAG: {
                        //nextText()用于获取结点内的具体内容
                        if ("partnerId".equals(nodeName))
                            partnerId = xmlPullParser.nextText();
                        else if ("prepayId".equals(nodeName))
                            prepayId = xmlPullParser.nextText();
                        else if ("packageValue".equals(nodeName))
                            packageValue = xmlPullParser.nextText();
                        else if ("nonceStr".equals(nodeName))
                            nonceStr = xmlPullParser.nextText();
                        else if ("timeStamp".equals(nodeName))
                            timeStamp = xmlPullParser.nextText();
                        else if ("sign".equals(nodeName))
                            sign = xmlPullParser.nextText();
                    }
                    break;
                    //结束解析
                    case XmlPullParser.END_TAG: {
                        if ("app".equals(nodeName)) {
                            dataMap.put("partnerId", partnerId);
                            dataMap.put("prepayId", prepayId);
                            dataMap.put("packageValue", packageValue);
                            dataMap.put("nonceStr", nonceStr);
                            dataMap.put("timeStamp", timeStamp);
                            dataMap.put("sign", sign);
                        }
                    }
                    break;
                    default:
                        break;
                }
                //下一个
                eventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtil.d(TAG, "parseXMLWithPull: " + dataMap.toString());
        return dataMap;
    }
}
