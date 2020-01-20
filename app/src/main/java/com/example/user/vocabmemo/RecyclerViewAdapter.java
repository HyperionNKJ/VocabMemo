package com.example.user.vocabmemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import static android.content.Context.MODE_PRIVATE;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<String> vocabList;
    private ArrayList<Boolean> visibilityArray;
    private String filename;
    private int lastPosition;
    private boolean checkBoxVisibility;

    private static final int DELETE = 0;
    private static final int EDIT = 1;

    public RecyclerViewAdapter(Context mContext, ArrayList<String> vocabList, String filename) {
        this.mContext = mContext;
        this.vocabList = vocabList;
        this.filename = filename;
        lastPosition = -1;
        this.checkBoxVisibility = true;
        visibilityArray = new ArrayList<>();
        for (int i = 0; i < vocabList.size(); i++) {
            visibilityArray.add(Boolean.TRUE);
        }
    }

    @NonNull
    @Override
    // Reponsible for inflating the view (Not important to understand. Same for any recyclerView adapter)
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_entry, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    //Display the data at the specified position. This method is used to update the contents of the itemView.
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        String engKorPair = vocabList.get(position);
        final String[] output = engKorPair.split(" = ");
        holder.english.setText(output[0]);
        if (checkBoxVisibility) {
            holder.visibility.setVisibility(View.VISIBLE);
        } else {
            holder.visibility.setVisibility(View.INVISIBLE);
        }
        int backupLastPos = lastPosition;
        if (visibilityArray.get(position)) {
            holder.visibility.setChecked(true);
            holder.korean.setText(output[1]);
        } else {
            holder.visibility.setChecked(false);
            holder.korean.setText("");
        }
        lastPosition = backupLastPos;
        holder.visibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int h_position = holder.getAdapterPosition();
                if (buttonView.isChecked()) {
                    holder.korean.setText(output[1]);
                    visibilityArray.set(h_position, true);
                } else {
                    holder.korean.setText("");
                    visibilityArray.set(h_position, false);
                }
                if (h_position == visibilityArray.size() - 1) {
                    lastPosition = -1;
                } else {
                    lastPosition = h_position;
                }
            }
        });
        holder.english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(output[0] + " = " + output[1] + "\n\nWhat do you want to do with it?")
                        .setCancelable(true)
                        .setNeutralButton("Nothing", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                                builder1.setMessage(output[0] + " = " + output[1] + " will be deleted.")
                                        .setCancelable(true)
                                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                performAction(holder.getAdapterPosition(), DELETE, null, null);
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                AlertDialog alert = builder1.create();
                                alert.setTitle("Confirm delete?");
                                alert.show();
                            }
                        })
                        .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                                final View dialogView = inflater.inflate(R.layout.edit_dialog, null);

                                builder.setCancelable(true)
                                        .setView(dialogView)
                                        .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                EditText et_englishInput = dialogView.findViewById(R.id.ed_englishInput);
                                                EditText et_koreanInput = dialogView.findViewById(R.id.ed_koreanInput);
                                                String englishInput = et_englishInput.getText().toString();
                                                String koreanInput = et_koreanInput.getText().toString();

                                                if (englishInput.equals("") || koreanInput.equals("")) {
                                                    Toast.makeText(mContext, "Invalid input. Entry not edited", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    performAction(holder.getAdapterPosition(), EDIT, englishInput, koreanInput);
                                                }
                                                dialog.dismiss();
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                builder.create().show();
                            }
                        });
                builder.create().show();
            }
        });
    }

    // action can be either edit or delete
    private void performAction(int position, int action, String englishInput, String koreanInput) {
        String entry = vocabList.get(position);

        if (action == DELETE) {
            vocabList.remove(position);
            visibilityArray.remove(position);
            this.notifyItemRemoved(position);
        } else if (action == EDIT) {
            vocabList.set(position, englishInput + " = " + koreanInput);
            this.notifyDataSetChanged();
        }

        // edit or delete from file
        StringBuilder sb = new StringBuilder();
        FileInputStream fis;
        FileOutputStream fos = null;

        try {
            fis = mContext.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String engKorPair;
            while ((engKorPair = br.readLine()) != null) {
                if (engKorPair.equals(entry)) {
                    if (action == DELETE) {
                        continue;
                    } else if (action == EDIT) {
                        sb.append(englishInput).append(" = ").append(koreanInput).append("\n");
                        continue;
                    }
                }
                sb.append(engKorPair).append("\n");
            }
            fos = mContext.openFileOutput(filename, MODE_PRIVATE);
            fos.write(sb.toString().getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
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

    public void toggleAll(boolean toggleTo) {
        Collections.fill(visibilityArray, toggleTo);
        notifyDataSetChanged();
        lastPosition = -1;
    }

    public void toggleAfterLastPosition() {
        if (visibilityArray.isEmpty()) {
            Toast.makeText(mContext, filename + " is empty!", Toast.LENGTH_LONG).show();
            return;
        }
        if (visibilityArray.get(lastPosition + 1)) {
            visibilityArray.set(lastPosition + 1, false);
        } else {
            visibilityArray.set(lastPosition + 1, true);
        }
        notifyItemChanged(lastPosition + 1);
        if (lastPosition + 1 == vocabList.size() - 1) {
            lastPosition = -1;
        } else {
            lastPosition++;
        }
    }

    public void setCheckBoxVisibility(boolean checkBoxVisibility) {
        this.checkBoxVisibility = checkBoxVisibility;
        this.notifyDataSetChanged();
    }

    @Override
    // Tells adapter how many entries are in list. If 0 then recyclerview will be empty
    public int getItemCount() {
        return vocabList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder { // holds each entry in memory
        TextView english;
        TextView korean;
        CheckBox visibility;
        LinearLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            english = itemView.findViewById(R.id.english);
            korean = itemView.findViewById(R.id.korean);
            visibility = itemView.findViewById(R.id.cb_visibility);
            parentLayout = itemView.findViewById(R.id.parentLayout);
        }
    }
}