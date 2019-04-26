package com.gameex.dw.justtalk.ObjPack;

import android.net.Uri;

import com.gameex.dw.justtalk.BottomBarActivity;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.util.DataUtil;

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
     * 名称
     */
    private String name;

    public Contact(String index, Uri uri, String name) {
        this.index = index;
        this.iconUri = uri;
        this.name = name;
    }

    public Contact(String index, int iconResource, String name) {
        this.index = index;
        this.iconResource = iconResource;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "索引='" + index + '\'' +
                ", 头像=" + iconUri +
                ", 名称='" + name + '\'' +
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
        contacts.add(new Contact("我", DataUtil.resourceIdToUri(
                BottomBarActivity.sBottomBarActivity.getPackageName(), R.drawable.icon_user),
                "我的昵称"));
        return contacts;
    }
}
