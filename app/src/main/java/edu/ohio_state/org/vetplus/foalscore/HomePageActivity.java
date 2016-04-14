package edu.ohio_state.org.vetplus.foalscore;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;

import edu.ohio_state.org.vetplus.foalscore.util.HomeGridAdapter;
import edu.ohio_state.org.vetplus.foalscore.util.HomeGridItem;

/**
 * Created by Veena on 3/18/2015.
 */
public class HomePageActivity extends Activity {

    private GridView gridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleView = (TextView) findViewById(titleId);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        titleView.setTypeface(face);

        gridView = (GridView) findViewById(R.id.gridView);
        HomeGridItem survivalScore = new HomeGridItem(
                "Calculate Foal Survival Score",
                FoalScoreActivity.class,
                R.drawable.round_button_aqua_green,
                this
                );
        HomeGridItem sepsisScore = new HomeGridItem(
                "Calculate Sepsis Score",
                SepsisScoreActivity.class,
                R.drawable.round_button_light_blue,
                this
                );
        HomeGridItem foals = new HomeGridItem(
                "Foals",
                FoalsActivity.class,
                R.drawable.round_button_light_orange,
                this
                );
        HomeGridItem userProfile = new HomeGridItem(
                "User Profile",
                UserProfileActivity.class,
                R.drawable.round_button_purple,
                this
                );
        HomeGridItem overview = new HomeGridItem(
                "Overview",
                OverviewActivity.class,
                R.drawable.round_button_dark_blue,
                this
                );
        HomeGridItem miscellaneous = new HomeGridItem(
                "Miscellaneous",
                MiscellaneousActivity.class,
                R.drawable.round_button_brown,
                this
                );
        HomeGridItem[] items = new HomeGridItem[] {survivalScore, sepsisScore, foals, userProfile, overview, miscellaneous};
        HomeGridAdapter adapter = new HomeGridAdapter(this, items);
        gridView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
