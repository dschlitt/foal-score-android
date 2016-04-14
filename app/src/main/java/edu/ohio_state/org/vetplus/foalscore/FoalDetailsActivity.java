package edu.ohio_state.org.vetplus.foalscore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
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
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.ohio_state.org.vetplus.foalscore.util.Calculation;
import edu.ohio_state.org.vetplus.foalscore.util.CalculationsListAdapter;
import edu.ohio_state.org.vetplus.foalscore.util.ConnectionDetector;
import edu.ohio_state.org.vetplus.foalscore.util.Foal;
import edu.ohio_state.org.vetplus.foalscore.util.Utilities;


public class FoalDetailsActivity extends Activity {

    private TextView foalNameView;
    private TextView foalAgeView;
    private TextView foalGenderView;
    private TextView foalBreedView;
    private TextView foalTemperatureView;
    private TextView foalRespiratoryRateView;
    private TextView foalHeartRateView;
    private TextView foalDystociaView;
    private TextView foalSurvivedView;
    private ListView calculationsListView;
    private Button editFoalButton;
    private String id;
    private TextView text;
    private FoalDetailsActivityTask ref;
    private ProgressDialog pd;
    Foal foal = null;
    // flag for Internet connection status
    private Boolean isInternetPresent = false;

    // Connection detector class
    private ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foal_details);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(font);

        Intent i = getIntent();
        foal = (Foal) i.getSerializableExtra("foalObj");
        id = foal.getId();
        foalNameView = (TextView) findViewById(R.id.foal_name);
        foalAgeView = (TextView) findViewById(R.id.foal_age);
        foalGenderView = (TextView) findViewById(R.id.foal_gender);
        foalBreedView = (TextView) findViewById(R.id.foal_breed);
        foalTemperatureView = (TextView) findViewById(R.id.foal_temperature);
        foalRespiratoryRateView = (TextView) findViewById(R.id.foal_respiratory_rate);
        foalHeartRateView = (TextView) findViewById(R.id.foal_heart_rate);
        foalDystociaView = (TextView) findViewById(R.id.foal_dystocia);
        foalSurvivedView = (TextView) findViewById(R.id.foal_survived);
        text = (TextView) findViewById(R.id.calculations);
        calculationsListView = (ListView) findViewById(R.id.calculations_list);

        foalNameView.setText(foalNameView.getText() + " " + foal.getName());
        foalAgeView.setText(foalAgeView.getText() + " " + foal.getAge());
        foalBreedView.setText(foalBreedView.getText() + " " + foal.getBreed());
        foalGenderView.setText(foalGenderView.getText() + " " + foal.getGender());
        foalTemperatureView.setText(foalTemperatureView.getText() + " " + foal.getTemperature());
        foalRespiratoryRateView.setText(foalRespiratoryRateView.getText() + " " + foal.getRespiratoryRate());
        foalHeartRateView.setText(foalHeartRateView.getText() + " " + foal.getHeartRate());
        foalDystociaView.setText(foalDystociaView.getText() + " " + foal.getDystocia());
        foalSurvivedView.setText(foalSurvivedView.getText() + " " + foal.getSurvivedUntilDischarge());
        cd = new ConnectionDetector(getApplicationContext());
        // get Internet status
        isInternetPresent = cd.isConnectingToInternet();

        // check for Internet status
        if (isInternetPresent) {
            show();
        }
        else {
            // Internet connection is not present
            // Ask user to connect to Internet
            AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(FoalDetailsActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
                    .setTitle("No Active Internet Connection.")
                    .setMessage("Please connect to the Internet and try again.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            Intent homepage = new Intent(FoalDetailsActivity.this, HomePageActivity.class);
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
        //scoreList = (ListView) findViewById(R.id.scoreList);
        editFoalButton = (Button) findViewById(R.id.editFoal);
        editFoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent k = new Intent(getApplicationContext(), AddFoalActivity.class);
                //Log.i("FoalDetailsActivity", "Foal ID: " +foal.getId());
                k.putExtra("Name",foal.getName());
                k.putExtra("Age",foal.getAge());
                k.putExtra("Breed",foal.getBreed());
                k.putExtra("Gender",foal.getGender());
                k.putExtra("ID",foal.getId());
                k.putExtra("Temperature",foal.getTemperature());
                k.putExtra("RespiratoryRate",foal.getRespiratoryRate());
                k.putExtra("HeartRate",foal.getHeartRate());
                k.putExtra("Dystocia",foal.getDystocia());
                k.putExtra("Date", foal.getDateCreated());
                k.putExtra("isAdd","0");
                k.putExtra("allowShare",foal.getIsChecked());
                startActivity(k);
                //FoalDetailsActivity.this.startActivity(new Intent(FoalDetailsActivity.this, AddFoalActivity.class));
            }
        });

    }

    private void show() {
        ref = new FoalDetailsActivityTask();
        pd = ProgressDialog.show(FoalDetailsActivity.this, "Loading...", "Please wait...", true, true);
        ref.execute((Void) null);
        return;
    }

    public class FoalDetailsActivityTask extends AsyncTask<Void, Void, Boolean> {

        private String refText = "";
        private String errorMsg = "Server Error.";
        private List<Calculation> calculationList = new ArrayList<Calculation>();
        String result="";

        FoalDetailsActivityTask() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            String token = "";
            token = Utilities.createHash(id);
            nameValuePairs.add(new BasicNameValuePair("foalid", id));
            nameValuePairs.add(new BasicNameValuePair("token", token));
            Log.i("FoalDetails", "ID: "+id);HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            int timeoutConnection = 6000;
            int timeoutSocketConnection = 4000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocketConnection);
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            HttpPost httppost = new HttpPost("http://foalscore.org.ohio-state.edu/foalscore/server/foals/foalcalculations.json");
            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                Log.i("Foal Details", "About to post data...");
                HttpResponse response = httpclient.execute(httppost);
                String responseText = EntityUtils.toString(response.getEntity());
                Log.i("Foal Details", "Response Text: " + responseText);
                JSONObject json = new JSONObject(responseText);
                String status = json.getString("status");
                Log.i("Foal Details", "Status: " + status);
                if (status.equals("success")) {
                    JSONArray foal = json.getJSONArray("results");
                    for (int i=0; i<foal.length(); i++) {
                        JSONObject foalObject = foal.getJSONObject(i);
                        String scoreType = foalObject.getString("ScoreType");
                        String score = foalObject.getString("Score");
                        String resultString = foalObject.getString("ResultString");
                        String date = foalObject.getString("Date");
                        if (scoreType.equals("Sepsis Score")) {
                            result = "Sepsis Score: " +score;
                        }
                        else {
                            result = "Foal Survival Score: " +score;
                        }
                        /*result +="\n"+resultString+"\n";
                        result += "Calculated on: "+date;
                        result += "\n\n";*/
                        Calculation c = new Calculation(scoreType, result, resultString, date);
                        calculationList.add(c);
                    }
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
            if (success) {
                /*// Store the user email in shared preference
                Toast.makeText(ChangePasswordActivity.this, "Password Changed Successfully!", Toast.LENGTH_SHORT).show();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ChangePasswordActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                //editor.putString("userEmail", mEmail);
                //editor.apply();
                ChangePasswordActivity.this.startActivity(new Intent(ChangePasswordActivity.this, HomePageActivity.class));*/
                pd.dismiss();
                Log.i("FoalDetailsActivity","Result: "+result);
                if(!result.isEmpty())
                {
                    //text.setText("Calculations: "+"\n"+ result);
                    text.setVisibility(View.VISIBLE);
                    CalculationsListAdapter adapter = new CalculationsListAdapter(FoalDetailsActivity.this, calculationList);
                    calculationsListView.setAdapter(adapter);
                }

            } else {
                AlertDialog dialog = new AlertDialog.Builder( new ContextThemeWrapper(FoalDetailsActivity.this, R.style.Base_V7_Theme_AppCompat_Dialog))
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
    }

   @Override
    public void onBackPressed() {
       NavUtils.navigateUpFromSameTask(this);
    }
}
