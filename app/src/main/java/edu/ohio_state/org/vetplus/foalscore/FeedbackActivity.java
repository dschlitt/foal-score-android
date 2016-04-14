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
public class FeedbackActivity extends Activity {
    private Button feedbackSubmit;
    private EditText feedback;
    private ProgressDialog pd;
    private Feedback css;
    // flag for Internet connection status
    private Boolean isInternetPresent = false;

    // Connection detector class
    private ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(face);

        feedbackSubmit = (Button) findViewById(R.id.feedback_submit);
        feedback = (EditText) findViewById(R.id.feedback_edit);
        feedbackSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cd = new ConnectionDetector(getApplicationContext());
                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();

                // check for Internet status
                if (isInternetPresent) {
                    feedback();
                }
                else {
                    // Internet connection is not present
                    // Ask user to connect to Internet
                    AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper( FeedbackActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
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

    public void feedback() {
        String feedback = this.feedback.getText().toString();
        if(feedback.trim().length()>0) {
            pd = ProgressDialog.show(this, "Processing", "Please wait...", true, true);
            css = new Feedback();
            css.execute((Void) null);
        } else {
            this.feedback.setError("Please enter your feedback.");
        }
    }

    public class Feedback extends AsyncTask<Void, Void, Boolean> {

        private String refText = "";
        private String errorMsg = "Server Error.";

        Feedback() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {


            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ForgotPasswordActivity.this);
            String errorMsg = "Invalid Email ID.";
            String token ="";
            String feedbackText = feedback.getText().toString();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FeedbackActivity.this);
            String userId = preferences.getString("userId","");
            token = Utilities.createHash(feedbackText + userId);
            nameValuePairs.add(new BasicNameValuePair("feedback", feedbackText));
            nameValuePairs.add(new BasicNameValuePair("userId", userId));
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
            HttpPost httppost = new HttpPost("http://foalscore.org.ohio-state.edu/foalscore/server/users/feedback.json");
            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                Log.i("Feedback", "About to post data...");
                Log.i("Feedback","Feedback: " +feedbackText);
                HttpResponse response = httpclient.execute(httppost);
                String responseText = EntityUtils.toString(response.getEntity());
                Log.i("Feedback", "Response Text: " + responseText);
                JSONObject json = new JSONObject(responseText);
                String status = json.getString("status");
                Log.i("Feedback", "Status: " + status);
                if (status.equals("success")) {
                    Log.i("Feedback", "Forgot Password success...");
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
                Toast.makeText(FeedbackActivity.this, "Your feedback is submitted successfully.", Toast.LENGTH_SHORT).show();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FeedbackActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                //editor.putString("userEmail", mEmail);
                //editor.apply();
                FeedbackActivity.this.startActivity(new Intent(FeedbackActivity.this, MiscellaneousActivity.class));
                finish();
            } else {
                feedback.setError(this.errorMsg);
                feedback.requestFocus();
            }
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
