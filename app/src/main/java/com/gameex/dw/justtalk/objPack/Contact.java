package com.gameex.dw.justtalk.objPack;

import android.graphics.Bitmap;
import android.net.Uri;

import com.gameex.dw.justtalk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系人对象，存储单个联系人信息
 */
public class Contact {
    /**
     * 索引
     */
    private String index;
    /**
     * 头像id
     */
    private Uri iconUri;
    /**
     * 头像资源id
     */
    private int iconResource;
    /**
     * 用户头像位图
     */
    private Bitmap iconMap;
    /**
     * 名称
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * gouzao
     *
     * @param index        索引
     * @param iconResource 头像资源id
     * @param name         用户名
     */
    public Contact(String index, int iconResource, String name) {
        this.index = index;
        this.iconResource = iconResource;
        this.name = name;
    }

    /**
     * 构造
     *
     * @param index   索引
     * @param iconMap 头像位图
     * @param name    用户民
     * @param phone   用户手机号
     */
    public Contact(String index, Bitmap iconMap, String name, String phone) {
        this.index = index;
        this.iconMap = iconMap;
        this.name = name;
        this.phone = phone;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Uri getIconUri() {
        return iconUri;
    }

    public void setIconUri(Uri uri) {
        this.iconUri = uri;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }

    public Bitmap getIconMap() {
        return iconMap;
    }

    public void setIconMap(Bitmap iconMap) {
        this.iconMap = iconMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "index='" + index + '\'' +
                ", iconUri=" + iconUri +
                ", iconResource=" + iconResource +
                ", iconMap=" + iconMap +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    /**
     * 设置联系人列表基础信息
     *
     * @return
     */
    public static List<Contact> getBasicContact() {
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact("↑", R.drawable.icon_new_friends, "新的朋友"));
        contacts.add(new Contact("↑", R.drawable.icon_invite_friends, "邀请好友"));
        contacts.add(new Contact("↑", R.drawable.icon_group, "我的群组"));
        return contacts;
    }
}
