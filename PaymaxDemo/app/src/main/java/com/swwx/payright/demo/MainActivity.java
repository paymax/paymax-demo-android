package com.swwx.payright.demo;

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
import com.swwx.payright.PayResult;
import com.swwx.payright.PayRightCallback;
import com.swwx.payright.PayRightSDK;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements PayRightCallback {

    private static final String URL = "Test url";
    private EditText amountEditText;
    private String currentAmount = "";

    private ImageButton ibWechat;
    private ImageButton ibAlipay;
    private int channel = PayRightSDK.CHANNEL_ALIPAY;

    /**
     * 支付宝支付渠道
     */
    private static final String CHANNEL_ALIPAY = "alipay_app";

    /**
     * 微信支付渠道
     */
    private static final String CHANNEL_WECHAT = "wechat_app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivWX = (ImageView) findViewById(R.id.iv_wx);
        ivWX.setImageResource(R.drawable.wx);
        ImageView ivAli = (ImageView) findViewById(R.id.iv_ali);
        ivAli.setImageResource(R.drawable.ali);

        // select channel button
        ibWechat = (ImageButton) findViewById(R.id.ibWechat);
        ibAlipay = (ImageButton) findViewById(R.id.ibAlipay);
        onChannelClick(ibWechat);

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
        if (checkInputValid(amountText)) {
            double amount = parseInputTxt(amountText);
            amount /= 100;
            new PaymentTask().execute(new PaymentRequest((channel == PayRightSDK.CHANNEL_WX) ? CHANNEL_WECHAT : CHANNEL_ALIPAY, amount, "测试商品007", "测试商品Body"));
        }
    }

    public void onChannelClick(View v) {
        switch (v.getId()) {
            case R.id.ibAlipay:
                channel = PayRightSDK.CHANNEL_ALIPAY;
                ibAlipay.setBackgroundResource(R.drawable.selected);
                ibWechat.setBackgroundResource(R.drawable.unselected);
                break;

            case R.id.ibWechat:
                channel = PayRightSDK.CHANNEL_WX;
                ibAlipay.setBackgroundResource(R.drawable.unselected);
                ibWechat.setBackgroundResource(R.drawable.selected);
                break;

            default:
                break;
        }
    }

    @Override
    public void onPayFinished(PayResult result) {
        String msg = "Unknow";
        switch (result.getCode()) {
            case PayRightSDK.CODE_SUCCESS:
                msg = "Complete, Success!.";
                break;

            case PayRightSDK.CODE_ERROR_CHARGE_JSON:
                msg = "Json error.";
                break;

            case PayRightSDK.CODE_FAIL_CANCEL:
                msg = "cancel pay.";
                break;

            case PayRightSDK.CODE_ERROR_CHARGE_PARAMETER:
                msg = "appid error.";
                break;

            case PayRightSDK.CODE_ERROR_WX_NOT_INSTALL:
                msg = "wx not install.";
                break;

            case PayRightSDK.CODE_ERROR_WX_NOT_SUPPORT_PAY:
                msg = "ex not support pay.";
                break;

            case PayRightSDK.CODE_ERROR_WX_UNKNOW:
                msg = "wechat failed.";
                break;

            case PayRightSDK.CODE_ERROR_ALI_DEAL:
                msg = "alipay dealing.";
                break;

            case PayRightSDK.CODE_ERROR_ALI_CONNECT:
                msg = "alipay network connection failed.";
                break;

        }
        Snackbar.make(getWindow().getDecorView(), msg, Snackbar.LENGTH_LONG)
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

    private String postJson(String url, String json) throws IOException {
        MediaType type = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(type, json);
        Request request = new Request.Builder().url(url).post(body).build();
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(5, TimeUnit.SECONDS);
        client.setReadTimeout(5, TimeUnit.SECONDS);
        Response response = client.newCall(request).execute();
        Log.d("PayRightSDK", "response code = " + response.code());
        return response.code() == 200 ? response.body().string() : null;

    }

    class PaymentTask extends AsyncTask<PaymentRequest, Void, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(PaymentRequest... pr) {
            PaymentRequest paymentRequest = pr[0];
            String data = null;
            String json = new Gson().toJson(paymentRequest);
            Log.d("PayRightSDK", "json=" + json);
            try {

                // 向 PayRightSDK Server SDK请求数据
                data = postJson(URL, json);
                Log.d("PayRightSDK", "data=" + data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }

        /**
         * 获得服务端的charge，调用 sdk。
         */
        @Override
        protected void onPostExecute(String data) {
            if (null == data || data.length() == 0) {
                Snackbar.make(getWindow().getDecorView(), "no data", Snackbar.LENGTH_LONG)
                        .setAction("Close", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        }).show();
                return;
            }

            PayRightSDK.pay(MainActivity.this, data, MainActivity.this);
        }
    }

    class PaymentRequest {
        String channel;
        double totalPrice;
        String title;
        String body;

        public PaymentRequest(String channel, double totalPrice, String title, String body) {
            this.channel = channel;
            this.totalPrice = totalPrice;
            this.title = title;
            this.body = body;
        }
    }
}




