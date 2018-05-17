package com.example.payment.wxPay;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.payment.R;
import com.example.payment.wxPay.wxUtils.Constants;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by zhouqiang on 2018/5/16.
 * 服务端 ：统一下单需要10个参数 其中第10个是签名 是对前9个参数进行签名处理，微信后台会返回生成的预付款订单 ，然后服务端会给客户端返回7个参数  其中第7个参数 是对前6个参数整体进行的签名
 * app端 ：掉起微信支付需要服务器传递过来的7个参数
 *
 * 本demo 将服务端的操作也放在本地进行操作 本地 调用统一下单 ，本地签名
 * 注意 ：微信支付需要认证开发者资质，支付宝支付需要签约
 *
 *
 */

public class PayActivity extends AppCompatActivity {
    private PayReq req;
    private TextView show;
    private StringBuffer sb;


    final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay);
        show = (TextView) findViewById(R.id.editText_prepay_id);
         req = new PayReq();
        sb = new StringBuffer();

        msgApi.registerApp(Constants.APP_ID);


        Button appayBtn = (Button) findViewById(R.id.appay_btn);
        appayBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //1. 正常的流程是服务器调用统一下单的接口    生成预付款订单PrepayId 返回给客户端  ，但是本次是模拟在客户端调用统一下单
                GetPrepayIdTask getPrepayId = new GetPrepayIdTask(PayActivity.this,show,req,sb);
                getPrepayId.execute();
            }
        });
    }



}
