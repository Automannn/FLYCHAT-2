package com.gameex.dw.justtalk.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.activity.UserBasicInfoActivity;
import com.gameex.dw.justtalk.util.UserInfoUtils;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * 联系人适配器
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {
    private static final String TAG = "ContactAdapter";
    private Context mContext;
    private List<UserInfo> mUserInfos;

    public ContactAdapter(Context context, List<UserInfo> userInfos) {
        mContext = context;
        mUserInfos = userInfos;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup viewGroup
            , int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.recycler_item_contact, viewGroup, false);
        return new ContactHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactHolder holder, int position) {
        UserInfo userInfo = mUserInfos.get(position);
        if (position == 0 || !mUserInfos.get(position - 1).
                getExtra("index").equals(userInfo.getExtra("index"))) {
            holder.index.setVisibility(View.VISIBLE);
            holder.index.setText(userInfo.getExtra("index"));
        } else {
            holder.index.setVisibility(View.GONE);
        }
        UserInfoUtils.initUserIcon(userInfo, mContext, holder.icon);
        holder.name.setText(TextUtils.isEmpty(userInfo.getNickname())
                ? userInfo.getUserName() : userInfo.getNickname());
    }

    @Override
    public int getItemCount() {
        return mUserInfos == null ? 0 : mUserInfos.size();
    }

    class ContactHolder extends RecyclerView.ViewHolder {
        LinearLayout contactInfo;
        TextView index, name;
        CircularImageView icon;

        ContactHolder(@NonNull View itemView) {
            super(itemView);
            contactInfo = itemView.findViewById(R.id.contact_info_layout);
            contactInfo.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, UserBasicInfoActivity.class);
                intent.putExtra("user_info_json"
                        , mUserInfos.get(getAdapterPosition()).toJson());
                mContext.startActivity(intent);
            });
            index = itemView.findViewById(R.id.index_text_contact);
            name = itemView.findViewById(R.id.name_contact);
            icon = itemView.findViewById(R.id.icon_contact);
        }
    }
}
