package com.gameex.dw.justtalk.addFriends;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.publicInterface.RecyclerItemClick;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;

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
        View view = inflater.inflate(R.layout.item_add_friends, viewGroup, false);
        return new AddFriendsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFriendsHolder holder, final int position) {
        if (mItemClick != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemClick.onClick(position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mItemClick.onLongClick(position);
                    return false;
                }
            });
        }

        UserInfo user = mUsers.get(position);
        if (user.getExtra("username")!=null){
            holder.name.setText(user.getExtra("username"));
        }else{
            holder.name.setText(user.getUserName());
        }
    }

    @Override
    public int getItemCount() {
        return mUsers == null ? 0 : mUsers.size();
    }

    class AddFriendsHolder extends RecyclerView.ViewHolder {
        CircularImageView mCircleView;
        TextView name;

        public AddFriendsHolder(@NonNull View itemView) {
            super(itemView);
            mCircleView = itemView.findViewById(R.id.icon_add_friends);
            name = itemView.findViewById(R.id.name_add_friends);
        }
    }

    void setItemClick(RecyclerItemClick itemClick) {
        mItemClick = itemClick;
    }
}
