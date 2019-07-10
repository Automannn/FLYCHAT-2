package com.gameex.dw.justtalk.holder;

import android.view.View;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.github.siyamed.shapeimageview.CircularImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ChooseOwnerHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.icon)
    public CircularImageView icon;
    @BindView(R.id.name)
    public TextView name;

    public ChooseOwnerHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
