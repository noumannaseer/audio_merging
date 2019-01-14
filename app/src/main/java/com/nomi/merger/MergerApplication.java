package com.nomi.merger;

import android.app.Application;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.nomi.merger.util.AndroidUtil;

import lombok.Getter;
import lombok.NonNull;

public class MergerApplication
        extends Application
{
    private @Getter FFmpeg ffmpeg;
    private static final String TAG = "Merging";
    @Override
    public void onCreate()
    {
        super.onCreate();
        AndroidUtil.setContext(this);
        ffmpeg = FFmpeg.getInstance(this);
        try
        {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler()
            {

                @Override
                public void onStart()
                {
                    Log.d(TAG, "onStart - -");
                }

                @Override
                public void onFailure()
                {
                    Log.d(TAG, "onFailure - -");
                }

                @Override
                public void onSuccess()
                {
                    Log.d(TAG, "OnSuccess - -");
                }

                @Override
                public void onFinish()
                {
                    Log.d(TAG, "onFinish - -");
                }
            });
        }
        catch (FFmpegNotSupportedException e)
        {
            Log.d(TAG, "exception not supported by device. - -");
            // Handle if FFmpeg is not supported by device
        }
    }


    //*********************************************************************
    public static @NonNull
    MergerApplication instance()
    //*********************************************************************
    {
        return (MergerApplication)AndroidUtil.getApplicationContext();
    }

}
