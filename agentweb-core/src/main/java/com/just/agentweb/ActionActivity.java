
/*
 * Copyright (C)  Justson(https://github.com/Justson/AgentWeb)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.just.agentweb;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.Log;
import android.webkit.WebChromeClient;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.provider.MediaStore.EXTRA_OUTPUT;


/**
 * @since 2.0.0
 * @author cenxiaozhong
 */
public  class ActionActivity extends Activity {


    public static final String KEY_ACTION = "KEY_ACTION";
    public static final String KEY_URI = "KEY_URI";
    public static final String KEY_FROM_INTENTION = "KEY_FROM_INTENTION";
    public static final String KEY_FILE_CHOOSER_INTENT = "KEY_FILE_CHOOSER_INTENT";
    private static RationaleListener mRationaleListener;
    private static PermissionListener mPermissionListener;
    private static ChooserListener mChooserListener;
    private static final String TAG = ActionActivity.class.getSimpleName();
    private Action mAction;
    public static final int REQUEST_CODE = 0x254;

    private int chooseMode = PictureMimeType.ofImage();

    private int themeId;

    private List<LocalMedia> selectList = new ArrayList<>();

    private List<LocalMedia> selectListCompress = new ArrayList<>();

    public static void start(Activity activity, Action action) {
        Intent mIntent = new Intent(activity, ActionActivity.class);
        mIntent.putExtra(KEY_ACTION, action);
//        mIntent.setExtrasClassLoader(Action.class.getClassLoader());
        activity.startActivity(mIntent);

    }

    public static void setChooserListener(ChooserListener chooserListener) {
        mChooserListener = chooserListener;
    }

    public static void setPermissionListener(PermissionListener permissionListener) {
        mPermissionListener = permissionListener;
    }

    private void cancelAction() {
        mChooserListener = null;
        mPermissionListener = null;
        mRationaleListener = null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            LogUtils.i(TAG, "savedInstanceState:" + savedInstanceState);
            return;
        }

