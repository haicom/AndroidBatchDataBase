package com.importdata.providers;




import com.importdata.R;
import com.importdata.transation.DataReceiver;
import com.importdata.transation.DataTaskService;



import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
    Button button;
    Button mButtonQuery;
    private ContentResolver mContentResolver;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button)findViewById(R.id.button);
        mButtonQuery = (Button)findViewById(R.id.query_button);
        button.setOnClickListener(this);
        mButtonQuery.setOnClickListener(this);
        mContentResolver = this.getContentResolver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view == button) {
            sendBroadcast(new Intent(DataTaskService.ACTION_IMPORT_DATA,
                    null,
                    this,
                    DataReceiver.class));
        }
    }
}
