package bkav.android.btalk.bmsblocked;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import bkav.android.btalk.R;

public class BlockCallsOnlyBmsActivity extends AppCompatActivity implements View.OnClickListener,
        ListItemCallsBlockedAdapter.CallsBlockedAdapterListener {
    private RecyclerView mRecyclerView;
    private TextView mAddCallsBlocked;
    private RecyclerView.LayoutManager mLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_calls_only_bms);
        mRecyclerView = findViewById(R.id.list_calls_blocked);
        mLayoutManager =new LinearLayoutManager(this,LinearLayout.VERTICAL,false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAddCallsBlocked = findViewById(R.id.txt_add_calls_blocks);
        mAddCallsBlocked.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.txt_add_calls_blocks) {
            showDialogAddCallsBlocked();
        }
    }

    @Override
    public void onDeleteCallsBlocked() {
    }

    private void showDialogAddCallsBlocked(){
        AlertDialog.Builder builder = new AlertDialog.Builder(BlockCallsOnlyBmsActivity.this);
        builder.setTitle(R.string.add_number_phone_blocked);
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setPositiveButton(getString(R.string.text_block), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String numberBlock = input.getText().toString();
                BmsUtils.insertNumberBlock(getApplicationContext(), numberBlock);
                Toast.makeText(BlockCallsOnlyBmsActivity.this, getString(R.string.text_block, numberBlock), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}