        themeId  = R.style.picture_default_style;
       //startActivity(new Intent(this,CameraActivity.class));
        Intent intent = getIntent();
        mAction = intent.getParcelableExtra(KEY_ACTION);
        if (mAction == null) {
            cancelAction();
            this.finish();
            return;
        }
        if (mAction.getAction() == Action.ACTION_PERMISSION) {
            permission(mAction);
        } else if (mAction.getAction() == Action.ACTION_CAMERA) {
            realOpenCamera();
        } else if (mAction.getAction() == Action.ACTION_VIDEO){
            realOpenVideo();
        } else {
            fetchFile(mAction);
        }
    }

    private void fetchFile(Action action) {
        if (mChooserListener == null) {
            finish();
        }
        //realOpenFileChooser();
        openImage();
    }

    private void realOpenFileChooser() {
        try {
            if (mChooserListener == null) {
                finish();
                return;
            }
            Intent mIntent = getIntent().getParcelableExtra(KEY_FILE_CHOOSER_INTENT);
            if (mIntent == null) {
                cancelAction();
                return;
            }
            this.startActivityForResult(mIntent, REQUEST_CODE);
        } catch (Throwable throwable) {
            LogUtils.i(TAG, "找不到文件选择器");
            chooserActionCallback(-1, null);
            if (LogUtils.isDebug()) {
                throwable.printStackTrace();
            }
        }
    }

    private void chooserActionCallback(int resultCode, Intent data) {
        if (mChooserListener != null) {
            mChooserListener.onChoiceResult(REQUEST_CODE, resultCode, data);
            mChooserListener = null;
        }
        finish();
    }

    /**
     * 打开相册
     */
    private void openImage()
    {
        PictureSelector.create(this)
                .openGallery(chooseMode)
                .theme(themeId)
                .maxSelectNum(1)
                .minSelectNum(1)
                .selectionMode( PictureConfig.MULTIPLE )
                .previewImage(true)
                .previewVideo(false)
                .enablePreviewAudio(false) // 是否可播放音频
                .isCamera(true)
                .enableCrop(false)
                .compress(true)
                .glideOverride(160, 160)
                .previewEggs(true)
                .hideBottomControls( true)
                .isGif(false)
                .freeStyleCropEnabled(false)
                .showCropGrid(false)
                .openClickSound(false)
                .selectionMedia(selectList)
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       // if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择
                    selectListCompress = PictureSelector.obtainMultipleResult(data);
                    Intent compressData = new Intent();
                    if(selectListCompress.size() >  0) {

                        compressData.putExtra("path", selectListCompress.get(0).getCompressPath());
                    }
                        chooserActionCallback(resultCode, mUri != null ? new Intent().putExtra(KEY_URI, mUri) : compressData);

                    break;
                case REQUEST_CODE :
                    chooserActionCallback(resultCode, mUri != null ? new Intent().putExtra(KEY_URI, mUri) : data);
             break;

            }


    }

    private void permission(Action action) {
        List<String> permissions = action.getPermissions();
        if (AgentWebUtils.isEmptyCollection(permissions)) {
            mPermissionListener = null;
            mRationaleListener = null;
            finish();
            return;
        }
        if (mRationaleListener != null) {
            boolean rationale = false;
            for (String permission : permissions) {
                rationale = shouldShowRequestPermissionRationale(permission);
                if (rationale) {
                    break;
                }
            }
            mRationaleListener.onRationaleResult(rationale, new Bundle());
            mRationaleListener = null;
            finish();
            return;
        }
        if (mPermissionListener != null){
            requestPermissions(permissions.toArray(new String[]{}), 1);
        }
    }

    private Uri mUri;

    private void realOpenCamera() {
        try {
            if (mChooserListener == null){
                finish();
            }
            File mFile = AgentWebUtils.createImageFile(this);
            if (mFile == null) {
                mChooserListener.onChoiceResult(REQUEST_CODE, Activity.RESULT_CANCELED, null);
                mChooserListener = null;
                finish();
            }
            //Intent intent = AgentWebUtils.getIntentCaptureCompat(this, mFile);
            // 指定开启系统相机的Action
           // mUri = intent.getParcelableExtra(EXTRA_OUTPUT);
           // CameraActivity.startActivity(this);
            openImage();
            //this.startActivityForResult(intent, REQUEST_CODE);
        } catch (Throwable ignore) {
            LogUtils.e(TAG, "找不到系统相机");
            if (mChooserListener != null) {
                mChooserListener.onChoiceResult(REQUEST_CODE, Activity.RESULT_CANCELED, null);
            }
            mChooserListener = null;
            if (LogUtils.isDebug()){
                ignore.printStackTrace();
            }
        }
    }

    private void realOpenVideo(){
        try {
            if (mChooserListener == null){
                finish();
            }
            File mFile = AgentWebUtils.createVideoFile(this);
            if (mFile == null) {
                mChooserListener.onChoiceResult(REQUEST_CODE, Activity.RESULT_CANCELED, null);
                mChooserListener = null;
                finish();
            }
            //Intent intent = AgentWebUtils.getIntentVideoCompat(this, mFile);

            // 指定开启系统相机的Action
           // mUri = intent.getParcelableExtra(EXTRA_OUTPUT);
            CameraActivity.startActivity(this);
            //this.startActivityForResult(intent, REQUEST_CODE);
        } catch (Throwable ignore) {
            LogUtils.e(TAG, "找不到系统相机");
            if (mChooserListener != null) {
                mChooserListener.onChoiceResult(REQUEST_CODE, Activity.RESULT_CANCELED, null);
            }
            mChooserListener = null;
            if (LogUtils.isDebug()){
                ignore.printStackTrace();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionListener != null) {
            Bundle mBundle = new Bundle();
            mBundle.putInt(KEY_FROM_INTENTION, mAction.getFromIntention());
            mPermissionListener.onRequestPermissionsResult(permissions, grantResults, mBundle);
        }
        mPermissionListener = null;
        finish();
    }

    public interface RationaleListener {
        void onRationaleResult(boolean showRationale, Bundle extras);
    }

    public interface PermissionListener {
        void onRequestPermissionsResult(@NonNull String[] permissions, @NonNull int[] grantResults, Bundle extras);
    }

    public interface ChooserListener {
        void onChoiceResult(int requestCode, int resultCode, Intent data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
