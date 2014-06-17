package com.ftp.clientftp;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
 
public class ListeFonctions extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.listefonctions);
    	
    	TextView fonctionsView = (TextView)findViewById(R.id.listeFonctionsLabel);
    	
		try {
            InputStream in_s = getResources().openRawResource(R.raw.fonctions);

            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            fonctionsView.setText(new String(b));
        } catch (Exception e) {
            e.printStackTrace();
        }

    	setResult(3);
    }
 
}