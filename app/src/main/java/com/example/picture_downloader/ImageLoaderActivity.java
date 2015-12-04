package com.example.picture_downloader;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import com.example.picture_downloader.ImageLoader;

public class ImageLoaderActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Void> {
    private Loader imLoad;
    private static int LOADER_IMAGE_ID = 1;
    private Button btnLoad;
    private TextView statusLabel;
    private ProgressBar progressBar;
    private static String url;
    private Handler handler;
    private long status;
    private int downloaded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                status = msg.what;
                switch (msg.what) {
                    case ImageLoader.IDLE_STATUS: {
                        btnLoad.setEnabled(true);
                        btnLoad.setText(R.string.download);

                        progressBar.setVisibility(View.GONE);
                        progressBar.setMax(ImageLoader.MAX_PROGRESS);
                        progressBar.setProgress(0);

                        statusLabel.setText(R.string.status_idle);
                    }
                    break;
                    case ImageLoader.DOWNLOADING_STATUS: {

                        int progress = ((ImageLoader) imLoad).getProgress();

                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress(progress);

                        btnLoad.setEnabled(false);

                        statusLabel.setText(R.string.status_downloading);
                    }
                    break;

                    case ImageLoader.DOWNLOADED_STATUS: {
                        progressBar.setVisibility(View.GONE);

                        btnLoad.setEnabled(true);
                        btnLoad.setText(R.string.downloaded);

                        statusLabel.setText(R.string.status_downloaded);

                    }
                    break;

                    case ImageLoader.ERROR_STATUS: {
                        Toast.makeText(ImageLoaderActivity.this,
                                R.string.loading_error, Toast.LENGTH_SHORT)
                                .show();

                        imLoad = getSupportLoaderManager().restartLoader(LOADER_IMAGE_ID, new Bundle(), ImageLoaderActivity.this);

                        this.sendEmptyMessage(ImageLoader.IDLE_STATUS);
                    }
                    break;
                }
            }

        };

        btnLoad = (Button)findViewById(R.id.btnLoad);

        statusLabel = (TextView) findViewById(R.id.status_label);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setVisibility(View.INVISIBLE);

        this.url = getString(R.string.url);

        imLoad = getSupportLoaderManager().getLoader(LOADER_IMAGE_ID);
        if (imLoad == null) {
            getSupportLoaderManager().initLoader(LOADER_IMAGE_ID, new Bundle(), ImageLoaderActivity.this);
            ((ImageLoader)imLoad).setHandler(handler);
        }
        else
            ((ImageLoader)imLoad).setHandler(handler);
        if(savedInstanceState != null && savedInstanceState.getInt("downloaded") == 1) {
            status = ImageLoader.DOWNLOADED_STATUS;
            statusLabel.setText(R.string.status_downloaded);
            btnLoad.setText(R.string.downloaded);
        }
        else
            status = ImageLoader.IDLE_STATUS;
        if (imLoad == null) {
            handler.sendEmptyMessage(ImageLoader.ERROR_STATUS);
            btnLoad.setEnabled(false);
            return;
        }

    }


    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        imLoad = new ImageLoader(this, url, "", handler);
        System.out.println("create loader");
        return imLoad;
    }


    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
        System.out.println("onLoadFinished for loader " + loader.hashCode());
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }


    @Override
    public void onLoaderReset(Loader<Void> loader) {
        System.out.println("onLoaderReset for loader " + loader.hashCode());
    }

    public void loadImageClick(View v) {
        System.out.println("onclick hashcode: " + handler.hashCode());
        if(status == ImageLoader.IDLE_STATUS) {
            imLoad.forceLoad();
            downloaded = 1;
        }
        if(status == ImageLoader.DOWNLOADED_STATUS) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/pic.png"), "image/*");
            startActivity(intent);

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("downloaded", downloaded);
    }
}

