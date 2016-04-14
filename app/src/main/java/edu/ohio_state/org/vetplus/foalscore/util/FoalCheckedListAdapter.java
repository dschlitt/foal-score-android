package edu.ohio_state.org.vetplus.foalscore.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.ohio_state.org.vetplus.foalscore.R;

/**
 * Created by Veena on 4/8/2015.
 */
public class FoalCheckedListAdapter extends ArrayAdapter<FoalChecked>{

    private final Context context;
    private List<FoalChecked> values;

    public FoalCheckedListAdapter(Context context, List<FoalChecked> values) {
        super(context, R.layout.email_foal_list, values);

        this.context = context;
        this.values = values;
    }

    static class ViewsHolder {
        TextView foalNameView;
        TextView genderView;
        TextView dateView;
        CheckBox checkBox;
    }

    public void setValues(List<FoalChecked> values) {
        this.values = values;
    }

    public List<FoalChecked> getValues() {
        return this.values;
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
            rowView = inflater.inflate(R.layout.email_foal_list, parent, false);
            TextView nameView = (TextView) rowView.findViewById(R.id.foal_name);
            TextView genderView = (TextView) rowView.findViewById(R.id.foal_gender);
            TextView dateView = (TextView) rowView.findViewById(R.id.date);
            CheckBox check = (CheckBox) rowView.findViewById(R.id.checkboxEmail);

            holder = new ViewsHolder();
            holder.foalNameView = nameView;
            holder.genderView = genderView;
            holder.checkBox = check;
            holder.dateView = dateView;

            rowView.setTag(holder);
        } else {
            holder = (ViewsHolder) rowView.getTag();
        }

        holder.foalNameView.setText(values.get(position).getName());
        holder.genderView.setText(values.get(position).getGender());
        String dateString = values.get(position).getDateCreated();

        if (values.get(position).getIsChecked().equals("1")) {
            holder.checkBox.setChecked(true);
        }
        else {
            holder.checkBox.setChecked(false);
        }

        DateFormat format = new SimpleDateFormat("yyyy-MM-d H:m:s", Locale.ENGLISH);
        try {
            Date date = format.parse(dateString);
            holder.dateView.setText(DateFormat.getDateInstance().format(date));
        } catch(Exception e) {
            e.printStackTrace();
        }

        return rowView;
    }


}
