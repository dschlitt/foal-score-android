package edu.ohio_state.org.vetplus.foalscore.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by vivek on 3/30/15.
 */
public class SpinAdapter extends ArrayAdapter<SpinnerObj> {

    private Context context;
    private SpinnerObj[] values;
    private String errorMsg;

    public SpinAdapter(Context context, int textViewResourceId,
                       SpinnerObj[] values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    public int getCount(){
        return values.length;
    }

    public SpinnerObj getItem(int position){
        return values[position];
    }

    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //TextView label = new TextView(context);
        //TextView label = (TextView) View.inflate(context, android.R.layout.simple_spinner_item, null);
        final TextView label =
                (TextView) ((LayoutInflater)getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE)).inflate(
                        android.R.layout.simple_spinner_item,parent,false);
        //v.setTextColor(getResources().getColor(R.color.hint_foreground_material_light));
        label.setTextColor(Color.BLACK);
        Typeface face = Typeface.createFromAsset(this.context.getAssets(), "fonts/Roboto-Light.ttf");
        label.setTypeface(face);

        //label.setTextColor(Color.BLACK);
        // Then you can get the current item using the values array (SpinnerObj array) and the current position
        label.setText(Html.fromHtml(values[position].getLabel()));

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        final TextView label =
                (TextView) ((LayoutInflater)getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE)).inflate(
                        android.R.layout.simple_spinner_dropdown_item,parent,false);
        label.setTextColor(Color.BLACK);
        Typeface face = Typeface.createFromAsset(this.context.getAssets(), "fonts/Roboto-Light.ttf");
        label.setTypeface(face);
        label.setText(Html.fromHtml(values[position].getLabel()));
        return label;
    }
}
