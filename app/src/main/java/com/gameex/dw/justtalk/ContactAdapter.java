package com.gameex.dw.justtalk;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gameex.dw.justtalk.ObjPack.Contact;
import com.gameex.dw.justtalk.inviteFriends.InviteFriendsActivity;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {

    private Context mContext;
    private List<Contact> mContacts;

    public ContactAdapter(Context context, List<Contact> contacts) {
        this.mContext = context;
        mContacts = contacts;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.contact_item, viewGroup, false);
        ContactHolder holder = new ContactHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactHolder holder, int position) {
        Contact contact = mContacts.get(position);
        if (mContacts.get(position).getIndex().equals("↑")) {
            holder.index.setVisibility(View.GONE);
        } else {
            if (position == 3 || !mContacts.get(position - 1).getIndex().equals(contact.getIndex())) {
                holder.index.setVisibility(View.VISIBLE);
                holder.index.setText(contact.getIndex());
            } else {
                holder.index.setVisibility(View.GONE);
            }
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
    }

    @Override
    public int getItemCount() {
        return mContacts == null ? 0 : mContacts.size();
    }

    public class ContactHolder extends RecyclerView.ViewHolder {
        LinearLayout contactInfo;
        TextView index, name;
        CircularImageView icon;

        public ContactHolder(@NonNull View itemView) {
            super(itemView);
            contactInfo = itemView.findViewById(R.id.contact_info_layout);
            contactInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (mContacts.get(getAdapterPosition()).getName()) {
                        case "新的朋友":
                            Toast.makeText(mContext, "新的朋友", Toast.LENGTH_SHORT).show();
                            break;
                        case "邀请好友":
                            Intent intent = new Intent(mContext, InviteFriendsActivity.class);
                            mContext.startActivity(intent);
                            break;
                        case "我的群组":
                            Toast.makeText(mContext, "我的群组", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(mContext, "查看联系人详细信息", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
            index = itemView.findViewById(R.id.index_text_contact);
            name = itemView.findViewById(R.id.name_contact);
            icon = itemView.findViewById(R.id.icon_contact);
        }
    }
}
