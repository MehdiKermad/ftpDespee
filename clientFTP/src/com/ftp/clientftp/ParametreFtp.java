package com.ftp.clientftp;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
 
public class ParametreFtp extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.parametreftp);
    	
    	Intent intent = getIntent();
    	
    	EditText adresseFtpEdit = (EditText)findViewById(R.id.adresseFtpEditText);
    	adresseFtpEdit.setText(intent.getStringExtra("hote"));
    	
    	EditText loginFtpEdit = (EditText)findViewById(R.id.loginFtpEditText);
    	loginFtpEdit.setText(intent.getStringExtra("login"));
    	
    	EditText passFtpEdit = (EditText)findViewById(R.id.passFtpEditText);
    	passFtpEdit.setText(intent.getStringExtra("pass"));
    	
    	CheckBox modeBinaire = (CheckBox)findViewById(R.id.modeBinaireCheckBox);
    	if(intent.getStringExtra("mode").equals("I"))
    		modeBinaire.setChecked(true);
    	else
    		modeBinaire.setChecked(false);
    	
    	CheckBox modeAnonyme = (CheckBox)findViewById(R.id.modeAnonymeCheckBox);
    	if(intent.getStringExtra("login").equals("anonymous") && intent.getStringExtra("pass").equals("anonymous"))
    		modeAnonyme.setChecked(true);
    	else
    		modeAnonyme.setChecked(false);
    	
    }
    
    public void validerParam(View v) {
    	EditText champ;
    	Intent output = new Intent();
    	
    	champ = (EditText)findViewById(R.id.adresseFtpEditText);
    	output.putExtra("hote",champ.getText().toString());
    	
    	CheckBox checkBox = (CheckBox)findViewById(R.id.modeBinaireCheckBox);
    	if(checkBox.isChecked())
    		output.putExtra("mode", "I");
    	else
    		output.putExtra("mode", "A");
    	
    	checkBox = (CheckBox)findViewById(R.id.modeAnonymeCheckBox);
    	if(checkBox.isChecked())
    	{
    		output.putExtra("login", "anonymous");
	    	output.putExtra("pass", "anonymous");
    	}
    	else
    	{
    		champ = (EditText)findViewById(R.id.loginFtpEditText);
    		output.putExtra("login", champ.getText().toString());
    		champ = (EditText)findViewById(R.id.passFtpEditText);
	    	output.putExtra("pass", champ.getText().toString());
    	}
    	
    	setResult(1,output);
    	finish();
    }
 
}