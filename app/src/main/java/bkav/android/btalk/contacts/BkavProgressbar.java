package bkav.android.btalk.contacts;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import bkav.android.btalk.R;
/**
 * HienDTk: create 14/09/2020: dialog chon sdt
 */
public class BkavProgressbar extends Dialog {
    private Context mContext;

    private ProgressBar mProgressbar;

    private TextView mPercent;

    private TextView mPercentNumber;

    private TextView mPercentTotal;

    private TextView mCurrentFile;
    public BkavProgressbar(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        View view = View.inflate(mContext, R.layout.bkav_dialog_get_all_number, null);
        mPercent = (TextView) view.findViewById(R.id.progress_horizen_percent);
        mPercentNumber = (TextView) view.findViewById(R.id.progress_horizen_percent_number);
        mProgressbar = (ProgressBar) view.findViewById(R.id.progress_horizen_progressbar);
        mPercentTotal = (TextView) view.findViewById(R.id.progress_horizen_percent_total);
        mCurrentFile = (TextView) view.findViewById(R.id.progress_horizen_currentfile);
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
        setContentView(view);
        getWindow().setGravity(Gravity.CENTER_VERTICAL);
        getWindow().setLayout((13 * width) / 14, ActionBar.LayoutParams.WRAP_CONTENT);
    }

    public void setPerCent(String string) {
        mPercent.setText(string);
    }

    public void setPercentNumber(String string) {
        mPercentNumber.setText(string);
    }

    public void setPercentTotal(String string) {
        mPercentTotal.setText(string);
    }

    public void setCurrentFile(String string) {
        mCurrentFile.setText(string);
    }

    public void setProgress(int value) {
        mProgressbar.setProgress(value);
    }

    public void setMaxProgress(int value) {
        mProgressbar.setMax(value);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    @Override
    protected void onStop() {
        dismiss();
        super.onStop();
    }
}
