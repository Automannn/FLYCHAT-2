package com.gameex.dw.justtalk.holder;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.activity.WithdrawActivity;
import com.gameex.dw.justtalk.entry.BankInfo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.gameex.dw.justtalk.activity.WithdrawActivity.RECEIVER_UPDATE_BANK;

public class BottomBankDialogHolder extends RecyclerView.ViewHolder {

    @OnClick(R.id.container)
    void choose() {
        //TODO: 设置WithDrawActivity的到账银行数据
        Intent intent = new Intent(RECEIVER_UPDATE_BANK);
        intent.putExtra("bankInfo", mBankInfos.get(getAdapterPosition()));
        itemView.getContext().sendBroadcast(intent);
    }

    /**
     * 银行卡图标
     */
    @BindView(R.id.icon)
    public ImageView icon;
    /**
     * 银行卡名称
     */
    @BindView(R.id.name)
    public TextView name;

    private List<BankInfo> mBankInfos;

    public BottomBankDialogHolder(@NonNull View itemView, List<BankInfo> bankInfos) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mBankInfos = bankInfos;
    }
}
