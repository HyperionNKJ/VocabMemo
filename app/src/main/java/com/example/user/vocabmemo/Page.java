package com.example.user.vocabmemo;

import java.util.ArrayList;

public class Page {
    private String filename;
    private int layoutResId;
    private int rvId;
    private int buttonId;
    private int nextButtonId;
    private int titleId;
    private ArrayList<String> vocabList;

    public Page(String filename, int layoutResId, int rvId, int buttonId, int nextButtonId, int titleId) {
        this.filename = filename;
        this.layoutResId = layoutResId;
        this.rvId = rvId;
        this.buttonId = buttonId;
        this.nextButtonId = nextButtonId;
        this.titleId = titleId;
        this.vocabList = new ArrayList<>();
    }

    public void appendVocabList(String engKorPair) {
        vocabList.add(engKorPair);
    }

    public String getFilename() {
        return filename;
    }

    public int getLayoutResId() {
        return layoutResId;
    }

    public int getRvId() {
        return rvId;
    }

    public int getButtonId() {
        return buttonId;
    }

    public int getNextButtonId() {
        return nextButtonId;
    }

    public int getTitleId() {
        return titleId;
    }

    public ArrayList<String> getVocabList() {
        return vocabList;
    }
}
