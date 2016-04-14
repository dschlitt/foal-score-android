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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import edu.ohio_state.org.vetplus.foalscore.util.ConnectionDetector;
import edu.ohio_state.org.vetplus.foalscore.util.FoalChecked;
import edu.ohio_state.org.vetplus.foalscore.util.FoalCheckedListAdapter;
import edu.ohio_state.org.vetplus.foalscore.util.Utilities;

/**
 * Created by Veena on 3/18/2015.
 */
public class EMailActivity extends Activity {

    private Button button;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ProgressDialog pd;
    private AddFoalTask ref;
    private EMailTask e;
    private CheckBox selectAll;
    private static ArrayList<String> listOfIds;
    // flag for Internet connection status
    private Boolean isInternetPresent = false;
    private String ids;

    // Connection detector class
    private ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(face);

        View v = getLayoutInflater().inflate(R.layout.email_footer, null);

        listView = (ListView) findViewById(R.id.list);
        listView.addFooterView(v);
        listOfIds = new ArrayList<String>();
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
            AlertDialog dialog = new AlertDialog.Builder(EMailActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog)
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
            textView.setTypeface(face);
            alertTextView.setTypeface(face);
        }
        button = (Button) findViewById(R.id.emailButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(getApplicationContext());
                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();

                // check for Internet status
                if (isInternetPresent) {
                    email();
                }
                else {
                    // Internet connection is not present
                    // Ask user to connect to Internet
                    AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(EMailActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
                            .setTitle("No Active Internet Connection.")
                            .setMessage("Please connect to the Internet and try again.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                    Intent homepage = new Intent(EMailActivity.this, MiscellaneousActivity.class);
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
        });
        selectAll = (CheckBox) findViewById(R.id.selectAll);
    }
    public void email() {
        if(ref != null) return;


        if (listOfIds.size()==0) {
            Toast.makeText(EMailActivity.this, "Please select any foal to export via email.", Toast.LENGTH_SHORT).show();
        }
        else {
            ids ="";
            boolean isFirst = true;
            for (String s : listOfIds) {
                Log.i("EMailActivity", "Item: " + s);
                if (isFirst) {
                    ids += s;
                    isFirst = false;
                } else {
                    ids += "," + s;
                }
            }
            pd = ProgressDialog.show(this, "Sending Email...", "Please wait...", true, true);
            Log.i("EMailActivity", "ids: " + ids);
            e = new EMailTask();
            e.execute();
        }
    }

    public void loadFoals() {
        if(ref != null) return;
        pd = ProgressDialog.show(this, "Loading Foals...", "Please wait...", true, true);
        ref = new AddFoalTask();
        ref.execute();
    }

    public class AddFoalTask extends AsyncTask<Void, Void, Boolean> {

        private String refText = "";
        boolean noFoals = false;
        private String errorMsg = "Server Error.";
        private List<FoalChecked> foalList = new ArrayList<FoalChecked>();

        AddFoalTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Create a new HttpClient and Post Header
            //boolean noFoals = false;HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            int timeoutConnection = 6000;
            int timeoutSocketConnection = 4000;
            String token ="";
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocketConnection);
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            HttpPost httppost = new HttpPost("http://foalscore.org.ohio-state.edu/foalscore/server/foals/all.json");
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(EMailActivity.this);
            String userId = preferences.getString("userId","");
            token = Utilities.createHash(userId);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("userId", userId));
            nameValuePairs.add(new BasicNameValuePair("token",token));
            Log.i("EMailActivity", "About to post data. userId: " + userId);
            try {
                // Execute HTTP Get Request
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                String responseText = EntityUtils.toString(response.getEntity());
                Log.i("EmailActivity", "Response Text: " + responseText);
                JSONObject json = new JSONObject(responseText);
                String status = json.getString("status");
                Log.i("EMailActivity","Status: " + status);
                if(status.equals("success")) {
                    refText = json.getString("foals");
                    JSONArray foal = json.getJSONArray("foals");
                    Log.i("EMailActivity", "Foal Length: " + foal.length());
                    if (foal.length() == 0) {
                        noFoals = true;
                    }
                    else {
                        noFoals = false;
                        for (int i = 0; i < foal.length(); i++) {
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
                            FoalChecked foalObj = new FoalChecked(id, name, age, gender, breed, date, temperature, respiratoryRate, heartRate, dystocia, "0");

                            foalList.add(foalObj);
                        }
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
            if (success) {
                // This is the array adapter, it takes the context of the activity as a
                // first parameter, the type of list view as a second parameter and your
                // array as a third parameter.
                Log.i("EMailActivity", "Success. " + noFoals);
                if (noFoals) {
                    Log.i("EMailActivity", "About to redirect...");
                    Toast.makeText(EMailActivity.this, "No foals to export via email. Please add a foal and try again.", Toast.LENGTH_SHORT).show();
                    EMailActivity.this.startActivity(new Intent(EMailActivity.this, HomePageActivity.class));
                    finish();
                } else {
                    final FoalCheckedListAdapter adapter = new FoalCheckedListAdapter(EMailActivity.this, foalList);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            Log.i("EMail Activity", "Inside onclick ");
                            FoalChecked selectedFoal = foalList.get(position);
                            String temp = selectedFoal.getId();
                            listView.setSelection(position);
                            if (foalList.get(position).getIsChecked().equals("1")) {
                                Log.i("EMailActivity", "Unchecking item " + position);
                                foalList.get(position).setIsChecked("0");
                                if (EMailActivity.listOfIds.contains(temp)) {
                                    Log.i("EMailActivity", "Contains item " + position + "; Removing...");
                                    EMailActivity.listOfIds.remove(temp);
                                    selectAll.setChecked(false);
                                }
                            } else {
                                foalList.get(position).setIsChecked("1");
                                Log.i("EMailActivity", "Adding foal id " + temp);
                                EMailActivity.listOfIds.add(temp);
                                Log.i("EMailActivity", "List Length: " + EMailActivity.listOfIds.size());
                            }
                            adapter.setValues(foalList);
                            adapter.notifyDataSetChanged();
                            Log.i("EMail Activity", "FoalID: " + temp);
                        }
                    });
                    selectAll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            List<FoalChecked> list = adapter.getValues();
                            if (selectAll.isChecked()) {
                                for (FoalChecked foal : list) {
                                    foal.setIsChecked("1");
                                    listOfIds.add(foal.getId());
                                }
                            } else {
                                for (FoalChecked foal : list) {
                                    foal.setIsChecked("0");
                                    listOfIds = new ArrayList<String>();
                                }
                            }
                            adapter.setValues(list);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
                Log.i("FoalsActivity", "Status: success");
            } else{
                AlertDialog dialog = new AlertDialog.Builder( new ContextThemeWrapper(EMailActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
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

    public class EMailTask extends AsyncTask<Void, Void, Boolean> {
        private String errorMsg = "Server Error.";

        EMailTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(EMailActivity.this);
            String userEmail = preferences.getString("userEmail","");
            //Log.i("EMail Activity","List of IDs: "+listOfIds);
            String token= "";
            token = Utilities.createHash(ids + userEmail);
            nameValuePairs.add(new BasicNameValuePair("userEmail",userEmail));
            nameValuePairs.add(new BasicNameValuePair("foalids", ids));
            nameValuePairs.add(new BasicNameValuePair("token",token));
            Log.i("EMail Activity","Foal IDs: " +ids);
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://foalscore.org.ohio-state.edu/foalscore/server/foals/export.json");

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                //Log.i("RegistrationActivity", "About to post data...");
                HttpResponse response = httpclient.execute(httpPost);
                String responseText = EntityUtils.toString(response.getEntity());
                Log.i("EMail Activity", "Response Text: " + responseText);
                JSONObject json = new JSONObject(responseText);
                String status = json.getString("status");
                Log.i("EMail Activity","Status: " + status);
                if(status.equals("success")) {
                    // Log.i("LoginActivity","");
                    return true;
                } else {
                    this.errorMsg = json.getString("error");
                    return false;
                }
            } catch (Exception e) {
                Log.e("EMail Activity", "Exception Caught...");
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            ref = null;
            pd.dismiss();
            if(success) {
                Toast.makeText(EMailActivity.this, "An E-mail has been sent to you.", Toast.LENGTH_SHORT).show();
                EMailActivity.this.startActivity(new Intent(EMailActivity.this, HomePageActivity.class));
            } else {
                AlertDialog dialog = new AlertDialog.Builder( new ContextThemeWrapper(EMailActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
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
