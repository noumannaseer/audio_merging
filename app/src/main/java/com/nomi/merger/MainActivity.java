package com.nomi.merger;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import lombok.NonNull;
import lombok.val;

//******************************************************
public class MainActivity
        extends AppCompatActivity
//******************************************************
{

    TextView outputText;
    Button btnRun;
    EditText commandInput;
    Button firstFileButton;
    TextView firstFilePath;
    TextView secondFilePath;
    Button secondFileButton;
    String path1;
    String path2;
    private static final String TAG = "Merging";
    private final int FIRST_FILE = 1;
    private final int SECOND_FILE = FIRST_FILE + 1;
    private int selectedPath = -1;

    //******************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState)
    //******************************************************
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initControls();

    }


    //******************************************************
    private void initControls()
    //******************************************************
    {
        outputText = findViewById(R.id.textView);
        btnRun = findViewById(R.id.button);
        firstFileButton = findViewById(R.id.first_file_btn);
        firstFilePath = findViewById(R.id.first_file_path);

        secondFileButton = findViewById(R.id.second_file_btn);
        secondFilePath = findViewById(R.id.second_file_path);

        btnRun.setOnClickListener(v -> {
            submitCommand();
        });
        Log.d(TAG, "Permission requested");
        Permissions.check(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, null,
                          new PermissionHandler()
                          {
                              @Override
                              public void onGranted()
                              {
                                  Log.d(TAG, "Permission granted");
                                  outputFilePath = getOutputFilePath("output.mp3");
                              }
                          });
        firstFileButton.setOnClickListener(v -> {
            requestFilePath(FIRST_FILE);
        });
        secondFileButton.setOnClickListener(v -> {
            requestFilePath(SECOND_FILE);
        });
    }


    String outputFilePath = null;
    final int FILE_REQUEST_CODE = 100;

    //******************************************************
    private void requestFilePath(int pathId)
    //******************************************************
    {
        selectedPath = pathId;
        Intent intent = new Intent(this, FilePickerActivity.class);
        intent.putExtra(FilePickerActivity.CONFIGS, new Configurations.Builder().setShowAudios(true)
                                                                                .setCheckPermission(
                                                                                        true)
                                                                                .enableImageCapture(
                                                                                        true)
                                                                                .setShowImages(
                                                                                        false)
                                                                                .setShowVideos(
                                                                                        false)
                                                                                .setSuffixes("mp3",
                                                                                             "wav")
                                                                                .setMaxSelection(1)
                                                                                .setSkipZeroSizeFiles(
                                                                                        true)
                                                                                .build());
        startActivityForResult(intent, FILE_REQUEST_CODE);
    }

    //******************************************************
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    //******************************************************
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
            case FILE_REQUEST_CODE:
                ArrayList<MediaFile> list = data.getParcelableArrayListExtra(
                        FilePickerActivity.MEDIA_FILES);
                if (list != null && list.size() > 0)
                {
                    if (selectedPath == FIRST_FILE)
                    {
                        firstFilePath.setText(list.get(0)
                                                  .getPath());
                        path1 = list.get(0)
                                    .getPath();
                        selectedPath = -1;
                    }

                    else
                    {
                        path2 = list.get(0)
                                    .getPath();
                        selectedPath = -1;
                        secondFilePath.setText(list.get(0)
                                                   .getPath());
                    }
                }

                break;
            }

        }

    }


    //******************************************************
    private @NonNull
    String getOutputFilePath(String fileName)
    //******************************************************
    {
        String filePath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/" + fileName;
        return filePath;
    }

    //******************************************************
    private void updateOutputText(String message)
    //******************************************************
    {
        outputText.clearComposingText();
        outputText.setText(message);
    }

    //******************************************************
    public class CommandThread extends AsyncTask<String, Void, Boolean>
    //******************************************************
    {
        boolean resultFlag = false;

        @Override
        protected Boolean doInBackground(String... strings)
        {


            return resultFlag;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            super.onPostExecute(aBoolean);
            Log.d(TAG, "onPostExecute " + aBoolean);
        }
    }

    //******************************************************
    private void submitCommand()
    //******************************************************
    {
        if (TextUtils.isEmpty(path1) || TextUtils.isEmpty(path2) || TextUtils.isEmpty(outputFilePath))
            return;

        String s = "-y -i " + path1 + " -i " + path2 + " -filter_complex amix=inputs=2:duration=longest " + outputFilePath;

        Log.d(TAG, "Command -> " + s);
        String[] commandArray = s.split(" ");
        val ffmpegInstance = MergerApplication.instance().getFfmpeg();


        try
        {
            if (ffmpegInstance.isFFmpegCommandRunning())
            {
                ffmpegInstance.killRunningProcesses();
                Log.d(TAG, "Command Already running");
                return;
            }
            Log.d(TAG, "Device-Version - - " + ffmpegInstance.getDeviceFFmpegVersion());
            Log.d(TAG, "Library-Version - - " + ffmpegInstance.getLibraryFFmpegVersion());
            ffmpegInstance.execute(commandArray, new ExecuteBinaryResponseHandler()
            {
                @Override
                public void onProgress(String message)
                {
                    super.onProgress(message);
                    updateOutputText("onProgress \n" + message);
                    Log.d(TAG, "onProgress - - " + message);
                }

                @Override
                public void onSuccess(String message)
                {
                    super.onSuccess(message);
                    Log.d(TAG, "OnSuccess - -");
                    updateOutputText("Success \n" + message);
                }

                @Override
                public void onFailure(String message)
                {
                    super.onFailure(message);
                    Log.d(TAG, "onFailure - -" + message);
                    updateOutputText("Failure \n" + message);
                }

                @Override
                public void onStart()
                {
                    super.onStart();
                    updateOutputText("onStart \n" + commandArray.toString());
                    Log.d(TAG, "onStart");
                }

                @Override
                public void onFinish()
                {
                    super.onFinish();
                    Log.d(TAG, "onFinish");
                }
            });
        }
        catch (FFmpegCommandAlreadyRunningException e)
        {
            updateOutputText("Exception \n" + e.getLocalizedMessage());
            e.printStackTrace();
        }

    }

}
