package com.nahgames.gamebox;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
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
import com.nahgames.gamebox.models.User;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    //Auth
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mUser;
    private String mUid;

    //Database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mUsernameDatabaseReference;
    private ValueEventListener mUserValueEventListener;
    private ChildEventListener mChildEventListener;
    private static final int RC_SIGN_IN = 1;
    private String mUsername;

    private Context mContext;

    private TextView mUsernameView;
    private TextView mNameView;
    private TextView mEmailView;
    private AlertDialog dialog;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mUsernameView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.username);
        mNameView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.name);
        mEmailView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.email);

        mContext = this;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUserDatabaseReference = mFirebaseDatabase.getReference().child("users");
        mUsernameDatabaseReference = mFirebaseDatabase.getReference().child("usernames");

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mUser = firebaseAuth.getCurrentUser();
                if(mUser != null) {
                    // mUser is signed in
                    onSignedInInitialize(mUser.getDisplayName());
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(System.currentTimeMillis());
                    User appUser = new User(mUser.getDisplayName(), mUser.getEmail(), cal.getTime().toString());
                    mUid = mUser.getUid();
                    mNameView.setText(mUser.getDisplayName());
                    mEmailView.setText(mUser.getEmail());
                    attachDatabaseReadListener();
                    mUserDatabaseReference.child(mUid).updateChildren(appUser.toMapNameAndLastLoginFields());

                } else {
                    // mUser is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void onSignedOutCleanup() {
        mUsername = "";
        detachDatabaseReadListener();
        mUid = null;
    }

    private void detachDatabaseReadListener() {
        if(mUserValueEventListener != null) {
            mUserDatabaseReference.child(mUid).removeEventListener(mUserValueEventListener);
            mUserValueEventListener = null;
        }
    }

    private void onSignedInInitialize(String displayName) {
        mUsername = displayName;
        attachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if(mUid != null) {
            dialog = null;
            mUserValueEventListener = new ValueEventListener() {
                User user = null;
                
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(user == null && dialog == null) {
                        user = dataSnapshot.getValue(User.class);
                        if (user.getUsername() == null) {
                            // custom dialog
                            LayoutInflater inflater = getLayoutInflater();
                            View view = inflater.inflate(R.layout.dialog_handle, null);
                            final EditText text = (EditText) view.findViewById(R.id.text);
                            final TextView error = (TextView) view.findViewById(R.id.error);
                            final TextView error2 = (TextView) view.findViewById(R.id.error2);
                            text.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                    if(error.getVisibility() == View.VISIBLE){
                                        error.setVisibility(View.GONE);
                                    }
                                    boolean isLetterOrNumber = true;
                                    for (int index = 0; index < charSequence.length(); index++){
                                        if(!Character.isLetterOrDigit(charSequence.charAt(index))){
                                            isLetterOrNumber = false;
                                        }
                                    }
                                    if(!isLetterOrNumber){
                                        error2.setVisibility(View.VISIBLE);
                                    } else {
                                        error2.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void afterTextChanged(Editable editable) {}
                            });
                            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setView(view)
                                .setTitle(R.string.create_username)
                                .setPositiveButton(R.string.ok, null)
                                .setCancelable(false);
                            dialog = builder.create();
                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialogInterface) {
                                    Button okButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                                    okButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            // mUser clicked OK button
                                            user.setUsername(text.getText().toString());
                                            mUsernameView.setText(text.getText().toString());
                                            mUserDatabaseReference.child(mUid).child("username").setValue(user.getUsername()).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    String error_msg = getString(R.string.username_in_use);
                                                    error.setTextColor(getResources().getColor(R.color.red));
                                                    error.setText(error_msg);
                                                    error.setVisibility(View.VISIBLE);
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Map<String, Object> usernameMap = new HashMap<String, Object>();
                                                    usernameMap.put(user.getUsername(), mUid);
                                                    text.setText("");
                                                    error.setVisibility(View.INVISIBLE);
                                                    dialog.dismiss();
                                                    user = null;
                                                    mUsernameDatabaseReference.updateChildren(usernameMap);

                                                }
                                            });
                                        }
                                    });
                                }
                            });

                            if(!dialog.isShowing()){
                                dialog.show();
                            }
                        } else {
                            mUsernameView.setText(user.getUsername());
                            if(tv != null) {
                                tv.setText(""+ (user.getMessages() != null ? user.getMessages().size() : ""));
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mUserDatabaseReference.child(mUid).addValueEventListener(mUserValueEventListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN) {
            if(resultCode == RESULT_OK) {
            } else if(resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.sign_out_menu) {
            // sign out
            FirebaseAuth.getInstance().signOut();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.friends){
            Intent intent = new Intent(this, FriendsActivity.class);
            intent.putExtra("uid", mUid);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.messages);
        MenuItemCompat.setActionView(item, R.layout.badge_layout);
        RelativeLayout badgeLayout = (RelativeLayout) MenuItemCompat.getActionView(item);
        tv = (TextView) badgeLayout.findViewById(R.id.actionbar_notifcation_textview);
        badgeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                intent.putExtra("uid", mUid);
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.messages:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
