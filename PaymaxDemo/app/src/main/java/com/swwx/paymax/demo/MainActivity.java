package com.swwx.paymax.demo;

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
import android.widget.Switch;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.swwx.paymax.PayResult;
import com.swwx.paymax.PaymaxCallback;
import com.swwx.paymax.PaymaxSDK;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements PaymaxCallback {

    private static final String URL_TEST = "http://118.186.238.194:12317/v1/chargeOrders"; // 测试环境
    private static final String URL_DEV = "http://118.186.238.194:8899/v1/chargeOrders"; //（开发环境）

    private EditText amountEditText;

    private EditText useridEditText;

    private String currentAmount = "";

    private ImageButton ibWechat;
    private ImageButton ibAlipay;
    private ImageButton ibLKL;

    private Switch mSwitch;

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
    private static final String CHANNEL_LKL = "lakala_app";

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

        mSwitch = (Switch) findViewById(R.id.switch_view);
        mSwitch.setChecked(true);

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
        String userid = useridEditText.getText().toString();
        if (checkInputValid(amountText)) {
            double amount = parseInputTxt(amountText);
            amount /= 100;

            switch (channel) {
                case PaymaxSDK.CHANNEL_WX:
                    new PaymentTask(mSwitch.isChecked()).execute(new PaymentRequest(CHANNEL_WECHAT, amount, "测试商品007", "测试商品Body", userid));
                    break;

                case PaymaxSDK.CHANNEL_ALIPAY:
                    new PaymentTask(mSwitch.isChecked()).execute(new PaymentRequest(CHANNEL_ALIPAY, amount, "测试商品007", "测试商品Body", userid));
                    break;

                case PaymaxSDK.CHANNEL_LKL:
                    new PaymentTask(mSwitch.isChecked()).execute(new PaymentRequest(CHANNEL_LKL, amount, "测试商品007", "测试商品Body", userid));
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
        Log.d("PaymaxSDK", "response code = " + response.code());
        return response.code() == 200 ? response.body().string() : null;

    }

    class PaymentTask extends AsyncTask<PaymentRequest, Void, String> {

        private boolean isTestMode = true;

        public PaymentTask(boolean isTestMode) {
            this.isTestMode = isTestMode;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(PaymentRequest... pr) {
            PaymentRequest paymentRequest = pr[0];
            String data = null;
            String json = new Gson().toJson(paymentRequest);
            Log.d("PaymaxSDK", "json=" + json);
            try {

                // 向 PaymaxSDK Server SDK请求数据
                data = postJson(isTestMode ? URL_TEST : URL_DEV, json);
                Log.d("PaymaxSDK", "data=" + data);
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

//            String test = "{\"amount\":0.01,\"app\":\"app_49b0f1dd741646d2b277524de2785836\",\"body\":\"测试商品Body\",\"channel\":\"lkl_app\",\"credential\":{\"lkl_app\":{\"orderInfo\":\"_input_charset=\\\"utf-8\\\"&notify_url=\\\"http://118.186.238.194:12306/lkl_app\\\"&out_trade_no=\\\"ch_37a542d2a9062650fda9dfe2\\\"&partner=\\\"2088221494146238\\\"&payment_type=\\\"1\\\"&seller_id=\\\"471332824@qq.com\\\"&service=\\\"mobile.securitypay.pay\\\"&subject=\\\"测试商品007\\\"&total_fee=\\\"0.01\\\"&sign=\\\"FkedgBzbbPG4PVJiG3UiSyzNCxAsV8Fzwud358jsvsgxgtcBHVhKGl9OQa0gQkA2wxOa9uwYHJMhyVc4At%2F1EpScOven85Qxdi2yaUWH%2FzXbLYvnOEDZDQKO8zBxPb1n2puc6MToa12MBSTS1cFinwOibycN9xUk2JzhVF7aCQs%3D\\\"&sign_type=\\\"RSA\\\"\"}},\"currency\":\"cny\",\"description\":\"\",\"id\":\"ch_37a542d2a9062650fda9dfe2\",\"liveMode\":false,\"refunded\":false,\"refunds\":[],\"status\":\"PROCESSING\",\"subject\":\"测试商品007\"}";

            PaymaxSDK.pay(MainActivity.this, data, MainActivity.this);
        }
    }

    class PaymentRequest {
        String channel;
        double totalPrice;
        String title;
        String body;
        User extra;

        public PaymentRequest(String channel, double totalPrice, String title, String body, String userid) {
            this.channel = channel;
            this.totalPrice = totalPrice;
            this.title = title;
            this.body = body;

            extra = new User(userid);
        }
    }

    class User {
        String user_id;

        public User(String userid) {
            this.user_id = userid;
        }
    }
}




