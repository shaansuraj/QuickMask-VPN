package com.liberty.apps.studio.libertyvpn.view.activites;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.billingclient.api.SkuDetails;
import com.liberty.apps.studio.libertyvpn.R;
import com.liberty.apps.studio.libertyvpn.billing.BillingClass;

import java.util.List;

public class SubscriptionActivity extends AppCompatActivity implements BillingClass.BillingErrorHandler, BillingClass.SkuDetailsListener {

    private ConstraintLayout oneMonthCard, threeMonthsCard, oneYearCard;
    private TextView oneMonthText, threeMonthsText, oneYearText;
    private TextView oneMonthPayment, threeMonthsPayment, oneYearPayment;
    private TextView oneMonthPaymentText, threeMonthsPaymentText, oneYearPaymentText;
    private ImageButton oneMonthCheck, threeMonthsCheck, oneYearCheck;
    private TextView noThanksText, cancelSubscriptionDetailsText;
    private ImageView backButton;

    private BillingClass billingClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        initComponents();

        billingClass = new BillingClass(SubscriptionActivity.this);
        billingClass.setmCallback(this, this);
        billingClass.startConnection();

        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    /*
     * Views Initialization
     * */
    private void initComponents() {
        noThanksText = findViewById(R.id.no_thanks_text);
        cancelSubscriptionDetailsText = findViewById(R.id.cancel_subscription_detail_text);

        oneMonthCard = findViewById(R.id.one_month_card);
        threeMonthsCard = findViewById(R.id.three_months_card);
        oneYearCard = findViewById(R.id.one_year_card);

        //        One Month Card Details
        oneMonthText = findViewById(R.id.one_month_text);
        oneMonthPayment = findViewById(R.id.one_month_payment);
        oneMonthPaymentText = findViewById(R.id.one_month_payment_text);
        oneMonthCheck = findViewById(R.id.one_month_check);

        //        Three Months Card Details
        threeMonthsText = findViewById(R.id.three_months_text);
        threeMonthsPayment = findViewById(R.id.three_months_payment);
        threeMonthsPaymentText = findViewById(R.id.three_months_payment_text);
        threeMonthsCheck = findViewById(R.id.three_months_check);

        //        One Year Card Details
        oneYearText = findViewById(R.id.one_year_text);
        oneYearPayment = findViewById(R.id.one_year_payment);
        oneYearPaymentText = findViewById(R.id.one_year_payment_text);
        oneYearCheck = findViewById(R.id.one_year_check);

        backButton = findViewById(R.id.subscription_back_btn);
    }


    /*
     * Change to background of the cards to show unselect the card...
     * */
    private void unselectCards() {
        oneMonthCard.setBackground(getResources().getDrawable(R.drawable.subscription_card_bg_light));
        threeMonthsCard.setBackground(getResources().getDrawable(R.drawable.subscription_card_bg_light));
        oneYearCard.setBackground(getResources().getDrawable(R.drawable.subscription_card_bg_light));
    }

    /*
     * Change the Corresponding criteria after card1 Selection
     * */
    private void card1Details() {
        //        One Month Card Details
        oneMonthText.setTextColor(this.getResources().getColor(R.color.white));
        oneMonthPayment.setTextColor(this.getResources().getColor(R.color.white));
        oneMonthPaymentText.setTextColor(this.getResources().getColor(R.color.white));
        oneMonthCheck.setImageResource(R.drawable.ic_check_circle);
    }

    /*
     * Change the Corresponding criteria after card2 Selection
     * */
    private void card2Details() {
        //        Three Months Card Details
        threeMonthsText.setTextColor(this.getResources().getColor(R.color.white));
        threeMonthsPayment.setTextColor(this.getResources().getColor(R.color.white));
        threeMonthsPaymentText.setTextColor(this.getResources().getColor(R.color.white));
        threeMonthsCheck.setImageResource(R.drawable.ic_check_circle);
    }

    /*
     * Change the Corresponding criteria after card3 Selection
     * */
    private void card3Details() {
        //        One Year Card Details
        oneYearText.setTextColor(this.getResources().getColor(R.color.white));
        oneYearPayment.setTextColor(this.getResources().getColor(R.color.white));
        oneYearPaymentText.setTextColor(this.getResources().getColor(R.color.white));
        oneYearCheck.setImageResource(R.drawable.ic_check_circle);
    }

    /*
     * Change the Corresponding criteria of the cards
     * after the unselection of the cards
     * */
    private void cardsDetails() {
        //        One Month Card Details
        oneMonthText.setTextColor(this.getResources().getColor(R.color.black));
        oneMonthPayment.setTextColor(this.getResources().getColor(R.color.black));
        oneMonthPaymentText.setTextColor(this.getResources().getColor(R.color.black));
        oneMonthCheck.setImageResource(R.drawable.empty_circle_subscription);

        //        Three Months Card Details
        threeMonthsText.setTextColor(this.getResources().getColor(R.color.black));
        threeMonthsPayment.setTextColor(this.getResources().getColor(R.color.black));
        threeMonthsPaymentText.setTextColor(this.getResources().getColor(R.color.black));
        threeMonthsCheck.setImageResource(R.drawable.empty_circle_subscription);

        //        One Year Card Details
        oneYearText.setTextColor(this.getResources().getColor(R.color.black));
        oneYearPayment.setTextColor(this.getResources().getColor(R.color.black));
        oneYearPaymentText.setTextColor(this.getResources().getColor(R.color.black));
        oneYearCheck.setImageResource(R.drawable.empty_circle_subscription);
    }

    private void initClickListeners() {
        /*
         * Clicks Implementation
         * */
        oneMonthCard.setOnClickListener(view -> {
            unselectCards();
            cardsDetails();
            card1Details();
            oneMonthCard.setBackground(getResources().getDrawable(R.drawable.subscription_card_bg_dark));
            try {
                billingClass.purchaseSubscriptionItemByPos(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        threeMonthsCard.setOnClickListener(view -> {
            unselectCards();
            cardsDetails();
            card2Details();
            threeMonthsCard.setBackground(getResources().getDrawable(R.drawable.subscription_card_bg_dark));
            try {
                billingClass.purchaseSubscriptionItemByPos(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        oneYearCard.setOnClickListener(view -> {
            unselectCards();
            cardsDetails();
            card3Details();
            oneYearCard.setBackground(getResources().getDrawable(R.drawable.subscription_card_bg_dark));
            try {
                billingClass.purchaseSubscriptionItemByPos(2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        noThanksText.setOnClickListener(view -> finish());

        cancelSubscriptionDetailsText.setOnClickListener(v -> {
            Intent webActivity = new Intent(SubscriptionActivity.this, loadingWebData.class);
            webActivity.putExtra("activityName", "Privacy Policy");
            webActivity.putExtra("webLink", getString(R.string.privacy_policy_link));
            startActivity(webActivity);
        });
    }

    @Override
    public void displayErrorMessage(String message) {
        if (message.equals("done")) {
            initClickListeners();
        } else if (message.equals("error")) {
            Toast.makeText(SubscriptionActivity.this, "Error getting billing services", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(SubscriptionActivity.this, "Error getting billing services", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void subscriptionsDetailList(List<SkuDetails> skuDetailsList) {
        for (int i = 0; i < skuDetailsList.size(); i++) {
            switch (i) {
                case 0:
                    try {
                        oneMonthPayment.setText(skuDetailsList.get(i).getPrice());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    try {
                        threeMonthsPayment.setText(skuDetailsList.get(i).getPrice());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        oneYearPayment.setText(skuDetailsList.get(i).getPrice());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}