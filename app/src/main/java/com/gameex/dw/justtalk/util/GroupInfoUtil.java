package com.gameex.dw.justtalk.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

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
    public static void initGroupIcon(GroupInfo groupInfo, final Context context, final View imgView) {
        if (groupInfo != null)
            groupInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                @Override
                public void gotResult(int i, String s, Bitmap bitmap) {
                    if (i == 0) {
                        Glide.with(context)
                                .load(bitmap)
                                .into((CircularImageView) imgView);
                    } else {
                        Glide.with(context)
                                .load(R.drawable.icon_group)
                                .into((CircularImageView) imgView);
                    }
                    LogUtil.d(TAG, "initGroupIcon: " + "responseCode = " + i + " ;desc = " + s);
                }
            });
    }
}
