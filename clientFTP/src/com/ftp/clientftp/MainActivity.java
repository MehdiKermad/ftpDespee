package com.ftp.clientftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
	private List<String> listeRecu = null;
	private ConnectivityManager connManager = null;
	private NetworkInfo mWifi = null;
	private String nomFichier = null;
	private String pathFichier = null;
	private String pathParent = null;
	private String adresseFtp = "ftpperso.free.fr";
	private String loginFtp = "";
	private String passFtp = "";
	private String modeFtp = "I";
	private String ipRecu = "";
	private int portRecu = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ecranlancement); //ecran de lancement
		
		SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0); //preparation du son
		int sound = soundPool.load(this, R.raw.sonlancement, 1);
		
		soundPool.play(sound, 1, 1, 0, 0, 1);
		
		MediaPlayer mPlayer = MediaPlayer.create(getBaseContext(), R.raw.sonlancement); //lancement du son
		mPlayer.start();
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		    @Override
		    public void run() {
		    	
			setContentView(R.layout.activity_main);
			
			//on regarde l'etat de la connexion
			connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		    mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			
		    if (!mWifi.isConnected())
		        erreurToast("Veuillez vous connecter à un réseau Wi-Fi");
		    
		    //chargement des parametres FTP depuis le registre
		    
		    SharedPreferences prefs = getPreferences(MODE_PRIVATE); 
		    String restoredText = prefs.getString("hote", null);
		    if (restoredText != null) 
		    {
		      adresseFtp = prefs.getString("hote", adresseFtp);
		      loginFtp = prefs.getString("login", loginFtp);
		      passFtp = prefs.getString("pass", passFtp);
		      modeFtp = prefs.getString("mode", modeFtp);
		    }
		    
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
		    			  nomFichier=rep.getName();
		    			  rep = new File(rep.getParent());
		    			  pathParent=rep.getAbsolutePath();
		    			  erreurToast(pathFichier);
		    			  quit=false;
		    			  openContextMenu(view);
		    			  
		    		  }
		    	  }
		    	});
		    
		    registerForContextMenu(liste); //on associe la menu context à notre liste
		    }
		}, 2000);
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
	            
	            rep2 = new File(pathParent+"/"+nomsFichiers[position]);
	            TextView textView=(TextView) view.findViewById(android.R.id.text1);
	            textView.setTextColor(Color.WHITE); //couleur des éléments
	            
	            if(rep2.isFile())
	            {
	            	textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.fichier, 0, 0, 0);
	            	
	            }
	            else
	            {
	            	textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dossier, 0, 0, 0);
	            }

	            return view;
	        }
	    };
	    
	    liste.setAdapter(adapter);
	}
	
	public void ouvertureFichier(){
		
		rep = new File(pathFichier);
		Uri uri = Uri.fromFile(rep);
        
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (pathFichier.contains(".doc") || pathFichier.contains(".docx"))
        {
            intent.setDataAndType(uri, "application/msword");
        }
        else if(pathFichier.contains(".pdf"))
        {
            intent.setDataAndType(uri, "application/pdf");
        }
        else if(pathFichier.contains(".ppt") || pathFichier.contains(".pptx"))
        {
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        }
        else if(pathFichier.contains(".xls") || pathFichier.contains(".xlsx"))
        {
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        }
        else if(pathFichier.contains(".wav"))
        {
            intent.setDataAndType(uri, "application/x-wav");
        }
        else if(pathFichier.contains(".rtf"))
        {
            intent.setDataAndType(uri, "application/rtf");
        }
        else if(pathFichier.contains(".wav") || pathFichier.contains(".mp3"))
        {
            intent.setDataAndType(uri, "audio/x-wav");
        }
        else if(pathFichier.contains(".gif"))
        {
            intent.setDataAndType(uri, "image/gif");
        }
        else if(pathFichier.contains(".jpg") || pathFichier.contains(".jpeg") || pathFichier.contains(".png"))
        {
            intent.setDataAndType(uri, "image/jpeg");
        }
        else if(pathFichier.contains(".txt"))
        {
            intent.setDataAndType(uri, "text/plain");
        }
        else if(pathFichier.contains(".3gp") || pathFichier.contains(".mpg") || pathFichier.contains(".mpeg") || pathFichier.contains(".mpe") || pathFichier.contains(".mp4") || pathFichier.contains(".avi"))
        {
            intent.setDataAndType(uri, "video/*");
        }
        
        startActivity(intent);
        rep = new File(rep.getParent());
	}
	
	public void creerListeFtp(){
		
		liste = (ListView) findViewById(R.id.listView1); //on créer la liste qui sera affichée
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listeRecu){

	        @Override
	        public View getView(int position, View convertView,ViewGroup parent) {
	            View view = super.getView(position, convertView, parent);
	            
	            TextView textView=(TextView) view.findViewById(android.R.id.text1);
	            textView.setTextColor(Color.WHITE); //couleur des éléments
	            
	            String res = listeRecu.get(position);
	            
	            if(res.lastIndexOf(".")!=-1 && res.indexOf(".")==res.lastIndexOf(".")) //on verifie si on a un fichier ou un repertoire
	            {
	            	textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.fichier, 0, 0, 0);	
	            }
	            else
	            {
	            	textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dossier, 0, 0, 0);
	            }
	            
	            return view;
	        }
	    };
	    
	    liste.setAdapter(adapter);
	}
	
	public void erreurToast(String msg){
		Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
	}
	
	public class upload extends Thread {
		 
		private Socket socketControle;
		private Socket socketDonnee;
	    private BufferedReader myReader1;
	    private PrintWriter myWriter1;
	    private OutputStream myOutput;
	    private File rep3;
	    public String resultat = "";
	    
		  public upload() {}
		  
		  public void run() {
				try {
					Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
					
					socketControle = new Socket(adresseFtp, 21);
					 
					myReader1 = new BufferedReader(new InputStreamReader(socketControle.getInputStream()));
					myWriter1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketControle.getOutputStream())),true);
				
					
					String messageServeur = myReader1.readLine();
					Log.e(messageServeur,"lol");
					
					String operation="";
					resultat="";
					
					// envoi login
					operation = "USER "+loginFtp;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					Log.e(resultat,"lol");
					
					// envoi pass
					operation = "PASS "+passFtp;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					Log.e(resultat,"lol");
					
					// envoi
					operation = "CWD upload"; //on ouvre le répertoire dédié sur le serveur
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					Log.e(resultat,"lol");
					
					// envoi
					operation = "TYPE "+modeFtp;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					Log.e(resultat,"lol");
					
					// mode passif
					operation = "PASV";
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					Log.e(resultat,"lol");
					
					//traitement de la chaine de caractère
					String a = resultat;
					a=a.substring(a.indexOf("(",0)+1);
					String str[]=a.split(",");
					str[5]=str[5].substring(0,str[5].indexOf(").",0));
					
					ipRecu="";
					
					for(int cpt=0;cpt<4;cpt++)
					{
						ipRecu+=str[cpt];
						if(cpt<3)
							ipRecu+=".";
					}
					
					portRecu=Integer.parseInt(str[4])*256;
					portRecu+=Integer.parseInt(str[5]);
					
					// mode passif
					operation = "STOR "+nomFichier;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					Log.e(resultat,"lol");
					
					//envoi des données
					
					socketDonnee = new Socket(ipRecu, portRecu);
					 
					myOutput = socketDonnee.getOutputStream();

					// envoi
					rep3 = new File(pathFichier);
					FileInputStream fis=new FileInputStream(rep3);
					byte fileContent[] = new byte[(int)rep.length()];

					int n=0;
			        while((n=fis.read(fileContent))!=-1)
			        {
			        	myOutput.write(fileContent,0,n);
			        }
			        
			        myOutput.flush();
					Log.e("ok","lol");
					
					socketDonnee.close();
					
					resultat = myReader1.readLine();
					Log.e(resultat,"lol");

					fis.close();
					
					// déconnexion
					operation = "QUIT";
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					Log.e(resultat,"lol");
					
					socketControle.close();
					
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("Erreur socket","lol");
				}

		  }
		}
	
	@Override  
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
    super.onCreateContextMenu(menu, v, menuInfo);   
    	menu.add(0, v.getId(), 0, "Ouvrir");
        menu.add(0, v.getId(), 0, "Envoyer"); 
        menu.add(0, v.getId(), 0, "Renommer"); 
        menu.add(0, v.getId(), 0, "Supprimer");
    }  
  
    @Override  
    public boolean onContextItemSelected(MenuItem item) {
    	if(item.getTitle().equals("Ouvrir")){
    		
    		erreurToast(pathFichier);
    		ouvertureFichier();
    	}
        if(item.getTitle().equals("Envoyer")){
        	
        	mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			
		    if (!mWifi.isConnected())
		    {
		        erreurToast("Veuillez vous connecter à un réseau Wi-Fi");
		    }
		    else
		    {
	        	upload a = new upload();
	    		a.start();
	    		
	    		creerListe(pathParent);
		    }
        }  
        else if(item.getTitle().equals("Renommer")){
        	
        	boiteDialogRenommerFichier();
        }
        else if(item.getTitle().equals("Supprimer")){
        	
        	erreurToast("Le fichier a été supprimé");

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
      	case R.id.explorer:
  		  Intent intent2 = new Intent(MainActivity.this, ExplorerFtp.class); //on démarre l'explorateur
   		  intent2.putExtra("hote",adresseFtp);
   		  intent2.putExtra("login",loginFtp);
   		  intent2.putExtra("pass",passFtp);
   		  intent2.putExtra("mode",modeFtp);
    	  startActivityForResult(intent2, 2);
    	  return true;
      
      	case R.id.creerFichier:
        	boiteDialogNewFichier();
        	return true;
          
        case R.id.creerRepertoire:
        	boiteDialogNewDossier();
        	return true;
        	
        case R.id.parametreFtp:
        	Intent intent = new Intent(MainActivity.this, ParametreFtp.class);
        	intent.putExtra("hote",adresseFtp);
        	intent.putExtra("login",loginFtp);
        	intent.putExtra("pass",passFtp);
        	intent.putExtra("mode",modeFtp);
        	startActivityForResult(intent, 1);
        	return true;
        	
        case R.id.listeFonctions:
        	Intent intent3 = new Intent(MainActivity.this, ListeFonctions.class);
     		startActivityForResult(intent3, 3);
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
			    	erreurToast("Appuyer une 2ème fois pour quitter");
		    	}
		    }
		    return true;
		 }
		return super.onKeyDown(keyCode, event);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==1) //on récupère les paramètres FTP
		{
			adresseFtp=data.getStringExtra("hote");
			loginFtp=data.getStringExtra("login");
			passFtp=data.getStringExtra("pass");
			modeFtp=data.getStringExtra("mode");
			
			//on enregistre les parametres dans le registre du tel
			SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
			editor.putString("hote", adresseFtp);
			editor.putString("login", loginFtp);
			editor.putString("pass", passFtp);
			editor.putString("mode", modeFtp);
			editor.commit();
			 
			erreurToast("Vos modifications ont été prises en compte");
		}
		if(resultCode==2) //en quittant le serveur
		{	
			creerListe(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
		}
		if(resultCode==3) //en quittant les fonctionnalités
		{	
			erreurToast("Mehdi Kermad");
		}
	}

}
