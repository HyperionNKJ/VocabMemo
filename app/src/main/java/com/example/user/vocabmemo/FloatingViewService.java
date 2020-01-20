package com.example.user.vocabmemo;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FloatingViewService extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View mFloatingView;
    private View collapsedView;
    private View expandedView;
    private EditText englishInput;
    private EditText koreanInput;
    private RadioGroup vocabTypeRG;

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //getting the widget layout from xml using layout inflater
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                0,410, // Harded coded so it appears just above keyboard
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);


        //getting the collapsed and expanded view from the floating view
        collapsedView = mFloatingView.findViewById(R.id.layoutCollapsed);
        expandedView = mFloatingView.findViewById(R.id.layoutExpanded);

        //adding click listener to close button and expanded view
        mFloatingView.findViewById(R.id.closeButtonCollasped).setOnClickListener(this);
        mFloatingView.findViewById(R.id.addButton).setOnClickListener(this);
        mFloatingView.findViewById(R.id.addButton).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (vocabTypeRG.getCheckedRadioButtonId() != -1 && !englishInput.getText().toString().equals("") && !koreanInput.getText().toString().equals("")) {
                    addVocab();
                }
                stopSelf();
                startActivity(new Intent(FloatingViewService.this, NoteActivity.class));
                return true;
            }
        });
        englishInput = mFloatingView.findViewById(R.id.englishInput);
        koreanInput = mFloatingView.findViewById(R.id.koreanInput);
        vocabTypeRG = mFloatingView.findViewById(R.id.radioGroup);

        //adding an touchlistener to make drag movement of the floating widget
        mFloatingView.findViewById(R.id.relativeLayoutParent).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);
                        // cannot onTouchListener and onClickListener at the same time. So below act as onClickListener
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            } else {
                                collapsedView.setVisibility(View.VISIBLE);
                                expandedView.setVisibility(View.GONE);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private boolean isViewCollapsed() {
        return mFloatingView == null || mFloatingView.findViewById(R.id.layoutCollapsed).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeButtonCollasped:
                //closing the widget
                stopSelf();
                break;
            case R.id.addButton:
                if (vocabTypeRG.getCheckedRadioButtonId() == -1 || englishInput.getText().toString().equals("") || koreanInput.getText().toString().equals("")) {
                    Toast.makeText(this, "Invalid input.", Toast.LENGTH_SHORT).show();
                } else {
                    addVocab();
                }
                stopSelf();
                break;
        }
    }

    private void addVocab() {
        String english = englishInput.getText().toString();
        String korean = koreanInput.getText().toString();
        String inputType = null;
        FileOutputStream fos = null;

        try {
            switch (vocabTypeRG.getCheckedRadioButtonId()) {
                case R.id.verbRB: fos = openFileOutput("Verb List", MODE_APPEND); inputType = "VERB"; break;
                case R.id.adjectiveRB: fos = openFileOutput("Adjective List", MODE_APPEND); inputType = "ADJECTIVE"; break;
                case R.id.nounRB: fos = openFileOutput("Noun List", MODE_APPEND); inputType = "NOUN"; break;
                case R.id.otherRB: fos = openFileOutput("Other List", MODE_APPEND); inputType = "OTHER"; break;
            }
            fos.write((english + " = " + korean + "\n").getBytes());
            Toast.makeText(this, "Added to Vocab list!\n" + english + " = " + korean + "\nType: " + inputType, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}