package com.example.payment.wxPay;

/**
 * Created by zhouqiang on 2018/5/17.
 */


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.widget.TextView;

import com.example.payment.R;
import com.example.payment.wxPay.wxUtils.Constants;
import com.example.payment.wxPay.wxUtils.MD5;
import com.example.payment.wxPay.wxUtils.MD5Util;
import com.example.payment.wxPay.wxUtils.Util;
import com.example.payment.wxPay.wxUtils.XmlUtil;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 生成预付款订单
 * 微信文档  ：https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_1
 */
public class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String, String>> {
    private final TextView showTextView;
    private PayReq req;
    private StringBuffer sb;
    private final PayActivity context;
    private ProgressDialog dialog;
    public Map<String, String> resultunifiedorder;
    IWXAPI msgApi = null;

    public GetPrepayIdTask(PayActivity payActivity, TextView show, PayReq req, StringBuffer sb) {
        this.req = req;
        this.sb = sb;
        this.context = payActivity;
        this.msgApi = WXAPIFactory.createWXAPI(context, null);
        this.showTextView = show;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context, context.getString(R.string.app_tip), context.getString(R.string.getting_prepayid));
    }


    @Override
    protected void onPostExecute(Map<String, String> result) {
        if (dialog != null) {
            dialog.dismiss();
        }
        //1.得到prepay_id
        sb.append("prepay_id\n" + result.get("prepay_id") + "\n\n");
        //2.显示prepay_id
        showTextView.setText(sb.toString());
        resultunifiedorder = result;
        //3.调微信支付
        genPayReq();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected Map<String, String> doInBackground(Void... params) {
        //本地调用统一下单的接口    微信返回的数据格式是xml 需要本地处理
        String url = String.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
        String entity = genEntity();
        Log.e("实体", entity);
        byte[] buf = Util.httpPost(url, entity);
        String content = new String(buf);
        Log.e("内容", content);
        Map<String, String> xml = decodeXml(content);
        return xml;
    }


    /**
     * 掉起微信支付 这里是客户端必须进行的 也就是接受服务器 返回的数据 进行处理
     */
    private void genPayReq() {

        req.appId = Constants.APP_ID;
        req.partnerId = Constants.MCH_ID;
        req.prepayId = resultunifiedorder.get("prepay_id");
        req.packageValue = "Sign=WXPay";
        String prepayID = resultunifiedorder.get("prepay_id");

        req.nonceStr = genNonceStr();
        req.timeStamp = String.valueOf(genTimeStamp());

        List<NameValuePair> signParams = new LinkedList<NameValuePair>();
        signParams.add(new BasicNameValuePair("appid", req.appId));
        signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
       signParams.add(new BasicNameValuePair("package", req.packageValue));
        signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
        signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
        signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
        //因为微信支付需要签2次名  这里是第二次签名  本应该放在服务端的
        req.sign = genAppSign(signParams);


        sb.append("sign\n" + req.sign + "\n\n");
        //show.setText(sb.toString());


        Log.e("orion", signParams.toString());
        //调起微信支付
        msgApi.registerApp(Constants.APP_ID);
        msgApi.sendReq(req);

    }


    private long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 遵循微信签名的工具类
     * @param params
     * @return
     */
    private String genAppSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(Constants.API_KEY);

        this.sb.append("sign str\n" + sb.toString() + "\n\n");
        String appSign = MD5.getMessageDigest(sb.toString().getBytes());
        Log.e("orion", appSign);
        return appSign;
    }

    public Map<String, String> decodeXml(String content) {

        try {
            Map<String, String> xml = new HashMap<String, String>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:

                        break;
                    case XmlPullParser.START_TAG:

                        if ("xml".equals(nodeName) == false) {
                            //实例化student对象
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }

            return xml;
        } catch (Exception e) {
            Log.e("orion", e.toString());
        }
        return null;

    }

    public String genEntity() {
        String nonceStr = genNonceStr();
        List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
        packageParams.add(new BasicNameValuePair("appid", Constants.APP_ID));
        packageParams.add(new BasicNameValuePair("body", "我是购买的商品"));
        packageParams.add(new BasicNameValuePair("mch_id", Constants.MCH_ID));
        packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
        packageParams.add(new BasicNameValuePair("notify_url", "http://121.40.35.3/test"));
        packageParams.add(new BasicNameValuePair("out_trade_no", genOutTradNo()));
        packageParams.add(new BasicNameValuePair("spbill_create_ip", "127.0.0.1"));
        packageParams.add(new BasicNameValuePair("total_fee", "1"));
        packageParams.add(new BasicNameValuePair("trade_type", "APP"));
        String sign = genPackageSign(packageParams);
        packageParams.add(new BasicNameValuePair("sign", sign));
        String xmlstring = XmlUtil.toXml(packageParams);
        try {
            return new String(xmlstring.toString().getBytes(), "ISO-8859-1");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 随机生成一个满足微信需求的 商户订单号
     * @return
     */
    private String genOutTradNo() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }


    /**
     * 签名
     *
     * @param params
     * @return
     */

    public static String genPackageSign(List<NameValuePair> params) {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < params.size(); i++) {
                sb.append(params.get(i).getName());
                sb.append('=');
                sb.append(params.get(i).getValue());
                sb.append('&');
            }
            sb.append("key=");
            sb.append(Constants.API_KEY);

            String packageSign = MD5Util.getMessageDigest(
                    sb.toString().getBytes("utf-8")).toUpperCase();
            return packageSign;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成的随机字符串
     * @return
     */
    public static String genNonceStr() {
        try {
            Random random = new Random();
            String rStr = MD5Util.getMessageDigest(String.valueOf(
                    random.nextInt(10000)).getBytes("utf-8"));
            return rStr;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}