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
import android.support.v4.app.NavUtils;
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
 */
public class ReferAFriendActivity extends Activity {
    private Button referFriend;
    private ProgressDialog pd;
    private ReferAFriend css;
    private EditText email;
    // flag for Internet connection status
    private Boolean isInternetPresent = false;

    // Connection detector class
    private ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refer_friend);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(font);

        email = (EditText) findViewById(R.id.editText9);
        referFriend = (Button) findViewById(R.id.submitReferral);
        referFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cd = new ConnectionDetector(getApplicationContext());
                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();

                // check for Internet status
                if (isInternetPresent) {
                    refer();
                }
                else {
                    // Internet connection is not present
                    // Ask user to connect to Internet
                    AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(ReferAFriendActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
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

    public void refer() {
        pd = ProgressDialog.show(this, "Processing", "Please wait...", true, true);
        css = new ReferAFriend();
        css.execute((Void) null);
    }

    public class ReferAFriend extends AsyncTask<Void, Void, Boolean> {

        private String refText = "";
        private String errorMsg = "Server Error.";

        ReferAFriend() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {


            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            String token = "";
            //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ForgotPasswordActivity.this);
            String errorMsg = "Invalid Email ID.";
            String referEmail = email.getText().toString();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ReferAFriendActivity.this);
            String userId = preferences.getString("userId","");
            Log.i("ReferAFriend ","userId:"+ userId);
            Log.i("ReferAFriend ","Email: " + referEmail);
            token = Utilities.createHash(referEmail + userId);
            nameValuePairs.add(new BasicNameValuePair("userId", userId));
            nameValuePairs.add(new BasicNameValuePair("referredEmail", referEmail));
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
            HttpPost httppost = new HttpPost("http://foalscore.org.ohio-state.edu/foalscore/server/users/referuser.json");
            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                Log.i("ReferAFriend", "About to post data...");
                HttpResponse response = httpclient.execute(httppost);
                String responseText = EntityUtils.toString(response.getEntity());
                Log.i("ReferAFriend", "Response Text: " + responseText);
                JSONObject json = new JSONObject(responseText);
                String status = json.getString("status");
                Log.i("ReferAFriend", "Status: " + status);
                if (status.equals("success")) {
                    Log.i("Forgot Password", "Forgot Password success...");
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
                Toast.makeText(ReferAFriendActivity.this, "An email has been sent to your friend.", Toast.LENGTH_SHORT).show();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ReferAFriendActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                //editor.putString("userEmail", mEmail);
                //editor.apply();
                ReferAFriendActivity.this.startActivity(new Intent(ReferAFriendActivity.this, MiscellaneousActivity.class));
                finish();
            } else {
                email.setError(this.errorMsg);
                email.requestFocus();
            }
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
