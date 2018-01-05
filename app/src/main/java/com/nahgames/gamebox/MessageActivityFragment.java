package com.nahgames.gamebox;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nahgames.gamebox.models.User;
import com.nahgames.gamebox.models.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MessageActivityFragment extends Fragment {

    //DB
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private ChildEventListener mChildEventListener;

    //Auth
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mUser;
    private String mUid;

    private MessageAdapter mMessageAdapter;
    private List<UserMessage> mUserMessages;
    public MessageActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.message_list);
        mUserMessages = new ArrayList<>();
        mUserDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mUser = firebaseAuth.getCurrentUser();
                if (mUser != null) {
                    // User is signed in
                    mUid = mUser.getUid();
                    mMessageAdapter.setmDBReference(mUserDatabaseReference.child("users"));
                    mMessageAdapter.setmUser(mUser);
                    mUserDatabaseReference.child("users").child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User thisUser = dataSnapshot.getValue(User.class);
                            mMessageAdapter.setmCurrentUsername(thisUser.getUsername());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    setupDB();
                } else {
                    // User is signed out
//                    updateFriends(false);
                    getActivity().finish();
                }
            }
        };
        mMessageAdapter = new MessageAdapter(mUserMessages, getActivity());
        recyclerView.setAdapter(mMessageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }


    @Override
    public void onPause() {
        super.onPause();
        if(mAuthStateListener != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();

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
            mUserDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }


    private void attachDatabaseReadListener() {
        if(mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    UserMessage message = dataSnapshot.getValue(UserMessage.class);
                    message.setKey(dataSnapshot.getKey());
                    mUserMessages.add(message);
                    mMessageAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    UserMessage message = dataSnapshot.getValue(UserMessage.class);
                    for (int index = 0; index < mUserMessages.size(); index++) {
                        if(mUserMessages.get(index).getFromUsername().equals(message.getFromUsername())){
                            mUserMessages.set(index, message);
                            mMessageAdapter.notifyDataSetChanged();
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
            mUserDatabaseReference.child("users").child(mUid).child("messages").addChildEventListener(mChildEventListener);
        }
    }

}
