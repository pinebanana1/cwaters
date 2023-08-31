package com.ora.calmwaters.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.box.androidsdk.content.requests.BoxResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * Sample content app that demonstrates session creation, and use of file api.
 */
public class BoxActivity extends AppCompatActivity implements BoxAuthentication.AuthListener {

    static BoxSession mSession = null;
    BoxSession mOldSession = null;

    //private ListView mListView;

    private ProgressDialog mDialog;

    //private ArrayAdapter<BoxItem> mAdapter;

    private BoxApiFolder mFolderApi;
    private BoxApiFile mFileApi;

    private static final String TAG = "OraBox";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        //registerReceiver(mDateChangedReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        //OverlayService.createLogfile();
        //dailyLog = new DailyLog(getExternalMediaDirs()[0]);

        //setContentView(R.layout.activity_main);
        //mListView = (ListView) findViewById(android.R.id.list);
        //mAdapter = new BoxItemAdapter(this);
        //mListView.setAdapter(mAdapter);
        BoxConfig.IS_LOG_ENABLED = true;
        configureClient();
        initSession();
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.v(TAG, "onActivityResult: request:" + requestCode+ " result:" + resultCode );
        //dailyLog.append("Logged out");
        super.onActivityResult(requestCode, resultCode, data);

        logout();
    }

    /**
     * Set required config parameters. Use values from your application settings in the box developer console.
     */
    private void configureClient() {
        BoxConfig.CLIENT_ID = "4ac4bcw60rds6xp7qt1xc951iwao0lxi";
        BoxConfig.CLIENT_SECRET = "EG4xQsTrW6zku4xsmhXFRUV6WPMDwIJ1";

        // needs to match redirect uri in developer settings if set.
        //   BoxConfig.REDIRECT_URL = "<YOUR_REDIRECT_URI>";
    }

    /**
     * Create a BoxSession and authenticate.
     */
    private void initSession() {
        //clearAdapter();
        Log.v(TAG, "initSession");
        mSession = new BoxSession(this);
        mSession.setSessionAuthListener(this);
        mSession.authenticate(this);
    }

    @Override
    public void onRefreshed(BoxAuthentication.BoxAuthenticationInfo info) {
        Log.v(TAG, "onRefreshed");
        // do nothing when auth info is refreshed
    }

    @Override
    public void onAuthCreated(BoxAuthentication.BoxAuthenticationInfo info) {
        //Init file, and folder apis; and use them to fetch the root folder
        //mFolderApi = new BoxApiFolder(mSession);
        //mFileApi = new BoxApiFile(mSession);
        //loadRootFolder();

        BoxUser user = mSession.getUser();
        BoxUser infouser = info.getUser();
        String userid = infouser.getId();

        Intent intent = new Intent(this, ProtocolActivity.class);
        Log.v(TAG, "onAuthCreated:" + infouser.getAddress() + " " +
                infouser.getId()+ " " +
                infouser.getName().replace(" ", "") + " " +
                infouser.getType() + " " + mSession.getBoxAccountEmail() + " " + mSession.getUserAgent() + " " + mSession.getUserId()
        );
        //dailyLog.append("Logged in: " + infouser.getName());
        intent.putExtra("userid", infouser.getName().replace(" ", ""));

        startActivityForResult(intent, 1);
    }

    @Override
    public void onAuthFailure(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
        //dailyLog.append("Login failure");
        if (ex != null) {
            Log.e(TAG, "onAuthFailure:" + ex + " info:" + info + " oldSession:"+ mOldSession);
            //clearAdapter();
        } else if (info == null && mOldSession != null) {
            Log.e(TAG, "onAuthFailure oldSession");
            mSession = mOldSession;
            mSession.setSessionAuthListener(this);
            mOldSession = null;
            onAuthCreated(mSession.getAuthInfo());
        } else {
            Log.e(TAG, "onAuthFailure");
        }
    }

    @Override
    public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {
        //clearAdapter();
        Log.e(TAG, "onLoggedOut:" + ex + " info:" + info + " oldSession:"+ mOldSession);
        //dailyLog.append("onLoggedOut");
        initSession();
    }


//    //Method to demonstrate fetching folder items from the root folder
//    private void loadRootFolder() {
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    //Api to fetch root folder
//                    final BoxIteratorItems folderItems = mFolderApi.getItemsRequest(BoxConstants.ROOT_FOLDER_ID).send();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mAdapter.clear();
//                            for (BoxItem boxItem: folderItems) {
//                                mAdapter.add(boxItem);
//                            }
//                        }
//                    });
//                } catch (BoxException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }.start();
//    }

    /**
     * Method demonstrates a sample file being uploaded using the file api
     */
