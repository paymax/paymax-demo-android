package com.swwx.paymax.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.EditText;

import com.swwx.facesdk.FaceRecoSDK;
import com.swwx.facesdk.ResponseHelper;
import com.swwx.facesdk.ui.FaceLivenessActivity;
import com.swwx.paymax.PayResult;
import com.swwx.paymax.PaymaxCallback;
import com.swwx.paymax.PaymaxSDK;

public class InputActivity extends AppCompatActivity implements PaymaxCallback {

    String realName = "";
    String idCardNo = "";

    double amount;
    String userid;
    EditText etName;
    EditText etIdCardNo;

    @Override
    final protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_input);

        amount = getIntent().getDoubleExtra("amount", 0.0);
        userid = getIntent().getStringExtra("userid");

        etName = (EditText) findViewById(R.id.et_name);
        etIdCardNo = (EditText) findViewById(R.id.et_idcard);

        etIdCardNo.setKeyListener(new DigitsKeyListener() {
            @Override
            public int getInputType() {
                return InputType.TYPE_TEXT_VARIATION_PASSWORD;
            }

            @Override
            protected char[] getAcceptedChars() {
                char[] data = getStringData(R.string.login_only_can_input).toCharArray();
                return data;
            }
        });

        findViewById(R.id.bt_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realName = etName.getText().toString();
                idCardNo = etIdCardNo.getText().toString();

                // 5b97b3138041437587646b37f52dc7f7 开发
                // 27ab6b803cae4e69959543aacf0836d6 测试
                FaceRecoSDK.startReco(userid, "55970fdbbf10459f966a8e276afa86fa", realName, idCardNo, InputActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {

            case Activity.RESULT_OK:
                new PaymentTask(this, this).execute(new PaymentRequest(MainActivity.CHANNEL_LKL, amount, "测试商品007", "测试商品Body", userid));
                break;

            case Activity.RESULT_CANCELED: {
                if (data != null) {
                    int code = data.getIntExtra(FaceLivenessActivity.RESULT_VERIFY_CODE, ResponseHelper.UNKONW_ERROR);
                    String msg = data.getStringExtra(FaceLivenessActivity.RESULT_VERIFY_MESSAGE);
                    switch (code) {
                        case ResponseHelper.CODE_LIVENESS_INITIALIZE_FAIL:
                            msg = "活体检测初始化失败";
                            break;

                        case ResponseHelper.CODE_LIVENESS_FAIL:
                            msg = "活体检测取样失败";
                            break;
                    }

                    Snackbar.make(findViewById(android.R.id.content), "", Snackbar.LENGTH_LONG)
                            .setAction(msg == null ? "未知错误" : msg, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).show();
                }
            }


            break;
        }

    }

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

    public String getStringData(int id) {
        return getResources().getString(id);
    }
}
