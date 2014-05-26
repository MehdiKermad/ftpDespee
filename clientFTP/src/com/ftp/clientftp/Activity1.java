package com.ftp.clientftp;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
 
public class Activity1 extends Activity {
 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
 
    	setContentView(R.layout.activity1);
        Toast.makeText(Activity1.this,"azert",Toast.LENGTH_LONG).show();
        
        setResult(4);
        //finish();
    }
 
}