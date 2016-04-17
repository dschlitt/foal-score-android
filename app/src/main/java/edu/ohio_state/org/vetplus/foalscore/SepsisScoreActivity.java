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
 * Created by Veena on 3/18/2015.
 */
public class SepsisScoreActivity extends Activity {

    private ListView sepsisListView;
    private Button calculateSepsis;
    private ProgressDialog pd;
    private calculateSepsisScore css = null;
    // flag for Internet connection status
    private Boolean isInternetPresent = false;
    private CheckBox sepsis;
    private String allowShare = null;
    // Connection detector class
    private ConnectionDetector cd;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sepsis_score);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(font);

        sepsisListView = (ListView) findViewById(R.id.sepsis_list);

        SepsisListItem neutrophil = new SepsisListItem("Neutrophil Count", new SpinnerObj[] {
                new SpinnerObj("&lt; 2.0 x 10 <sup><small>9</small></sup>/l", "3"),
                new SpinnerObj("2.0 - 4.0 x 10 <sup><small>9</small></sup>/l", "2"),
                new SpinnerObj("8.0 - 12.0 x 10 <sup><small>9</small></sup>/l", "1"),
                new SpinnerObj("&gt; 12.0 x 10 <sup><small>9</small></sup>/l", "2"),
                new SpinnerObj("Normal", "0"),
                new SpinnerObj("Not Available", "0")
                }, "CBC_neutrophilCount");

        SepsisListItem bandNeutrophil = new SepsisListItem("Band Neutrophil Count", new SpinnerObj[] {
                new SpinnerObj("&gt; 0.2 x 10 <sup><small>9</small></sup>/l", "3"),
                new SpinnerObj("0.05 - 0.20 x 10 <sup><small>9</small></sup>/l", "2"),
                new SpinnerObj("&lt; 0.05 x 10 <sup><small>9</small></sup>/l", "0"),
                new SpinnerObj("Not Available", "0")
        }, "CBC_bandNeutrophilCount");

        SepsisListItem granulation = new SepsisListItem("Doehle bodies, toxic changes, granulation, or vacuolization in neutrophils", new SpinnerObj[] {
                new SpinnerObj("Marked", "4"),
                new SpinnerObj("Moderate", "3"),
                new SpinnerObj("Slight", "2"),
                new SpinnerObj("None", "0"),
        }, "CBC_otherNeutrophilCount");

        SepsisListItem fibrinogen = new SepsisListItem("Fibrinogen (mg/dL)", new SpinnerObj[] {
                new SpinnerObj("&gt; 600", "2"),
                new SpinnerObj("410 - 600", "1"),
                new SpinnerObj("&lt; 400", "0"),
                new SpinnerObj("Not Available", "0")
        }, "CBC_fibrinogen");

        SepsisListItem hypoglycemia = new SepsisListItem("Hypoglycemia (mg/dL)", new SpinnerObj[] {
                new SpinnerObj("&lt; 49", "2"),
                new SpinnerObj("49 - 79", "1"),
                new SpinnerObj("&gt; 79", "0"),
                new SpinnerObj("Not Available", "0")
        }, "otherLabData_hypoglycemia");

        SepsisListItem lgG = new SepsisListItem("lgG (mg/dL)", new SpinnerObj[] {
                new SpinnerObj("&lt; 200", "4"),
                new SpinnerObj("200 - 400", "3"),
                new SpinnerObj("400 - 800", "2"),
                new SpinnerObj("&gt; 800", "0"),
                new SpinnerObj("Not Available", "0")
        }, "otherLabData_igG");

        SepsisListItem aterial = new SepsisListItem("Aterial oxygen", new SpinnerObj[] {
                new SpinnerObj("&lt; 40 Torr", "3"),
                new SpinnerObj("40 - 50 Torr", "2"),
                new SpinnerObj("51 - 70 Torr", "1"),
                new SpinnerObj("&gt; 70 Torr", "0"),
                new SpinnerObj("Not Available", "0")
        }, "otherLabData_aterialOxygen");

        SepsisListItem metabolicAcidosis = new SepsisListItem("Metabolic acidosis", new SpinnerObj[] {
                new SpinnerObj("Yes", "1"),
                new SpinnerObj("No", "0"),
                new SpinnerObj("Not Available", "0")
        }, "otherLabData_metabolicAcidosis");

        SepsisListItem scleral = new SepsisListItem("Petechiation or scleral injection, no secondary to eye disease or trauma", new SpinnerObj[] {
                new SpinnerObj("Marked", "3"),
                new SpinnerObj("Moderate", "2"),
                new SpinnerObj("Mild", "1"),
                new SpinnerObj("None", "0")
        }, "clinicExam_injection");

        SepsisListItem fever = new SepsisListItem("Fever", new SpinnerObj[] {
                new SpinnerObj("&gt; 102&#176; F or &gt; 38.8&#176; C", "2"),
                new SpinnerObj("&lt; 100&#176; F or &lt; 37.7&#176; C", "1"),
                new SpinnerObj("Normal", "0")
        }, "clinicExam_fever");

        SepsisListItem hypotonia = new SepsisListItem("Hypotonia, coma depression, convulsions", new SpinnerObj[] {
                new SpinnerObj("Marked", "2"),
                new SpinnerObj("Mild", "1"),
                new SpinnerObj("Normal", "0"),
                new SpinnerObj("Not Available", "0")
        }, "clinicExam_hypotoniaComa");

        SepsisListItem diarrhea = new SepsisListItem("Anterior uveitis, diarrhea, respiratory distress, swollen joints, open wounds", new SpinnerObj[] {
                new SpinnerObj("Yes", "3"),
                new SpinnerObj("No", "0"),
                new SpinnerObj("Not Available", "0")
        }, "clinicExam_anteriorUveitisDiarrhea");

        SepsisListItem dystocia = new SepsisListItem("Placentitis, vulvar discharge prior to delivery, dystocia, long transport of mare, mare sick, foal induced", new SpinnerObj[] {
                new SpinnerObj("Yes", "3"),
                new SpinnerObj("No", "0"),
                new SpinnerObj("Not Available", "0")
        }, "histData_placentitisVulvar");

        SepsisListItem prematurity = new SepsisListItem("Prematurity", new SpinnerObj[] {
                new SpinnerObj("&lt; 300 days", "3"),
                new SpinnerObj("300 - 310 days", "2"),
                new SpinnerObj("311 - 330 days", "1"),
                new SpinnerObj("&gt; 330 days", "0"),
                new SpinnerObj("Not Available", "0")
        }, "histData_prematurity");


        SepsisListItem[] items = new SepsisListItem[] {neutrophil, bandNeutrophil, granulation, fibrinogen, hypoglycemia, lgG, aterial, metabolicAcidosis, scleral, fever, hypotonia, diarrhea, dystocia, prematurity};
        CustomListAdapter listAdapter = new CustomListAdapter(this, items);
        View v = getLayoutInflater().inflate(R.layout.sepsis_footer_layout, null);
        sepsisListView.addFooterView(v);
        sepsisListView.setAdapter(listAdapter);
        sepsis = (CheckBox) v.findViewById(R.id.checkBoxSepsis);
        text = (TextView) v.findViewById(R.id.text);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(SepsisScoreActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
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
                final int alertTitle = getResources().getIdentifier( "alertTitle", "id", "android" );
                TextView alertTextView = (TextView) dialog.findViewById(alertTitle);
                Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
                textView.setTypeface(face);
                alertTextView.setTypeface(face);
            }
        });



        calculateSepsis = (Button) v.findViewById(R.id.calculateSepsis);
        calculateSepsis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cd = new ConnectionDetector(getApplicationContext());
                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();

                // check for Internet status
                if (isInternetPresent) {
                    validate();
                }
                else {
                    // Internet connection is not present
                    offlineSetUp();
                }
            }
        });
    }

    public void offlineSetUp(){
      // Validate if all the spinners have values
        CustomListAdapter adapter = (CustomListAdapter)((HeaderViewListAdapter)sepsisListView.getAdapter()).getWrappedAdapter();
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
                if(spObj.getLabel().equalsIgnoreCase("Not Available")) {
                    isNotAvailablePresent = true;
                }
                Log.i("SepsisScoreActivity", "key: " + paramKey + " Val: " + paramValue);
            } catch(Exception e) {
                Log.i("SepsisScoreActivity", "Exception: " + e.getMessage());
                Log.i("SepsisScoreActivity", "Error at: " + i + " - " + fieldTitle.getText());
                adapter.setError(i, "Please choose an option.");
                adapter.notifyDataSetChanged();
                adapter.getView(i, null, null).findViewById(R.id.field_title).requestFocus();
                Toast.makeText(this, "Choose an option for " + fieldTitle.getText(), Toast.LENGTH_SHORT).show();
                isValid = false;
                break;
            }


        }
        if(isValid) {
            // Show an alert if at-least one field has "Not Available" option
            if(isNotAvailablePresent) {
                AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(SepsisScoreActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
                        .setTitle("Warning")
                        .setMessage("You have selected the option \"Not Available\" for one or more questions. This may affect the accuracy of the results.")
                        .setPositiveButton(R.string.continue_text, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ArrayList<String> result =  offlineCalculation(nameValuePairs);
                                Log.i("SepsisScoreActivity","This is the offline result: " + result);
                            }
                        })
                        .setNegativeButton(R.string.back_text, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {

                ArrayList<String> result = offlineCalculation(nameValuePairs);
                Log.i("SepsisScoreActivity","This is the offline result: " + result);
                Intent intent = new Intent(SepsisScoreActivity.this, ResultsActivity.class);
                intent.putExtra("result",result.get(0));
                intent.putExtra("score", result.get(1));
                intent.putExtra("calculationId", (String)null);
                intent.putExtra("scoreType", "sepsisScore");
                startActivity(intent);
            }
        }
        return;

    }

    public ArrayList<String> offlineCalculation(List<NameValuePair> nameValuePairs) {
        ArrayList<String> totalResult = new ArrayList<String>();
        String prediction = "";
        int totalScore = 0;

        for(NameValuePair nvPair :nameValuePairs) {

                String value = nvPair.getValue();
                int valueInteger = Integer.parseInt(value);
                totalScore += valueInteger;
        }
        if(totalScore < 12) {
            prediction = "The foal is predicted to not have Sepsis with 88% accuracy.";
        }else {
            prediction = "The foal is predicted to have Sepsis with 93% accuracy.";
        }

        totalResult.add(0,prediction);
        totalResult.add(1,Integer.toString(totalScore));
        return totalResult;
    }

    public void validate() {
        if(css != null) return;

        if (sepsis.isChecked()) {
            allowShare = "1";
        }
        else {
            allowShare = "0";
        }
        // Validate if all the spinners have values
        CustomListAdapter adapter = (CustomListAdapter)((HeaderViewListAdapter)sepsisListView.getAdapter()).getWrappedAdapter();
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
                if(spObj.getLabel().equalsIgnoreCase("Not Available")) {
                    isNotAvailablePresent = true;
                }
                Log.i("SepsisScoreActivity", "key: " + paramKey + " Val: " + paramValue);
            } catch(Exception e) {
                Log.i("SepsisScoreActivity", "Exception: " + e.getMessage());
                Log.i("SepsisScoreActivity", "Error at: " + i + " - " + fieldTitle.getText());
                adapter.setError(i, "Please choose an option.");
                adapter.notifyDataSetChanged();
                adapter.getView(i, null, null).findViewById(R.id.field_title).requestFocus();
                Toast.makeText(this, "Choose an option for " + fieldTitle.getText(), Toast.LENGTH_SHORT).show();
                isValid = false;
                break;
            }
        }
        if(isValid) {
            // Show an alert if at-least one field has "Not Available" option
            if(isNotAvailablePresent) {
                AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(SepsisScoreActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
                        .setTitle("Warning")
                        .setMessage("You have selected the option \"Not Available\" for one or more questions. This may affect the accuracy of the results.")
                        .setPositiveButton(R.string.continue_text, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                calculate(nameValuePairs);
                            }
                        })
                        .setNegativeButton(R.string.back_text, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                calculate(nameValuePairs);
            }
        }
        return;
    }

    public void calculate(List<NameValuePair> nameValuePairs) {
        // Show a progress dialog, and kick off a background task to
        // perform the calculation.
        pd = ProgressDialog.show(this, "Calculating...", "Please wait...", true, true);
        css = new calculateSepsisScore(nameValuePairs);
        css.execute((Void) null);
    }
    /**
     * Represents an asynchronous task used to calculate
     * sepsis score.
     */
    public class calculateSepsisScore extends AsyncTask<Void, Void, Boolean> {

        private String score;
        private String calculationId;
        private String errorMsg = "An unknown error occurred!";
        private String result;
        private List<NameValuePair> nameValuePairs;
        calculateSepsisScore(List<NameValuePair> nameValuePairs) {
            this.nameValuePairs = nameValuePairs;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Set user id
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SepsisScoreActivity.this);
            String token = "";
            String result_temp = "";
            String userId = preferences.getString("userId","");
            token = Utilities.createHash(allowShare + userId);
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
            }
            token = Utilities.createHash(result_temp);
            nameValuePairs.add(new BasicNameValuePair("token", token));
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
            HttpPost httppost = new HttpPost("http://FoalScore.org.ohio-state.edu/foalscore/server/sepsisscores/add.json");

            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                String responseText = EntityUtils.toString(response.getEntity());
                Log.i("SepsisScoreActivity","Response Text: " + responseText);
                JSONObject json = new JSONObject(responseText);
                String status = json.getString("status");
                Log.i("SepsisScoreActivity","Status: " + status);
                if(status.equals("success")) {
                    score = json.getString("score");
                    calculationId = json.getString("calculationId");
                    result = json.getString("scoreResultResponse");
                    Log.i("SepsisScoreActivity","Score: " + score);
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
                Log.e("SepsisScoreActivity", "Exception Caught...");
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
                Intent intent = new Intent(SepsisScoreActivity.this, ResultsActivity.class);
                intent.putExtra("result", result);
                intent.putExtra("score", score);
                intent.putExtra("calculationId", calculationId);
                intent.putExtra("scoreType", "sepsisScore");
                startActivity(intent);
            } else {
                finish();
                //LoginActivity.this.startActivity(new Intent(LoginActivity.this, HomePageActivity.class));
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
