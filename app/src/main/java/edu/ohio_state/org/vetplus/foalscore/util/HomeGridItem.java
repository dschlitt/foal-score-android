package edu.ohio_state.org.vetplus.foalscore.util;

import android.content.Context;

/**
 * Created by vivek on 3/30/15.
 */
public class HomeGridItem {
    private String title;
    private Class linkActivityClass;
    private int buttonBackground;
    private Context context;

    public HomeGridItem(String title, Class linkActivity, int buttonBackground, Context context) {
        this.title = title;
        this.linkActivityClass = linkActivity;
        this.buttonBackground = buttonBackground;
        this.context = context;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Class getLinkActivityClass() {
        return linkActivityClass;
    }

    public void setLinkActivityClass(Class linkActivity) {
        this.linkActivityClass = linkActivity;
    }

    public int getButtonBackground() {
        return buttonBackground;
    }

    public void setButtonBackground(int buttonBackground) {
        this.buttonBackground = buttonBackground;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
