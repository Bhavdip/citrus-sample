package com.citrus.sample;

import com.citrus.sdk.Environment;

public interface Const {
    String BILL_URL = "https://salty-plateau-1529.herokuapp.com/billGenerator.sandbox.php";
    String RETURN_URL_LOAD_MONEY = "https://salty-plateau-1529.herokuapp.com/redirectUrlLoadCash.php";

    String SIGNUP_ID = "3jt4qqyuq4-signup";
    String SIGNUP_SECRET = "0619abcb872cd68156a8eba66bcb4992";
    String SIGNIN_ID = "3jt4qqyuq4-signin";
    String SIGNIN_SECRET = "aa614f1c1c5773fedd4a2f42fd9f5314";
    String VANITY = "3jt4qqyuq4";
    Environment environment = Environment.SANDBOX;

    boolean enableLogging = true;

    String colorPrimaryDark = "#E7961D";
    String colorPrimary = "#F9A323";
    String textColor = "#ffffff";
}
