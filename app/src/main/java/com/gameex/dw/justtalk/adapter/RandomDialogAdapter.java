package com.gameex.dw.justtalk.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.holder.RandomDialogHolder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 特殊红包弹窗适配器
 */
public class RandomDialogAdapter extends RecyclerView.Adapter<RandomDialogHolder> {

    private Context mContext;
    private List<String> mNum;
    private RandomDialogHolder mHolder;

    public RandomDialogAdapter(Context context, List<String> num) {
        mContext = context;
        mNum = num;
    }

    @NonNull
    @Override
    public RandomDialogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_random_dialog, parent, false);
        mHolder = new RandomDialogHolder(view);
        mHolder.setNum(mNum);
        return mHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RandomDialogHolder holder, int position) {
        String num = mNum.get(position);
        holder.amount.setText(num);
        holder.position.setText(position + 1 + "、尾数 ( ");
    }

    @Override
    public int getItemCount() {
        return mNum == null ? 0 : mNum.size();
    }

    public String getList() {
        String listStr = mHolder.getNum().toString();
        String str = listStr.substring(1, listStr.length() - 1);
        return str.replaceAll(" ", "");
    }
}
