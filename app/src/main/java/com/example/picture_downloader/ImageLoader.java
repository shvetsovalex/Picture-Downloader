package com.example.picture_downloader;


import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class ImageLoader extends AsyncTaskLoader<Integer> {
    protected final static int DOWNLOADING_STATUS = 0;
    protected final static int DOWNLOADED_STATUS = 1;
    protected final static int IDLE_STATUS = 2;
    protected final static int ERROR_STATUS = 3;
    protected final static String ACTION_PROGRESS = "com.example.picture_downloader.Status";
    protected static final String STATUS_EXTRA = "status";
    protected static final String PROGRESS_EXTRA = "progress";
    protected final static String fileName = "pic.jpg";

    private final String TAG = this.getClass().getName();;
    private String url;
    private int progress;
    private int status;
    private Handler handler;
    private String file_path;
    private Context context;

    public ImageLoader(Context context, String url, String file_path) {
        super(context);
        this.url = url;
        this.file_path = file_path;
        this.status = IDLE_STATUS;
        this.context = context;
    }

    @Override
    public Integer loadInBackground() {
        status = DOWNLOADING_STATUS;
        Log.d(TAG, status + "start loading");
        try {
            URL url = new URL(this.url);
            URLConnection connection = url.openConnection();
            connection.connect();
            int lengthOfFile = connection.getContentLength();
            Log.d(TAG, String.valueOf(lengthOfFile));
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            OutputStream output = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/" + this.fileName));

            byte data[] = new byte[1024];

            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                progress = (int) ((total * 100) / lengthOfFile);
                output.write(data, 0, count);
                publishStatus();
            }

            output.flush();
            output.close();
            input.close();

        } catch (IOException e) {
            status = ERROR_STATUS;
            Log.e(TAG, e.getMessage());
            publishStatus();
            return null;
        }

        status = DOWNLOADED_STATUS;
        publishStatus();
        return null;
    }

    private void publishStatus() {
        Intent intent = new Intent(ACTION_PROGRESS);
        intent.putExtra(STATUS_EXTRA, status);
        if(status == DOWNLOADING_STATUS) {
            intent.putExtra(PROGRESS_EXTRA, progress);
        }
        context.sendBroadcast(intent);
    }
}