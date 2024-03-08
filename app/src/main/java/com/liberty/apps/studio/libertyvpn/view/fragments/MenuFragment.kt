package com.liberty.apps.studio.libertyvpn.view.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.facebook.ads.*
import com.google.android.gms.ads.AdRequest
import com.liberty.apps.studio.libertyvpn.AppSettings
import com.liberty.apps.studio.libertyvpn.databinding.FragmentMenuBinding
import com.liberty.apps.studio.libertyvpn.dialogs.RateDialog
import com.liberty.apps.studio.libertyvpn.view.activites.SubscriptionActivity
import com.liberty.apps.studio.libertyvpn.view.activites.faq_activity
import com.liberty.apps.studio.libertyvpn.view.activites.loadingWebData
import com.liberty.apps.studio.libertyvpn.R

class MenuFragment : Fragment() {

    private lateinit var mContext: Context
    private var binding: FragmentMenuBinding? = null

    //facebook and google ads
    private var nativeAdLayout: NativeAdLayout? = null
    private var adView: LinearLayout? = null
    private var nativeBannerAd: NativeBannerAd? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFacebookSdk()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMenuBinding.inflate(layoutInflater, container, false)
        loadBannerAd()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding!!.drawerPremiumItem.setOnClickListener {
            val subscriptionActivity = Intent(mContext, SubscriptionActivity::class.java)
            startActivity(subscriptionActivity)
        }
        binding!!.drawerShareItem.setOnClickListener {
            shareApp()
        }
        binding!!.drawerPrivacyItem.setOnClickListener {
            privacyPolicyLink()
        }
        binding!!.drawerAboutItem.setOnClickListener {
            showAboutDialog()
        }
        binding!!.drawerRateItem.setOnClickListener {
            val rateDialog = RateDialog(requireActivity())
            rateDialog.show()
        }
        binding!!.drawerFaqItem.setOnClickListener {
            val faq_intent = Intent(mContext, faq_activity::class.java)
            startActivity(faq_intent)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun showAboutDialog() {
        val dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.dialog_about)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        (dialog.findViewById<View>(R.id.bt_close) as RelativeLayout).setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun privacyPolicyLink() {
        val webActivity = Intent(mContext, loadingWebData::class.java)
        webActivity.putExtra("activityName", "Privacy Policy")
        webActivity.putExtra("webLink", resources.getString(R.string.privacy_policy_link))
        startActivity(webActivity)
    }

    fun shareApp() {
        val msg = StringBuilder()
        msg.append(getString(R.string.share_msg))
        msg.append("\n")
        msg.append("https://play.google.com/store/apps/details?id=com.snaptube.savevideos.all.videos.downloader2020.allvideodownload")
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg.toString())
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    private fun loadBannerAd() {
        if (!AppSettings.isUserPaid) {
            binding!!.menuBannerBlock.visibility = View.VISIBLE
            binding!!.menuBannerContainerAdmob.visibility = View.GONE
            binding!!.menuBannerContainerFacebook.visibility = View.GONE

            if (AppSettings.enableAdmobAds) {
                binding!!.menuBannerContainerAdmob.visibility = View.VISIBLE
                loadAdmobBannerAd()

            } else if (AppSettings.enableFacebookAds) {
                binding!!.menuBannerContainerFacebook.visibility = View.VISIBLE
                loadFacebookBannerAd()
            }
        } else {
            binding!!.menuBannerBlock.visibility = View.GONE
        }
    }

    private fun loadAdmobBannerAd() {
        val adview = binding!!.menuBannerContainerAdmob
        val adRequest = AdRequest.Builder().build()
        adview.loadAd(adRequest)
    }

    private fun loadFacebookBannerAd() {
        nativeBannerAd =
            NativeBannerAd(mContext, resources.getString(R.string.facebook_native_banner_id))

        val listener = object : NativeAdListener {
            override fun onError(p0: Ad?, p1: AdError?) {}

            override fun onAdLoaded(p0: Ad?) {
                if (nativeBannerAd == null || nativeBannerAd != p0) {
                    return;
                }
                inflateAd(nativeBannerAd!!);
            }

            override fun onAdClicked(p0: Ad?) {}

            override fun onLoggingImpression(p0: Ad?) {}

            override fun onMediaDownloaded(p0: Ad?) {}
        }
        nativeBannerAd!!.loadAd(
            nativeBannerAd!!.buildLoadAdConfig()
                .withAdListener(listener)
                .build()
        );
    }

    private fun inflateAd(nativeBannerAd: NativeBannerAd) {
        // Unregister last ad
        nativeBannerAd.unregisterView()

        // Add the Ad view into the ad container.
        nativeAdLayout = binding!!.menuBannerContainerFacebook
        val inflater = LayoutInflater.from(mContext)
        // Inflate the Ad view.  The layout referenced is the one you created in the last step.
        adView = inflater.inflate(
            R.layout.native_banner_ad_unit,
            nativeAdLayout,
            false
        ) as LinearLayout
        nativeAdLayout!!.addView(adView)

        // Add the AdChoices icon
        val adChoicesContainer: RelativeLayout = adView!!.findViewById(R.id.ad_choices_container)
        val adOptionsView =
            AdOptionsView(mContext, nativeBannerAd, nativeAdLayout)
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)

        // Create native UI using the ad metadata.
        val nativeAdTitle: TextView = adView!!.findViewById(R.id.native_ad_title)
        val nativeAdSocialContext: TextView = adView!!.findViewById(R.id.native_ad_social_context)
        val sponsoredLabel: TextView = adView!!.findViewById(R.id.native_ad_sponsored_label)
        val nativeAdIconView: MediaView = adView!!.findViewById(R.id.native_icon_view)
        val nativeAdCallToAction: Button = adView!!.findViewById(R.id.native_ad_call_to_action)

        // Set the Text.
        nativeAdCallToAction.setText(nativeBannerAd.adCallToAction)
        nativeAdCallToAction.setVisibility(
            if (nativeBannerAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        )
        nativeAdTitle.setText(nativeBannerAd.advertiserName)
        nativeAdSocialContext.setText(nativeBannerAd.adSocialContext)
        sponsoredLabel.setText(nativeBannerAd.sponsoredTranslation)

        // Register the Title and CTA button to listen for clicks.
        val clickableViews: MutableList<View> = ArrayList()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)
        nativeBannerAd.registerViewForInteraction(adView, nativeAdIconView, clickableViews)
    }

    private fun initFacebookSdk() {
        AudienceNetworkAds.initialize(mContext);
    }
}