package com.gameex.dw.justtalk.addFriends;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.publicInterface.RecyclerItemClick;
import com.gameex.dw.justtalk.util.UserInfoUtils;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.model.UserInfo;

public class AddFriendsAdapter extends RecyclerView.Adapter<AddFriendsAdapter.AddFriendsHolder> {

    private RecyclerItemClick mItemClick;

    private List<UserInfo> mUsers;
    private Context mContext;

    AddFriendsAdapter(List<UserInfo> users, Context context) {
        mUsers = users;
        mContext = context;
    }

    @NonNull
    @Override
    public AddFriendsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.recycler_item_add_friends, viewGroup, false);
        return new AddFriendsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFriendsHolder holder
            , @SuppressLint("RecyclerView") final int position) {
        if (mItemClick != null) {
            holder.itemView.setOnClickListener(view -> mItemClick.onClick(position));
            holder.itemView.setOnLongClickListener(view -> {
                mItemClick.onLongClick(position);
                return false;
            });
        }

        UserInfo user = mUsers.get(position);
        UserInfoUtils.initUserIcon(user, mContext, holder.mCircleView);
        holder.name.setText(TextUtils.isEmpty(user.getNickname()) ? user.getUserName() : user.getNickname());
    }

    @Override
    public int getItemCount() {
        return mUsers == null ? 0 : mUsers.size();
    }

    class AddFriendsHolder extends RecyclerView.ViewHolder {
        CircularImageView mCircleView;
        TextView name;

        AddFriendsHolder(@NonNull View itemView) {
            super(itemView);
            mCircleView = itemView.findViewById(R.id.icon_add_friends);
            name = itemView.findViewById(R.id.name_add_friends);
        }
    }

    void setItemClick(RecyclerItemClick itemClick) {
        mItemClick = itemClick;
    }
}
