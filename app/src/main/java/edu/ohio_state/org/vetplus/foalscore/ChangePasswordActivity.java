package edu.ohio_state.org.vetplus.foalscore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.ohio_state.org.vetplus.foalscore.util.ConnectionDetector;
import edu.ohio_state.org.vetplus.foalscore.util.Utilities;

/**
 * Created by Veena on 3/18/2015.
 *
 */
public class ChangePasswordActivity extends Activity {
    private String errorMsg;
    private EditText oldPassword;
    private EditText newPassword;
    private EditText confirmPassword;
    private Button submit;
    private ChangePasswordActivityTask ref = null;
    private ProgressDialog pd;
    // flag for Internet connection status
    private Boolean isInternetPresent = false;

    // Connection detector class
    private ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(face);

        oldPassword = (EditText) (findViewById(R.id.old));
        newPassword = (EditText) (findViewById(R.id.newP));
        confirmPassword = (EditText) (findViewById(R.id.confirm));
        submit = (Button) findViewById(R.id.submit_pass);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(getApplicationContext());
                isInternetPresent = cd.isConnectingToInternet();

                if (isInternetPresent) {
                    loadContent();
                } else {
                        AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(ChangePasswordActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
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
    }

    public void loadContent() {
        String old = oldPassword.getText().toString();
        String newP = newPassword.getText().toString();
        String conP = confirmPassword.getText().toString();

        newPassword.setError(null);
        oldPassword.setError(null);
        ref = new ChangePasswordActivityTask();
        if (newP.equals(conP)) {
            if (!isPasswordValid(newP)) {
                newPassword.setError(getString(R.string.error_invalid_password));
                newPassword.requestFocus();
                return;
            } else {
                pd = ProgressDialog.show(ChangePasswordActivity.this, "Loading...", "Please wait...", true, true);
                ref.execute((Void) null);
            }
        }
            else {
            newPassword.setError("Password Mismatch!");
            newPassword.requestFocus();
            return;
            }
    }

    public class ChangePasswordActivityTask extends AsyncTask<Void, Void, Boolean> {

        private String refText = "";
        private String errorMsg = "Server Error.";

        ChangePasswordActivityTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String old = oldPassword.getText().toString();
            String newP = newPassword.getText().toString();
            String conP = confirmPassword.getText().toString();
            String token = "";

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ChangePasswordActivity.this);
            String errorMsg = "Password mismatch.";
            String userEmail = preferences.getString("userEmail", "");
            token = Utilities.createHash(userEmail + newP + old);
            nameValuePairs.add(new BasicNameValuePair("email", userEmail));
            nameValuePairs.add(new BasicNameValuePair("oldPassword", old));
            nameValuePairs.add(new BasicNameValuePair("newPassword", newP));
            nameValuePairs.add(new BasicNameValuePair("token", token));
            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            int timeoutConnection = 6000;
            int timeoutSocketConnection = 4000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocketConnection);
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
                HttpPost httppost = new HttpPost("http://FoalScore.org.ohio-state.edu/foalscore/server/users/changepassword.json");
                try {
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    // Execute HTTP Post Request
                    Log.i("Change Password", "About to post data...");
                    HttpResponse response = httpclient.execute(httppost);
                    String responseText = EntityUtils.toString(response.getEntity());
                    Log.i("Change Password", "Response Text: " + responseText);
                    JSONObject json = new JSONObject(responseText);
                    String status = json.getString("status");
                    Log.i("LoginActivity", "Status: " + status);
                    if (status.equals("success")) {
                        Log.i("ChangePassword", "Change Password success...");
                        return true;
                        //ChangePasswordActivity.this.startActivity(new Intent(ChangePasswordActivity.this, HomePageActivity.class));
                        //finish();
                    } else {
                        this.errorMsg = json.getString("error");
                        //oldPassword.setError(errorMsg);
                        //oldPassword.requestFocus();
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

            return false;
        }


        protected void onPostExecute(final Boolean success) {

            pd.dismiss();
            if (success) {
                // Store the user email in shared preference
                Toast.makeText(ChangePasswordActivity.this, "Password Changed Successfully!", Toast.LENGTH_SHORT).show();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ChangePasswordActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                //editor.putString("userEmail", mEmail);
                //editor.apply();
                ChangePasswordActivity.this.startActivity(new Intent(ChangePasswordActivity.this, HomePageActivity.class));
                finish();
            } else {
                oldPassword.setError(this.errorMsg);
                oldPassword.requestFocus();
            }
        }

    }
    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 3;
    }
}
