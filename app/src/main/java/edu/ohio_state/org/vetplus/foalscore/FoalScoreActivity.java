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
import android.widget.CheckBox;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.ohio_state.org.vetplus.foalscore.util.ConnectionDetector;
import edu.ohio_state.org.vetplus.foalscore.util.CustomListAdapter;
import edu.ohio_state.org.vetplus.foalscore.util.SepsisListItem;
import edu.ohio_state.org.vetplus.foalscore.util.SpinnerObj;
import edu.ohio_state.org.vetplus.foalscore.util.Utilities;

/**
 * Created by Veena on 3/24/2015.
 */
public class FoalScoreActivity extends Activity {

    private Button calculateSurvival;
    private ProgressDialog pd;
    private calculateSurvivalScore css = null;
    private ListView listView;
    private SepsisListItem[] items;
    private CheckBox checkBoxSurvival;
    private TextView text;
    // flag for Internet connection status
    private Boolean isInternetPresent = false;
    private String allowShare;

    // Connection detector class
    private ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foal_score);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(font);

        listView = (ListView) findViewById(R.id.survival_list);

        SepsisListItem coldExtremeties = new SepsisListItem("Cold Extremities", new SpinnerObj[] {
                new SpinnerObj("Yes", "0"),
                new SpinnerObj("No", "2")
        }, "coldExtremities");
        SepsisListItem prematurity = new SepsisListItem("Prematurity (&lt; 320 days)", new SpinnerObj[] {
                new SpinnerObj("Yes", "0"),
                new SpinnerObj("No", "1")
        }, "prematurity");
        SepsisListItem infection = new SepsisListItem("&ge; 2 infection/inflammation sites", new SpinnerObj[] {
                new SpinnerObj("Yes", "0"),
                new SpinnerObj("No", "1")
        }, "GreaterThanEqualToTwoInfectionSites");
        SepsisListItem lgG = new SepsisListItem("lgG (mg/dL)", new SpinnerObj[] {
                new SpinnerObj("&lt; 400", "0"),
                new SpinnerObj("&ge; 400", "1")
        }, "igG");
        SepsisListItem glucose = new SepsisListItem("Glucose (mg/dL)", new SpinnerObj[] {
                new SpinnerObj("&lt; 80", "0"),
                new SpinnerObj("&ge; 80", "1")
        }, "glucose");
        SepsisListItem wbc = new SepsisListItem("WBC", new SpinnerObj[] {
                new SpinnerObj("&le; 4 x 10 <sup><small>3</small></sup>/microliter", "0"),
                new SpinnerObj("&gt; 4 x 10 <sup><small>3</small></sup>/microliter", "1")
        }, "wbc");

        items = new SepsisListItem[] {coldExtremeties, prematurity, infection, lgG, glucose, wbc};
        CustomListAdapter listAdapter = new CustomListAdapter(this, items);
        View v = getLayoutInflater().inflate(R.layout.survival_footer_layout, null);
        listView.addFooterView(v);
        listView.setAdapter(listAdapter);
        checkBoxSurvival = (CheckBox) v.findViewById(R.id.checkBoxSurvival);

        text = (TextView) v.findViewById(R.id.text);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(FoalScoreActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
                        .setTitle("Sharing Information with The Ohio State University")
                        .setMessage("The FoalScore App offers an option to share data with The Ohio State University that will be used for future studies. If shared, data from this App will ONLY be used for research purposes and it will not reveal personal information from its users. User information is not required to use this App.")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
                TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                final int alertTitle = getResources().getIdentifier("alertTitle", "id", "android");
                TextView alertTextView = (TextView) dialog.findViewById(alertTitle);
                Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
                textView.setTypeface(face);
                alertTextView.setTypeface(face);
            }
        });
        calculateSurvival = (Button) v.findViewById(R.id.calculateSurvival);
        calculateSurvival.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cd = new ConnectionDetector(getApplicationContext());
                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();

                // check for Internet status
                if (isInternetPresent) {
                    calculate();
                } else {
                    // Internet connection is not present
                    offlineSetUp();
                }
            }
        });
    }
    public void offlineSetUp(){
        // Validate if all the spinners have values
        CustomListAdapter adapter = (CustomListAdapter)((HeaderViewListAdapter)listView.getAdapter()).getWrappedAdapter();
        // Flag to check if a value has been selected for all the fields
        boolean isValid = true;
        // Flag to check if "Not Available" option is selected for at-least one
        boolean isNotAvailablePresent = false;
        final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        adapter.resetError();
        for(int i = 0; i < adapter.getCount(); i++) {
            SepsisListItem it = adapter.getItem(i);
            String paramKey = it.getParameterKey();
            Spinner spinner = (Spinner) adapter.getView(i, null, null).findViewById(R.id.spinner);
            TextView fieldTitle = (TextView) adapter.getView(i, null, null).findViewById(R.id.field_title);
            try {
                SpinnerObj spObj = (SpinnerObj) spinner.getSelectedItem();
                String paramValue = spObj.getValue();
                nameValuePairs.add(new BasicNameValuePair(paramKey, paramValue));
                Log.i("FoalScoreActivity","key: " + paramKey + " Val: " + paramValue);
                //temp.put(paramKey,paramValue);
            } catch(Exception e) {
                adapter.setError(i, "Please choose an option.");
                //spinner.requestFocus();
                //fieldTitle.requestFocus();
                adapter.notifyDataSetChanged();
                adapter.getView(i, null, null).findViewById(R.id.field_title).requestFocus();
                Toast.makeText(this, "Choose an option for " + fieldTitle.getText(), Toast.LENGTH_SHORT).show();
                isValid = false;
                break;
            }
        }

        if (isValid) {
            ArrayList<String> result = offlineCalculation(nameValuePairs);
            Log.i("FoalScoreActivity", "This is the offline result: " + result);
            Intent intent = new Intent(FoalScoreActivity.this, ResultsActivity.class);
            intent.putExtra("result", result.get(0));
            intent.putExtra("score", result.get(1));
            intent.putExtra("calculationId", (String) null);
            intent.putExtra("scoreType", "survivalScore");
            startActivity(intent);
        }
    }

    public ArrayList<String> offlineCalculation(List<NameValuePair> nameValuePairs) {
        ArrayList<String> totalResult = new ArrayList<String>();
        String prediction = "";
        int totalScore = 0;

        for (NameValuePair nvPair : nameValuePairs) {

            String value = nvPair.getValue();
            int valueInteger = Integer.parseInt(value);
            totalScore += valueInteger;
        }
        switch (totalScore){
            case 0:
                prediction = "The foal has 3% chance of survival.";
                break;
            case 1:
                prediction = "The foal has 8% chance of survival.";
                break;
            case 2:
                prediction = "The foal has 18% chance of survival.";
                break;
            case 3:
                prediction = "The foal has 38% chance of survival.";
                break;
            case 4:
                prediction = "The foal has 62% chance of survival.";
                break;
            case 5:
                prediction = "The foal has 82% chance of survival.";
                break;
            case 6:
                prediction = "The foal has 92% chance of survival.";
                break;
            case 7:
                prediction = "The foal has 97% chance of survival.";
                break;
        }


        totalResult.add(0,prediction);
        totalResult.add(1, Integer.toString(totalScore));
        return totalResult;
    }

    public void calculate() {
        if(css != null) return;

        // Validate if all the spinners have values
        CustomListAdapter adapter = (CustomListAdapter)((HeaderViewListAdapter)listView.getAdapter()).getWrappedAdapter();
        boolean isValid = true;
        if (checkBoxSurvival.isChecked()) {
            allowShare = "1";
        }
        else {
            allowShare = "0";
        }
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        adapter.resetError();
        //temp = new HashMap<String,String>();
        for(int i = 0; i < adapter.getCount(); i++) {
            SepsisListItem it = adapter.getItem(i);
            String paramKey = it.getParameterKey();
            Spinner spinner = (Spinner) adapter.getView(i, null, null).findViewById(R.id.spinner);
            TextView fieldTitle = (TextView) adapter.getView(i, null, null).findViewById(R.id.field_title);
            try {
                SpinnerObj spObj = (SpinnerObj) spinner.getSelectedItem();
                String paramValue = spObj.getValue();
                nameValuePairs.add(new BasicNameValuePair(paramKey, paramValue));
                Log.i("FoalScoreActivity","key: " + paramKey + " Val: " + paramValue);
                //temp.put(paramKey,paramValue);
            } catch(Exception e) {
                adapter.setError(i, "Please choose an option.");
                //spinner.requestFocus();
                //fieldTitle.requestFocus();
                adapter.notifyDataSetChanged();
                adapter.getView(i, null, null).findViewById(R.id.field_title).requestFocus();
                Toast.makeText(this, "Choose an option for " + fieldTitle.getText(), Toast.LENGTH_SHORT).show();
                isValid = false;
                break;
            }
        }
        if(isValid) {
            // Show a progress dialog, and kick off a background task to
            // perform the calculation.
            pd = ProgressDialog.show(this, "Calculating...", "Please wait...", true, true);
            css = new calculateSurvivalScore(nameValuePairs);
            css.execute((Void) null);
        }
    }
    /**
     * Represents an asynchronous task used to calculate
     * sepsis score.
     */
    public class calculateSurvivalScore extends AsyncTask<Void, Void, Boolean> {

        private String score;
        private String calculationId;
        private String errorMsg = "An unknown error occurred!";
        private String result;
        private List<NameValuePair> nameValuePairs;
        calculateSurvivalScore(List<NameValuePair> nameValuePairs) {
            this.nameValuePairs = nameValuePairs;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Set user id
            String token = "";
            String result_temp = "";
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FoalScoreActivity.this);
            String userId = preferences.getString("userId","");
            nameValuePairs.add(new BasicNameValuePair("userId", userId));
            nameValuePairs.add(new BasicNameValuePair("allowShare", allowShare));

            Collections.sort(nameValuePairs, new Comparator<NameValuePair>() {
                @Override
                public int compare(NameValuePair lhs, NameValuePair rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
            for (int i = 0; i<nameValuePairs.size();i++) {
                String temp = nameValuePairs.get(i).toString().substring(Utilities.equalsIndex(nameValuePairs.get(i).toString())+1);
                result_temp += temp;
                //Log.i("FoalScoreActivity", "Sorted List: "+ nameValuePairs.get(i) + temp);
            }
            token = Utilities.createHash(result_temp);
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
            HttpPost httppost = new HttpPost("http://FoalScore.org.ohio-state.edu/foalscore/server/survivalscores/add.json");

            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                String responseText = EntityUtils.toString(response.getEntity());
                Log.i("SurvivalScoreActivity","Response Text: " + responseText);
                JSONObject json = new JSONObject(responseText);
                String status = json.getString("status");
                Log.i("SurvivalScoreActivity","Status: " + status);
                if(status.equals("success")) {
                    score = json.getString("score");
                    calculationId = json.getString("calculationId");
                    result = json.getString("scoreResultResponse");
                    Log.i("LoginActivity","Score: " + score);
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
            css = null;
            pd.dismiss();

            if(success) {
                finish();
                //LoginActivity.this.startActivity(new Intent(LoginActivity.this, HomePageActivity.class));
                Intent intent = new Intent(FoalScoreActivity.this, ResultsActivity.class);
                intent.putExtra("result", result);
                intent.putExtra("score", score);
                intent.putExtra("calculationId", calculationId);
                intent.putExtra("scoreType", "survivalScore");
                startActivity(intent);
            } else {
                offlineSetUp();
            }
        }

        @Override
        protected void onCancelled() {
            css = null;
            pd.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
