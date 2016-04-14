package edu.ohio_state.org.vetplus.foalscore.util;

/**
 * Created by vivek on 3/30/15.
 */
public class SepsisListItem {
    private String fieldTitle;
    private SpinnerObj[] spinnerOptions;
    private String parameterKey;
    private String subscore;
    private int spinnerSelectedPosition;

    public SepsisListItem(String fieldTitle, SpinnerObj[] spinnerOptions, String parameterKey) {
        this.fieldTitle = fieldTitle;
        this.spinnerOptions = spinnerOptions;
        this.parameterKey = parameterKey;
        this.subscore = "0";
        this.spinnerSelectedPosition = -1;
    }

    public SpinnerObj[] getSpinnerOptions() {
        return spinnerOptions;
    }

    public void setSpinnerOptions(SpinnerObj[] spinnerOptions) {
        this.spinnerOptions = spinnerOptions;
    }

    public String getFieldTitle() {
        return fieldTitle;
    }

    public void setFieldTitle(String fieldTitle) {
        this.fieldTitle = fieldTitle;
    }

    public String getParameterKey() {
        return parameterKey;
    }

    public void setParameterKey(String parameterKey) {
        this.parameterKey = parameterKey;
    }

    public String getSubscore() {
        return subscore;
    }

    public void setSubscore(String subscore) {
        this.subscore = subscore;
    }

    public int getSpinnerSelectedPosition() {
        return spinnerSelectedPosition;
    }

    public void setSpinnerSelectedPosition(int spinnerSelectedPosition) {
        this.spinnerSelectedPosition = spinnerSelectedPosition;
    }
}
