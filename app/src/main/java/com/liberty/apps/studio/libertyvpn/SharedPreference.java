package com.liberty.apps.studio.libertyvpn;

import android.content.Context;
import android.content.SharedPreferences;

import com.liberty.apps.studio.libertyvpn.model.Server;

public class SharedPreference {

    private static final String APP_PREFS_NAME = "LibertyVPNPreference";

    private SharedPreferences mPreference;
    private SharedPreferences.Editor mPrefEditor;
    private Context context;

    private static final String SERVER_COUNTRY_LONG = "server_country_long";
    private static final String SERVER_COUNTRY_SHORT = "server_country_short";
    private static final String SERVER_SPEED = "server_speed";
    private static final String SERVER_PING = "server_ping";
    private static final String SERVER_PROTOCOL = "server_protocol";
    private static final String SERVER_IP_ADDRESS = "server_ip";
    private static final String SERVER_HOSTNAME = "server_hostname";
    private static final String SERVER_OVPN = "server_ovpn";
    private static final String SERVER_PORT = "server_port";

    public SharedPreference(Context context) {
        this.mPreference = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE);
        this.mPrefEditor = mPreference.edit();
        this.context = context;
    }

    /**
     * Save server details
     *
     * @param server details of ovpn server
     */
    public void saveServer(Server server) {
        mPrefEditor.putString(SERVER_HOSTNAME, server.getHostName());
        mPrefEditor.putString(SERVER_IP_ADDRESS, server.getIpAddress());
        mPrefEditor.putString(SERVER_COUNTRY_LONG, server.getCountryLong());
        mPrefEditor.putString(SERVER_COUNTRY_SHORT, server.getCountryShort());
        mPrefEditor.putLong(SERVER_SPEED, server.getSpeed());
        mPrefEditor.putString(SERVER_PING, server.getPing());
        mPrefEditor.putString(SERVER_PROTOCOL, server.getProtocol());
        mPrefEditor.putString(SERVER_OVPN, server.getOvpnConfigData());
        mPrefEditor.putInt(SERVER_PORT, server.getPort());
        mPrefEditor.commit();
    }

    /**
     * Get server data from shared preference
     *
     * @return server model object
     */
    public Server getServer() {

        Server server = new Server(
                mPreference.getString(SERVER_HOSTNAME,"Japan"),
                mPreference.getString(SERVER_IP_ADDRESS,"x.x.x.x"),
                mPreference.getString(SERVER_PING,"10ms"),
                mPreference.getLong(SERVER_SPEED,10),
                mPreference.getString(SERVER_COUNTRY_LONG,"Japan"),
                mPreference.getString(SERVER_COUNTRY_SHORT,"Japan"),
                mPreference.getString(SERVER_OVPN,"null"),
                mPreference.getInt(SERVER_PORT,402),
                mPreference.getString(SERVER_PROTOCOL,"UDP")
        );

        return server;
    }

    public Boolean isPrefsHasServer() {
        return mPreference.contains(SERVER_IP_ADDRESS);
    }
}
