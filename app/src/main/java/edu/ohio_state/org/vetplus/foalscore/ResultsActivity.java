package edu.ohio_state.org.vetplus.foalscore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
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
import edu.ohio_state.org.vetplus.foalscore.util.Foal;
import edu.ohio_state.org.vetplus.foalscore.util.Utilities;

/**
 * Created by Veena on 3/25/2015.
 */
public class ResultsActivity extends Activity {
    private WebView resultView;
    private Button saveResult;
    private String calculationId;
    List<NameValuePair> nameValuePairs;
    private String scoreType;
    private ProgressDialog pd;
    private AddResultTask ref = null;
    private static final int REQUEST_CODE = 1;
    // flag for Internet connection status
    private Boolean isInternetPresent = false;

    // Connection detector class
    private ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(font);

        resultView = (WebView) findViewById(R.id.result);
        Bundle extras = getIntent().getExtras();
        Utilities.changeFontSize(resultView, this);
        if (extras != null) {
            String score = extras.getString("score");
            String res = extras.getString("result");
            scoreType = extras.getString("scoreType");
            calculationId = extras.getString("calculationId");

            String result = "<html><head>"
                    + "<style type=\"text/css\">body{color: #fff; background-color: #962A1E;}"
                    + "</style></head>"
                    + "<body> <table style=\"height:100%;width:100%\"> <tr> <td style=\"vertical-align:bottom; text-align:center\"> "
                    + "<b>Total score: " + score + "</b>"
                    + "</td></tr> <tr><td style=\"vertical-align: top; text-align:center\"><div style=\"padding-top:15px;\">"
                    + res
                    + "</div> </td> </tr> </table>"
                    + "</body></html>";

            resultView.loadData(result, "text/html", "utf-8");
        }

        saveResult = (Button) findViewById(R.id.saveScore);
        saveResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cd = new ConnectionDetector(getApplicationContext());
                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();

                // check for Internet status
                if (isInternetPresent) {
                    Intent i = new Intent(ResultsActivity.this, FoalsActivity.class);
                    i.putExtra("requestCode", REQUEST_CODE);
                    ResultsActivity.this.startActivityForResult(i, REQUEST_CODE);
                }
                else {
                    // Internet connection is not present
                    // Ask user to connect to Internet
                    AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(ResultsActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String token = "";
        if(requestCode == REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                String foalId = data.getStringExtra("foalId");
                Log.i("ResultsActivity", "Foal Id: " + foalId);
                token = Utilities.createHash(calculationId + foalId);
                nameValuePairs =  new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("foalId", foalId));
                nameValuePairs.add(new BasicNameValuePair("calculationId", calculationId));
                nameValuePairs.add(new BasicNameValuePair("token", token));
                pd = ProgressDialog.show(this, "Saving results...", "Please wait...", true, true);
                ref = new AddResultTask();
                ref.execute();
            }
        }
    }

    public class AddResultTask extends AsyncTask<Void, Void, Boolean> {

        private String errorMsg = "Server Error.";
        private List<Foal> foalList = new ArrayList<Foal>();

        AddResultTask() {
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
            HttpPost httppost = null;
            if(scoreType.equals("survivalScore")) {
                httppost = new HttpPost("http://foalscore.org.ohio-state.edu/foalscore/server/survivalscores/foalsurvivalcalculatorlink.json");
            } else {
                httppost = new HttpPost("http://foalscore.org.ohio-state.edu/foalscore/server/sepsisscores/foalsepsiscalculatorlink.json");
            }

            Log.i("ResultsActivity", "About to post data. " + scoreType + " ;calcId: " + calculationId);
            try {
                // Execute HTTP Get Request
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                String responseText = EntityUtils.toString(response.getEntity());
                Log.i("ResultsActivity", "Response Text: " + responseText);
                JSONObject json = new JSONObject(responseText);
                String status = json.getString("status");
                Log.i("ResultsActivity","Status: " + status);
                if(status.equals("success")) {
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
            pd.dismiss();
            if(success) {
                Toast.makeText(ResultsActivity.this, "Results saved successfully!", Toast.LENGTH_SHORT).show();
                ResultsActivity.this.startActivity(new Intent(ResultsActivity.this, HomePageActivity.class));
            } else {
                AlertDialog dialog = new AlertDialog.Builder( new ContextThemeWrapper(ResultsActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
                        .setTitle("Error")
                        .setMessage(errorMsg)
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

        @Override
        protected void onCancelled() {
            ref = null;
            pd.dismiss();
        }
    }
}