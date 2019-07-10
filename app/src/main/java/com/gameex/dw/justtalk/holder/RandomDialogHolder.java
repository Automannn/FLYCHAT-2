package com.gameex.dw.justtalk.holder;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class RandomDialogHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.position)
    public TextView position;

    @BindView(R.id.amount)
    public EditText amount;

    @OnTextChanged(R.id.amount)
    void afterTextChanged(Editable editable) {
        mNum.set(getAdapterPosition(), editable.toString());
    }

    private List<String> mNum;

    public List<String> getNum() {
        return mNum;
    }

    public void setNum(List<String> num) {
        mNum = num;
    }

    public RandomDialogHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