//    private void uploadSampleFile() {
//        mDialog = ProgressDialog.show(MainActivity.this, getText(R.string.boxsdk_Please_wait), getText(R.string.boxsdk_Please_wait));
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    String uploadFileName = "box_logo.png";
//                    InputStream uploadStream = getResources().getAssets().open(uploadFileName);
//                    String destinationFolderId = "0";
//                    String uploadName = "BoxSDKUpload.png";
//                    BoxRequestsFile.UploadFile request = mFileApi.getUploadRequest(uploadStream, uploadName, destinationFolderId);
//                    final BoxFile uploadFileInfo = request.send();
//                    showToast("Uploaded " + uploadFileInfo.getName());
//                    loadRootFolder();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (BoxException e) {
//                    e.printStackTrace();
//                    BoxError error = e.getAsBoxError();
//                    if (error != null && error.getStatus() == HttpURLConnection.HTTP_CONFLICT) {
//                        ArrayList<BoxEntity> conflicts = error.getContextInfo().getConflicts();
//                        if (conflicts != null && conflicts.size() == 1 && conflicts.get(0) instanceof BoxFile) {
//                            uploadNewVersion((BoxFile) conflicts.get(0));
//                            return;
//                        }
//                    }
//                    showToast("Upload failed");
//                } finally {
//                    mDialog.dismiss();
//                }
//            }
//        }.start();
//
//    }

    /**
     * Method demonstrates a new version of a file being uploaded using the file api
     * @param file
     */
    private void uploadNewVersion(final BoxFile file) {
        new Thread() {
            @Override
            public void run() {
                try {
                    String uploadFileName = "box_logo.png";
                    InputStream uploadStream = getResources().getAssets().open(uploadFileName);
                    BoxRequestsFile.UploadNewVersion request = mFileApi.getUploadNewVersionRequest(uploadStream, file.getId());
                    final BoxFile uploadFileVersionInfo = request.send();
                    showToast("Uploaded new version of " + uploadFileVersionInfo.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BoxException e) {
                    e.printStackTrace();
                    showToast("Upload failed");
                } finally {
                    mDialog.dismiss();
                }
            }
        }.start();
    }

    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BoxActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }


//    private void clearAdapter() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mAdapter.clear();
//            }
//        });
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        int numAccounts = BoxAuthentication.getInstance().getStoredAuthInfo(this).keySet().size();
//        menu.findItem(R.id.logoutAll).setVisible(numAccounts > 1);
//        menu.findItem(R.id.logout).setVisible(numAccounts > 0);
//        menu.findItem(R.id.switch_accounts).setTitle(numAccounts > 0 ? R.string.switch_accounts : R.string.login);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.upload) {
//            uploadSampleFile();
//            return true;
//        } else if (id == R.id.switch_accounts) {
//            switchAccounts();
//            return true;
//        } else if (id == R.id.logout) {
//            mSession.logout();
//            return true;
//        } else if (id == R.id.logoutAll) {
//            new Thread() {
//                @Override
//                public void run() {
//                    BoxAuthentication.getInstance().logoutAllUsers(getApplicationContext());
//                }
//            }.start();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private void switchAccounts() {
        mOldSession = mSession;
        // when switching accounts we don't care about events for the old session.
        mOldSession.setSessionAuthListener(null);
        mSession = new BoxSession(this, null);
        mSession.setSessionAuthListener(this);
        mSession.authenticate(this).addOnCompletedListener(new BoxFutureTask.OnCompletedListener<BoxSession>() {
            @Override
            public void onCompleted(BoxResponse<BoxSession> response) {
                if (response.isSuccess()) {
                    //clearAdapter();
                }
            }
        });
    }



//    private class BoxItemAdapter extends ArrayAdapter<BoxItem> {
//        public BoxItemAdapter(Context context) {
//            super(context, 0);
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            BoxItem item = getItem(position);
//            if (convertView == null) {
//                convertView = LayoutInflater.from(getContext()).inflate(R.layout.boxsdk_list_item, parent, false);
//            }
//
//            TextView name = (TextView) convertView.findViewById(R.id.name);
//            name.setText(item.getName());
//
//            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
//            if (item instanceof BoxFolder) {
//                icon.setImageResource(R.drawable.boxsdk_icon_folder_yellow);
//            } else {
//                icon.setImageResource(R.drawable.boxsdk_generic);
//            }
//
//            return convertView;
//        }
//
//    }

    static void logout() {
        Log.i(TAG, "logout");

        mSession.logout();
    }
}
