package com.liberty.apps.studio.libertyvpn.view.fragments

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.ads.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.liberty.apps.studio.libertyvpn.AppSettings
import com.liberty.apps.studio.libertyvpn.CheckInternetConnection
import com.liberty.apps.studio.libertyvpn.SharedPreference
import com.liberty.apps.studio.libertyvpn.databinding.FragmentHomeBinding
import com.liberty.apps.studio.libertyvpn.model.Server
import com.liberty.apps.studio.libertyvpn.utils.toast
import com.liberty.apps.studio.libertyvpn.view.activites.ChangeServerActivity
import com.liberty.apps.studio.libertyvpn.view.activites.SubscriptionActivity
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNThread
import de.blinkt.openvpn.core.VpnStatus
import java.io.IOException
import com.liberty.apps.studio.libertyvpn.R


class HomeFragment : Fragment() {

    private lateinit var mContext: Context

    private var binding: FragmentHomeBinding? = null
    private var connection: CheckInternetConnection? = null
    private var vpnStart = false

    private lateinit var globalServer: Server
    private lateinit var vpnThread: OpenVPNThread
    private lateinit var vpnService: OpenVPNService
    private lateinit var sharedPreference: SharedPreference

    private var isServerSelected: Boolean = false

    //facebook and google ads
    private var nativeAdLayout: NativeAdLayout? = null
    private var adView: LinearLayout? = null
    private var nativeBannerAd: NativeBannerAd? = null
    private var admobInterstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd? = null
    private var facebookInterstitialAd: InterstitialAd? = null

    private val getServerResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedServer = result.data!!.getParcelableExtra<Server>("serverextra");
                globalServer = selectedServer!!

                //update selected server
                binding!!.serverFlagName.text = selectedServer.getCountryLong()
                binding!!.serverFlagDes.text = selectedServer.getIpAddress()

