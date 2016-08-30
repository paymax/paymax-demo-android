package com.swwx.paymax.demo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.swwx.paymax.PayLog;
import com.swwx.paymax.PayResult;
import com.swwx.paymax.PaymaxCallback;
import com.swwx.paymax.PaymaxSDK;

import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements PaymaxCallback {

    private EditText amountEditText;


    private EditText useridEditText;


    private String currentAmount = "";

    private ImageButton ibWechat;
    private ImageButton ibAlipay;
    private ImageButton ibLKL;

    protected double amount = 0.0;
    protected String userid = "";


    private int channel = PaymaxSDK.CHANNEL_ALIPAY;

    /**
     * 支付宝支付渠道
     */
    private static final String CHANNEL_ALIPAY = "alipay_app";

    /**
     * 微信支付渠道
     */
    private static final String CHANNEL_WECHAT = "wechat_app";

    /**
     * 微信支付渠道
     */
    protected static final String CHANNEL_LKL = "lakala_app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivWX = (ImageView) findViewById(R.id.iv_wx);
        ivWX.setImageResource(R.drawable.wx);
        ImageView ivAli = (ImageView) findViewById(R.id.iv_ali);
        ivAli.setImageResource(R.drawable.ali);
        ImageView ivLkl = (ImageView) findViewById(R.id.iv_lkl);
        ivLkl.setImageResource(R.drawable.lkl);


        // select channel button
        ibWechat = (ImageButton) findViewById(R.id.ibWechat);
        ibAlipay = (ImageButton) findViewById(R.id.ibAlipay);
        ibLKL = (ImageButton) findViewById(R.id.ibLKL);
        onChannelClick(ibWechat);

        useridEditText = (EditText) findViewById(R.id.edit_userid);
        amountEditText = (EditText) findViewById(R.id.et_right);
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(currentAmount)) {
                    amountEditText.removeTextChangedListener(this);
                    String replaceable = String.format("[%s, \\s.]", NumberFormat.getCurrencyInstance(Locale.CHINA).getCurrency().getSymbol(Locale.CHINA));
                    String cleanString = s.toString().replaceAll(replaceable, "");

                    if (cleanString.equals("") || new BigDecimal(cleanString).toString().equals("0")) {
                        amountEditText.setText(null);
                    } else {
                        double parsed = Double.parseDouble(cleanString);
                        String formatted = NumberFormat.getCurrencyInstance(Locale.CHINA).format((parsed / 100));
                        currentAmount = formatted;
                        amountEditText.setText(formatted);
                        amountEditText.setSelection(formatted.length());
                    }
                    amountEditText.addTextChangedListener(this);
                }
            }
        });
    }

    public void onCharge(View view) {
        String amountText = amountEditText.getText().toString();
        userid = useridEditText.getText().toString();
        if (checkInputValid(amountText)) {
            amount = parseInputTxt(amountText);
            amount /= 100;

            switch (channel) {
                case PaymaxSDK.CHANNEL_WX:
                    new PaymentTask(MainActivity.this, MainActivity.this).execute(new PaymentRequest(CHANNEL_WECHAT, amount, "测试商品007", "测试商品Body", userid));
                    break;

                case PaymaxSDK.CHANNEL_ALIPAY:
                    new PaymentTask(MainActivity.this, MainActivity.this).execute(new PaymentRequest(CHANNEL_ALIPAY, amount, "测试商品007", "测试商品Body", userid));
                    break;

                case PaymaxSDK.CHANNEL_LKL: {
                    new FaceTask().execute(new FaceRequest("123", "123", userid));
                }
                break;
            }

        }
    }

    public void onChannelClick(View v) {
        switch (v.getId()) {
            case R.id.ibAlipay:
                channel = PaymaxSDK.CHANNEL_ALIPAY;
                ibAlipay.setBackgroundResource(R.drawable.selected);
                ibWechat.setBackgroundResource(R.drawable.unselected);
                ibLKL.setBackgroundResource(R.drawable.unselected);
                break;

            case R.id.ibWechat:
                channel = PaymaxSDK.CHANNEL_WX;
                ibAlipay.setBackgroundResource(R.drawable.unselected);
                ibWechat.setBackgroundResource(R.drawable.selected);
                ibLKL.setBackgroundResource(R.drawable.unselected);
                break;

            case R.id.ibLKL:
                channel = PaymaxSDK.CHANNEL_LKL;
                ibAlipay.setBackgroundResource(R.drawable.unselected);
                ibWechat.setBackgroundResource(R.drawable.unselected);
                ibLKL.setBackgroundResource(R.drawable.selected);
                break;

            default:
                break;
        }
    }

    @Override
    public void onPayFinished(PayResult result) {
        String msg = "Unknow";
        switch (result.getCode()) {
            case PaymaxSDK.CODE_SUCCESS:
                msg = "Complete, Success!.";
                break;

            case PaymaxSDK.CODE_ERROR_CHARGE_JSON:
                msg = "Json error.";
                break;

            case PaymaxSDK.CODE_FAIL_CANCEL:
                msg = "cancel pay.";
                break;

            case PaymaxSDK.CODE_ERROR_CHARGE_PARAMETER:
                msg = "appid error.";
                break;

            case PaymaxSDK.CODE_ERROR_WX_NOT_INSTALL:
                msg = "wx not install.";
                break;

            case PaymaxSDK.CODE_ERROR_WX_NOT_SUPPORT_PAY:
                msg = "ex not support pay.";
                break;

            case PaymaxSDK.CODE_ERROR_WX_UNKNOW:
                msg = "wechat failed.";
                break;

            case PaymaxSDK.CODE_ERROR_ALI_DEAL:
                msg = "alipay dealing.";
                break;

            case PaymaxSDK.CODE_ERROR_ALI_CONNECT:
                msg = "alipay network connection failed.";
                break;

            case PaymaxSDK.CODE_ERROR_CHANNEL:
                msg = "channel error.";
                break;

            case PaymaxSDK.CODE_ERROR_LAK_USER_NO_NULL:
                msg = "lklpay user no is null.";
                break;

        }
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
                .setAction("Close", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
    }

    private boolean checkInputValid(String amountText) {
        return !(null == amountText || amountText.length() == 0);
    }

    private double parseInputTxt(String amountText) {
        String replaceable = String.format("[%s, \\s.]", NumberFormat.getCurrencyInstance(Locale.CHINA).getCurrency().getSymbol(Locale.CHINA));
        String cleanString = amountText.replaceAll(replaceable, "");
        return Double.valueOf(new BigDecimal(cleanString).toString());
    }

    class FaceTask extends AsyncTask<FaceRequest, Void, String> {


        public FaceTask() {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(FaceRequest... pr) {
            FaceRequest faceRequest = pr[0];
            String data = null;
            String json = new Gson().toJson(faceRequest);
            Log.d("FaceRecoSDK", "json=" + json);
            try {

                // 向 PaymaxSDK Server SDK请求数据
                // 开发环境：http://172.30.21.20:8888/mock_merchant_server/v1/face/auth/{uId}/dev
                // 测试环境：http://172.30.21.22:8888/mock_merchant_server/v1/face/auth/{uId}/test
                // 测试环境(域名): http://test.paymax.cc/mock_merchant_server/v1/face/auth/%s/test
                // https://www.paymax.cc/mock_merchant_server/v1/face/auth/{uId}/product
                String url = String.format("https://www.paymax.cc/mock_merchant_server/v1/face/auth/%s/product", pr[0].uId);
                data = postJson(url, json);
                Log.d("FaceRecoSDK", "data=" + data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }


        @Override
        protected void onPostExecute(String data) {
            if (null == data || data.length() == 0) {
                Snackbar.make(findViewById(android.R.id.content), "no face data", Snackbar.LENGTH_LONG)
                        .setAction("Close", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        }).show();
            } else if (getRtn(data)) {
                new PaymentTask(MainActivity.this, MainActivity.this).execute(new PaymentRequest(CHANNEL_LKL, amount, "测试商品007", "测试商品Body", userid));
            } else {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra("amount", amount);
                intent.putExtra("userid", userid);
                startActivity(intent);
            }
        }
    }

    class FaceRequest {
        String key;//商户key
        String merchantNo;//商户号
        String uId;//商户用户id， 在商户系统能唯一标示某个用户商户用户名

        public FaceRequest(String key, String merchantNo, String uId) {
            this.key = key;
            this.merchantNo = merchantNo;
            this.uId = uId;
        }
    }


    /**
     * 获得返回结果码,来判断此用户是否通过人脸识别
     *
     * @param data
     * @return
     */
    private boolean getRtn(String data) {
        boolean flag = false;
        try {
            JSONObject jsonObject = new JSONObject(data);
            flag = jsonObject.optBoolean("authValid");
        } catch (Exception e) {
            PayLog.e(e);
        }
        return flag;
    }


    private String postJson(String url, String json) throws IOException {
        MediaType type = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(type, json);
        Request request = new Request.Builder().url(url).get().build();
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(5, TimeUnit.SECONDS);
        client.setReadTimeout(5, TimeUnit.SECONDS);
        Response response = client.newCall(request).execute();
        Log.d("PaymaxSDK", "response code = " + response.code());
        return response.body().string();

    }

}




