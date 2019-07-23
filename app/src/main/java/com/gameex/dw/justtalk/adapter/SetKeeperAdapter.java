package com.gameex.dw.justtalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.holder.SetKeeperHolder;
import com.gameex.dw.justtalk.util.UserInfoUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.model.GroupMemberInfo;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * 设置群管理时，群成员列表适配器
 */
public class SetKeeperAdapter extends RecyclerView.Adapter<SetKeeperHolder> {

    private Context mContext;
    private List<GroupMemberInfo> mKeeperInfo;

    private OnItemLongClick mItemLongClick;

    public void setContext(Context context) {
        mContext = context;
    }

    public void setKeeperInfo(List<GroupMemberInfo> keeperInfo) {
        mKeeperInfo = keeperInfo;
    }

    public void setItemLongClick(OnItemLongClick itemLongClick) {
        mItemLongClick = itemLongClick;
    }

    public SetKeeperAdapter() {
    }

    public SetKeeperAdapter(Context context, List<GroupMemberInfo> keeperInfo) {
        mContext = context;
        mKeeperInfo = keeperInfo;
    }

    @NonNull
    @Override
    public SetKeeperHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_set_keeper, parent, false);
        view.setOnLongClickListener(view1 -> {
            if (mItemLongClick != null) {
                mItemLongClick.onLongClick((Integer) view1.getTag());
            }
            return true;
        });
        return new SetKeeperHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetKeeperHolder holder, int position) {
        GroupMemberInfo keeperInfo = mKeeperInfo.get(position);
        UserInfo userInfo = keeperInfo.getUserInfo();
        holder.itemView.setTag(position);
        UserInfoUtils.initUserIcon(userInfo, mContext, holder.icon);
        holder.name.setText(keeperInfo.getDisplayName());
    }

    @Override
    public int getItemCount() {
        return mKeeperInfo == null ? 0 : mKeeperInfo.size();
    }

    public interface OnItemLongClick {
        void onLongClick(int position);
    }
}
