package com.swwx.paymax.demo;

class PaymentRequest {
    String channel;
    double totalPrice;
    String title;
    String body;
    User extra;
    long time_expire;

    PaymentRequest(String channel, double totalPrice, String title, String body, String userid,long time_expire) {
        this.channel = channel;
        this.totalPrice = totalPrice;
        this.title = title;
        this.body = body;
        this.time_expire=time_expire;

        extra = new User(userid);
    }

    class User {
        String user_id;

        User(String userid) {
            this.user_id = userid;
        }

    }
}
