package com.developers.wajbaty.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.developers.wajbaty.R;

public class ProgressDialogFragment extends DialogFragment {

    private String title;
    private String message;

    public ProgressDialogFragment(){
        setCancelable(false);
    }



    @Override
    public void onStart() {
        super.onStart();
        requireDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view;

        if(getTitle() == null && getMessage() == null){
            view = inflater.inflate(R.layout.main_progress_dialog, container, false);
        }else if(getTitle() !=null && getMessage() == null){

            view = inflater.inflate(R.layout.main_progress_dialog_with_title, container, false);

            final TextView progressTitleTv = view.findViewById(R.id.progressTitleTv);
            progressTitleTv.setText(getTitle());

        }else{

            view = inflater.inflate(R.layout.main_progress_dialog_with_title_and_message, container, false);

            final TextView progressTitleTv = view.findViewById(R.id.progressTitleTv);
            progressTitleTv.setText(getTitle());

             final TextView progressMessageTv = view.findViewById(R.id.progressMessageTv);
            progressMessageTv.setText(getMessage());

        }

        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
