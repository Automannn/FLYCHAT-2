package com.gameex.dw.justtalk.updateVersion;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.gameex.dw.justtalk.R;

/**
 * Created by Allen Liu on 2017/2/23.
 */
public class BaseDialog extends Dialog {
    private int res;

    public BaseDialog(Context context, int theme, int layout) {
        super(context, theme);
        // TODO 自动生成的构造函数存根
        setContentView(layout);
        this.res = layout;
        setCanceledOnTouchOutside(false);
        Window window=getWindow();
        window.setWindowAnimations(R.style.RecordDialog);
    }

}