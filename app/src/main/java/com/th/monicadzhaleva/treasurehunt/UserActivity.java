package com.th.monicadzhaleva.treasurehunt;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    protected User activeUser;
    protected TextView usernameInfoVal;
    protected TextView firstNameInfoVal;
    protected TextView lastNameInfoVal;
    protected TextView levelInfoVal;
    protected TextView expInfoVal;
    protected ImageView avatar;
    String treasureInfo;

    FirebaseDatabase database;
    private List<UserToTreasure> collectedTreasureList=new ArrayList<UserToTreasure>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        database = FirebaseDatabase.getInstance();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        getUserDetails();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
    }

    public void getUserDetails()
    {
        // Get user details from login intent screen
        activeUser=new User();
        final DatabaseReference activeUserRef=database.getReference().child("users").child(getIntent().getStringExtra("username"));
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                activeUser= dataSnapshot.getValue(User.class);
                setUserInfo(activeUser);
                getCollectedTreasures(activeUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        activeUserRef.addValueEventListener(postListener);
    }

    protected void setUserInfo(User user)
    {
        usernameInfoVal= (TextView) findViewById(R.id.usernameinfovalue);
        firstNameInfoVal= (TextView) findViewById(R.id.firstNameinfovalue);
        lastNameInfoVal= (TextView) findViewById(R.id.lastNameinfovalue);
        levelInfoVal= (TextView) findViewById(R.id.levelinfovalue);
        expInfoVal= (TextView) findViewById(R.id.experienceinfovalue);
        avatar= (ImageView) findViewById(R.id.avatarView);

        String uri = "@drawable/"+user.getAvatar();  // where myresource (without the extension) is the file
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        if(imageResource!=0)
        {
            Drawable res = getResources().getDrawable(imageResource);
            if(res!=null) {
                avatar.setImageDrawable(res);
                avatar.setTag(user.getAvatar().replace("avatar",""));
            }}else
        {
            Log.i("Does not exist","Image does not exist");
        }

        try {
            this.setTitle(user.getUsername());
            usernameInfoVal.setText(user.getUsername());
            firstNameInfoVal.setText(user.getFirstName());
            lastNameInfoVal.setText(user.getLastName());
            levelInfoVal.setText(String.valueOf(user.getLevel()));
            expInfoVal.setText(String.valueOf(user.getExperience()));
        }catch(NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    public void getCollectedTreasures(User user)
    {
        final DatabaseReference userToTreasureRef=database.getReference().child("user_to_treasure");
        final DatabaseReference userToTreasureInstanceRef =  userToTreasureRef.child(user.getUsername());
        userToTreasureInstanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserToTreasure userToTreasure = snapshot.getValue(UserToTreasure.class);
                    userToTreasure.setTreasureName(snapshot.getKey());
                    collectedTreasureList.add(userToTreasure);
                    addCollectedTreasures(collectedTreasureList);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public void addCollectedTreasures(List<UserToTreasure> collectedTreasureList)
    {
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.collectedTreasuresContainer);
        int prevTextViewId = 0;

        for (final UserToTreasure userToTreasure : collectedTreasureList) {
            final TextView textView = new TextView(this);
            DatabaseReference treasureInfoRef=database.getReference().child("treasures").child(userToTreasure.getTreasureName()).child("info");
            treasureInfoRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    treasureInfo=snapshot.getValue().toString();
                    textView.setText(userToTreasure.getTreasureName()+"\n" + treasureInfo+"\n \n");
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            textView.setTextColor(Color.WHITE);

            int curTextViewId = prevTextViewId + 1;
            textView.setId(curTextViewId);
            final RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);

            params.addRule(RelativeLayout.BELOW, prevTextViewId);
            textView.setLayoutParams(params);

            prevTextViewId = curTextViewId;
            layout.addView(textView, params);
        }
    }

}