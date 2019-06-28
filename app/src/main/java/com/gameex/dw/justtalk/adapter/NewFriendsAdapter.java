package com.gameex.dw.justtalk.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.holder.NewFriendsHolder;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.UserInfoUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.model.UserInfo;

public class NewFriendsAdapter extends RecyclerView.Adapter<NewFriendsHolder> {
    private static final String TAG = "NewFriendsAdapter";

    private Context mContext;
    private List<Map<String, String>> mDates;
    private String mCurrentDate;

    public NewFriendsAdapter(Context context, List<Map<String, String>> dates) {
        mContext = context;
        mDates = dates;
        mCurrentDate = DataUtil.msFormMMDD(System.currentTimeMillis());
    }

    @NonNull
    @Override
    public NewFriendsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_new_friends, parent, false);
        return new NewFriendsHolder(view, mContext, mDates);
    }

    @Override
    public void onBindViewHolder(@NonNull NewFriendsHolder holder, int position) {
        Map<String, String> data = mDates.get(position);
        String date = data.get("date");
        UserInfo userInfo = UserInfo.fromJson(data.get("userInfo"));
        assert date != null;
        //设置来邀日期，只显示当天的日期
        if (position == 0 || DataUtil.isMoreThanOneDay(Objects.requireNonNull(mDates.get(position - 1).get("date")), date)) {
            holder.date.setVisibility(View.VISIBLE);
            if (date.equals(mCurrentDate)) {
                holder.date.setText("今天");
            } else if (DataUtil.isMoreThanOneDay(date, mCurrentDate)) {
                holder.date.setText("昨天");
            } else {
                holder.date.setText(date);
            }
        } else {
            holder.date.setVisibility(View.GONE);
        }
        UserInfoUtils.initUserIcon(userInfo, mContext, holder.icon);    //用户头像
        holder.name.setText(TextUtils.isEmpty(userInfo.getNickname()) ? userInfo.getUserName()
                : userInfo.getNickname());  //用户名称
        holder.reason.setText(data.get("reason"));
        if (userInfo.isFriend()) {  //判断是否已经是好友，是则是指按钮可按且text为“同意”
            holder.accept.setText(mContext.getString(R.string.accept_str));
            holder.accept.setEnabled(true);
        } else {
            holder.accept.setText(mContext.getString(R.string.accepted_str));
            holder.accept.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return mDates == null ? 0 : mDates.size();
    }


}
