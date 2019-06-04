package com.gameex.dw.justtalk.payPackage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.R;
import com.github.siyamed.shapeimageview.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AddBankAdapter extends RecyclerView.Adapter<AddBankAdapter.AddBankHolder> {

    private Context mContext;
    private JSONArray mBanks;

    AddBankAdapter(Context context, JSONArray banks) {
        mContext = context;
        mBanks = banks;
    }

    @NonNull
    @Override
    public AddBankHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_bank_card, viewGroup, false);
        return new AddBankHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AddBankHolder holder, int position) {
        String bankCardNumber = null;
        try {
            JSONObject bank = mBanks.getJSONObject(position);
            bankCardNumber = bank.getString("bankCardNumber");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Glide.with(mContext)
                .load(R.drawable.icon_construction)
                .into(holder.icon);
        holder.name.setText("待优化");
        holder.type.setText("待优化");
        assert bankCardNumber != null;
        holder.number.setText(bankCardNumber + "");
    }

    @Override
    public int getItemCount() {
        return mBanks == null ? 0 : mBanks.length();
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
