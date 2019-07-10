package com.gameex.dw.justtalk.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ChooseToKeeperHolder extends RecyclerView.ViewHolder {

    /**
     * 用户头像
     */
    @BindView(R.id.icon)
    public CircularImageView icon;
    /**
     * 用户群昵称(displayName)
     */
    @BindView(R.id.name)
    public TextView name;
    /**
     * 选中标识
     */
    @BindView(R.id.check)
    public ImageView isCheck;

    public ChooseToKeeperHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
