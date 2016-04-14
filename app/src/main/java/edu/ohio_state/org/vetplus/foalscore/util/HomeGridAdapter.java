package edu.ohio_state.org.vetplus.foalscore.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import edu.ohio_state.org.vetplus.foalscore.R;

/**
 * Created by vivek on 4/5/15.
 */
public class HomeGridAdapter extends BaseAdapter {
    private Context context;
    private final HomeGridItem[] values;

    public HomeGridAdapter(Context context, HomeGridItem[] values) {
        this.context = context;
        this.values = values;
    }

    public class ViewsHolder {
        Button buttonView;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewsHolder holder;
        View gridView = convertView;

        if (gridView == null) {
            // get layout from homepage_grid_item.xml
            gridView = inflater.inflate(R.layout.homepage_grid_item, null);
            Button button = (Button) gridView.findViewById(R.id.homepage_button);
            holder = new ViewsHolder();
            holder.buttonView = button;
            gridView.setTag(holder);
        } else {
            holder = (ViewsHolder) gridView.getTag();
        }

        // Set background and text for button
        holder.buttonView.setText(values[position].getTitle());
        holder.buttonView.setBackgroundResource(values[position].getButtonBackground());
        holder.buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("GridAdapter", "Button clicked: " + values[position].getTitle());
                values[position].getContext().startActivity(new Intent(values[position].getContext(), values[position].getLinkActivityClass()));
            }
        });
        return gridView;
    }

    @Override
    public int getCount() {
        return values.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
