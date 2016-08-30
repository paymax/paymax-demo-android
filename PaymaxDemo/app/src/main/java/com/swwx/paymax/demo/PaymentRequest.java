package com.swwx.paymax.demo;

class PaymentRequest {
    String channel;
    double totalPrice;
    String title;
    String body;
    User extra;

    PaymentRequest(String channel, double totalPrice, String title, String body, String userid) {
        this.channel = channel;
        this.totalPrice = totalPrice;
        this.title = title;
        this.body = body;

        extra = new User(userid);
    }

    class User {
        String user_id;

        User(String userid) {
            this.user_id = userid;
        }

    }
}