                binding!!.connectionIp.text = selectedServer.getIpAddress()
                isServerSelected = true
            }
        }

    private val vpnResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { vpnResult ->
            if (vpnResult.resultCode == Activity.RESULT_OK) {
                //Permission granted, start the VPN
                startVpn()
            } else {
                mContext.toast("For a successful VPN connection, permission must be granted.")
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vpnThread = OpenVPNThread()
        vpnService = OpenVPNService()
        connection = CheckInternetConnection()
        sharedPreference = SharedPreference(mContext)
        initFacebookSdk()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Checking is vpn already running or not
        isServiceRunning()
        VpnStatus.initLogCache(mContext.cacheDir)

        binding!!.serverSelectionBlock.setOnClickListener {
            if (!vpnStart) {
                getServerResult.launch(
                    Intent(mContext, ChangeServerActivity::class.java)
                )
            } else {
                mContext.toast(resources.getString(R.string.disconnect_first))
            }
        }

        binding!!.connectionButtonBlock.setOnClickListener {
            if (!vpnStart && isServerSelected) {
                prepareVpn()
            } else if (!isServerSelected && !vpnStart) {
                getServerResult.launch(
                    Intent(mContext, ChangeServerActivity::class.java)
                )
            } else if (vpnStart && !isServerSelected) {
                mContext.toast(resources.getString(R.string.disconnect_first))
            } else {
                mContext.toast("Unable to connect the VPN")
            }
        }

        binding!!.disconnectButton.setOnClickListener {
            if (vpnStart) {
                confirmDisconnect()
            }
        }
        binding!!.subscriptionButton.setOnClickListener {
            val subscriptionActivity = Intent(mContext, SubscriptionActivity::class.java)
            startActivity(subscriptionActivity)
        }

        loadBannerAd()
        if (!vpnStart) {
            loadInterstitialAd()
        }
    }

    override fun onDestroyView() {
        binding = null
        if (facebookInterstitialAd != null) {
            facebookInterstitialAd!!.destroy()
        }
        super.onDestroyView()
    }

    private fun isServiceRunning() {
        setStatus(OpenVPNService.getStatus())
    }

    private fun getInternetStatus(): Boolean {
        return connection!!.netCheck(mContext)
    }

    fun setStatus(connectionState: String?) {
        if (connectionState != null) when (connectionState) {
            "DISCONNECTED" -> {
                status("Connect")
                vpnStart = false
                OpenVPNService.setDefaultStatus()
                binding!!.connectionTextStatus.text = "Disconnected"
            }
            "CONNECTED" -> {
                vpnStart = true // it will use after restart this activity
                status("Connected")
                binding!!.connectionTextStatus.text = "Connected"
            }
            "WAIT" -> binding!!.connectionTextStatus.text = "Waiting for server connection"
            "AUTH" -> binding!!.connectionTextStatus.text = "Authenticating server"
            "RECONNECTING" -> {
                status("Connecting")
                binding!!.connectionTextStatus.text = "Reconnecting..."
            }
            "NONETWORK" -> binding!!.connectionTextStatus.text = "No network connection"
        }
    }

    private fun status(status: String) {
        //update UI here
        when (status) {
            "Connect" -> {
                onDisconnectDone()
            }
            "Connecting" -> {
            }
            "Connected" -> {
                onConnectionDone()
            }
            "tryDifferentServer" -> {
            }
            "loading" -> {
            }
            "invalidDevice" -> {
            }
            "authenticationCheck" -> {
            }
        }
    }

    private fun prepareVpn() {
        if (!vpnStart) {
            if (getInternetStatus()) {
                val intent = VpnService.prepare(context)
                if (intent != null) {
                    vpnResult.launch(intent)
                } else {
                    startVpn()
                }
                status("Connecting")
            } else {
                mContext.toast("No Internet Connection")
            }
        } else if (stopVpn()) {
            mContext.toast("Disconnect Successfully")
        }
    }

    private fun confirmDisconnect() {
        val builder = AlertDialog.Builder(
            mContext
        )
        builder.setMessage(mContext.getString(R.string.connection_close_confirm))
        builder.setPositiveButton(
            mContext.getString(R.string.yes)
        ) { dialog, id -> stopVpn() }
        builder.setNegativeButton(
            mContext.getString(R.string.no)
        ) { dialog, id ->
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun stopVpn(): Boolean {
        try {
            OpenVPNThread.stop()
            status("Connect")
            vpnStart = false
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun startVpn() {
        try {
            val conf = globalServer.getOvpnConfigData()
            OpenVpnApi.startVpn(context, conf, globalServer.getCountryShort(), "vpn", "vpn")
            binding!!.connectionTextStatus.text = "Connecting..."
            vpnStart = true
        } catch (exception: IOException) {
            exception.printStackTrace()
        } catch (exception: RemoteException) {
            exception.printStackTrace()
        }

        showInterstitialAd()
    }

    /**
     * Broadcast receivers ***************************
     */

    var broadcastReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                setStatus(intent.getStringExtra("state"))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            try {
                var duration = intent.getStringExtra("duration")
                var lastPacketReceive = intent.getStringExtra("lastPacketReceive")
                var byteIn = intent.getStringExtra("byteIn")
                var byteOut = intent.getStringExtra("byteOut")
                if (duration == null) duration = "00:00:00"
                if (lastPacketReceive == null) lastPacketReceive = "0"
                if (byteIn == null) byteIn = "0.0"
                if (byteOut == null) byteOut = "0.0"
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Update status UI
     * @param duration: running time
     * @param lastPacketReceive: last packet receive time
     * @param byteIn: incoming data
     * @param byteOut: outgoing data
     */
    fun updateConnectionStatus(
        duration: String,
        lastPacketReceive: String,
        byteIn: String,
        byteOut: String
    ) {
        binding!!.vpnConnectionTime.setText("$duration")
        binding!!.downloadSpeed.text = "$byteIn"
        binding!!.uploadSpeed.text = "$byteOut"
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(mContext).registerReceiver(
            broadcastReceiver!!, IntentFilter("connectionState")
        )
        if (!this::globalServer.isInitialized) {
            if (sharedPreference.isPrefsHasServer) {
                globalServer = sharedPreference.server
                //update selected server
                binding!!.serverFlagName.text = globalServer.getCountryLong()
                binding!!.serverFlagDes.text = globalServer.getIpAddress()

                binding!!.connectionIp.text = globalServer.getIpAddress()
                isServerSelected = true

            } else {
                binding!!.serverFlagName.text = resources.getString(R.string.country_name)
                binding!!.serverFlagDes.text = resources.getString(R.string.IP_address)

                binding!!.connectionIp.text = resources.getString(R.string.IP_address)
            }
        }
        super.onResume()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(
            broadcastReceiver!!
        )
        super.onPause()
    }

    /**
     * Save current selected server on local shared preference
     */
    override fun onStop() {
        if (this::globalServer.isInitialized) {
            sharedPreference.saveServer(globalServer)
        }
        super.onStop()
    }

    private fun onConnectionDone() {
        binding!!.connectionTextBlock.visibility = View.GONE
        binding!!.connectionButtonBlock.visibility = View.GONE
        binding!!.serverSelectionBlock.visibility = View.GONE

        binding!!.afterConnectionDetailBlock.visibility = View.VISIBLE
        binding!!.disconnectButton.visibility = View.VISIBLE
    }

    private fun onDisconnectDone() {
        binding!!.connectionTextBlock.visibility = View.VISIBLE
        binding!!.connectionButtonBlock.visibility = View.VISIBLE
        binding!!.serverSelectionBlock.visibility = View.VISIBLE
        binding!!.afterConnectionDetailBlock.visibility = View.GONE
        binding!!.disconnectButton.visibility = View.GONE
    }

    private fun loadBannerAd() {
        if (!AppSettings.isUserPaid) {
            binding!!.adBlock.visibility = View.VISIBLE
            binding!!.bannerContainerAdmob.visibility = View.GONE
            binding!!.bannerContainerFacebook.visibility = View.GONE

            if (AppSettings.enableAdmobAds) {
                binding!!.bannerContainerAdmob.visibility = View.VISIBLE
                loadAdmobBannerAd()

            } else if (AppSettings.enableFacebookAds) {
                binding!!.bannerContainerFacebook.visibility = View.VISIBLE
                loadFacebookBannerAd()
            }
        } else {
            binding!!.adBlock.visibility = View.GONE
        }
    }

    private fun loadAdmobBannerAd() {
        val adview = binding!!.bannerContainerAdmob
        val adRequest = AdRequest.Builder().build()
        adview.loadAd(adRequest)
    }

    private fun loadFacebookBannerAd() {
        nativeBannerAd =
            NativeBannerAd(mContext, resources.getString(R.string.facebook_native_banner_id))

        val listener = object : NativeAdListener {
            override fun onError(p0: Ad?, p1: AdError?) {
            }

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
        nativeAdLayout = binding!!.bannerContainerFacebook
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

    private fun loadInterstitialAd() {
        if (!AppSettings.isUserPaid) {
            if (AppSettings.enableAdmobAds) {
                var adRequest = AdRequest.Builder().build()
                com.google.android.gms.ads.interstitial.InterstitialAd.load(
                    mContext,
                    resources.getString(R.string.admob_interstitial_id),
                    adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            admobInterstitialAd = null

                            Log.w("Asdasd", "onError: ${p0!!.message}")
                        }

                        override fun onAdLoaded(p0: com.google.android.gms.ads.interstitial.InterstitialAd) {
                            super.onAdLoaded(p0)
                            admobInterstitialAd = p0
                            admobInterstitialAd!!.fullScreenContentCallback = object :
                                FullScreenContentCallback() {
                                override fun onAdClicked() {
                                    super.onAdClicked()
                                }

                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    admobInterstitialAd = null
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {
                                    super.onAdFailedToShowFullScreenContent(p0)
                                }

                                override fun onAdImpression() {
                                    super.onAdImpression()
                                }

                                override fun onAdShowedFullScreenContent() {
                                    super.onAdShowedFullScreenContent()
                                    admobInterstitialAd = null
                                }
                            }
                        }
                    }
                );
            } else if (AppSettings.enableFacebookAds) {
                facebookInterstitialAd =
                    InterstitialAd(mContext, resources.getString(R.string.facebook_interstitial_id))
                val interstitialAdListener = object : InterstitialAdListener {
                    override fun onError(p0: Ad?, p1: AdError?) {
                    }

                    override fun onAdLoaded(p0: Ad?) {
                    }

                    override fun onAdClicked(p0: Ad?) {
                    }

                    override fun onLoggingImpression(p0: Ad?) {
                    }

                    override fun onInterstitialDisplayed(p0: Ad?) {
                    }

                    override fun onInterstitialDismissed(p0: Ad?) {
                    }

                }

                facebookInterstitialAd!!.loadAd(
                    facebookInterstitialAd!!.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build()
                );
            }
        } else {
            admobInterstitialAd = null
            facebookInterstitialAd = null
        }
    }

    private fun showInterstitialAd() {
        if (admobInterstitialAd != null) {
            admobInterstitialAd!!.show(requireActivity())
        } else if (facebookInterstitialAd != null) {
            if (facebookInterstitialAd!!.isAdLoaded) {
                facebookInterstitialAd!!.show()
            }
        }
    }
}