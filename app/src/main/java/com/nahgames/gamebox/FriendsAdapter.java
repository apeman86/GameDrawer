package com.nahgames.gamebox;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nahgames.gamebox.models.Friend;

import java.util.List;

public class FriendsAdapter extends ArrayAdapter<Friend> {
    public FriendsAdapter(Context context, int resource, List<Friend> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.list_item, parent, false);
        }
        
        TextView nameView = (TextView) convertView.findViewById(R.id.friend_name);
        TextView onlineView = (TextView) convertView.findViewById(R.id.online);

        Friend friend = getItem(position);


        nameView.setVisibility(View.VISIBLE);
        nameView.setText(friend.getName());
        onlineView.setText(friend.getOnlineStatus());

        return convertView;
    }
}
