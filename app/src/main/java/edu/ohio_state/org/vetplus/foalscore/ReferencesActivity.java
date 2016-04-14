package edu.ohio_state.org.vetplus.foalscore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.webkit.WebView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

import edu.ohio_state.org.vetplus.foalscore.util.ConnectionDetector;
import edu.ohio_state.org.vetplus.foalscore.util.Utilities;

/**
 * Created by Veena on 3/25/2015.
 */
public class ReferencesActivity extends Activity {

    private ReferencesTask ref = null;
    private ProgressDialog pd;
    private WebView webView;
    // flag for Internet connection status
    private Boolean isInternetPresent = false;

    // Connection detector class
    private ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_references);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(font);

        webView = (WebView) findViewById(R.id.references);
        cd = new ConnectionDetector(getApplicationContext());
        // get Internet status
        isInternetPresent = cd.isConnectingToInternet();

        // check for Internet status
        if (isInternetPresent) {
            loadContent();
        }
        else {
            // Internet connection is not present
            // Ask user to connect to Internet
            AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(ReferencesActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
                    .setTitle("No Active Internet Connection.")
                    .setMessage("Please connect to the Internet and try again.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            Intent homepage = new Intent(ReferencesActivity.this, OverviewActivity.class);
                            startActivity(homepage);
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
        Utilities.changeFontSize(webView, this);
    }

    public void loadContent() {
        if(ref != null) return;
        pd = ProgressDialog.show(this, "Loading...", "Please wait...", true, true);
        ref = new ReferencesTask();
        ref.execute();
    }

    public class ReferencesTask extends AsyncTask<Void, Void, Boolean> {

        private String refText = "";
        private String errorMsg = "Server Error.";

        ReferencesTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {

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
            HttpGet httpget = new HttpGet("http://foalscore.org.ohio-state.edu/foalscore/server/contents/references.json");

            try {
                // Execute HTTP Post Request
                //Log.i("RegistrationActivity", "About to post data...");
                HttpResponse response = httpclient.execute(httpget);
                String responseText = EntityUtils.toString(response.getEntity());
                Log.i("Overview", "Response Text: " + responseText);
                JSONObject json = new JSONObject(responseText);
                String status = json.getString("status");
                Log.i("Overview","Status: " + status);
                if(status.equals("success")) {
                    // Log.i("LoginActivity","");
                    refText = "<body style=\"text-align:justify\"><div style = 'padding: 4px;'" + json.getString("text") + "</div></body>";
                    Log.i("Overview","Ref Text: " + refText);
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
            ref = null;
            pd.dismiss();
            if(success) {
                webView.loadData(refText, "text/html", "utf-8");
                Log.i("Overview","Status: success");
            } else {
                webView.loadData(errorMsg, "text/html", "utf-8");
            }
        }

        @Override
        protected void onCancelled() {
            ref = null;
            pd.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
