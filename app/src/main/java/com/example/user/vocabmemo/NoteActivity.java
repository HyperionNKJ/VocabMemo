package com.example.user.vocabmemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.viewpagerindicator.CirclePageIndicator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;

public class NoteActivity extends AppCompatActivity {
    private List<Page> pages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    startService(new Intent(NoteActivity.this, FloatingViewService.class));
                    finish();
                } else if (Settings.canDrawOverlays(getApplicationContext())) {
                    startService(new Intent(NoteActivity.this, FloatingViewService.class));
                    finish();
                }
            }
        });
        retrieveData();
        setViewPager();
    }

    private void retrieveData() {
        // instantiate 4 page objects
        pages.add(new Page("Verb List", R.layout.note_verb, R.id.rvVerb, R.id.buttonVerb, R.id.nextVerb, R.id.tv_verb));
        pages.add(new Page("Adjective List", R.layout.note_adjective, R.id.rvAdjective, R.id.buttonAdjective, R.id.nextAdjective, R.id.tv_adjective));
        pages.add(new Page("Noun List", R.layout.note_noun, R.id.rvNoun, R.id.buttonNoun, R.id.nextNoun, R.id.tv_noun));
        pages.add(new Page("Other List", R.layout.note_adverb_other, R.id.rvOther, R.id.buttonOther, R.id.nextOther, R.id.tv_other));

        // retrieve data
        for (Page p: pages) {
            FileInputStream fis = null;
            try {
                fis = openFileInput(p.getFilename());
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                String engKorPair;
                while ((engKorPair = br.readLine()) != null) {
                    p.appendVocabList(engKorPair);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setViewPager() {
        PagerAdapter adapter = new PagerAdapter(NoteActivity.this, pages);
        AutoScrollViewPager pager = findViewById(R.id.notes_pager);
        pager.setAdapter(adapter);

        CirclePageIndicator pageIndicator = findViewById(R.id.notes_page_indicator);
        pageIndicator.setViewPager(pager);
        pageIndicator.setFillColor(Color.parseColor("#FFFF3E3E")); //fill colour of the selected circle
        pageIndicator.setStrokeColor(Color.parseColor("#000000")); //stroke or the circle's border colour
        pageIndicator.setPageColor(Color.parseColor("#C0C0C0")); //default fill colour of the circle
        pageIndicator.setRadius(14);
        pageIndicator.setCurrentItem(0);
    }
}
