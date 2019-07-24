package com.example.hp.ambulenceproject;

import android.app.ProgressDialog;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    EditText _emailText;
    EditText _passwordText;
    Button _loginButton;
    TextView _signupLink;
    TextView _forgotPasswordLink;
    static String uid = "";
    public ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            if(getSupportActionBar()!=null)
                this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_login);
        firebaseAuth=FirebaseAuth.getInstance();
        String name,email,location;
        if (getIntent().getExtras() != null /*&& getIntent().getExtras().get("Name")!=null && getIntent().getExtras().get("Location")!=null*/) {
                name = (String) getIntent().getExtras().get("Name");
                Log.d("Received in login", "Name: + "+ name);
                email = (String) getIntent().getExtras().get("Email");
                Log.d("Received in login", "Email: + "+ email);
                location = (String) getIntent().getExtras().get("Location");
                Log.d("Received in login", "Location: + "+ location);
            if(firebaseAuth.getCurrentUser() != null)
            {
                Intent intent = new Intent(this,MapsActivity.class);
                intent.putExtra("Name",name);
                intent.putExtra("Email",email);
                intent.putExtra("Location",location);
                startActivity(intent);
            }

        }
        else if(firebaseAuth.getCurrentUser() != null)
        {
            Intent intent = new Intent(this,MapsActivity.class);
            startActivity(intent);
        }



        _emailText=(EditText) findViewById(R.id.input_email);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);
        _signupLink = (TextView) findViewById(R.id.link_signup);
        _forgotPasswordLink = (TextView) findViewById(R.id.link_forgotPassword);
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(),Register.class);
                startActivity(intent);
            }
        });
        _forgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }



    public void login() {

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (!email.equals("") && !password.equals("")) {
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(getBaseContext(), "User Signed In", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(LoginActivity.this, MapsActivity.class);
                        startActivity(i);
                    }else{
                        Toast.makeText(getBaseContext(), "Invalid Credential", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else
        {
            Toast.makeText(getBaseContext(), "One or more fields are empty", Toast.LENGTH_LONG).show();
        }



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

}


