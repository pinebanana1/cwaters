// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.ora.calmwaters;

import static com.ora.calmwaters.ui.ProtocolActivity.dailyLog;

import android.accessibilityservice.AccessibilityGestureEvent;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityRecord;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.ora.calmwaters.data.localstorage.DailyLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import calmwaters.R;

public class OverlayService extends AccessibilityService {



    FrameLayout mLayout;
    private static final String TAG = "OraService";
    public static OverlayService instance = null;
    DailyLog accesibilityLog;

    TextToSpeech tts;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    @Override
    protected void onServiceConnected() {
        instance = this;

        // Create an overlay and display the action bar
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayout = new FrameLayout(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.TOP;
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.overlay, mLayout);
        mLayout.setVisibility(View.GONE);
        wm.addView(mLayout, lp);
//
//        configureSwipeButton();


        Log.i(TAG, "onServiceConnected");
        accesibilityLog = new DailyLog(getExternalMediaDirs()[0], "Accessibility");
        if (dailyLog == null)
            dailyLog = new DailyLog(getExternalMediaDirs()[0], "Log");
        dailyLog.append("onServiceConnected");


//        Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
//        //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(cameraIntent);

        tts=new TextToSpeech(getApplicationContext(), status -> {
            if(status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.i(TAG, "onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("OraService", "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    AccessibilityNodeInfo shutter;
    void dumpNode(AccessibilityNodeInfo info) {
        //Log.i(TAG, "onAccessibilityEvent: info:"+toString());
        List<AccessibilityNodeInfo.AccessibilityAction> al = info.getActionList();
        if (al != null) {
            al.forEach((temp) -> {
                CharSequence label = temp.getLabel();
                accesibilityLog.i(TAG, "AccessibilityNodeInfo: actionList :" + label + " S:" + temp.toString() + " id:" + temp.getId());
            });
        }
        List<String> extra = info.getAvailableExtraData();
        if (!extra.isEmpty()) {
            accesibilityLog.i(TAG, "AccessibilityNodeInfo getAvailableExtraData:"+extra);
            for (String temp: extra)
                accesibilityLog.i(TAG, "AccessibilityNodeInfo: getAvailableExtraData item:" + temp);
        }

        if (info.getClassName() != null)
            accesibilityLog.i(TAG, "AccessibilityNodeInfo getClassName:"+info.getClassName());

        if (info.getHintText() != null)
            accesibilityLog.i(TAG, "AccessibilityNodeInfo getHintText:"+info.getHintText());

        if (info.getStateDescription() != null)
            accesibilityLog.i(TAG, "AccessibilityNodeInfo getStateDescription:"+info.getStateDescription());

        if (info.getText() != null)
            accesibilityLog.i(TAG, "AccessibilityNodeInfo getText:"+info.getText());

        if (info.getTooltipText() != null)
            accesibilityLog.i(TAG, "AccessibilityNodeInfo getTooltipText:"+info.getTooltipText());

        if (info.getViewIdResourceName() != null)
            accesibilityLog.i(TAG, "AccessibilityNodeInfo getViewIdResourceName:"+info.getViewIdResourceName());

        if (info.getLabeledBy() != null)
            accesibilityLog.i(TAG, "AccessibilityNodeInfo getLabeledBy");

        if (info.getLabelFor() != null)
            accesibilityLog.i(TAG, "AccessibilityNodeInfo getLabelFor");

        if (info.getTouchDelegateInfo() != null)
            accesibilityLog.i(TAG, "AccessibilityNodeInfo getTouchDelegateInfo");

        //info.findAccessibilityNodeInfosByText("")
        CharSequence pane = info.getPaneTitle();
        if (pane != null)
            accesibilityLog.i(TAG, "AccessibilityNodeInfo: pane :"+info.getPaneTitle());

        CharSequence content = info.getContentDescription();
        if (content!=null)
            accesibilityLog.i(TAG, "AccessibilityNodeInfo: content: " + content);

        if (info.getWindow()!=null)
            accesibilityLog.i(TAG, "AccessibilityNodeInfo: getWindow:"+ info.getWindow().toString());

//        List<AccessibilityNodeInfo> found = info.findAccessibilityNodeInfosByText("Camera");
//        if (found !=null && found.stream().count() > 0)
//            dailyLog.i(TAG, "AccessibilityNodeInfo: found Camera:"+found.stream().count());
//        List<AccessibilityNodeInfo> foundVid = info.findAccessibilityNodeInfosByText("Video");
//        if (foundVid !=null && foundVid.stream().count() > 0)
//            dailyLog.i(TAG, "AccessibilityNodeInfo: found Video:"+foundVid.stream().count());
//        List<AccessibilityNodeInfo> foundOptions = info.findAccessibilityNodeInfosByText("Options");
//        if (foundOptions !=null && foundOptions.stream().count()>0)
//            dailyLog.i(TAG, "AccessibilityNodeInfo: found Options:"+foundOptions.stream().count());

        List<AccessibilityNodeInfo> found10s = info.findAccessibilityNodeInfosByText("10 S");
        if (found10s !=null && found10s.stream().count()>0) {
            accesibilityLog.i(TAG, "AccessibilityNodeInfo: found 10s:" + found10s.stream().count());
            //found10s.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            //dumpNode(found10s.get(0));
        }

        List<AccessibilityNodeInfo> foundOptionsResource = info.findAccessibilityNodeInfosByViewId("com.google.android.GoogleCamera:id/minibar");//_item_chevron");
        if (foundOptionsResource !=null && foundOptionsResource.stream().count()>0) {
            accesibilityLog.i(TAG, "AccessibilityNodeInfo: found Options Resource:" + foundOptionsResource.stream().count());
            //dumpNode(foundOptionsResource.get(0));
            //foundOptionsResource.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

        List<AccessibilityNodeInfo> foundShutterResource = info.findAccessibilityNodeInfosByViewId("com.google.android.GoogleCamera:id/shutter_button");
        if (foundShutterResource !=null && foundShutterResource.stream().count()>0) {
            accesibilityLog.i(TAG, "onAccessibilityEvent: found Shutter Resource:" + foundShutterResource.stream().count());
            if (shutter == null)
                shutter = foundOptionsResource.get(0);
            //dumpNode(foundOptionsResource.get(0));
            //foundShutterResource.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

//        if (info.getChildCount() != 0) {
//            dailyLog.i(TAG, "AccessibilityNodeInfo getChildCount:" + info.getChildCount());
//            for (int i = 0; i<info.getChildCount(); i++) {
//                dailyLog.i(TAG, "AccessibilityNodeInfo Child " + i);
//                AccessibilityNodeInfo node = info.getChild(i);
//                if (node != null)
//                    dumpNode(info.getChild(i));
//            }
//        }
    }
    boolean isFirstEvent=true;
    boolean isTimerEnabled;
    boolean isOptionOpened;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        accesibilityLog.i(TAG, "onAccessibilityEvent");
        if (!isFirstEvent)
            return;

        accesibilityLog.i(TAG, "onAccessibilityEvent event:"+event.toString());
        accesibilityLog.i(TAG, "onAccessibilityEvent getEventType:"+event.getEventType());
        //if (pkg.equals(/*android:packageNames=*/"com.google.android.GoogleCamera"))
        {
            int recCnt = event.getRecordCount();
            if (recCnt > 0) {
                accesibilityLog.i(TAG, "onAccessibilityEvent: recCnt:"+recCnt);
                AccessibilityRecord record = event.getRecord(0);
            }
        }

        //CharSequence pkg = event.getPackageName();
        //dailyLog.i(TAG, pkg.toString());

        if (event.getClassName() != null)
            accesibilityLog.i(TAG, "onAccessibilityEvent getClassName:"+event.getClassName());

        if (event.getContentDescription() != null)
            accesibilityLog.i(TAG, "onAccessibilityEvent getContentDescription:"+event.getContentDescription());

        List<CharSequence> text = event.getText();
        if (!text.isEmpty())
            accesibilityLog.i(TAG, "onAccessibilityEvent getText:"+text);

        if (event.getBeforeText() != null)
            accesibilityLog.i(TAG, "onAccessibilityEvent getBeforeText:"+event.getBeforeText());
        if (event.getCurrentItemIndex() != -1)
            accesibilityLog.i(TAG, "onAccessibilityEvent getCurrentItemIndex:"+event.getCurrentItemIndex());
        if (event.getItemCount() != -1)
            accesibilityLog.i(TAG, "onAccessibilityEvent getItemCount:"+event.getItemCount());
        if (event.getAction() != 0)
            accesibilityLog.i(TAG, "onAccessibilityEvent getAction:"+event.getAction());

        accesibilityLog.i(TAG, "onAccessibilityEvent getWindowId:"+event.getWindowId());
        accesibilityLog.i(TAG, "onAccessibilityEvent getContentChangeTypes:"+event.getContentChangeTypes());

        AccessibilityNodeInfo info = event.getSource();
        if (info!=null) {
            dumpNode(info);
            //isFirstEvent = false;
        }
        else {
            accesibilityLog.i(TAG, "onAccessibilityEvent info null");
            return;
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            if (text.contains("Open options menu")) {
                accesibilityLog.w(TAG, "onAccessibilityEvent: Open options menu");

                List<AccessibilityNodeInfo> foundOptionsResource = info.findAccessibilityNodeInfosByViewId("com.google.android.GoogleCamera:id/minibar");//_item_chevron");
                if (foundOptionsResource != null && foundOptionsResource.stream().count() > 0) {
                    accesibilityLog.i(TAG, "onAccessibilityEvent: found Options Resource:" + foundOptionsResource.stream().count());
                    //dumpNode(foundOptionsResource.get(0));
                    accesibilityLog.i(TAG, "onAccessibilityEvent: clicking Open options");
                    foundOptionsResource.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                return;
            }

            if (text.contains("Flash on")) {
                accesibilityLog.i(TAG, "onAccessibilityEvent: text.contains Flash on");

                AccessibilityNodeInfo parent = info.getParent();
    //            for (; parent.getParent() != null; parent = parent.getParent()) {
    //                dailyLog.i(TAG, "onAccessibilityEvent: dumping ancestor");
    //                dumpNode(parent);
    //            }
    //            dailyLog.i(TAG, "onAccessibilityEvent: dumping root");
                dumpNode(parent);

                List<AccessibilityNodeInfo> found3s = parent.findAccessibilityNodeInfosByText("3 S");
                if (found3s != null && found3s.stream().count() > 0) {
                    accesibilityLog.i(TAG, "onAccessibilityEvent: found 3s:" + found3s.stream().count());
                    accesibilityLog.i(TAG, "onAccessibilityEvent: clicking 3s" + found3s.stream().count());
                    found3s.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    //dumpNode(found10s.get(0));
                } else {
                    accesibilityLog.w(TAG, "onAccessibilityEvent: No 3s");

                    List<AccessibilityNodeInfo> foundOptionsResource = info.findAccessibilityNodeInfosByViewId("com.google.android.GoogleCamera:id/minibar");//_item_chevron");
                    if (foundOptionsResource != null && foundOptionsResource.stream().count() > 0) {
                        accesibilityLog.i(TAG, "onAccessibilityEvent: found Options Resource:" + foundOptionsResource.stream().count());
                        //dumpNode(foundOptionsResource.get(0));
                        accesibilityLog.i(TAG, "onAccessibilityEvent: clicking options again");
                        foundOptionsResource.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
                return;
            } else {
                if (text.contains("Close options menu")) {
                    accesibilityLog.i(TAG, "onAccessibilityEvent: Close options menu");

                    AccessibilityNodeInfo parent = info.getParent();
                    for (; parent.getParent() != null; parent = parent.getParent()) {
                        accesibilityLog.i(TAG, "onAccessibilityEvent: dumping ancestor");
                        dumpNode(parent);
                    }
                    accesibilityLog.i(TAG, "onAccessibilityEvent: dumping root");
                    dumpNode(parent);

                    List<AccessibilityNodeInfo> foundFlash = parent.findAccessibilityNodeInfosByText("Flash On");
                    if (foundFlash != null && foundFlash.stream().count() > 0) {
                        accesibilityLog.i(TAG, "onAccessibilityEvent: found foundFlash:" + foundFlash.stream().count());
                        foundFlash.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        //dumpNode(found10s.get(0));
                    }
                    return;
                }
            }

            if (event.getContentDescription() != null && event.getContentDescription().toString().contains("Timer 3 Seconds")) {
                isTimerEnabled = true;
                //shutter.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                AccessibilityWindowInfo window = info.getWindow();
                if (window!=null) {
                    dumpNode(window.getRoot());
                    accesibilityLog.i(TAG, "onAccessibilityEvent: dumping root");
                }
                else
                    accesibilityLog.i(TAG, "onAccessibilityEvent: window null");

                AccessibilityNodeInfo parent = info.getParent();
                for (; parent.getParent() != null; parent = parent.getParent()) {
                    accesibilityLog.i(TAG, "onAccessibilityEvent: dumping ancestor");
                    //dumpNode(parent);
                }
                accesibilityLog.i(TAG, "onAccessibilityEvent: dumping root");
                dumpNode(parent);

                List<AccessibilityNodeInfo> foundShutterResource = parent.findAccessibilityNodeInfosByViewId("com.google.android.GoogleCamera:id/shutter_button");
                if (foundShutterResource !=null && foundShutterResource.stream().count()>0) {
                    accesibilityLog.i(TAG, "onAccessibilityEvent: found Shutter Resource:" + foundShutterResource.stream().count());
                    //dumpNode(foundOptionsResource.get(0));
                    accesibilityLog.i(TAG, "onAccessibilityEvent: Clicking shutter with delay");
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // this code will be executed after 3 seconds
                            accesibilityLog.i(TAG, "onAccessibilityEvent: Clicking shutter after delay");
                            foundShutterResource.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            isOptionOpened = false;
                        }
                    }, 3000);

                    //mLayout.setVisibility(View.VISIBLE);
                }
//            List<AccessibilityNodeInfo> foundOptionsResource = parent.findAccessibilityNodeInfosByViewId("com.google.android.GoogleCamera:id/minibar");//_item_chevron");
//            if (foundOptionsResource !=null && foundOptionsResource.stream().count()>0) {
//                dailyLog.i(TAG, "onAccessibilityEvent: found Options Resource:" + foundOptionsResource.stream().count());
//                //dumpNode(foundOptionsResource.get(0));
//                foundOptionsResource.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//            }
            }
        }

        if (event.getContentDescription() != null && event.getContentDescription().toString().contains("Options") &&
                event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {

            List<AccessibilityNodeInfo> found10s = info.findAccessibilityNodeInfosByText("10 S");
            if (found10s != null && found10s.stream().count() > 0) {
                accesibilityLog.i(TAG, "onAccessibilityEvent: found 10s:" + found10s.stream().count());
                //found10s.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                //dumpNode(found10s.get(0));
            }
        }


//        if (isTimerEnabled && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
//                event.getClassName().toString().contains("FrameLayout"))  {
//            dailyLog.i(TAG, "onAccessibilityEvent: isTimerEnabled FrameLayout");
//
//            List<AccessibilityNodeInfo> foundShutterResource = info.findAccessibilityNodeInfosByViewId("com.google.android.GoogleCamera:id/shutter_button");
//            if (foundShutterResource !=null && foundShutterResource.stream().count()>0) {
//                dailyLog.i(TAG, "onAccessibilityEvent: found Shutter Resource:" + foundShutterResource.stream().count());
//                //dumpNode(foundOptionsResource.get(0));
//                dailyLog.i(TAG, "onAccessibilityEvent: Clicking shutter");
//                foundShutterResource.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//            }
//            isTimerEnabled = false;
//        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                text.contains("Camera")) {
            enableClicks(false);
            accesibilityLog.i(TAG, "onAccessibilityEvent: TYPE_WINDOW_STATE_CHANGED Camera WindowId"+event.getWindowId()+":"+info.getWindowId());

            //if (event.getWindowId() != info.getWindowId())
            //    return;

            if (isOptionOpened)
                return;
            else
                isOptionOpened = true;

            List<AccessibilityNodeInfo> foundOptionsResource = info.findAccessibilityNodeInfosByViewId("com.google.android.GoogleCamera:id/minibar");//_item_chevron");
            if (foundOptionsResource !=null && foundOptionsResource.stream().count()>0) {
                accesibilityLog.i(TAG, "onAccessibilityEvent: found Options Resource:" + foundOptionsResource.stream().count());
                //dumpNode(foundOptionsResource.get(0));
                accesibilityLog.i(TAG, "onAccessibilityEvent: clicking");
                foundOptionsResource.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

        if ((event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) &&
                (event.getContentChangeTypes() & AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT) != 0)  {
            accesibilityLog.i(TAG, "onAccessibilityEvent: TYPE_WINDOW_CONTENT_CHANGED CONTENT_CHANGE_TYPE_TEXT");

            if (isOptionOpened)
                return;
            else
                isOptionOpened = true;
            List<AccessibilityNodeInfo> foundOptionsResource = info.findAccessibilityNodeInfosByViewId("com.google.android.GoogleCamera:id/minibar");//_item_chevron");
            if (foundOptionsResource !=null && foundOptionsResource.stream().count()>0) {
                accesibilityLog.i(TAG, "onAccessibilityEvent: found Options Resource:" + foundOptionsResource.stream().count());
                //dumpNode(foundOptionsResource.get(0));
                accesibilityLog.i(TAG, "onAccessibilityEvent: clicking");
                foundOptionsResource.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.i(TAG, "onInterrupt");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged");
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.i(TAG, "onKeyEvent:" + event.toString());
        return super.onKeyEvent(event);
    }

    @Override
    protected boolean onGesture(int gestureId) {
        Log.i(TAG, "onGesture:"+gestureId);
        return super.onGesture(gestureId);
    }

    @Override
    public boolean onGesture(@NonNull AccessibilityGestureEvent gestureEvent) {
        Log.i(TAG, "onGesture event:"+gestureEvent.toString());

        return super.onGesture(gestureEvent);
    }

    public void startCamera(Activity activity) {
        Log.d(TAG,"startCamera without request code");
        isOptionOpened = false;
        Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK /*| Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_TASK_ON_HOME/* | Intent.FLAG_ACTIVITY_FORWARD_RESULT*/);
        //startActivityForResult(activity, cameraIntent, 1888, null);
        activity.startActivity(cameraIntent);

        //dispatchTimerDisableClicks();


//        tts.speak(getString(R.string.text_to_speech_part_1) + ProtocolActivity.side.name() + getString(R.string.text_to_speech_part_2) + ProtocolActivity.direction.name(), TextToSpeech.QUEUE_FLUSH, null);
        //tts.speak(myText2, TextToSpeech.QUEUE_ADD, null);
    }

    public void startCamera(Activity activity, int requestCode) {
        LogHelper.d("startCamera with code");
        isOptionOpened = false;
        Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(cameraIntent, requestCode);
    }

    public void startVideoRecording(Activity activity) {
        LogHelper.d("starting Video Recording without requestCode");
        isOptionOpened = false;
        Intent videoRecordingIntent = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA);
        videoRecordingIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
        videoRecordingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(videoRecordingIntent);
    }

    public void startVideoRecording(Activity activity, int requestCode) {
        LogHelper.d("starting Video Recording");
        isOptionOpened = false;
        Intent videoRecordingIntent = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA);
        videoRecordingIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
        videoRecordingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(videoRecordingIntent, requestCode);
    }

    public void dispatchTimerDisableClicks() {
        int delay = takePhotoGesture();

//        mock.setVisibility(View.VISIBLE);
        //mLayout.setVisibility(View.VISIBLE);
        View mock = mLayout.findViewById(R.id.mockView);
////        mock.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        final Handler handler = new Handler();
        mock.getHandler().postDelayed(() -> {
            //Write whatever to want to do after delay specified (1 sec)
            Log.d("Handler", "Running Handler");
            //mock.setVisibility(View.VISIBLE);
            mLayout.setVisibility(View.VISIBLE);

            Rect rect = new Rect(0, 0, mLayout.getWidth(), mLayout.getHeight());
            List<Rect> rects = new ArrayList<>();
            rects.add(rect);
            mLayout.setSystemGestureExclusionRects(rects);
            mock.setSystemGestureExclusionRects(rects);
        }, delay);
    }

//    private void configureSwipeButton() {
//        Button swipeButton = (Button) mLayout.findViewById(R.id.swipe);
//        swipeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Path swipePath = new Path();
////                swipePath.moveTo(1000, 1000);
////                swipePath.lineTo(100, 1000);
//                Log.i("Ora Service", "click captured");
//                View mock = mLayout.findViewById(R.id.mock);
//                mock.setVisibility(View.VISIBLE);
//
////                takephotoGesture();
//            }
//        });
//
////        View overlay = mLayout.findViewById(R.id.overlay);
////        overlay.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                Log.i("Ora Service", "click captured");
////            }
////        });
//    }

    static boolean wasFlashEnabled;

    int takePhotoGesture() {
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        int time = 0;
        //gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));
        Path click = new Path();
        click.moveTo(550,100);
        //click.moveTo(550,200);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(click, time+=3000, 500));
        //click.moveTo(814,1100); //timer
        click.moveTo(950,925); //timer
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(click, time+=1000, 500));
        //950x500 flash on
        if (!wasFlashEnabled) {
            click.moveTo(950, 550);
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(click, time+=1000, 500));
            wasFlashEnabled = true;
        }
        click.moveTo(550,1950);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(click, time+=1000, 100));

        dispatchGesture(gestureBuilder.build(), null, null);
        return time+100;
    }

    public void enableClicks(boolean enable) {
        //View mock = mLayout.findViewById(R.id.mockView);
        Log.i(TAG,"enableClicks:"+enable);
        mLayout.setVisibility(enable? View.GONE: View.VISIBLE);
        //mock.setVisibility(View.GONE);
    }

    @Override
    public void onSystemActionsChanged() {
        Log.i(TAG,"onSystemActionsChanged");
        super.onSystemActionsChanged();
    }

    @Override
    public AccessibilityNodeInfo findFocus(int focus) {
        return super.findFocus(focus);
    }

    @Override
    public List<AccessibilityWindowInfo> getWindows() {
        return super.getWindows();
    }
}
