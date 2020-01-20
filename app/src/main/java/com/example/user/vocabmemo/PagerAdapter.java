package com.example.user.vocabmemo;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

public class PagerAdapter extends android.support.v4.view.PagerAdapter{
    private Context context;
    private List<Page> pages;
    private LayoutInflater inflater;

    PagerAdapter(Context context, List<Page> pages) {
        this.context = context;
        this.pages = pages;
        inflater = ((Activity) context).getLayoutInflater();
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Page currentPage = pages.get(position);
        View view = inflater.inflate(currentPage.getLayoutResId(), container, false);
        RecyclerView mRecyclerView = view.findViewById(currentPage.getRvId());
        final RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(context, currentPage.getVocabList(), currentPage.getFilename());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(recyclerViewAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        final Button showHideButton = view.findViewById(currentPage.getButtonId());
        showHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showHideButton.getText().toString().equals("SHOW ALL")) {
                    showHideButton.setText("HIDE ALL");
                    recyclerViewAdapter.toggleAll(true);
                } else {
                    showHideButton.setText("SHOW ALL");
                    recyclerViewAdapter.toggleAll(false);
                }
            }
        });
        view.findViewById(currentPage.getNextButtonId()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerViewAdapter.toggleAfterLastPosition();
            }
        });
        view.findViewById(currentPage.getTitleId()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showHideButton.getVisibility() == View.INVISIBLE) {
                    showHideButton.setVisibility(View.VISIBLE);
                    recyclerViewAdapter.setCheckBoxVisibility(true);
                } else {
                    showHideButton.setVisibility(View.INVISIBLE);
                    recyclerViewAdapter.setCheckBoxVisibility(false);
                }
            }
        });

        view.setTag(position);
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}


