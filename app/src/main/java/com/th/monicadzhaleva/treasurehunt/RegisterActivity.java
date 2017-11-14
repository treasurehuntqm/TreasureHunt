package com.th.monicadzhaleva.treasurehunt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    Button emailRegisterButton;
    ImageButton avatarBtnNext;
    ImageButton avatarBtnPrev;
    AutoCompleteTextView username;
    AutoCompleteTextView password;
    AutoCompleteTextView firstName;
    AutoCompleteTextView lastName;
    ImageView chosenAvatar;
    FirebaseDatabase database;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth=FirebaseAuth.getInstance();
        init();
        avatarButtonsHandler();

        /* SAMPLE FIREBASE DATABASE */
        database = FirebaseDatabase.getInstance();

    }

    private void init()
    {
        username=(AutoCompleteTextView) findViewById(R.id.usernameRegister);
        password=(AutoCompleteTextView) findViewById(R.id.passwordRegister);
        firstName=(AutoCompleteTextView) findViewById(R.id.firstnameRegister);
        lastName=(AutoCompleteTextView) findViewById(R.id.lastnameRegister);
        chosenAvatar=(ImageView) findViewById(R.id.chosenAvatar);
        emailRegisterButton = (Button) findViewById(R.id.email_register_button);
        emailRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerNewUser();
            }
        });
    }

    public void avatarButtonsHandler()
    {
        avatarBtnNext= (ImageButton) findViewById(R.id.imageButtonNext);
        avatarBtnPrev= (ImageButton) findViewById(R.id.imageButtonPrev);
        avatarBtnPrev.setAlpha((float) 0.3);
        avatarBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentTag=Integer.parseInt(chosenAvatar.getTag().toString());
                int nextTag=currentTag+1;
                String uri = "@drawable/avatar"+nextTag;  // where myresource (without the extension) is the file
                int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                if(imageResource!=0)
                {
                    Drawable res = getResources().getDrawable(imageResource);
                    if(res!=null) {
                        chosenAvatar.setImageDrawable(res);
                        chosenAvatar.setTag(nextTag);
                        avatarBtnPrev.setAlpha((float) 1);

                    }
                }else
                {
                    avatarBtnNext.setAlpha((float) 0.3);
                }

            }
        });

        avatarBtnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentTag=Integer.parseInt(chosenAvatar.getTag().toString());
                avatarBtnNext.setAlpha((float) 1);
                int nextTag=currentTag-1;
                String uri = "@drawable/avatar"+nextTag;  // where myresource (without the extension) is the file
                int imageResource = getResources().getIdentifier(uri, null, getPackageName());
                if(imageResource!=0)
                {
                    Drawable res = getResources().getDrawable(imageResource);
                    if(res!=null) {
                        chosenAvatar.setImageDrawable(res);
                        chosenAvatar.setTag(nextTag);
                    }}else
                {
                    Log.i("Does not exist","Image does not exist");
                    avatarBtnPrev.setAlpha((float) 0.3);
                }
            }
        });

    }

    protected void registerNewUser( )
    {
        final User newUser=generateNewUserObject();
        if(newUser!=null) {
            insertUserInDb(newUser);
        }
    }

    protected User generateNewUserObject()
    {

        String usernameValue=username.getText().toString();
        String passwordValue=password.getText().toString();
        String firstNameValue=firstName.getText().toString();
        String lastNameValue=lastName.getText().toString();

        if(TextUtils.isEmpty(passwordValue))
        {
            password.setError("Password field is required!");
            password.requestFocus();
            return null;
        } else if (!isPasswordValid(passwordValue)){
            password.setError("Password is too short!");
            password.requestFocus();
            return null;
        } else if (TextUtils.isEmpty(usernameValue)) {
            username.setError("Username is required!");
            username.requestFocus();
            return null;
        }
        Log.d("Generate new user: ", "Generating ..");
        final User newUser=new User();
        newUser.setUsername(usernameValue);
        newUser.setPassword(passwordValue);
        newUser.setFirstName(firstNameValue);
        newUser.setLastName(lastNameValue);
        newUser.setAvatar("avatar"+chosenAvatar.getTag().toString());
        return newUser;
    }

    protected void insertUserInDb(final User newUser)
    {
        Log.d("Insert: ", "Inserting ..");
        Log.i("Inserting: ", newUser.toString());

        /* Using Firebase NoSQL database - Store user credentials*/
        DatabaseReference databaseReference=database.getReference();
        final DatabaseReference userRef=databaseReference.child("users");
        userRef.child(newUser.getUsername()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    //user exists
                    Toast.makeText(getApplicationContext(), "A user with this username already exists",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // insert user
                    userRef.child(newUser.getUsername()).setValue(newUser);
                    Toast.makeText(getApplicationContext(), "Succesfully Registered!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /* Store user with custom email in auth session*/
         /*
        String customEmail=newUser.getUsername()+ "@treasurehunt.com";
        mAuth.createUserWithEmailAndPassword(customEmail,newUser.getPassword()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Success", "createUserWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(getApplicationContext(), "Successfully registered! ",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Log.w("Error", "createUserWithEmail:failure", task.getException());
                    Toast.makeText(getApplicationContext(), "Registration failed.",
                            Toast.LENGTH_SHORT).show();
                }

                // ...
            }
        });
        */
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }
}

