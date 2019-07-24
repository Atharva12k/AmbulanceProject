package com.example.hp.ambulenceproject;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;




public class Register extends AppCompatActivity {

    private static final String TAG = "Register";
    EditText _emailText;
    EditText _passwordText;
    EditText _retypePasswordText;
    EditText _ambulenceNumber;
    Button _signupButton;
    TextView _loginLink;
    EditText _fnameText;
    static String uid = "";
    private String fname,email,password,retypePassword,ambNumber;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private CollectionReference collectionReference;
    private DocumentReference documentReference;
    private HashMap<String,String> hashMap;
    private FirebaseUser user;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            if(getSupportActionBar()!=null)
                this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_register);
        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _retypePasswordText = findViewById(R.id.input_retypePassword);
        _signupButton = findViewById(R.id.btn_signup);
        _loginLink = findViewById(R.id.link_login);
        _fnameText = findViewById(R.id.input_fname);
        _ambulenceNumber=findViewById(R.id.input_anumber);
        firebaseAuth = FirebaseAuth.getInstance();
        hashMap = new HashMap<>();
        firestore = FirebaseFirestore.getInstance();


        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(signup()) {
                    email = _emailText.getText().toString();
                    password = _passwordText.getText().toString();


                    firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                            //Toast.makeText(getBaseContext(),"User created successfully",Toast.LENGTH_SHORT).show();
                            user = firebaseAuth.getCurrentUser();
                            documentReference = firestore.collection("AMBULANCE LIST").document(user.getUid());
                            hashMap.put("Name",fname);hashMap.put("Email",email);hashMap.put("Ambulence Number",ambNumber);
                            documentReference.set(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("Register", "Data added to database");
                                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG,"Registration failed " + e.getMessage());
                                }
                            });
                            }
                            else
                            {
                                Toast.makeText(getBaseContext(),"User creation failed ",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getBaseContext(),"User creation failed "+e.getMessage(),Toast.LENGTH_SHORT).show();
                            Log.d("User creation failed ",e.getMessage());
                        }
                    });
                }
                else
                {
                    Toast.makeText(getBaseContext(), "One or more fields are empty", Toast.LENGTH_LONG).show();
                }
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the com.example.rift.jiofinal.Login activity
                Intent intent = new Intent(Register.this,LoginActivity.class);
                startActivity(intent);

            }
        });

    }
    public boolean signup(){
        Log.d(TAG, "SignUp");
        fname = _fnameText.getText().toString();
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();
        retypePassword = _retypePasswordText.getText().toString();
        ambNumber=_ambulenceNumber.getText().toString();
        if (email.equals("") || password.equals("") || fname.equals("") || ambNumber.equals("") ) {
            return false;
        }
        return true;
    }






}




