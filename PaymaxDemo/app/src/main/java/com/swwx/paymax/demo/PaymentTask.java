package com.swwx.paymax.demo;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.swwx.paymax.PaymaxCallback;
import com.swwx.paymax.PaymaxSDK;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class PaymentTask extends AsyncTask<PaymentRequest, Void, String> {

    private static final String URL_CHAGE_URL = "https://www.paymax.cc/mock_merchant_server/v1/chargeOrders/product";

    Activity mActivity;

    PaymaxCallback mPaymaxCallback;

    private Button btn_ok;

    PaymentTask(Activity activity, PaymaxCallback paymaxCallback, Button btn_ok) {
        this.mActivity = activity;
        this.mPaymaxCallback = paymaxCallback;
        this.btn_ok = btn_ok;
    }

    @Override
    protected void onPreExecute() {
        btn_ok.setEnabled(false);
        btn_ok.setBackgroundResource(R.drawable.button_gray);
    }

    @Override
    protected String doInBackground(PaymentRequest... pr) {
        PaymentRequest paymentRequest = pr[0];
        String data = null;
        String json = new Gson().toJson(paymentRequest);
        try {
            data = postJson(URL_CHAGE_URL, json);
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
        btn_ok.setEnabled(true);
        btn_ok.setBackgroundResource(R.drawable.button_style);
        if (null == data || data.length() == 0) {
            Snackbar.make(mActivity.findViewById(android.R.id.content), "no data", Snackbar.LENGTH_LONG)
                    .setAction("Close", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    }).show();
            return;
        }
        PaymaxSDK.pay(mActivity, data, mPaymaxCallback);
    }

    private String postJson(String url, String json) throws IOException {
        MediaType type = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(type, json);
        Request request = new Request.Builder().url(url).post(body).build();
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(5, TimeUnit.SECONDS);
        client.setReadTimeout(5, TimeUnit.SECONDS);
        Response response = client.newCall(request).execute();
        return response.code() == 200 ? response.body().string() : null;
    }

}



