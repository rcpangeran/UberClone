package com.learn.uberclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.apache.commons.lang3.StringUtils;

public class SignUpLoginActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {
    enum State {SIGNUP, LOGIN}

    private State state;
    private EditText edtSignUp_Username, edtSignUp_Password, edtSignUp_DriverPassenger;
    private RadioButton rdoSignUp_Passenger, rdoSignUp_Driver;
    private Button btnSignUp_Action, btnSignUp_OneTimeLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Save the current Installation to Back4App
        installToBack4App();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_login);
        assignUI();

        // Initialize state enum
        initializeState();

        setTitle("UBER CLONE");

        // Check if user already login or not yet login
        checkUserAlreadyLoggedIn();

        // Call All OnClick Event Handler
        callAllOnClickEvent();

        // Call All OnKey Event Handler
        callOnKeyEvent();
    }

    private void installToBack4App() {
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signup, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.menuSignUp_SignUpLogin) :
                onOptionsItemSelected_selectSignUpLogin(item);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void onOptionsItemSelected_selectSignUpLogin(MenuItem item) {
        if (state == State.SIGNUP) {
            state = State.LOGIN;
            item.setTitle("Sign Up");
            btnSignUp_Action.setText("Log In");
        } else if (state == State.LOGIN) {
            state = State.SIGNUP;
            item.setTitle("Log In");
            btnSignUp_Action.setText("Sign Up");
        }
    }

    private void assignUI() {
        edtSignUp_Username = findViewById(R.id.edtSignUp_Username);
        edtSignUp_Password = findViewById(R.id.edtSignUp_Password);
        edtSignUp_DriverPassenger = findViewById(R.id.edtSignUp_DriverPassenger);
        rdoSignUp_Passenger = findViewById(R.id.rdoSignUp_Passenger);
        rdoSignUp_Driver = findViewById(R.id.rdoSignUp_Driver);
        btnSignUp_Action = findViewById(R.id.btnSignUp_Action);
        btnSignUp_OneTimeLogin = findViewById(R.id.btnSignUp_OneTimeLogin);
    }

    private void initializeState() {
        state = State.SIGNUP;
    }

    private void checkUserAlreadyLoggedIn() {
        // Check whether user already signed in or not
        if (ParseUser.getCurrentUser() != null) {
            // ParseUser.logOutInBackground();
            transitionToPassengerActivity();
        }
    }

    private void transitionToPassengerActivity() {
        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().get("as").equals("Passenger")) {
                Intent intent = new Intent(SignUpLoginActivity.this, PassengerActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void callAllOnClickEvent() {
        btnSignUp_Action.setOnClickListener(this);
        btnSignUp_OneTimeLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.btnSignUp_Action) :
                onClick_SignUpOrLogin();
                break;
            case (R.id.btnSignUp_OneTimeLogin) :
                onClick_AnonymousLogin();
                break;
        }
    }

    private void onClick_SignUpOrLogin() {
        if (state == State.SIGNUP) {
            if (!rdoSignUp_Passenger.isChecked() && !rdoSignUp_Driver.isChecked()) {
                Toast.makeText(this,
                        "Are you a passenger or a driver?",
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            ParseUser appUser = new ParseUser();
            appUser.setUsername(edtSignUp_Username.getText().toString());
            appUser.setPassword(edtSignUp_Password.getText().toString());

            if (rdoSignUp_Passenger.isChecked()) {
                appUser.put("as", "Passenger");
            } else if (rdoSignUp_Driver.isChecked()) {
                appUser.put("as", "Driver");
            }

            final ProgressDialog dialog = new ProgressDialog(SignUpLoginActivity.this);
            dialog.setMessage("Signing up, Please wait...");
            dialog.show();

            appUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        dialog.dismiss();
                        Toast.makeText(SignUpLoginActivity.this,
                                "Signed up",
                                Toast.LENGTH_LONG)
                                .show();
                        transitionToPassengerActivity();
                    }
                }
            });
        } else if (state == State.LOGIN) {
            final ProgressDialog dialog = new ProgressDialog(SignUpLoginActivity.this);
            dialog.setMessage("Logging in...");
            dialog.show();

            ParseUser.logInInBackground(edtSignUp_Username.getText().toString(), edtSignUp_Password.getText().toString(), new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (user != null && e == null) {
                        dialog.dismiss();
                        Toast.makeText(SignUpLoginActivity.this,
                                "User logged in",
                                Toast.LENGTH_LONG)
                                .show();
                        transitionToPassengerActivity();
                    }
                }
            });
        }
    }

    private void onClick_AnonymousLogin() {
        if (edtSignUp_DriverPassenger.getText().toString().toUpperCase().equals("DRIVER") ||
            edtSignUp_DriverPassenger.getText().toString().toUpperCase().equals("PASSENGER")) {

            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Please wait...");
            dialog.show();

            if (ParseUser.getCurrentUser() == null) {
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user != null && e == null) {
                            dialog.dismiss();
                            Toast.makeText(SignUpLoginActivity.this,
                                    "We have an anonymous user",
                                    Toast.LENGTH_SHORT)
                                .show();
                            user.put("as", StringUtils.capitalize(edtSignUp_DriverPassenger.getText().toString().toLowerCase()));
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    transitionToPassengerActivity();
                                }
                            });
                        }
                    }
                });
            }
        } else {
            Toast.makeText(this,
                    "Are you a driver or a passenger?",
                    Toast.LENGTH_SHORT)
                .show();
        }
    }

    private void callOnKeyEvent() {
        edtSignUp_Password.setOnKeyListener(this);
        edtSignUp_DriverPassenger.setOnKeyListener(this);
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        switch (view.getId()) {
            case (R.id.edtSignUp_Password) :
                onKey_SignUpOrLogin(keyCode, event);
                break;
            case (R.id.edtSignUp_DriverPassenger) :
                onKey_AnonymousLogin(keyCode, event);
                break;
        }
        return false;
    }

    private void onKey_SignUpOrLogin(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            onClick(btnSignUp_Action);
        }
    }

    private void onKey_AnonymousLogin(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            onClick(btnSignUp_OneTimeLogin);
        }
    }
}
