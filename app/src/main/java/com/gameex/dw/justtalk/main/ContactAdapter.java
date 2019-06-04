package com.gameex.dw.justtalk.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.R;
import com.gameex.dw.justtalk.objPack.Contact;
import com.gameex.dw.justtalk.inviteFriends.InviteFriendsActivity;
import com.gameex.dw.justtalk.myGroups.MyGroupActivity;
import com.gameex.dw.justtalk.userInfo.UserBasicInfoActivity;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.jpush.im.android.api.model.UserInfo;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {
    private static final String TAG = "ContactAdapter";
    private Context mContext;
    private List<Contact> mContacts;
    private List<UserInfo> mUserInfos;

    ContactAdapter(Context context, List<Contact> contacts, List<UserInfo> userInfos) {
        mContext = context;
        this.mContacts = contacts;
        mUserInfos = userInfos;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.contact_item, viewGroup, false);
        return new ContactHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactHolder holder, int position) {
        if (position < 3) {
            Contact contact = mContacts.get(position);
            if (contact.getIndex().equals("↑")) {
                holder.index.setVisibility(View.GONE);
            } else {
                holder.index.setVisibility(View.VISIBLE);
                holder.index.setText(contact.getIndex());
            }
            if (contact.getIconUri() != null) {
                Glide.with(mContext)
                        .load(contact.getIconUri())
                        .into(holder.icon);
            } else {
                Glide.with(mContext)
                        .load(contact.getIconResource())
                        .into(holder.icon);
            }
            holder.name.setText(contact.getName());
        } else {
            position -= 3;
            UserInfo userInfo = mUserInfos.get(position);
            if (position == 0 || !mUserInfos.get(position - 1).
                    getExtra("index").equals(userInfo.getExtra("index"))) {
                holder.index.setVisibility(View.VISIBLE);
                holder.index.setText(userInfo.getExtra("index"));
            } else {
                holder.index.setVisibility(View.GONE);
            }
            if (userInfo.getExtra("icon_uri") != null) {
                Glide.with(mContext)
                        .load(Uri.parse(userInfo.getExtra("icon_uri")))
                        .into(holder.icon);
            } else {
                Glide.with(mContext)
                        .load(R.drawable.icon_user)
                        .into(holder.icon);
            }
            holder.name.setText(TextUtils.isEmpty(userInfo.getNickname())
                    ? userInfo.getUserName() : userInfo.getNickname());
        }
    }

    @Override
    public int getItemCount() {
        return mUserInfos == null ? 3 : mUserInfos.size() + 3;
    }

    class ContactHolder extends RecyclerView.ViewHolder {
        LinearLayout contactInfo;
        TextView index, name;
        CircularImageView icon;

        ContactHolder(@NonNull View itemView) {
            super(itemView);
            contactInfo = itemView.findViewById(R.id.contact_info_layout);
            contactInfo.setOnClickListener(view -> {
                if (getAdapterPosition() < 3) {
                    Intent intent = new Intent();
                    switch (mContacts.get(getAdapterPosition()).getName()) {
                        case "新的朋友":
                            Toast.makeText(mContext, "新的朋友", Toast.LENGTH_SHORT).show();
                            break;
                        case "邀请好友":
                            intent.setClass(mContext, InviteFriendsActivity.class);
                            mContext.startActivity(intent);
                            break;
                        case "我的群组":
                            intent.setClass(mContext, MyGroupActivity.class);
                            intent.putExtra("user_infos", UserInfo.collectionToJson(mUserInfos));
                            mContext.startActivity(intent);
                            break;
                        default:
                            Toast.makeText(mContext, "查看我的详细信息", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else {
                    Intent intent = new Intent(mContext, UserBasicInfoActivity.class);
                    intent.putExtra("user_info_json"
                            , mUserInfos.get(getAdapterPosition() - 3).toJson());
                    mContext.startActivity(intent);
                }
            });
            index = itemView.findViewById(R.id.index_text_contact);
            name = itemView.findViewById(R.id.name_contact);
            icon = itemView.findViewById(R.id.icon_contact);
        }
    }
}
