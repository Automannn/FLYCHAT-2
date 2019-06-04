package com.gameex.dw.justtalk.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

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
    public static void initUserIcon(UserInfo userInfo, final Context context, final View imgView) {
        if (userInfo != null)
            userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                @Override
                public void gotResult(int i, String s, Bitmap bitmap) {
                    if (i == 0) {
                        Glide.with(context)
                                .load(bitmap)
                                .into((CircularImageView) imgView);
                    } else {
                        Glide.with(context)
                                .load(R.drawable.icon_user)
                                .into((CircularImageView) imgView);
                    }
                    LogUtil.d(TAG, "initUserIcon: " + "responseCode = " + i + " ;desc = " + s);
                }
            });
    }
}
