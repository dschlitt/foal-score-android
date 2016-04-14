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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import edu.ohio_state.org.vetplus.foalscore.util.ConnectionDetector;
import edu.ohio_state.org.vetplus.foalscore.util.Foal;
import edu.ohio_state.org.vetplus.foalscore.util.FoalListAdapter;
import edu.ohio_state.org.vetplus.foalscore.util.Utilities;

/**
 * Created by Veena on 3/18/2015.
 */
public class FoalsActivity extends Activity {

    private Button addFoal;
    private ListView listview;
    private ProgressDialog pd;
    private boolean isRequestFromIntent;
    private ListFoalsTask ref = null;
    private static final int REQUEST_CODE = 2;
    // flag for Internet connection status
    private Boolean isInternetPresent = false;
    private String allowShare;

    // Connection detector class
    private ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foals);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(font);

        listview = (ListView) findViewById(R.id.listView);
        // Check if this activity is started from another activity using intent
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            isRequestFromIntent = true;
        } else {
            isRequestFromIntent = false;
        }
        addFoal = (Button) findViewById(R.id.addFoal);
        addFoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRequestFromIntent) {
                    /*Intent intent = new Intent(FoalsActivity.this, AddFoalActivity.class);
                    intent.putExtra("requestCode", REQUEST_CODE);
                    Log.i("FoalsActivity", "About to send request " + REQUEST_CODE);
                    FoalsActivity.this.startActivityForResult(intent, REQUEST_CODE);*/
                    Intent intent = new Intent(FoalsActivity.this, AddFoalActivity.class);
                    intent.putExtra("requestCode", REQUEST_CODE);
                    intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    startActivity(intent);
                    finish();
                } else {
                    /*Intent k = new Intent(getApplicationContext(), AddFoalActivity.class);
                    k.putExtra("isAdd","1");
                    startActivity(k);*/
                    FoalsActivity.this.startActivity(new Intent(FoalsActivity.this, AddFoalActivity.class));
                }
            }
        });
        cd = new ConnectionDetector(getApplicationContext());
        // get Internet status
        isInternetPresent = cd.isConnectingToInternet();

        // check for Internet status
        if (isInternetPresent) {
            loadFoals();
        }
        else {
            // Internet connection is not present
            // Ask user to connect to Internet
            AlertDialog dialog = new AlertDialog.Builder( new ContextThemeWrapper(this, R.style.Base_V7_Theme_AppCompat_Dialog))
                    .setTitle("No Active Internet Connection.")
                    .setMessage("Please connect to the Internet and try again.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            Intent homepage = new Intent(FoalsActivity.this, HomePageActivity.class);
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
    }

    public void loadFoals() {
        if(ref != null) return;
        pd = ProgressDialog.show(this, "Loading Foals...", "Please wait...", true, true);
        ref = new ListFoalsTask();
        ref.execute();
    }

    public class ListFoalsTask extends AsyncTask<Void, Void, Boolean> {

        private String refText = "";
        private String errorMsg = "Server Error.";
        private List<Foal> foalList = new ArrayList<Foal>();

        ListFoalsTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            String token="";
            int timeoutConnection = 6000;
            int timeoutSocketConnection = 4000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocketConnection);
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            HttpPost httppost = new HttpPost("http://foalscore.org.ohio-state.edu/foalscore/server/foals/all.json");
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FoalsActivity.this);
            String userId = preferences.getString("userId","");
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            token = Utilities.createHash(userId);
            nameValuePairs.add(new BasicNameValuePair("userId", userId));
            nameValuePairs.add(new BasicNameValuePair("token", token));
            Log.i("FoalsActivity","token: "+ token);
            Log.i("FoalsActivity", "About to post data. userId: " + userId + " Time: " + new Date());
            try {
                // Execute HTTP POST Request
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                String responseText = EntityUtils.toString(response.getEntity());
                Log.i("FoalsActivity", "Response Text: " + responseText);
                JSONObject json = new JSONObject(responseText);
                String status = json.getString("status");
                Log.i("FoalsActivity","Status: " + status);
                if(status.equals("success")) {
                    refText = json.getString("foals");
                    JSONArray foal = json.getJSONArray("foals");
                    for (int i=0; i<foal.length(); i++) {
                        JSONObject foalObject = foal.getJSONObject(i);
                        String id = foalObject.getString("id");
                        String name = foalObject.getString("name");
                        String age = foalObject.getString("ageMonths");
                        String gender = foalObject.getString("gender");
                        String breed = foalObject.getString("breed");
                        String date = foalObject.getString("addedDate");
                        String temperature = foalObject.getString("temperature");
                        String respiratoryRate = foalObject.getString("respiratoryRate");
                        String heartRate = foalObject.getString("heartRate");
                        String dystocia = foalObject.getString("dystocia");
                        String survivedUntilDischarge = foalObject.getString("survivedUntilHospitalDischarge");
                        allowShare = foalObject.getString("allowShare");
                        Foal foalObj = new Foal(id, name, age, gender, breed, date,temperature,respiratoryRate,heartRate,dystocia, allowShare, survivedUntilDischarge);

                        foalList.add(foalObj);
                    }
                    Log.i("FoalsActivity","Foals: " + refText);
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
                // This is the array adapter, it takes the context of the activity as a
                // first parameter, the type of list view as a second parameter and your
                // array as a third parameter.
                FoalListAdapter adapter = new FoalListAdapter(FoalsActivity.this, foalList);
                listview.setAdapter(adapter);
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Foal selectedFoal = foalList.get(position);
                        Log.i("FoalsActivity", "List item selected: " + position);
                        Log.i("FoalsActivity", "isRequestFromIntent: " + isRequestFromIntent);
                        if(isRequestFromIntent) {
                            String foalId = selectedFoal.getId();
                            Intent intent = new Intent();
                            intent.putExtra("foalId", foalId);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            // Redirect to foal details activity
                            Intent intent = new Intent(FoalsActivity.this, FoalDetailsActivity.class);
                            intent.putExtra("foalObj", selectedFoal);
                            FoalsActivity.this.startActivity(intent);
                        }
                    }
                });
                Log.i("FoalsActivity","Status: success");
            } else {
                AlertDialog dialog = new AlertDialog.Builder( new ContextThemeWrapper(FoalsActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
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

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
