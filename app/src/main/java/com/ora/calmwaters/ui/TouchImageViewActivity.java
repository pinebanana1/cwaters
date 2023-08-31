package com.ora.calmwaters.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import static com.ora.calmwaters.ui.ProtocolActivity.dailyLog;
import com.ora.calmwaters.OverlayService;
import calmwaters.R;
import com.ortiz.touchview.TouchImageView;

public class TouchImageViewActivity extends AppCompatActivity {
    private Uri uri;
    private String url;
    private String imageS;
    private boolean retakePhoto;

    final static String TAG = "OraTouchImageView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_touchimageview);

        OverlayService.instance.enableClicks(true);
        retakePhoto = false;

        TouchImageView imageView = findViewById(R.id.touchImage);
        uri = getIntent().getData();
        imageView.setImageURI(uri);

        url = uri.toString();
        Log.i(TAG, "onCreate:"+url);

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DISPLAY_NAME}, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
        cursor.moveToFirst();
        imageS = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
        if (imageS.endsWith(".dng")) {
            cursor.moveToNext();
            imageS = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
        }

        Button acceptButton = findViewById(R.id.accept);
        acceptButton.setOnClickListener(v -> {
            Log.d(TAG, "Accepted:"+url);
            dailyLog.append("Accepted " + imageS);
            setResult(RESULT_OK, getIntent());
            finishActivity(ProtocolActivity.CAMERA_REQUEST);
            finish();
        });

        Button rejectButton = findViewById(R.id.reject);
        rejectButton.setOnClickListener(v -> {
            Log.w(TAG, "Rejected:"+url);
            dailyLog.append("Rejected " + imageS);
            retakePhoto = true;
            finishActivity(ProtocolActivity.CAMERA_REQUEST);
            finish();
        });

        rejectButton.setOnLongClickListener(v -> {
            Log.d(TAG, "Canceled:"+url);
            dailyLog.append("Canceled " + imageS);
            setResult(RESULT_FIRST_USER, getIntent());
            retakePhoto = false;
            finishActivity(ProtocolActivity.CAMERA_REQUEST);
            finish();
            return false;
        });

        setFinishOnTouchOutside(false);

        TextView description = findViewById(R.id.pixel_uri);
        description.setText("Should be the " + ProtocolActivity.side.name() + " eye, looking "+ ProtocolActivity.direction.name());
    }

    @Override
    public void onBackPressed() {
        Log.w(TAG, "Rejected back button:"+url);
        dailyLog.append("Rejected back button" + imageS);
        retakePhoto = true;
        finishActivity(ProtocolActivity.CAMERA_REQUEST);
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        boolean fin = this.isFinishing();
        Log.d(TAG, "onDestroy: isFinishing="+fin);
        //if (retakePhoto)
            //OverlayService.instance.enableClicks(false);
            //OverlayService.instance.dispatchTimerDisableClicks();
        super.onDestroy();
    }
}


