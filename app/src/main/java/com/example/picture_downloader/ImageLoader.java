package com.example.picture_downloader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.AsyncTaskLoader;

public class ImageLoader extends AsyncTaskLoader<Void> {
    protected final static int DOWNLOADING_STATUS = 0;
    protected final static int DOWNLOADED_STATUS = 1;
    protected final static int IDLE_STATUS = 2;
    protected final static int ERROR_STATUS = 3;
    protected final static int MAX_PROGRESS = 100;

    protected final static String fileName = "pic.png";

    private String url;
    private int progress;
    private int status;
    private Handler handler;
    private String file_path;


    public ImageLoader(Context context) {
        super(context);
    }

    public ImageLoader(Context context, String url, String file_path, Handler handler) {
        super(context);
        this.url = url;
        this.file_path = file_path;
        status = IDLE_STATUS;
        this.handler = handler;
    }

    @Override
    public Void loadInBackground() {
        status = DOWNLOADING_STATUS;

        try {
            URL url = new URL(this.url);
            URLConnection connection = url.openConnection();
            connection.connect();
            int lengthOfFile = connection.getContentLength();
            System.out.println(lengthOfFile);
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            OutputStream output = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/" + this.fileName));

            byte data[] = new byte[1024];

            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                progress = (int)((total*100)/lengthOfFile);
                output.write(data, 0, count);
                publishProgress();
            }

            output.flush();
            output.close();
            input.close();

        } catch (IOException e) {
            handler.sendEmptyMessage(ERROR_STATUS);
            return null;
        }

        handler.sendEmptyMessage(DOWNLOADED_STATUS);
        status = DOWNLOADED_STATUS;
        return null;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public int getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }

    private void publishProgress() {
        handler.sendEmptyMessage(DOWNLOADING_STATUS);
    }
}