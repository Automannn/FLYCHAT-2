package com.gameex.dw.justtalk.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.R;
import com.github.siyamed.shapeimageview.CircularImageView;

import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;

public class UserInfoUtils {
    private static final String TAG = "UserInfoUtils";

    /**
     * 加载用户头像
     */
    public static void initUserIcon(UserInfo userInfo, final Context context, final CircularImageView imgView) {
        if (userInfo != null)
            userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                @Override
                public void gotResult(int i, String s, Bitmap bitmap) {
                    if (i == 0) {
                        Glide.with(context)
                                .load(bitmap)
                                .into(imgView);
                    } else {
                        imgView.setImageDrawable(TextDrawUtil.getRoundTextDraw(getUNFirstPosStr(userInfo)));
                    }
                    LogUtil.d(TAG, "initUserIcon: " + "responseCode = " + i + " ;desc = " + s);
                }
            });
    }

    /**
     * string[0]: 用户昵称第一个字符，若未设置或null，则返回"#"
     * string[1]: 用户名（用户唯一标识）
     *
     * @param userInfo 用户信息对象
     * @return string[]
     */
    public static String[] getUNFirstPosStr(UserInfo userInfo) {
        String[] strings = new String[2];
        String nick = userInfo.getNickname();
        if (TextUtils.isEmpty(nick)) {
            strings[0] = "#";
        } else if (nick.length() == 0) {
            strings[0] = nick;
        } else {
            strings[0] = nick.substring(0, 1);
        }
        strings[1] = userInfo.getUserName();
        return strings;
    }
}
