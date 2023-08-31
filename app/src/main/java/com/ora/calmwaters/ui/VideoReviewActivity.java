package com.ora.calmwaters.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ora.calmwaters.LogHelper;
import com.ora.calmwaters.OverlayService;

import calmwaters.R;

public class VideoReviewActivity extends AppCompatActivity {

    public static final String VIDEO_INTENT_KEY = "videoPath";

    private VideoView videoView;
    private MediaController mController;

    private Uri uri;
    private String url;

    public static final int RESULT_REJECT = 3;

    public VideoReviewActivity() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_review);

        OverlayService.instance.enableClicks(true);

        videoView = findViewById(R.id.videoView);
        mController = new MediaController(this);

        uri = getIntent().getData();
        url = uri.toString();

        if (getIntent().getParcelableExtra(VIDEO_INTENT_KEY) != null) {
            LogHelper.d("VideoReviewActivity - onCreate(): videoIntent contains data. \n" +
                    "URI: " + getIntent().getData() + "\n" +
                    "Parceable Extra is: " + getIntent().getParcelableExtra(VIDEO_INTENT_KEY)
            );
            videoView.setVideoURI(getIntent().getParcelableExtra(VIDEO_INTENT_KEY));
            //grab the MediaController in the layout

            mController.setAnchorView(videoView);
            videoView.setMediaController(mController);

        } else {
            LogHelper.e("VideoReviewActivity - onCreate(): videoIntent is empty.");
            new MaterialAlertDialogBuilder(this)
                    .setMessage("Could not find the video that was last recorded.")
                    .setTitle("Error")
                    .setPositiveButton("", (dialog, which) -> {
                        //return back to the PatientInfoFragment
                    })
                    .create()
                    .show();
        }

        Button acceptButton = findViewById(R.id.videoAcceptButton);
        Button rejectButton = findViewById(R.id.videoRejectButton);

        acceptButton.setOnClickListener(v -> {
            LogHelper.d("ViewReviewActivity - acceptButton " + url);
            setResult(Activity.RESULT_OK, getIntent());
            finishActivity(ProtocolActivity.VIDEO_REQUEST);
            finish();
        });

        rejectButton.setOnClickListener(v -> {
            LogHelper.d("ViewReviewActivity - rejectButton " + url);
            setResult(RESULT_REJECT, getIntent());
            finishActivity(ProtocolActivity.VIDEO_REQUEST);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        finishActivity(ProtocolActivity.VIDEO_REQUEST);
        super.onBackPressed();
        finish();
    }
}