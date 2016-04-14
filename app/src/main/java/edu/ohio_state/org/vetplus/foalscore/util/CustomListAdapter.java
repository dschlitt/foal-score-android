package edu.ohio_state.org.vetplus.foalscore.util;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import edu.ohio_state.org.vetplus.foalscore.R;

/**
 * Created by vivek on 3/30/15.
 */
public class CustomListAdapter extends ArrayAdapter<SepsisListItem> {
    private final Context context;
    private final SepsisListItem[] values;
    private int errorPosition;
    private String errorMsg;

    public CustomListAdapter(Context context, SepsisListItem[] values) {
        super(context, R.layout.sepsis_score_list_item, values);

        this.context = context;
        this.values = values;
        this.errorPosition = -1;
    }

    static class ViewsHolder {
        TextView fieldTitleView;
        Spinner spinnerView;
        TextView subscoreView;
    }

    public int getCount() { return values.length; }

    public void setError(int position, String errorMsg) {
        this.errorMsg = errorMsg;
        this.errorPosition = position;
    }

    public void resetError() {
        this.errorPosition = -1;
        this.errorMsg = "";
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        final ViewsHolder holder;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.sepsis_score_list_item, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.field_title);
            Spinner spinner = (Spinner) rowView.findViewById(R.id.spinner);
            TextView subscoreView = (TextView) rowView.findViewById(R.id.subscore);

            holder = new ViewsHolder();
            holder.fieldTitleView = textView;
            holder.spinnerView = spinner;
            holder.subscoreView = subscoreView;

            rowView.setTag(holder);
        } else {
            holder = (ViewsHolder) rowView.getTag();
        }
        SpinnerObj[] spinnerOptions = values[position].getSpinnerOptions();
        final SpinAdapter adapter = new SpinAdapter(this.context, android.R.layout.simple_spinner_item, spinnerOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerView.setAdapter(adapter);
        final int pos = position;
        holder.spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerObj item = adapter.getItem(position);
                holder.subscoreView.setText(item.getValue());
                values[pos].setSubscore(item.getValue());
                values[pos].setSpinnerSelectedPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        holder.fieldTitleView.setError(null);
        holder.fieldTitleView.setText(Html.fromHtml(values[position].getFieldTitle()));
        holder.subscoreView.setText(values[position].getSubscore());
        int spinnerPosition = values[position].getSpinnerSelectedPosition();
        if (spinnerPosition >= 0) {
            Log.i("ListAdapter", "Set Spinner Position for: " + holder.fieldTitleView.getText() + " - " + spinnerPosition);
            holder.spinnerView.setSelection(spinnerPosition);
        }
        if (position == errorPosition) {
            Log.i("ListAdapter", "Setting error: " + errorPosition + " : " + errorMsg);
            holder.fieldTitleView.setError(errorMsg);
            holder.fieldTitleView.requestFocus();
        }

        return rowView;
    }
}
