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
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.AudioPickActivity;
import com.vincent.filepicker.filter.entity.AudioFile;

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

        firstFileButton.setOnClickListener(v -> {
            requestFilePath(FIRST_FILE);
        });
        secondFileButton.setOnClickListener(v -> {
            requestFilePath(SECOND_FILE);
        });
    }


    String outputFilePath = null;

    //******************************************************
    private void requestFilePath(int pathId)
    //******************************************************
    {
        Permissions.check(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, null,
                          new PermissionHandler()
                          {
                              @Override
                              public void onGranted()
                              {
                                  outputFilePath = getOutputFilePath("output.mp3");
                              }
                          });

        selectedPath = pathId;
        Intent intent3 = new Intent(this, AudioPickActivity.class);
        intent3.putExtra(AudioPickActivity.IS_NEED_RECORDER, true);
        intent3.putExtra(Constant.MAX_NUMBER, 9);
        startActivityForResult(intent3, Constant.REQUEST_CODE_PICK_AUDIO);
    }

    //******************************************************
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    //******************************************************
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            ArrayList<AudioFile> list = data.getParcelableArrayListExtra(
                    Constant.RESULT_PICK_AUDIO);
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
        @Override
        protected Boolean doInBackground(String... strings)
        {
            val ffmpegInstance = MergerApplication.instance()
                                                  .getFfmpeg();

            try
            {
                Log.d(TAG, "Device-Version - - " + ffmpegInstance.getDeviceFFmpegVersion());
                Log.d(TAG, "Library-Version - - " + ffmpegInstance.getLibraryFFmpegVersion());
                ffmpegInstance.execute(strings, new ExecuteBinaryResponseHandler()
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
                        updateOutputText("onStart \n" + strings.toString());
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

            return false;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            super.onPostExecute(aBoolean);
            Log.d(TAG, "onPostExecute "+aBoolean);
        }
    }

    //******************************************************
    private void submitCommand()
    //******************************************************
    {
        if (TextUtils.isEmpty(path1) || TextUtils.isEmpty(path2) || TextUtils.isEmpty(
                outputFilePath))
            return;
        //ffmpeg -y -i ad_sound/whistle.mp3 -i ad_sound/4s.wav -filter_complex "[0:0][1:0] amix=inputs=2:duration=longest" -c:a libmp3lame ad_sound/outputnow.mp3
        String s = "-i " + path1 + " -i " + path2 + " -filter_complex amix=inputs=2:duration=longest " + outputFilePath;

        Log.d(TAG, "Command -> " + s);

        String[] commandArray = s.split(" ");
        CommandThread thread = new CommandThread();
        thread.execute(commandArray);

    }

}
