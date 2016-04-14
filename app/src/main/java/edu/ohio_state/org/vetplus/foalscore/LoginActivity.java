package edu.ohio_state.org.vetplus.foalscore;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.*;

import edu.ohio_state.org.vetplus.foalscore.util.ConnectionDetector;
import edu.ohio_state.org.vetplus.foalscore.util.Utilities;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    // flag for Internet connection status
    private Boolean isInternetPresent = false;

    // Connection detector class
    private ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(font);

        checkFirstRun();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userId = preferences.getString("userId","");
        if(userId != "") {
            int UserID = Integer.parseInt(userId);
            if (UserID > 0) {
                Log.i("LoginActivity", "Auto logging in UserId: " + userId);
                this.startActivity(new Intent(this, HomePageActivity.class));
                finish();
            }
        }
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                cd = new ConnectionDetector(getApplicationContext());
                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();

                // check for Internet status
                if (isInternetPresent) {
                    attemptLogin();
                }
                else {
                    // Internet connection is not present
                    // Ask user to connect to Internet
                    AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
                            .setTitle("No Active Internet Connection.")
                            .setMessage("Please connect to the Internet and try again.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                    final int alertTitle = getResources().getIdentifier( "alertTitle", "id", "android" );
                    TextView alertTextView = (TextView) dialog.findViewById(alertTitle);
                    Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
                    textView.setTypeface(face);
                    alertTextView.setTypeface(face);
                }
            }
        });

        TextView registration = (TextView) findViewById(R.id.registration);
        registration.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }

        });

        TextView forgotPassword = (TextView) findViewById(R.id.forgotPassword);
            forgotPassword.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoginActivity.this.startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
                }

            });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void checkFirstRun() {
        boolean isFirstRun = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).getBoolean("isFirstRun", true);
        if (isFirstRun){
            // Display a popup with important information if the app is opened for the first time.
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Important Information").setMessage(R.string.important_information).setNeutralButton("OK", null).show();
            TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            final int alertTitle = getResources().getIdentifier("alertTitle", "id", "android");
            TextView alertTextView = (TextView) dialog.findViewById(alertTitle);
            Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
            textView.setTypeface(face);
            alertTextView.setTypeface(face);

            PreferenceManager.getDefaultSharedPreferences(LoginActivity.this)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private String userId;
        private String userName;
        private String errorMsg = "Email Id or password is invalid.";
        String isTempPassword = "0";

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String token="";

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            token = Utilities.createHash(mEmail + mPassword);
            nameValuePairs.add(new BasicNameValuePair("email", mEmail));
            nameValuePairs.add(new BasicNameValuePair("password", mPassword));
            nameValuePairs.add(new BasicNameValuePair("token",token));

            // Create a new HttpClient and Post Header
            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            int timeoutConnection = 6000;
            int timeoutSocketConnection = 4000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocketConnection);
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            HttpPost httppost = new HttpPost("http://foalscore.org.ohio-state.edu/foalscore/server/users/login.json");

            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                Log.i("LoginActivity","About to post data...");
                HttpResponse response = httpclient.execute(httppost);
                String responseText = EntityUtils.toString(response.getEntity());
                Log.i("LoginActivity","Response Text: " + responseText);
                JSONObject json = new JSONObject(responseText);
                String status = json.getString("status");
                Log.i("LoginActivity","Status: " + status);
                if(status.equals("success")) {
                    userId = json.getJSONObject("userObj").getJSONObject("User").getString("id");
                    userName = json.getJSONObject("userObj").getJSONObject("User").getString("name");
                    isTempPassword = json.getJSONObject("userObj").getJSONObject("User").getString("isTempPassword");
                    Log.i("isTempPassword: " ,"Status: "+ isTempPassword);
                    Log.i("LoginActivity","Login success...");
                    return true;
                } else {
                    this.errorMsg = json.getString("error");
                    return false;
                }
            } catch(ClientProtocolException e) {
                e.printStackTrace();
                this.errorMsg = "Unable to connect to server. Please check your internet connection and try again.";
                return false;
            } catch(IOException e) {
                e.printStackTrace();
                this.errorMsg = "Unable to connect to server. Please check your internet connection and try again.";
                return false;
            } catch (Exception e) {
                Log.e("FoalsActivity", "Exception Caught...");
                e.printStackTrace();
                this.errorMsg = "An unknown error occurred. Please try again later.";
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if(success) {
                // Store the user email in shared preference
                if (isTempPassword.equals("0")) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("userId", userId);
                    editor.putString("userName",userName);
                    editor.putString("userEmail", mEmail);
                    editor.apply();
                    LoginActivity.this.startActivity(new Intent(LoginActivity.this, HomePageActivity.class));
                    finish();
                }
                else if (isTempPassword.equals("1")) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("userId", userId);
                    editor.putString("userName",userName);
                    editor.putString("userEmail", mEmail);
                    editor.apply();
                    LoginActivity.this.startActivity(new Intent(LoginActivity.this, ChangePasswordActivity.class));
                    finish();
                }
            } else {
                mPasswordView.setError(this.errorMsg);
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}



