package com.gameex.dw.justtalk.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Interpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.adapter.BottomBankDialogAdapter;
import com.gameex.dw.justtalk.entry.BankInfo;
import com.rey.material.app.BottomSheetDialog;
import com.yzq.zxinglibrary.encode.CodeCreator;

import java.util.List;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * dailog显示工具类
 */
public class DialogUtil {


    /**
     * 二维码展示图片
     *
     * @param bitmap 二维码
     */
    public static void showQrDialog(Context context, String id, Bitmap bitmap) {
        /*
            contentEtString:字符串内容
            w:图片宽
            h:图片高
            logo:不需要的话，直接传空
        */
        Bitmap qrCodeBit = CodeCreator.createQRCode(id,
                400, 400, bitmap);
        Dialog mQRCodeDialog = new Dialog(context, R.style.qr_code_dialog_style);
        mQRCodeDialog.setContentView(R.layout.dialog_qr_code);
        ImageView qrImg = mQRCodeDialog.findViewById(R.id.qr_code_img_dialog);
        qrImg.setImageBitmap(qrCodeBit);
        mQRCodeDialog.setCanceledOnTouchOutside(true);
        Window window = mQRCodeDialog.getWindow();
        assert window != null;
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 40;
        mQRCodeDialog.onWindowAttributesChanged(params);
        mQRCodeDialog.show();
    }
}
