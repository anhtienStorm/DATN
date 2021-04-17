package bkav.android.btalk.qrcode;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.Result;

import bkav.android.btalk.R;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class BkavQrCode extends Activity implements ZXingScannerView.ResultHandler {
    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView mScannerView;
    private TextView mInstruction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bkav_qr_code);

        setupView();
        hideStatusBar();
    }

    private void hideStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void setupView() {
        mScannerView = findViewById(R.id.scannerView);
        mInstruction = findViewById(R.id.text_instruction);

        mInstruction.setText(getResources().getString(R.string.bkav_qr_code_instruction));

        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(mInstruction.getLayoutParams());
        marginParams.setMargins(0, (getScreenHeight() * 2) / 3, 0, 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        mInstruction.setLayoutParams(layoutParams);
    }

    private int getScreenHeight() {
//        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics dm = new DisplayMetrics();
//        wm.getDefaultDisplay().getMetrics(dm);

        DisplayManager mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display display = mDisplayManager.getDisplay(Display.DEFAULT_DISPLAY);
        Point size = new Point();
        display.getRealSize(size);
        return size.y;
    }

    private boolean checkPermission() {
        return ( ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA ) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if(mScannerView == null) {
                    mScannerView = new ZXingScannerView(this);
                    setContentView(mScannerView);
                }
                mScannerView.setResultHandler(this);
                mScannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish(); // Bkav HuyNQN dat finish o day de chanh viec chiem camera sau khi thuc hien home ra de su dung app camera
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        final String result = rawResult.getText();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.title_result_qr_code));

        if (URLUtil.isValidUrl(result)) {
            builder.setPositiveButton(getResources().getString(R.string.qr_code_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(result));
                    startActivity(browserIntent);
                }
            });

            builder.setNeutralButton(getResources().getString(R.string.qr_code_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mScannerView.resumeCameraPreview(BkavQrCode.this);
                }
            });

            builder.setMessage(getResources().getString(R.string.qr_code_link) + rawResult.getText()
                    + getResources().getString(R.string.qr_code_how_open));
        } else {
            builder.setPositiveButton(getResources().getString(R.string.qr_code_continue), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mScannerView.resumeCameraPreview(BkavQrCode.this);
                }
            });

            builder.setMessage(rawResult.getText());
        }
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void fixLoiCam(){
        if(mScannerView == null) {
            mScannerView = new ZXingScannerView(getApplicationContext());
            setContentView(mScannerView);
        }
        mScannerView.setResultHandler(BkavQrCode.this::handleResult);
        mScannerView.startCamera();
    }
}
