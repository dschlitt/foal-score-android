package edu.ohio_state.org.vetplus.foalscore;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Veena on 3/18/2015.
 */
public class MiscellaneousActivity extends Activity {
    private Button emailData;
    private Button refer;
    private Button feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miscellaneous);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(font);

        emailData = (Button) findViewById(R.id.emailData);
        emailData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MiscellaneousActivity.this.startActivity(new Intent(MiscellaneousActivity.this, EMailActivity.class));
            }
        });

        refer = (Button) findViewById(R.id.refer);
        refer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MiscellaneousActivity.this.startActivity(new Intent(MiscellaneousActivity.this, ReferAFriendActivity.class));
            }
        });

        refer = (Button) findViewById(R.id.refer);
        refer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MiscellaneousActivity.this.startActivity(new Intent(MiscellaneousActivity.this, ReferAFriendActivity.class));
            }
        });

        feedback = (Button) findViewById(R.id.feedback);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MiscellaneousActivity.this.startActivity(new Intent(MiscellaneousActivity.this, FeedbackActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
