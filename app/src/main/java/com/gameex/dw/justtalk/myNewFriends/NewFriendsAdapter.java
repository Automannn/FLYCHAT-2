package com.gameex.dw.justtalk.myNewFriends;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.util.DataUtil;
import com.gameex.dw.justtalk.util.LogUtil;
import com.gameex.dw.justtalk.util.UserInfoUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
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
        UserInfoUtils.initUserIcon(userInfo, mContext, holder.icon);
        holder.name.setText(TextUtils.isEmpty(userInfo.getNickname()) ? userInfo.getUserName()
                : userInfo.getNickname());
        holder.reason.setText(data.get("reason"));
    }

    @Override
    public int getItemCount() {
        return mDates == null ? 0 : mDates.size();
    }


}
