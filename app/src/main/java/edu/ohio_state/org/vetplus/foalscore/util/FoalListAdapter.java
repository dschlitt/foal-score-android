package edu.ohio_state.org.vetplus.foalscore.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.ohio_state.org.vetplus.foalscore.R;

/**
 * Created by vivek on 4/4/15.
 */
public class FoalListAdapter extends ArrayAdapter<Foal> {

    private final Context context;
    private final List<Foal> values;

    public FoalListAdapter(Context context, List<Foal> values) {
        super(context, R.layout.foal_list_item, values);

        this.context = context;
        this.values = values;
    }

    static class ViewsHolder {
        TextView foalNameView;
        TextView genderView;
        TextView dateView;
    }

    public int getCount() {
        return values.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        final ViewsHolder holder;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.foal_list_item, parent, false);
            TextView nameView = (TextView) rowView.findViewById(R.id.foal_name);
            TextView genderView = (TextView) rowView.findViewById(R.id.foal_gender);
            TextView dateView = (TextView) rowView.findViewById(R.id.date);

            holder = new ViewsHolder();
            holder.foalNameView = nameView;
            holder.genderView = genderView;
            holder.dateView = dateView;

            rowView.setTag(holder);
        } else {
            holder = (ViewsHolder) rowView.getTag();
        }

        holder.foalNameView.setText(values.get(position).getName());
        holder.genderView.setText(values.get(position).getGender());
        String dateString = values.get(position).getDateCreated();
        DateFormat format = new SimpleDateFormat("yyyy-MM-d H:m:s", Locale.ENGLISH);
        try {
            Date date = format.parse(dateString);
            //Log.i("FoalListAdapter", "Date: " + date.toString());
            holder.dateView.setText(DateFormat.getDateInstance().format(date));
        } catch(Exception e) {
            e.printStackTrace();
        }

        return rowView;
    }


}

