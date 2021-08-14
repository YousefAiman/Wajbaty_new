package com.developers.wajbaty.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.developers.wajbaty.R;

public class ProgressDialogFragment extends DialogFragment {

    private String title;
    private String message;
    private boolean canBeDismissed;
    private ProgressDialogListener progressDialogListener;

    public ProgressDialogFragment() {
        setCancelable(false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    @Override
    public void onStart() {
        super.onStart();

        requireDialog().getWindow().setLayout(title != null || message != null ? ViewGroup.LayoutParams.MATCH_PARENT :
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view;

        if (getTitle() == null && getMessage() == null) {
            view = inflater.inflate(R.layout.main_progress_dialog, container, false);
        } else if (getTitle() != null && getMessage() == null) {

            view = inflater.inflate(R.layout.main_progress_dialog_with_title, container, false);

            final TextView progressTitleTv = view.findViewById(R.id.progressTitleTv);
            progressTitleTv.setText(getTitle());

        } else if (!canBeDismissed) {

            view = inflater.inflate(R.layout.main_progress_dialog_with_title_and_message, container, false);

            final TextView progressTitleTv = view.findViewById(R.id.progressTitleTv);
            progressTitleTv.setText(getTitle());

            final TextView progressMessageTv = view.findViewById(R.id.progressMessageTv);
            progressMessageTv.setText(getMessage());

        } else {

            view = inflater.inflate(R.layout.main_progress_dialog_with_title_and_message_and_cancel, container, false);

            final TextView progressTitleTv = view.findViewById(R.id.progressTitleTv);
            progressTitleTv.setText(getTitle());

            final TextView progressMessageTv = view.findViewById(R.id.progressMessageTv);
            progressMessageTv.setText(getMessage());

            final ImageView progressCloseIv = view.findViewById(R.id.progressCloseIv);
            progressCloseIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (progressDialogListener != null) {
                        dismiss();
                        progressDialogListener.onProgressDismissed();
                    }
                }
            });

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

    public ProgressDialogListener getProgressDialogListener() {
        return progressDialogListener;
    }

    public void setProgressDialogListener(ProgressDialogListener progressDialogListener) {
        this.progressDialogListener = progressDialogListener;
    }

    public boolean isCanBeDismissed() {
        return canBeDismissed;
    }

    public void setCanBeDismissed(boolean canBeDismissed) {
        this.canBeDismissed = canBeDismissed;
    }

    public interface ProgressDialogListener {
        void onProgressDismissed();
    }

}
