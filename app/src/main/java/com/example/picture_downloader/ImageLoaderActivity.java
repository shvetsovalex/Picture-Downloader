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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.picture_downloader.ImageLoader;

public class ImageLoaderActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Integer> {
    private final String TAG = this.getClass().getName();
    private static final String url = "http://vignette4.wikia.nocookie.net/rio/images/9/91/Rio-2-Official-Trailer-3-40.jpg/revision/latest?cb=20131002062355";
    private static final String DOWNLOADED_STATUS = "downloaded";
    private Loader imageLoader;
    private static int LOADER_IMAGE_ID = 1;
    private Button btnLoad;
    private TextView statusLabel;
    private ProgressBar progressBar;
    private ProgressReceiver progressReceiver;
    private int status;
    private int progress;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAppComponents(savedInstanceState);
        progressReceiver = new ProgressReceiver();
        intentFilter = new IntentFilter(ImageLoader.ACTION_PROGRESS);
        this.registerReceiver(progressReceiver, intentFilter);
    }

    private void initAppComponents(Bundle savedInstanceState) {
        btnLoad = (Button) findViewById(R.id.btnLoad);

        statusLabel = (TextView) findViewById(R.id.status_label);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setVisibility(View.INVISIBLE);

        imageLoader = getSupportLoaderManager().getLoader(LOADER_IMAGE_ID);
        if (imageLoader == null) {
            getSupportLoaderManager().initLoader(LOADER_IMAGE_ID, new Bundle(), ImageLoaderActivity.this);
        }

        if (savedInstanceState != null) {
            status = savedInstanceState.getInt(ImageLoader.STATUS_EXTRA);
            progress = savedInstanceState.getInt(ImageLoader.PROGRESS_EXTRA);
        } else
            status = ImageLoader.IDLE_STATUS;
        updateStatus(status, progress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(progressReceiver);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        imageLoader = new ImageLoader(this, url, "");
        Log.d(TAG, "create loader");
        return imageLoader;
    }

    @Override
    public void onLoadFinished(Loader<Integer> loader, Integer data) {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Integer> loader) {
        Log.d(TAG, "onLoaderReset for loader " + loader.hashCode());
    }

    public void loadImageClick(View v) {
        Log.d(TAG, "onclick hashcode: ");
        if (status == ImageLoader.IDLE_STATUS) {
            imageLoader.forceLoad();
        }
        else if (status == ImageLoader.DOWNLOADED_STATUS) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/pic.jpg"), "image/*");
            startActivity(intent);

        }
    }

    private void updateStatus(int status, int progress) {
        switch (status) {
            case ImageLoader.IDLE_STATUS: {
                btnLoad.setEnabled(true);
                btnLoad.setText(R.string.download);

                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(0);

                statusLabel.setText(R.string.status_idle);
            }
            break;
            case ImageLoader.DOWNLOADING_STATUS: {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress);
                Log.d(TAG, String.valueOf(progress));
                btnLoad.setEnabled(false);

                statusLabel.setText(R.string.status_downloading);
            }
            break;

            case ImageLoader.DOWNLOADED_STATUS: {
                progressBar.setVisibility(View.GONE);

                btnLoad.setEnabled(true);
                btnLoad.setText(R.string.downloaded);

                statusLabel.setText(R.string.status_downloaded);
                this.status = ImageLoader.DOWNLOADED_STATUS;
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
        outState.putInt(ImageLoader.STATUS_EXTRA, status);
        outState.putInt(ImageLoader.PROGRESS_EXTRA, progress);
    }

    class ProgressReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            progress = intent.getIntExtra(ImageLoader.PROGRESS_EXTRA, 0);
            status = intent.getIntExtra(ImageLoader.STATUS_EXTRA, ImageLoader.ERROR_STATUS);
            updateStatus(status, progress);
        }
    }
}
