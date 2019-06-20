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
import cn.jpush.im.android.api.model.GroupInfo;

public class GroupInfoUtil {
    private static final String TAG = "GroupInfoUtil";

    /**
     * 加载群头像
     */
    public static void initGroupIcon(GroupInfo groupInfo, final Context context, final CircularImageView imgView) {
        if (groupInfo != null)
            groupInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                @Override
                public void gotResult(int i, String s, Bitmap bitmap) {
                    if (i == 0) {
                        Glide.with(context)
                                .load(bitmap)
                                .into(imgView);
                    } else {
                        imgView.setImageDrawable(TextDrawUtil.getRoundTextDraw(getGNFirstPosStr(groupInfo)));
                    }
                    LogUtil.d(TAG, "initGroupIcon: " + "responseCode = " + i + " ;desc = " + s);
                }
            });
    }

    /**
     * string[0]: 群名称的第一个字符，若未设置则返回"#"
     * string[1]: 群id（群唯一标识）
     *
     * @param groupInfo 群信息对象
     * @return string[]
     */
    public static String[] getGNFirstPosStr(GroupInfo groupInfo) {
        String[] strings = new String[2];
        String name = groupInfo.getGroupName();
        if (name.equals("未设置群名称") || TextUtils.isEmpty(name)) {
            strings[0] = "#";
        } else if (name.length() == 0) {
            strings[0] = name;
        } else {
            strings[0] = name.substring(0, 1);
        }
        strings[1] = String.valueOf(groupInfo.getGroupID());
        return strings;
    }
}
