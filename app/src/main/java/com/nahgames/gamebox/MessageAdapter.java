package com.nahgames.gamebox;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.nahgames.gamebox.models.Friend;
import com.nahgames.gamebox.models.UserMessage;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<UserMessage> mMessages;
    private Context mContext;
    private DatabaseReference mDBReference;
    private FirebaseUser mUser;
    private String mCurrentUsername;
    private MessageAdapter _this;

    public String getmCurrentUsername() {
        return mCurrentUsername;
    }

    public void setmCurrentUsername(String mCurrentUsername) {
        this.mCurrentUsername = mCurrentUsername;
    }

    public DatabaseReference getmDBReference() {
        return mDBReference;
    }

    public void setmDBReference(DatabaseReference mDBReference) {
        this.mDBReference = mDBReference;
    }

    public FirebaseUser getmUser() {
        return mUser;
    }

    public void setmUser(FirebaseUser mUser) {
        this.mUser = mUser;
    }

    public MessageAdapter(List<UserMessage> mMessages, Context mContext) {
        this.mMessages = mMessages;
        this.mContext = mContext;
        _this = this;
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View messageView = inflater.inflate(R.layout.message_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(messageView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder holder, final int position) {
        UserMessage userMessage = mMessages.get(position);
        holder.userName.setText(userMessage.getFromUsername());
        holder.message.setText(userMessage.getMessage());
        holder.name.setText(userMessage.getFromName());
        holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Friend newFriend = new Friend();
                newFriend.setUid(mMessages.get(position).getUid());
                newFriend.setName(mMessages.get(position).getFromUsername());
                mDBReference.child(mUser.getUid()).child("friends").child(mMessages.get(position).getFromUsername()).setValue(newFriend)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                          @Override
                          public void onSuccess(Void aVoid) {
                              Friend newFriend = new Friend();
                              newFriend.setName(mCurrentUsername);
                              newFriend.setUid(mUser.getUid());
                              mDBReference.child(mMessages.get(position).getUid()).child("friends").child(mCurrentUsername).setValue(newFriend).addOnSuccessListener(new OnSuccessListener<Void>() {
                                  @Override
                                  public void onSuccess(Void aVoid) {
                                      mDBReference.child(mUser.getUid()).child("messages").child(mMessages.get(position).getKey()).removeValue();
                                      mMessages.remove(position);
                                      _this.notifyDataSetChanged();
                                  }
                              });
                          }
                      }
                    );
            }
        });
        holder.declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.from_username) public TextView userName;
        @Bind(R.id.from_name) public TextView name;
        @Bind(R.id.message) public TextView message;
        @Bind(R.id.accept) public Button acceptBtn;
        @Bind(R.id.decline) public Button declineBtn;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}