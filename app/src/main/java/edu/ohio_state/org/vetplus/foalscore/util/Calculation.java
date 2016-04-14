package edu.ohio_state.org.vetplus.foalscore.util;

/**
 * Created by vivek on 4/7/15.
 */
public class Calculation {
    private String scoreType;
    private String score;
    private String result;
    private String date;

    public Calculation(String scoreType, String score, String result, String date) {
        this.scoreType = scoreType;
        this.score = score;
        this.result = result;
        this.date = date;
    }

    public String getScoreType() {
        return scoreType;
    }

    public void setScoreType(String scoreType) {
        this.scoreType = scoreType;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
