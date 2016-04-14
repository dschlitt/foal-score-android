package edu.ohio_state.org.vetplus.foalscore.util;

/**
 * Created by vivek on 3/30/15.
 */
public class SpinnerObj {
    private String label;
    private String value;

    public SpinnerObj(String label, String value) {
        this.label = label;
        this.value = value;
    }
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
