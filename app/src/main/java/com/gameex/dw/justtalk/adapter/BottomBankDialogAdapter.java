package com.gameex.dw.justtalk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.entry.BankInfo;
import com.gameex.dw.justtalk.holder.BottomBankDialogHolder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 提现界面选择银行卡的底部弹窗的列表适配器
 */
public class BottomBankDialogAdapter extends RecyclerView.Adapter<BottomBankDialogHolder> {
    private Context mContext;
    private List<BankInfo> mBankInfos;

    public BottomBankDialogAdapter(Context context, List<BankInfo> bankInfos) {
        mContext = context;
        mBankInfos = bankInfos;
    }

    @NonNull
    @Override
    public BottomBankDialogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_bottom_bank_dialog, parent, false);
        return new BottomBankDialogHolder(view, mBankInfos);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BottomBankDialogHolder holder, int position) {
        BankInfo bankInfo = mBankInfos.get(position);
        switch (bankInfo.getBankName()) {
            case "建设银行":
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_construction));
                break;
            case "农业银行":
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_agricultural));
                break;
            case "工商银行":
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_icbc));
                break;
            case "中兴银行":
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_zte));
                break;
            default:
                break;
        }
        holder.name.setText(bankInfo.getBankName() + "储蓄卡(" + bankInfo.getBankEndNum() + ")");
        holder.name.setTag(bankInfo.getBankId());
    }

    @Override
    public int getItemCount() {
        return mBankInfos == null ? 0 : mBankInfos.size();
    }
}
