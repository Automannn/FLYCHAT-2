package com.gameex.dw.justtalk.objPack;

import android.net.Uri;

import java.io.Serializable;

public class User implements Serializable {
    /**
     * 用户头像uri
     */
    private Uri uri;
    /**
     * 用户头像资源id
     */
    private Integer iconSourceId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 手机号
     */
    private String phone;

    public User() {
    }

    /**
     * 手机注册
     *
     * @param phone 手机号
     */
    public User(String phone) {
        this.phone = phone;
    }

    /**
     * 显示搜索结果
     *
     * @param username 用户名
     * @param phone    手机号
     */
    public User(String username, String phone) {
        this.username = username;
        this.phone = phone;
    }

    /**
     * 构造包含头像uri
     *
     * @param uri   头像uri
     * @param phone 手机号
     */
    public User(Uri uri, String phone) {
        this.uri = uri;
        this.phone = phone;
    }

    /**
     * 构造包含头像资源id
     *
     * @param iconSourceId 头像资源id
     * @param phone        手机号
     */
    public User(Integer iconSourceId, String phone) {
        this.iconSourceId = iconSourceId;
        this.phone = phone;
    }

    /**
     * 构造
     *
     * @param username 用户名
     * @param password 密码
     * @param phone    手机号
     */
    public User(String username, String password, String phone) {
        this.username = username;
        this.password = password;
        this.phone = phone;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Integer getIconSourceId() {
        return iconSourceId;
    }

    public void setIconSourceId(int iconSourceId) {
        this.iconSourceId = iconSourceId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "User{" +
                "uri=" + uri +
                ", iconSourceId=" + iconSourceId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
