package com.gameex.dw.justtalk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.entry.BankInfo;
import com.github.siyamed.shapeimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 添加银行卡适配器
 */
public class AddBankAdapter extends RecyclerView.Adapter<AddBankAdapter.AddBankHolder> {

    private Context mContext;
    private List<BankInfo> mBankInfos;

    public AddBankAdapter(Context context, List<BankInfo> bankInfos) {
        mContext = context;
        mBankInfos = bankInfos;
    }

    @NonNull
    @Override
    public AddBankHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.recycler_item_bank_card, viewGroup, false);
        return new AddBankHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AddBankHolder holder, int position) {
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
        holder.name.setText(bankInfo.getBankName());
        holder.number.setText("**** **** **** " + bankInfo.getBankEndNum());
        holder.type.setText("储蓄卡");
    }

    @Override
    public int getItemCount() {
        return mBankInfos == null ? 0 : mBankInfos.size();
    }

    class AddBankHolder extends RecyclerView.ViewHolder {
        /**
         * 银行卡图标
         */
        CircularImageView icon;
        /**
         * 银行卡名
         */
        TextView name;
        /**
         * 银行卡类型
         */
        TextView type;
        /**
         * 银行卡号
         */
        TextView number;

        AddBankHolder(@NonNull View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            name = v.findViewById(R.id.name);
            type = v.findViewById(R.id.type);
            number = v.findViewById(R.id.number);
        }
    }
}
