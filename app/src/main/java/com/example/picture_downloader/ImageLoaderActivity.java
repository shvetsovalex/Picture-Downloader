package com.example.picture_downloader;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.picture_downloader.ImageLoader;

public class ImageLoaderActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Integer> {
    private Loader imageLoader;
    private static int LOADER_IMAGE_ID = 1;
    private Button btnLoad;
    private TextView statusLabel;
    private ProgressBar progressBar;
    private ProgressReceiver progressReceiver;
    private static String url;
    private int status;
    private int downloaded;
    private IntentFilter intentFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLoad = (Button) findViewById(R.id.btnLoad);

        statusLabel = (TextView) findViewById(R.id.status_label);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setVisibility(View.INVISIBLE);

        this.url = getString(R.string.url);

        imageLoader = getSupportLoaderManager().getLoader(LOADER_IMAGE_ID);
        if (imageLoader == null) {
            getSupportLoaderManager().initLoader(LOADER_IMAGE_ID, new Bundle(), ImageLoaderActivity.this);
        }

        if (savedInstanceState != null && savedInstanceState.getInt("downloaded") == 1) {
            downloaded = 1;
            status = ImageLoader.DOWNLOADED_STATUS;
            statusLabel.setText(R.string.status_downloaded);
            btnLoad.setText(R.string.downloaded);
        } else
            status = ImageLoader.IDLE_STATUS;
        progressReceiver = new ProgressReceiver();
        intentFilter = new IntentFilter(ImageLoader.ACTION_PROGRESS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(progressReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(progressReceiver);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        imageLoader = new ImageLoader(this, url, "");
        System.out.println("create loader");
        return imageLoader;
    }

    @Override
    public void onLoadFinished(Loader<Integer> loader, Integer data) {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Integer> loader) {
        System.out.println("onLoaderReset for loader " + loader.hashCode());
    }

    public void loadImageClick(View v) {
        System.out.println("onclick hashcode: ");
        if (status == ImageLoader.IDLE_STATUS) {
            imageLoader.forceLoad();
            downloaded = 1;
            status = ImageLoader.DOWNLOADED_STATUS;
        }
        else if (status == ImageLoader.DOWNLOADED_STATUS) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/pic.jpg"), "image/*");
            startActivity(intent);

        }
    }

    private void updateStatus(int status) {
        switch (status) {
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

                int progress = ((ImageLoader) imageLoader).getProgress();

                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress);
                System.out.println(progress);
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

                imageLoader = getSupportLoaderManager().restartLoader(LOADER_IMAGE_ID, new Bundle(), ImageLoaderActivity.this);
                this.status = ImageLoader.IDLE_STATUS;
            }
            break;
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("downloaded", downloaded);
    }

    class ProgressReceiver extends BroadcastReceiver {
        private int status;
        public ProgressReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            status = intent.getIntExtra("status", status);
            updateStatus(status);
        }

    }
}
