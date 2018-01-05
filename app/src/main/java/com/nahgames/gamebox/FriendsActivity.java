package com.nahgames.gamebox;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class FriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            final FriendsActivityFragment friendsActivityFragment = new FriendsActivityFragment();
            friendsActivityFragment.setArguments(extras);
            getSupportFragmentManager().beginTransaction().add(friendsActivityFragment, "Friends").commit();
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    friendsActivityFragment.inviteFriend(view);
                }
            });
        }
    }

}
