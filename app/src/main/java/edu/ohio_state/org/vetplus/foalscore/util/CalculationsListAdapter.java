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
 * Created by vivek on 4/7/15.
 */
public class CalculationsListAdapter extends ArrayAdapter<Calculation>{
    private final Context context;
    private final List<Calculation> values;

    public CalculationsListAdapter(Context context, List<Calculation> values) {
        super(context, R.layout.calculation_list_item, values);

        this.context = context;
        this.values = values;
    }

    static class ViewsHolder {
        TextView scoreView;
        TextView dateView;
        TextView resultView;
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
            rowView = inflater.inflate(R.layout.calculation_list_item, parent, false);
            TextView scoreView = (TextView) rowView.findViewById(R.id.score);
            TextView dateView = (TextView) rowView.findViewById(R.id.date);
            TextView resultView = (TextView) rowView.findViewById(R.id.result);

            holder = new ViewsHolder();
            holder.scoreView = scoreView;
            holder.resultView = resultView;
            holder.dateView = dateView;

            rowView.setTag(holder);
        } else {
            holder = (ViewsHolder) rowView.getTag();
        }

        holder.scoreView.setText(values.get(position).getScore());
        holder.resultView.setText(values.get(position).getResult());
        String dateString = values.get(position).getDate();
        DateFormat format = new SimpleDateFormat("yyyy-MM-d H:m:s", Locale.ENGLISH);
        try {
            Date date = format.parse(dateString);
            holder.dateView.setText(DateFormat.getDateInstance().format(date));
        } catch(Exception e) {
            e.printStackTrace();
        }

        String scoreType = values.get(position).getScoreType();
        if(scoreType.equals("Sepsis Score")) {
            holder.scoreView.setTextColor(context.getResources().getColor(R.color.green_text));
            holder.resultView.setTextColor(context.getResources().getColor(R.color.green_text));
            holder.dateView.setTextColor(context.getResources().getColor(R.color.green_text));
        } else if(scoreType.equals("Survival Score")) {
            holder.scoreView.setTextColor(context.getResources().getColor(R.color.subscore));
            holder.resultView.setTextColor(context.getResources().getColor(R.color.subscore));
            holder.dateView.setTextColor(context.getResources().getColor(R.color.subscore));
        } else {
            holder.scoreView.setTextColor(context.getResources().getColor(R.color.black));
            holder.resultView.setTextColor(context.getResources().getColor(R.color.black));
            holder.dateView.setTextColor(context.getResources().getColor(R.color.black));
        }
        return rowView;
    }
}
