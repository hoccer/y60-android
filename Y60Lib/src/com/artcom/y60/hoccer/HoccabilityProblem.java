package com.artcom.y60.hoccer;

public class HoccabilityProblem {

    private String mRecoverySuggestion;
    private String mProblem;

    public void setProblem(String pProblem) {
        mProblem = pProblem;
    }

    public void setRecoverySuggestion(String pRecoverySuggestion) {
        mRecoverySuggestion = pRecoverySuggestion;
    }

    public String getDescription() {
        return mProblem;
    }

    public String getRecoverySuggestion() {
        return mRecoverySuggestion;
    }

}
