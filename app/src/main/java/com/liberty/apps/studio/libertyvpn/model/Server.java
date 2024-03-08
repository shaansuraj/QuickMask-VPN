package com.liberty.apps.studio.libertyvpn.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Server implements Parcelable {

    public String hostName;
    public String ipAddress;
    public int score;
    public String ping;
    public long speed;
    public String countryLong;
    public String countryShort;
    public long vpnSessions;
    public long uptime;
    public long totalUsers;
    public String totalTraffic;
    public String logType;
    public String operator;
    public String message;
    public String ovpnConfigData;
    public int port;
    public String protocol;
    public boolean isStarred;

    public Server() {}

    public Server(String hostName, String ipAddress, String ping, long speed, String countryLong, String countryShort, String ovpnConfigData, int port, String protocol) {
        this.hostName = hostName;
        this.ipAddress = ipAddress;
        this.ping = ping;
        this.speed = speed;
        this.countryLong = countryLong;
        this.countryShort = countryShort;
        this.ovpnConfigData = ovpnConfigData;
        this.port = port;
        this.protocol = protocol;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getPing() {
        return ping;
    }

    public void setPing(String ping) {
        this.ping = ping;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public String getCountryLong() {
        return countryLong;
    }

    public void setCountryLong(String countryLong) {
        this.countryLong = countryLong;
    }

    public String getCountryShort() {
        return countryShort;
    }

    public void setCountryShort(String countryShort) {
        this.countryShort = countryShort;
    }

    public long getVpnSessions() {
        return vpnSessions;
    }

    public void setVpnSessions(long vpnSessions) {
        this.vpnSessions = vpnSessions;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public String getTotalTraffic() {
        return totalTraffic;
    }

    public void setTotalTraffic(String totalTraffic) {
        this.totalTraffic = totalTraffic;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOvpnConfigData() {
        return ovpnConfigData;
    }

    public void setOvpnConfigData(String ovpnConfigData) {
        this.ovpnConfigData = ovpnConfigData;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.hostName);
        dest.writeString(this.ipAddress);
        dest.writeInt(this.score);
        dest.writeString(this.ping);
        dest.writeLong(this.speed);
        dest.writeString(this.countryLong);
        dest.writeString(this.countryShort);
        dest.writeLong(this.vpnSessions);
        dest.writeLong(this.uptime);
        dest.writeLong(this.totalUsers);
        dest.writeString(this.totalTraffic);
        dest.writeString(this.logType);
        dest.writeString(this.operator);
        dest.writeString(this.message);
        dest.writeString(this.ovpnConfigData);
        dest.writeInt(this.port);
        dest.writeString(this.protocol);
        dest.writeByte(this.isStarred ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.hostName = source.readString();
        this.ipAddress = source.readString();
        this.score = source.readInt();
        this.ping = source.readString();
        this.speed = source.readLong();
        this.countryLong = source.readString();
        this.countryShort = source.readString();
        this.vpnSessions = source.readLong();
        this.uptime = source.readLong();
        this.totalUsers = source.readLong();
        this.totalTraffic = source.readString();
        this.logType = source.readString();
        this.operator = source.readString();
        this.message = source.readString();
        this.ovpnConfigData = source.readString();
        this.port = source.readInt();
        this.protocol = source.readString();
        this.isStarred = source.readByte() != 0;
    }

    protected Server(Parcel in) {
        this.hostName = in.readString();
        this.ipAddress = in.readString();
        this.score = in.readInt();
        this.ping = in.readString();
        this.speed = in.readLong();
        this.countryLong = in.readString();
        this.countryShort = in.readString();
        this.vpnSessions = in.readLong();
        this.uptime = in.readLong();
        this.totalUsers = in.readLong();
        this.totalTraffic = in.readString();
        this.logType = in.readString();
        this.operator = in.readString();
        this.message = in.readString();
        this.ovpnConfigData = in.readString();
        this.port = in.readInt();
        this.protocol = in.readString();
        this.isStarred = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Server> CREATOR = new Parcelable.Creator<Server>() {
        @Override
        public Server createFromParcel(Parcel source) {
            return new Server(source);
        }

        @Override
        public Server[] newArray(int size) {
            return new Server[size];
        }
    };
}
