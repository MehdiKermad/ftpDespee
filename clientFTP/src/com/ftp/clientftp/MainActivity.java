package com.ftp.clientftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private ListView liste = null;
	private File rep = null;
	private File rep2 = null;
	private String[] nomsFichiers = null;
	private List<String> nomsListe = null;
	private ArrayAdapter<String> adapter = null;
	private boolean quit = false;
	private String pathFichier = null;
	private String pathParent = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//on actualise la liste par rapport au repertoire courant
		creerListe(Environment.getExternalStorageDirectory().toString());
	    
	    //on paramètre les click courts
	    liste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	  @Override
	    	  public void onItemClick(AdapterView<?> adapterView, View view, int position,long id) {
	    		  
	    		  String nomClick = liste.getItemAtPosition((int)id).toString();
    			  rep = new File(rep.getPath()+"/"+nomClick);
	    		  
	    		  if(rep.isDirectory()) //si c'est un repertoire,on l'ouvre
	    		  {
	    			  creerListe(rep.getPath());
	    			  quit=false;
	    			  
	    		  }
	    		  else //si c'est un fichier,on ouvre le menu contextuel
	    		  {
	    			  pathFichier=rep.getAbsolutePath(); //on récupère le chemin du fichier dans le cas d'une opération
	    			  rep = new File(rep.getParent());
	    			  pathParent=rep.getAbsolutePath();
	    			  quit=false;
	    			  openContextMenu(view);
	    			  
	    		  }
	    	  }
	    	});
	    
	    registerForContextMenu(liste); //on associe la menu context à notre liste
	}
	
	public void boiteDialogRenommerFichier()
	{
		// Récupération des ressources
		LayoutInflater inflater= LayoutInflater.from(this);
		final View textEntryView = inflater.inflate(R.layout.dialog1, null);

		// Création du builder
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("Saisissez le nouveau nom");
		dialogBuilder.setView(textEntryView);

		// Ajout du gestionnaire d'événements
		dialogBuilder.setPositiveButton("Ok",
		 new DialogInterface.OnClickListener() {
		   @Override
		   public void onClick(DialogInterface dialog, int which) {
			   	 EditText champ = (EditText)textEntryView.findViewById(R.id.EditText1);
			     File newNom = new File(rep.getPath()+"/"+champ.getText().toString());
			     rep = new File(pathFichier);
			     rep.renameTo(newNom);
			     rep = new File(rep.getParent());
			     creerListe(rep.getPath()); //on actualise la liste
		   }
		});
		dialogBuilder.setNegativeButton("Annuler", null);
		
		dialogBuilder.show();
	}
	
	public void boiteDialogNewDossier()
	{
		// Récupération des ressources
		LayoutInflater inflater= LayoutInflater.from(this);
		final View textEntryView = inflater.inflate(R.layout.dialog1, null);

		// Création du builder
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("Saisissez le nom du répertoire");
		dialogBuilder.setView(textEntryView);

		// Ajout du gestionnaire d'événements
		dialogBuilder.setPositiveButton("Ok",
		 new DialogInterface.OnClickListener() {
		   @Override
		   public void onClick(DialogInterface dialog, int which) {
			    EditText champ = (EditText)textEntryView.findViewById(R.id.EditText1);
			     
		     	rep = new File(rep.getPath()+"/"+champ.getText().toString());
	        	rep.mkdir();
	        	rep = new File(rep.getParent());
		     	
			    creerListe(rep.getPath()); //on actualise la liste
		   }
		});
		dialogBuilder.setNegativeButton("Annuler", null);
		
		dialogBuilder.show();
	}
	
	public void boiteDialogNewFichier()
	{
		// Récupération des ressources
		LayoutInflater inflater= LayoutInflater.from(this);
		final View textEntryView = inflater.inflate(R.layout.dialog1, null);

		// Création du builder
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("Saisissez le nom du fichier à créer");
		dialogBuilder.setView(textEntryView);

		// Ajout du gestionnaire d'événements
		dialogBuilder.setPositiveButton("Ok",
		 new DialogInterface.OnClickListener() {
		   @Override
		   public void onClick(DialogInterface dialog, int which) {
			    EditText champ = (EditText)textEntryView.findViewById(R.id.EditText1);
			    
	        	rep = new File(rep.getPath()+"/"+champ.getText().toString());
	        	try {
					rep.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        	rep = new File(rep.getParent());
		     	
			    creerListe(rep.getPath()); //on actualise la liste
		   }
		});
		dialogBuilder.setNegativeButton("Annuler", null);
		
		dialogBuilder.show();
	}
	
	public void creerListe(String chemin){
		//on ouvre le répertoire courant
		
		rep = new File(chemin);
		pathParent = rep.getPath();
		nomsFichiers = rep.list();
		
		liste = (ListView) findViewById(R.id.listView1); //on créer la liste qui sera affichée
	    nomsListe = new ArrayList<String>();
		
		for(int i=0;i<nomsFichiers.length;i++) //on affiche tout les fichiers du répertoire courant
		{
			nomsListe.add(nomsFichiers[i]);
		}
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nomsListe){

	        @Override
	        public View getView(int position, View convertView,ViewGroup parent) {
	            View view = super.getView(position, convertView, parent);
	            //TODO
	            
	            rep2 = new File(pathParent+"/"+nomsFichiers[position]);
	            
	            TextView textView=(TextView) view.findViewById(android.R.id.text1);
	            
	            if(rep2.isFile())
	            {
	            	textView.setTextColor(Color.BLACK); //couleur des fichiers
	            	
	            }
	            else
	            {
	            	textView.setTextColor(Color.BLUE); //couleur des dossiers
	            }

	            return view;
	        }
	    };
	    
	    liste.setAdapter(adapter);
	}  
	
	public void envoiFTP(String filePath){
		
		ftpThr a = new ftpThr();

		a.start();
		Toast.makeText(this,a.resultat,Toast.LENGTH_SHORT).show();
	}
	
	public class ftpThr extends Thread {
		 
		private Socket mySocket;
	    private BufferedReader myReader;
	    private PrintWriter myWriter;
	    public String resultat = "";
	    
		  public ftpThr() {}
		  
		  public void run() {
				try {
					mySocket = new Socket("shinichi93.free.fr", 21);
					Log.e("Demande de connexion","lol");
					 
					myReader = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
					myWriter = new PrintWriter(mySocket.getOutputStream());
					String messageServeur = myReader.readLine();
					Log.e(messageServeur,"lol");
					
					String operation="";
					resultat="";
					
					// envoi login
					operation = "USER shinichi93";
					myWriter.println(operation); //envoi du msg
					myWriter.flush();
					resultat = myReader.readLine();
					Log.e(resultat,"lol");
					
					// envoi pass
					operation = "PASS iDfjOR54";
					myWriter.println(operation); //envoi du msg
					myWriter.flush();
					resultat = myReader.readLine();
					Log.e(resultat,"lol");
					
					// mode passif
					operation = "PASV";
					myWriter.println(operation); //envoi du msg
					myWriter.flush();
					resultat = myReader.readLine();
					Log.e(resultat,"lol");
					
					// envoi
					operation = "CWD /";
					myWriter.println(operation); //envoi du msg
					myWriter.flush();
					resultat = myReader.readLine();
					Log.e(resultat,"lol");
					
					// envoi
					operation = "TYPE A";
					myWriter.println(operation); //envoi du msg
					myWriter.flush();
					resultat = myReader.readLine();
					Log.e(resultat,"lol");
					
					// envoi
					operation = "STOR ueventd.rc";
					myWriter.println(operation); //envoi du msg
					myWriter.flush();
					resultat = myReader.readLine();
					Log.e(resultat,"lol");
					
					mySocket.close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("Erreur socket","lol");
				}

		  }
		}
	
	@Override  
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
    super.onCreateContextMenu(menu, v, menuInfo);   
        menu.add(0, v.getId(), 0, "Envoyer"); 
        menu.add(0, v.getId(), 0, "Renommer"); 
        menu.add(0, v.getId(), 0, "Supprimer");
    }  
  
    @Override  
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals("Envoyer")){
        	
        	Toast.makeText(this,pathFichier,Toast.LENGTH_SHORT).show();
        	envoiFTP(pathFichier);
        }  
        else if(item.getTitle().equals("Renommer")){
        	
        	Toast.makeText(this,"Renommer",Toast.LENGTH_SHORT).show();
        	boiteDialogRenommerFichier();
        	
        }
        else if(item.getTitle().equals("Supprimer")){
        	
        	Toast.makeText(this,"Supprimer",Toast.LENGTH_SHORT).show();

        	//on supprime le fichier
        	rep = new File(pathFichier);
        	rep.delete();
        	
        	rep = new File(pathParent);
        	creerListe(rep.getPath()); //on actualise la liste
        }
        else {
        	
        	return false;
        }  
    return true;  
    }  
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      
      super.onCreateOptionsMenu(menu); //on créer l'actionBar (bouton Menu)
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.main, menu);
      
      return true;
    }
    
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
      switch(item.getItemId())
      {
        case R.id.creerFichier:
        	boiteDialogNewFichier();
        	return true;
          
        case R.id.creerRepertoire:
        	boiteDialogNewDossier();
        	return true;
        	
        case R.id.quitter:
        	finish();
            return true;
      }
      return super.onOptionsItemSelected(item);
    }
    
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		//touche retour  
		if(keyCode == KeyEvent.KEYCODE_BACK) {
		    if(!rep.getPath().equals("/")) //on remonte si c'est possible
		    {
				rep = new File(rep.getParent());
			    creerListe(rep.getPath());
		    }
		    else
		    {
		    	if(quit==true) //si on appuie 2 fois sur retour,on ferme l'appli
		    	{
		    		finish();
		    	}
		    	else
		    	{
			    	quit=true;
			    	Toast.makeText(MainActivity.this,"Appuyer une 2ème fois pour quitter",Toast.LENGTH_SHORT).show();
		    	}
		    }
		    return true;
		 }
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
