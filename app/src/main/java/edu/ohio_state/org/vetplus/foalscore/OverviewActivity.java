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
public class OverviewActivity extends Activity {
    private Button sepsis;
    private Button survival;
    private Button terminology;
    private Button refer;
    private Button overview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(font);

        sepsis = (Button) findViewById(R.id.sepsis);
        sepsis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OverviewActivity.this.startActivity(new Intent(OverviewActivity.this, SepsisScoreReferenceActivity.class));
            }
        });

        survival = (Button) findViewById(R.id.survival);
        survival.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OverviewActivity.this.startActivity(new Intent(OverviewActivity.this, SurvivalScoreReferencesActivity.class));
            }
        });

        terminology = (Button) findViewById(R.id.terminology);
        terminology.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OverviewActivity.this.startActivity(new Intent(OverviewActivity.this, TerminologyActivity.class));
            }
        });

        refer = (Button) findViewById(R.id.reference);
        refer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OverviewActivity.this.startActivity(new Intent(OverviewActivity.this, ReferencesActivity.class));
            }
        });

        overview = (Button) findViewById(R.id.ImportantInfo);
        overview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OverviewActivity.this.startActivity(new Intent(OverviewActivity.this, ImportantInformationActivity.class));
            }
        });

    }
    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
