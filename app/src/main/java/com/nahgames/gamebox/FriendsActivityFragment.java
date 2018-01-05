package com.nahgames.gamebox;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nahgames.gamebox.models.Friend;
import com.nahgames.gamebox.models.User;
import com.nahgames.gamebox.models.UserMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class FriendsActivityFragment extends Fragment {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFriendsDatabaseReference;
    private ChildEventListener mChildEventListener;

    //Auth
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mUser;
    private String mUid;

    private View mRootView;
    private ArrayList<Friend> mFriends;
    private FriendsAdapter mFriendAdapter;
    private String mUserName;
    private DatabaseReference mUsersDatabaseReferences;
    private AlertDialog mDialog;

    public FriendsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFriends = new ArrayList<Friend>();
        mFriendAdapter = new FriendsAdapter(getActivity(), R.layout.list_item, mFriends);
        mRootView = inflater.inflate(R.layout.fragment_friends, container, false);
        ListView listView = (ListView) mRootView.findViewById(R.id.list_view);
        listView.setAdapter(mFriendAdapter);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFriendsDatabaseReference = mFirebaseDatabase.getReference().child("users");
        mUsersDatabaseReferences = mFirebaseDatabase.getReference().child("users");
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mUser = firebaseAuth.getCurrentUser();
                if (mUser != null) {
                    // User is signed in
                    mUid = mUser.getUid();
                    mFirebaseDatabase.getReference().child("users").child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User thisUser = dataSnapshot.getValue(User.class);

                            mUserName = thisUser.getUsername();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    setupDB();
                } else {
                    // User is signed out
                    updateFriends(false);
                    getActivity().finish();
                }
            }
        };

        return mRootView;
    }

    private void updateFriends(boolean online) {
        for (int index = 0; index < mFriendAdapter.getCount(); index++) {
            Friend friend = mFriendAdapter.getItem(index);
            if(online){
                mUsersDatabaseReferences.child(friend.getUid()).child("friends").child(mUserName).child("onlineStatus").setValue("Online");
            } else {
                mUsersDatabaseReferences.child(friend.getUid()).child("friends").child(mUserName).child("onlineStatus").setValue("Offline");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAuthStateListener != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
        mFriendAdapter.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void setupDB() {
        attachDatabaseReadListener();
    }


    private void onSignedOutCleanup() {
        detachDatabaseReadListener();
    }

    private void detachDatabaseReadListener() {
        if(mChildEventListener != null) {
            mFriendsDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }


    private void attachDatabaseReadListener() {
        if(mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Friend friend = dataSnapshot.getValue(Friend.class);
                    mFriendAdapter.add(friend);
                    updateFriends(true);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Friend friend = dataSnapshot.getValue(Friend.class);
                    for (int index = 0; index < mFriendAdapter.getCount(); index++) {
                        if(mFriendAdapter.getItem(index).getName().equals(friend.getName())){
                            mFriendAdapter.getItem(index).setOnlineStatus(friend.getOnlineStatus());
                            mFriendAdapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mFriendsDatabaseReference.child(mUid).child("friends").addChildEventListener(mChildEventListener);
        }
    }

    public void inviteFriend(View fab){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_invite_friend, null);
        final TextView error = (TextView) view.findViewById(R.id.error);
        final TextView error2 = (TextView) view.findViewById(R.id.error2);
        final EditText newFriendName = (EditText) view.findViewById(R.id.new_friend_name);
        newFriendName.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

             }

             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                 if(error.getVisibility() == View.VISIBLE){
                     error.setVisibility(View.GONE);
                 }
                 if(error2.getVisibility() == View.VISIBLE){
                     error2.setVisibility(View.GONE);
                 }
             }

             @Override
             public void afterTextChanged(Editable editable) {

             }
         }
        );
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(R.string.invite_friend)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null);
        mDialog = builder.create();
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button okButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String value = newFriendName.getText().toString();
                        if(android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()){
                            mUsersDatabaseReferences.orderByChild("email").equalTo(value).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        UserMessage inviteInfo = new UserMessage();
                                        Map<String,String> userData = (Map<String, String>) dataSnapshot.getValue();
                                        inviteInfo.setFromName(mUser.getDisplayName());
                                        inviteInfo.setFromUsername(mUserName);
                                        inviteInfo.setMessage(getString(R.string.lets_be_friends));
                                        inviteInfo.setType(UserMessage.TYPE.FRIENDINVITE);
                                        inviteInfo.setUid(mUid);
                                        mUsersDatabaseReferences.child((String)((Map<String, Object>)dataSnapshot.getValue()).keySet().toArray()[0]).child("messages").child(mUserName+"_friendinvite").setValue(inviteInfo);
                                        mDialog.dismiss();
                                        Toast.makeText(getActivity(), R.string.invite_sent, Toast.LENGTH_SHORT).show();
                                    } else {
                                        //Todo: add share through email.
                                        Toast.makeText(getActivity(), R.string.invite_sent, Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {
                            if(mUserName.equals(value)){
                                error2.setVisibility(View.VISIBLE);
                            } else {
                                mFirebaseDatabase.getReference().child("usernames").orderByKey().equalTo(value).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            UserMessage inviteInfo = new UserMessage();
                                            Map<String,String> userData = (Map<String, String>) dataSnapshot.getValue();
                                            inviteInfo.setFromName(mUser.getDisplayName());
                                            inviteInfo.setFromUsername(mUserName);
                                            inviteInfo.setMessage(getString(R.string.lets_be_friends));
                                            inviteInfo.setType(UserMessage.TYPE.FRIENDINVITE);
                                            inviteInfo.setUid(mUid);
                                            mUsersDatabaseReferences.child(userData.get(value)).child("messages").child(mUserName+"_friendinvite").setValue(inviteInfo);
                                            mDialog.dismiss();
                                            Toast.makeText(getActivity(), R.string.invite_sent, Toast.LENGTH_SHORT).show();
                                        } else {
                                            error.setVisibility(View.VISIBLE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });

        if(!mDialog.isShowing()){
            mDialog.show();
        }
    }
}
