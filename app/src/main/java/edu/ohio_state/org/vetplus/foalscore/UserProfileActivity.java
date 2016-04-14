package edu.ohio_state.org.vetplus.foalscore;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Veena on 3/18/2015.
 */
public class UserProfileActivity extends Activity {
    private TextView userProfileEmail;
    private Button change;
    private Button logout;
    private TextView userProfileName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(font);

        userProfileEmail = (TextView) findViewById(R.id.userProfileEmail);
        userProfileName = (TextView) findViewById(R.id.userProfileName);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userEmail = preferences.getString("userEmail","");
        String userName = preferences.getString("userName","");
        userProfileName.setText(userName);

        userProfileEmail.setText(userEmail);

        change = (Button) findViewById(R.id.changePassword);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserProfileActivity.this.startActivity(new Intent(UserProfileActivity.this, ChangePasswordActivity.class));
            }
        });

        logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(UserProfileActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("userId", null);
                editor.putString("userName",null);
                editor.putString("userEmail", null);
                editor.apply();
                UserProfileActivity.this.startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
