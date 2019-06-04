package com.gameex.dw.justtalk.objPack;

import android.os.Parcel;
import android.os.Parcelable;

public class MsgInfo implements Parcelable {
    /**
     * 自定义消息体唯一标识
     */
    private String id;
    /**
     * 用户头像
     */
    private String uriPath;
    /**
     * 用户名
     */
    private String username;
    /**
     * 第一条未读消息的日期
     */
    private String date;
    /**
     * 最后一条未读消息
     */
    private String msgLast;
    /**
     * 是否设置不提醒
     */
    private boolean isNotify;
    /**
     * 是否是单聊
     */
    private boolean isSingle;
    /**
     * 用户对象Json字符
     */
    private String userInfoJson;
    /**
     * 群组对象Json字符
     */
    private String groupInfoJson;

    /**
     * 构造
     */
    public MsgInfo() {

    }

    public MsgInfo(String username, String date, String msgLast, boolean isNotify) {
        this.username = username;
        this.date = date;
        this.msgLast = msgLast;
        this.isNotify = isNotify;
    }

    protected MsgInfo(Parcel in) {
        id = in.readString();
        uriPath = in.readString();
        username = in.readString();
        date = in.readString();
        msgLast = in.readString();
        isNotify = in.readByte() != 0;
        isSingle = in.readByte() != 0;
        userInfoJson = in.readString();
        groupInfoJson = in.readString();
    }

    public static final Creator<MsgInfo> CREATOR = new Creator<MsgInfo>() {
        @Override
        public MsgInfo createFromParcel(Parcel in) {
            return new MsgInfo(in);
        }

        @Override
        public MsgInfo[] newArray(int size) {
            return new MsgInfo[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUriPath() {
        return uriPath;
    }

    public void setUriPath(String uriPath) {
        this.uriPath = uriPath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMsgLast() {
        return msgLast;
    }

    public void setMsgLast(String msgLast) {
        this.msgLast = msgLast;
    }

    public boolean isNotify() {
        return isNotify;
    }

    public void setIsNotify(boolean notification) {
        isNotify = notification;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public void setSingle(boolean single) {
        isSingle = single;
    }

    public String getUserInfoJson() {
        return userInfoJson;
    }

    public void setUserInfoJson(String userInfoJson) {
        this.userInfoJson = userInfoJson;
    }

    public String getGroupInfoJson() {
        return groupInfoJson;
    }

    public void setGroupInfoJson(String groupInfoJson) {
        this.groupInfoJson = groupInfoJson;
    }

    @Override
    public String toString() {
        return "MsgInfo{" +
                "id='" + id + '\'' +
                ", uriPath='" + uriPath + '\'' +
                ", username='" + username + '\'' +
                ", date='" + date + '\'' +
                ", msgLast='" + msgLast + '\'' +
                ", isNotify=" + isNotify +
                ", isSingle=" + isSingle +
                ", userInfoJson='" + userInfoJson + '\'' +
                ", groupInfoJson='" + groupInfoJson + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(uriPath);
        parcel.writeString(username);
        parcel.writeString(date);
        parcel.writeString(msgLast);
        parcel.writeByte((byte) (isNotify ? 1 : 0));
        parcel.writeByte((byte) (isSingle ? 1 : 0));
        parcel.writeString(userInfoJson);
        parcel.writeString(groupInfoJson);
    }
}
