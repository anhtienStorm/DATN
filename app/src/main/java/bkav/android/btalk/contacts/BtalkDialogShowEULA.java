package bkav.android.btalk.contacts;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import bkav.android.btalk.R;

/**
 * AnhNDd: DialogFragment dùng để hiển thị giao diện chính sách.
 */
public class BtalkDialogShowEULA extends DialogFragment {
    private WebView mWebView;
    private String mEulaUrl;
    private boolean mIsLoadingFinished;
    private boolean mIsRedirect;
    private Button mBtnAccept;

    public static String DIALOG_TAG = "dialog_eula";


    int mNum;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static BtalkDialogShowEULA newInstance(int num) {
        BtalkDialogShowEULA f = new BtalkDialogShowEULA();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.btalk_termsofuse_dialog, container, false);
        mWebView = (WebView) v.findViewById(R.id.wvEula);

        initWebView();
        mBtnAccept = (Button) v.findViewById(R.id.buttonAccept);
        mBtnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return v;
    }

    public void setEulaUrl(String url) {
        mEulaUrl = url;
    }

    public void initWebView() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!mIsLoadingFinished) {
                    mIsRedirect = true;
                }
                mIsLoadingFinished = false;

                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mIsLoadingFinished = false;
                // Show loading here
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!mIsRedirect) {
                    mIsLoadingFinished = true;
                }
                if (mIsLoadingFinished && !mIsRedirect) {
                    // Hide loading

                } else {
                    mIsRedirect = false;
                }
                super.onPageFinished(view, url);
            }

        });
        mWebView.getSettings().setSupportZoom(true);

        mWebView.setInitialScale(90);
        mWebView.loadUrl(mEulaUrl);
    }
}
