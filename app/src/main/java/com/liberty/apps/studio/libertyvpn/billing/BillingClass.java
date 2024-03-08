package com.liberty.apps.studio.libertyvpn.billing;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.liberty.apps.studio.libertyvpn.AppSettings;

import java.util.ArrayList;
import java.util.List;

public class BillingClass implements PurchasesUpdatedListener, BillingClientStateListener {

    private BillingClient billingClient;
    private List<String> skuListSubscriptionsList;
    private List<SkuDetails> skuListFromStore;

    //others
    private boolean isAvailable = false;
    private boolean isListGot = false;
    private Activity refActivity;

    private BillingErrorHandler mCallback;
    private SkuDetailsListener mDetailsCallback;

    //step-1 init
    public BillingClass(Activity activity) {

        refActivity = activity;

        billingClient = BillingClient.newBuilder(activity)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        skuListSubscriptionsList = new ArrayList<>();

        //add all products here (subscriptions)
        skuListSubscriptionsList.add(AppSettings.Companion.getOne_month_subscription_id());
        skuListSubscriptionsList.add(AppSettings.Companion.getThree_month_subscription_id());
        skuListSubscriptionsList.add(AppSettings.Companion.getOne_year_subscription_id());
    }

    //step-2 make connection to store
    public void startConnection() {
        billingClient.startConnection(this);
    }

    public void setmCallback(BillingErrorHandler mCallback, SkuDetailsListener skuDetailsListener) {
        if (this.mCallback == null) {
            this.mCallback = mCallback;
        }
        if (this.mDetailsCallback == null) {
            this.mDetailsCallback = skuDetailsListener;
        }
    }

    //purchase update listener
    //step-5
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && list != null) {
            for (Purchase purchase : list) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.

        } else {
            // Handle any other error codes.
        }
    }

    //step-3
    //state listener
    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

            mCallback.displayErrorMessage("done");

            //proceed, Client is ready
            isAvailable = true;

            SkuDetailsParams.Builder subscriptionParams = SkuDetailsParams.newBuilder();
            SkuDetailsParams.Builder oneTimeProductsParams = SkuDetailsParams.newBuilder();

            subscriptionParams.setSkusList(skuListSubscriptionsList).setType(BillingClient.SkuType.SUBS);

            if (!skuListSubscriptionsList.isEmpty()) {
                billingClient.querySkuDetailsAsync(subscriptionParams.build(), new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {

                        //subscription list
                        skuListFromStore = list;
                        isListGot = true;
                        mDetailsCallback.subscriptionsDetailList(list);
                    }
                });
            }

        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE || billingResult.getResponseCode() == BillingClient.BillingResponseCode.ERROR || billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
            mCallback.displayErrorMessage("error");
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        //restart, No connection to billing service
        isAvailable = false;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public boolean isListGot() {
        return isListGot;
    }

    //step-4
    public String purchaseSubscriptionItemByPos(int itemIndex) {

        int index = itemIndex;

        for (int i = 0; i < skuListFromStore.size(); i++) {
            if (skuListFromStore.get(i).getSku().equals(skuListSubscriptionsList.get(itemIndex))) {
                index = i;
            }
        }

        if (billingClient.isReady()) {

            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuListFromStore.get(index))
                    .build();

            BillingResult responseCode = billingClient.launchBillingFlow(refActivity, billingFlowParams);

            switch (responseCode.getResponseCode()) {

                case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                    mCallback.displayErrorMessage("Billing not supported for type of request");
                    return "Billing not supported for type of request";


                case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                    return "";

                case BillingClient.BillingResponseCode.ERROR:
                    mCallback.displayErrorMessage("Error completing request");
                    return "Error completing request";

                case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                    return "Error processing request.";

                case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                    return "Selected item is already owned";

                case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                    return "Item not available";

                case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                    return "Play Store service is not connected now";

                case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                    return "Timeout";

                case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                    mCallback.displayErrorMessage("Network error.");
                    return "Network Connection down";

                case BillingClient.BillingResponseCode.USER_CANCELED:
                    mCallback.displayErrorMessage("Request Canceled");
                    return "Request Canceled";

                case BillingClient.BillingResponseCode.OK:
                    return "Subscribed Successfully";
            }
        }

        return "";
    }

    void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            AcknowledgePurchaseParams acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                @Override
                public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {

                }
            });
        }
    }


    public boolean isSubscribedToSubscriptionItem(String sku) {
        if (skuListSubscriptionsList != null) {
            Purchase.PurchasesResult result = billingClient.queryPurchases(BillingClient.SkuType.SUBS);

            if (result.getResponseCode() == BillingClient.BillingResponseCode.OK && result.getPurchasesList() != null) {
                for (Purchase purchase :
                        result.getPurchasesList()) {

                    if (purchase.getSku().equals(sku)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }

        return false;
    }

    //message interface
    public interface BillingErrorHandler {
        void displayErrorMessage(String message);
    }

    public interface SkuDetailsListener {
        void subscriptionsDetailList(List<SkuDetails> skuDetailsList);
    }
}