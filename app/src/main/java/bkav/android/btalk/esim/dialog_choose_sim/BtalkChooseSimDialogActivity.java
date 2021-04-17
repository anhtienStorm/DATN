package bkav.android.btalk.esim.dialog_choose_sim;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**Bkav QuangNDb activity cho thang app khac goi vao de hien thi dialog chon sim*/
public class BtalkChooseSimDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String number = getIntent().getStringExtra("number");
        if (number != null && !number.isEmpty()) {
            DialogChooseSimFragment dialogChooseSimFragment = DialogChooseSimFragment.newInstance(number);
            dialogChooseSimFragment.show(getFragmentManager(), "chooseSim");
        }
    }
}
