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
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import edu.ohio_state.org.vetplus.foalscore.util.Foal;
import edu.ohio_state.org.vetplus.foalscore.util.Utilities;

/**
 * Created by Veena on 3/18/2015.
 */
public class AddFoalActivity extends Activity {

    private EditText foalNameView;
    private EditText foalBreedView;
    private EditText foalTemperatureView;
    private EditText foalAgeView;
    private EditText foalRespiratoryRateView;
    private EditText foalHeartRateView;
    private Spinner foalSexView;
    private Spinner foalDystociaView;
    private Spinner foalSurvivedView;
    private Button addFoalButton;
    private ProgressDialog pd;
    private AddFoalTask mAuthTask = null;
    private boolean isAdd;
    private String foalId;
    private boolean isRequestFromIntent = false;
    // flag for Internet connection status
    private Boolean isInternetPresent = false;
    String editFoalName = null;
    String editFoalBreed = null;
    String editFoalAge = null;
    String editFoalTemperature = null;
    String editFoalRespiratoryRate = null;
    String editFoalHeartRate = null;
    String editFoalDystocia = null;
    String editFoalSex = null;
    String editFoalSurvivedUntilDischarge = null;
    String date = null;
    private String allowShare;
    private TextView text;
    private CheckBox checkBox;

    // Connection detector class
    private ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_foal);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(face);

        allowShare = "";
        foalNameView = (EditText) findViewById(R.id.foalName);
        foalBreedView = (EditText) findViewById(R.id.foalBreed);
        foalTemperatureView = (EditText) findViewById(R.id.foalTemperature);
        foalAgeView = (EditText) findViewById(R.id.foalAge);
        foalRespiratoryRateView = (EditText) findViewById(R.id.foalRespiratoryRate);
        foalHeartRateView = (EditText) findViewById(R.id.foalHeartRate);
        foalSexView = (Spinner) findViewById(R.id.foalSex);
        foalDystociaView = (Spinner) findViewById(R.id.foalDystocia);
        foalSurvivedView = (Spinner) findViewById(R.id.foalSurvived);
        addFoalButton = (Button) findViewById(R.id.addFoal);
        Bundle extras = getIntent().getExtras();
        if (extras!= null) {
            // This can be when editing foal or when mapping a foal to a calculation
            String isAddTemp="";
            try {
                if(extras.containsKey("isAdd")) {
                    isAddTemp = extras.getString("isAdd");
                }
            } catch(Exception e) {
                isAddTemp = "";
            }
            if(isAddTemp.equals("0")) {
                // Edit foal
                //Log.i("EditFoals", "Hi");
                editFoalName = extras.getString("Name");
                editFoalAge = extras.getString("Age");
                editFoalBreed = extras.getString("Breed");
                editFoalDystocia = extras.getString("Dystocia");
                editFoalHeartRate = extras.getString("HeartRate");
                editFoalRespiratoryRate = extras.getString("RespiratoryRate");
                editFoalSex = extras.getString("Gender");
                editFoalTemperature = extras.getString("Temperature");
                foalId = extras.getString("ID");
                editFoalSurvivedUntilDischarge = extras.getString("survivedUntilDischarge");
                date = extras.getString("Date");
                isAdd = false;
                allowShare = extras.getString("allowShare");
            } else {
                // Add foal and return result for score activity
                isAdd = true;
                isRequestFromIntent = true;
            }
        } else {
            isAdd = true;
        }
        Log.i("AddFoalActivity","isAdd: "+isAdd);

        if(editFoalName!=null) {
            foalNameView.setText(editFoalName);
        }
        if (editFoalAge!=null) {
            foalAgeView.setText(editFoalAge);
        }
        if(editFoalBreed!=null) {
            foalBreedView.setText(editFoalBreed);
        }
        if (editFoalDystocia!=null) {
            int dys;
            //Log.i("EditFoalActivity","Dystocia: " +foalDystocia);
            if (editFoalDystocia.equalsIgnoreCase("Yes")) {
                dys = 0;
            } else {
                dys = 1;
            }
            foalDystociaView.setSelection(dys);
        }
        if (editFoalHeartRate != null) {
            foalHeartRateView.setText(editFoalHeartRate);
        }
        if (editFoalRespiratoryRate != null) {
            foalRespiratoryRateView.setText(editFoalRespiratoryRate);
        }
        if (editFoalTemperature != null) {
            foalTemperatureView.setText(editFoalTemperature);
        }
        if (editFoalSex != null) {
            int sex;
            if (editFoalSex.equalsIgnoreCase("Colt")) {
                sex = 0;
            } else {
                sex = 1;
            }
            foalSexView.setSelection(sex);
        }
        if (editFoalSurvivedUntilDischarge != null) {
            int survivedUntilDischarge;
            if (editFoalSurvivedUntilDischarge.equalsIgnoreCase("Yes")) {
                survivedUntilDischarge = 0;
            } else {
                survivedUntilDischarge = 1;
            }
            foalSurvivedView.setSelection(survivedUntilDischarge);
        }

        checkBox = (CheckBox) findViewById(R.id.checkBoxFoal);
        if (allowShare.equals("0")) {
            checkBox.setChecked(false);
        } else {
            checkBox.setChecked(true);
        }

        text = (TextView) findViewById(R.id.text);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(AddFoalActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
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

        addFoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd = new ConnectionDetector(getApplicationContext());
                // get Internet status
                isInternetPresent = cd.isConnectingToInternet();

                // check for Internet status
                if (isInternetPresent) {
                    validateForm();
                }
                else {
                    // Internet connection is not present
                    // Ask user to connect to Internet
                    AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(AddFoalActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
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

    public void validateForm() {
        if(mAuthTask != null) return;

        // Reset errors.
        foalNameView.setError(null);

        // Store values at the time of the login attempt.
        String foalName = foalNameView.getText().toString();
        String foalBreed = foalBreedView.getText().toString();
        String foalAge = foalAgeView.getText().toString();
        String foalTemperature = foalTemperatureView.getText().toString();
        String foalRespiratoryRate = foalRespiratoryRateView.getText().toString();
        String foalHeartRate = foalHeartRateView.getText().toString();
        String foalDystocia = foalDystociaView.getSelectedItem().toString();
        String foalSex = foalSexView.getSelectedItem().toString();
        String foalSurvived = foalSurvivedView.getSelectedItem().toString();

        boolean cancel = false;
        View focusView = null;

        if (checkBox.isChecked()) {
            allowShare = "1";
        }
        else {
            allowShare = "0";
        }

        // Check for a foal Name.
        if (TextUtils.isEmpty(foalName)) {
            foalNameView.setError(getString(R.string.error_field_required));
            focusView = foalNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt registration and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress dialog, and kick off a background task to
            // perform the user registration attempt.
            String pdString;
            if(isAdd) {
                pdString = "Adding Foal...";
            } else {
                pdString = "Editing Foal...";
            }
            pd = ProgressDialog.show(this, pdString , "Please wait...", true, true);
            mAuthTask = new AddFoalTask(foalId, foalName, foalBreed, foalAge, foalTemperature, foalRespiratoryRate, foalHeartRate, foalSex, foalDystocia, date, foalSurvived);
            mAuthTask.execute((Void) null);
        }
    }

    public class AddFoalTask extends AsyncTask<Void, Void, Boolean> {

        private final String foalName;
        private final String foalBreed;
        private final String foalTemperature;
        private final String foalAge;
        private final String foalRespiratoryRate;
        private final String foalHeartRate;
        private final String foalSex;
        private final String foalDystocia;
        private final String foalSurvivedUntilDischarge;
        private String foalId;
        private String foalAddedDate;
        private Foal foalObj;

        private String errorMsg = "An unknown error occurred! Please try again.";

        AddFoalTask(String foalId, String foalName, String foalBreed, String foalAge, String foalTemperature, String foalRespiratoryRate, String foalHeartRate, String foalSex, String foalDystocia, String date, String foalSurvivedUntilDischarge) {
            this.foalAge = foalAge;
            this.foalBreed = foalBreed;
            this.foalDystocia = foalDystocia;
            this.foalHeartRate = foalHeartRate;
            this.foalRespiratoryRate = foalRespiratoryRate;
            this.foalName = foalName;
            this.foalSex = foalSex;
            this.foalTemperature = foalTemperature;
            this.foalId = foalId;
            this.foalAddedDate = date;
            this.foalSurvivedUntilDischarge = foalSurvivedUntilDischarge;
            foalObj = new Foal(foalId, foalName, foalAge, foalSex, foalBreed, date, foalTemperature, foalRespiratoryRate, foalHeartRate, foalDystocia, allowShare, foalSurvivedUntilDischarge);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            String token = "";
            String result_temp = "";
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AddFoalActivity.this);
            String userId = preferences.getString("userId","");
            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            int timeoutConnection = 6000;
            int timeoutSocketConnection = 4000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocketConnection);
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            String url = "http://foalscore.org.ohio-state.edu/foalscore/server/foals/edit.json";
            if(isAdd) {
                url = "http://foalscore.org.ohio-state.edu/foalscore/server/foals/add.json";
            }
            HttpPost httppost = new HttpPost(url);

            try {
                nameValuePairs.add(new BasicNameValuePair("name", foalName));
                nameValuePairs.add(new BasicNameValuePair("ageMonths", foalAge));
                nameValuePairs.add(new BasicNameValuePair("gender", foalSex));
                nameValuePairs.add(new BasicNameValuePair("breed", foalBreed));
                nameValuePairs.add(new BasicNameValuePair("temperature",foalTemperature));
                nameValuePairs.add(new BasicNameValuePair("respiratoryRate",foalRespiratoryRate));
                nameValuePairs.add(new BasicNameValuePair("heartRate",foalHeartRate));
                nameValuePairs.add(new BasicNameValuePair("dystocia",foalDystocia));
                nameValuePairs.add(new BasicNameValuePair("survivedUntilHospitalDischarge",foalSurvivedUntilDischarge));
                nameValuePairs.add(new BasicNameValuePair("userId", userId));
                nameValuePairs.add(new BasicNameValuePair("allowShare", allowShare));
                Log.i("AddFoal", "allowShare: " +allowShare);
                if(!isAdd) {
                    nameValuePairs.add(new BasicNameValuePair("foalid",foalId));
                }
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
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                Log.i("AddFoalActivity", "About to post data..." + url);
                HttpResponse response = httpclient.execute(httppost);
                String responseText = EntityUtils.toString(response.getEntity());
                Log.i("AddFoalActivity","Response Text: " + responseText);
                JSONObject json = new JSONObject(responseText);
                String status = json.getString("status");
                Log.i("AddFoalActivity","Status: " + status);
                if(status.equals("success")) {
                    Log.i("AddFoalActivity","Foal added successfully...");
                    foalId = json.getString("foalid");
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
            pd.dismiss();

            if(success) {
                String toastText = "Foal Saved Successfully!";
                if(isAdd) {
                    toastText = "Foal Added Successfully!";
                }
                Toast.makeText(AddFoalActivity.this, toastText, Toast.LENGTH_SHORT).show();
                if(isRequestFromIntent) {
                    Intent i = new Intent();
                    i.putExtra("foalId", foalId);
                    setResult(RESULT_OK, i);
                    Log.i("AddFoalActivity", "Result ok.. About to finish.. Foalid: " + foalId);
                } else {
                    if(isAdd) {
                        AddFoalActivity.this.startActivity(new Intent(AddFoalActivity.this, FoalsActivity.class));
                    } else {
                        Intent i = new Intent(AddFoalActivity.this, FoalDetailsActivity.class);
                        i.putExtra("foalObj", foalObj);
                        AddFoalActivity.this.startActivity(i);
                    }
                }
                finish();
            } else {
                AlertDialog dialog = new AlertDialog.Builder( new ContextThemeWrapper(AddFoalActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
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
            mAuthTask = null;
            pd.dismiss();
        }
    }
}
