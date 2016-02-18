/*
 *
 *    Copyright 2014 Citrus Payment Solutions Pvt. Ltd.
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package com.citrus.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.citrus.sdk.Callback;
import com.citrus.sdk.CitrusClient;
import com.citrus.sdk.TransactionResponse;
import com.citrus.sdk.classes.Amount;
import com.citrus.sdk.classes.CashoutInfo;
import com.citrus.sdk.classes.CitrusConfig;
import com.citrus.sdk.classes.CitrusException;
import com.citrus.sdk.payment.PaymentType;
import com.citrus.sdk.response.CitrusError;
import com.citrus.sdk.response.CitrusResponse;
import com.citrus.sdk.response.PaymentResponse;


public class UIActivity extends AppCompatActivity implements UserManagementFragment.UserManagementInteractionListener, WalletFragmentListener {

    private FragmentManager fragmentManager = null;
    private Context mContext = this;
    private CitrusClient citrusClient = null;
    private CitrusConfig citrusConfig = null;
    private FrameLayout frameLayout = null;
    private View snackBarParent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui);

        frameLayout = (FrameLayout) findViewById(R.id.container);

        fragmentManager = getSupportFragmentManager();

        citrusClient = CitrusClient.getInstance(mContext);
        citrusClient.enableLog(Const.enableLogging);

        citrusClient.init(Const.SIGNUP_ID, Const.SIGNUP_SECRET, Const.SIGNIN_ID, Const.SIGNIN_SECRET, Const.VANITY, Const.environment);

        citrusClient.enableAutoOtpReading(false);
        citrusConfig = CitrusConfig.getInstance();
        citrusConfig.setColorPrimary(Const.colorPrimary);
        citrusConfig.setColorPrimaryDark(Const.colorPrimaryDark);
        citrusConfig.setTextColorPrimary(Const.textColor);

        showUI();


        snackBarParent = new View(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 200);
        layoutParams.gravity = Gravity.BOTTOM;
        snackBarParent.setLayoutParams(layoutParams);
        frameLayout.addView(snackBarParent);
    }

    private void showUI() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.container, UIActivityFragment.newInstance());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void onUserManagementClicked(View view) {
        UserManagementFragment fragment = UserManagementFragment.newInstance(this);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.container, fragment);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void onGuestPaymentClicked(View view) {
    }

    @Override
    public void onShowWalletScreen() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.container, WalletPaymentFragment.newInstance());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override
    public void onPaymentComplete(TransactionResponse transactionResponse) {
        if (transactionResponse != null) {
//            Utils.showToast(mContext, transactionResponse.getMessage());
            showSnackBar(transactionResponse.getMessage());
        }
    }

    @Override
    public void onPaymentTypeSelected(Utils.PaymentType paymentType, Amount amount) {
        if (paymentType == Utils.PaymentType.CITRUS_CASH) {

            try {
                citrusClient.payUsingCitrusCash(new PaymentType.CitrusCash(amount, Const.BILL_URL), new Callback<TransactionResponse>() {
                    @Override
                    public void success(TransactionResponse transactionResponse) {
                        Utils.showToast(getApplicationContext(), transactionResponse.getMessage());
                    }

                    @Override
                    public void error(CitrusError error) {
                        Utils.showToast(getApplicationContext(), error.getMessage());
                    }
                });
            } catch (CitrusException e) {
                e.printStackTrace();

//                Utils.showToast(UIActivity.this, e.getMessage());
                showSnackBar(e.getMessage());
            }
        } else if (paymentType == Utils.PaymentType.NEW_CITRUS_CASH) {

            try {
                citrusClient.prepaidPay(new PaymentType.CitrusCash(amount, Const.BILL_URL), new Callback<PaymentResponse>() {
                    @Override
                    public void success(PaymentResponse paymentResponse) {
                        Utils.showToast(getApplicationContext(), paymentResponse.getMessage());
                    }

                    @Override
                    public void error(CitrusError error) {
                        Utils.showToast(getApplicationContext(), error.getMessage());
                    }

                });
            } catch (CitrusException e) {
                e.printStackTrace();

                Utils.showToast(UIActivity.this, e.getMessage());
            }
        } else {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.container, CardPaymentFragment.newInstance(paymentType, amount));

            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onPaymentTypeSelected(Utils.DPRequestType dpRequestType, Amount
            originalAmount, String couponCode, Amount alteredAmount) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.container, CardPaymentFragment.newInstance(dpRequestType, originalAmount, couponCode, alteredAmount));
        fragmentTransaction.addToBackStack(null);
        fragmentManager.beginTransaction().commit();
    }

    @Override
    public void onCashoutSelected(CashoutInfo cashoutInfo) {
        citrusClient.saveCashoutInfo(cashoutInfo, new Callback<CitrusResponse>() {
            @Override
            public void success(CitrusResponse citrusResponse) {
//                Utils.showToast(getApplicationContext(), citrusResponse.getMessage());
                showSnackBar(citrusResponse.getMessage());
            }

            @Override
            public void error(CitrusError error) {
//                Utils.showToast(getApplicationContext(), error.getMessage());
                showSnackBar(error.getMessage());
            }
        });

        citrusClient.cashout(cashoutInfo, new Callback<PaymentResponse>() {
            @Override
            public void success(PaymentResponse paymentResponse) {
//                Utils.showToast(getApplicationContext(), paymentResponse.toString());
                String message = "";
                if (paymentResponse.getStatus() == CitrusResponse.Status.SUCCESSFUL) {
                    message = "Cashout Was Successful";
                }
                showSnackBar(message);
            }

            @Override
            public void error(CitrusError error) {
//                Utils.showToast(getApplicationContext(), error.getMessage());
                showSnackBar(error.getMessage());
            }
        });
    }

    @Override
    public void onUpdateProfileSelected() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.container, UpdateProfileFragment.newInstance());

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void onWalletPaymentClicked(View view) {
        onShowWalletScreen();
    }

    @Override
    public void showSnackBar(String message) {
        Snackbar.make(snackBarParent, message, Snackbar.LENGTH_LONG).show();
    }
}
