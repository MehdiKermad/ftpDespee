package com.ftp.clientftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ExplorerFtp extends Activity {
	private ListView liste = null;
	private ArrayAdapter<String> adapter = null;
	private boolean quit = false;
	private List<String> listeRecu = null;
	private List<String> isFileRecu = null;
	private String fichierDemande = null;
	private String adresseFtp = "ftpperso.free.fr";
	private String loginFtp = "";
	private String passFtp = "";
	private String modeFtp = "I";
	private String ipRecu = "";
	private String repOuvrir = "/";
	private String repCourant = "/";
	private String commande = "";
	private String aCreer = "";
	private String newNom = "";
	private int portRecu = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
	    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

	    if (!mWifi.isConnected())
	    {
	        erreurToast("Veuillez vous connecter à un réseau Wi-Fi");
	        finish();
	    }
		
		//on charge les paramètres FTP
		
		Intent intent = getIntent();
		if(intent.getStringExtra("hote")!=null) //si on vient de la 1ere activité
		{
			adresseFtp = intent.getStringExtra("hote");
			loginFtp = intent.getStringExtra("login");
			passFtp = intent.getStringExtra("pass");
			modeFtp = intent.getStringExtra("mode");
		}
		else //si on intercepte un lien
		{
			String url = intent.getDataString();
			url = url.replace("ftp://","");
			
			loginFtp = url.substring(0,url.indexOf(":"));
			url = url.substring(url.indexOf(":")+1);

			passFtp = url.substring(0,url.indexOf("@"));
			url = url.substring(url.indexOf("@")+1);
			
			adresseFtp = url.substring(0,url.indexOf("/"));
		}
		
		
		//creation de la liste
	    creerListeFtp(repOuvrir);
	    
	    //on paramètre les click courts
	    liste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	  @Override
	    	  public void onItemClick(AdapterView<?> adapterView, View view, int position,long id) {
	    		  
	    		  String nomClick = liste.getItemAtPosition((int)id).toString();
	    		  
	    		  if(!isFileRecu.get(position).toString().equals("d")) //si c'est un fichier
	    		  {
	    			  fichierDemande = nomClick;
	    			  quit=false;
	    			  openContextMenu(view);
	    			  
	    		  }
	    		  else //si c'est un dossier
	    		  {
	    			  repOuvrir = nomClick;
	    			  creerListeFtp(repOuvrir);
	    			  erreurToast(repCourant);
	    			  quit=false;
	    			  
	    		  }
	    	  }
	    	});
	    
	    registerForContextMenu(liste); //on associe le menu contextuel à notre liste
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
			     
			   	 commande="RNFR ";
			   	 newNom=champ.getText().toString();
			   	 gestion g = new gestion();
			   	 g.start();
			    
			   	 while(!g.getState().toString().equals("TERMINATED")){}
			    
			     erreurToast(fichierDemande+" renommé en "+newNom);
			   	 
			   	 creerListeFtp(repCourant);
			   	 aCreer="";
			   	 commande="";
			   	 
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
			    
	        	aCreer=champ.getText().toString();
	        	upload f = new upload();
			    f.start();
			    
			    while(!f.getState().toString().equals("TERMINATED")){}
			    
			    erreurToast("Fichier créé avec succès");
			    creerListeFtp(repCourant);
			    aCreer="";
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
		     	
			    aCreer=champ.getText().toString();
			    commande="MKD ";
			    gestion e = new gestion();
			    e.start();
			    
			    while(!e.getState().toString().equals("TERMINATED")){}
			    
			    erreurToast("Répertoire créé avec succès");
			    creerListeFtp(repCourant);
			    aCreer="";
			    commande="";
		   }
		});
		dialogBuilder.setNegativeButton("Annuler", null);
		
		dialogBuilder.show();
	}
	
	public void creerListeFtp(String ouvrir){
		
		repOuvrir = ouvrir;
		listage c = new listage();
	    c.start();
	  
	    while(!c.getState().toString().equals("TERMINATED")){} //on attend d'avoir tout recu
	    
		liste = (ListView) findViewById(R.id.listView1); //on créer la liste qui sera affichée
			
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listeRecu){

	        @Override
	        public View getView(int position, View convertView,ViewGroup parent) {
	            View view = super.getView(position, convertView, parent);
	            
	            TextView textView=(TextView) view.findViewById(android.R.id.text1);
	            textView.setTextColor(Color.WHITE); //couleur des éléments
	            
	            if(listeRecu.size()>1)
	            {
		            if(!isFileRecu.get(position).toString().equals("d")) //on verifie si on a un fichier ou un repertoire
		            {
		            	textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.fichier, 0, 0, 0);	
		            }
		            else
		            {
		            	textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dossier, 0, 0, 0);
		            }
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
	    public String resultat = "";
	    
		  public upload() {}
		  
		  public void run() {
				try {
					Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
					
					socketControle = new Socket(adresseFtp, 21);
					 
					myReader1 = new BufferedReader(new InputStreamReader(socketControle.getInputStream()));
					myWriter1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketControle.getOutputStream())),true);
				
					
					String messageServeur = myReader1.readLine();
					//Log.e(messageServeur,"lol");
					
					String operation="";
					resultat="";
					
					// envoi login
					operation = "USER "+loginFtp;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					// envoi pass
					operation = "PASS "+passFtp;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					// envoi
					operation = "CWD "+repCourant; //on ouvre le répertoire dédié sur le serveur
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					// envoi
					operation = "TYPE "+modeFtp;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					// mode passif
					operation = "PASV";
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					//traitement de la chaine de caractère
					String a = resultat;
					a=a.substring(a.indexOf("(",0)+1);
					String str[]=a.split(",");
					str[5]=str[5].substring(0,str[5].indexOf(").",0));
					
					for(int cpt=0;cpt<4;cpt++)
					{
						ipRecu+=str[cpt];
						if(cpt<3)
							ipRecu+=".";
					}
					
					portRecu=Integer.parseInt(str[4])*256;
					portRecu+=Integer.parseInt(str[5]);
					
					// mode passif
					operation = "STOR "+aCreer;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					//envoi des données
					
					socketDonnee = new Socket(ipRecu, portRecu);
					 
					myOutput = socketDonnee.getOutputStream();

					// envoi
					byte fileContent[] = new byte[1];
					fileContent[0]='\0';
					
			        myOutput.write(fileContent);

			        myOutput.flush();
					//Log.e("ok","lol");
					
					// mode passif
					operation = "QUIT";
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					socketDonnee.close();
					socketControle.close();
					
				} catch (IOException e) {
					e.printStackTrace();
					//Log.e("Erreur socket","lol");
				}

		  }
		}
	
	public class download extends Thread {
		 
		private Socket socketControle;
		private Socket socketDonnee;
	    private BufferedReader myReader1;
	    private PrintWriter myWriter1;
	    private InputStream myInput;
	    public String resultat = "";
	    
		  public download() {}
		  
		  public void run() {
				try {
					Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
					
					socketControle = new Socket(adresseFtp, 21);
					 
					myReader1 = new BufferedReader(new InputStreamReader(socketControle.getInputStream()));
					myWriter1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketControle.getOutputStream())),true);
				
					
					String messageServeur = myReader1.readLine();
					//Log.e(messageServeur,"lol");
					
					String operation="";
					resultat="";
					
					// envoi login
					operation = "USER "+loginFtp;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					// envoi pass
					operation = "PASS "+passFtp;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					// envoi
					operation = "CWD "+repCourant;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					// envoi
					operation = "TYPE "+modeFtp;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					// mode passif
					operation = "PASV";
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					//traitement de la chaine de caractère
					String a = resultat;
					a=a.substring(a.indexOf("(",0)+1);
					String str[]=a.split(",");
					str[5]=str[5].substring(0,str[5].indexOf(").",0));
					
					for(int cpt=0;cpt<4;cpt++)
					{
						ipRecu+=str[cpt];
						if(cpt<3)
							ipRecu+=".";
					}
					
					portRecu=Integer.parseInt(str[4])*256;
					portRecu+=Integer.parseInt(str[5]);
					
					// taille du fichier
					operation = "SIZE "+fichierDemande;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");

					// mode passif
					operation = "RETR "+fichierDemande;
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					//reception des données
					
					socketDonnee = new Socket(ipRecu, portRecu);
					 
					myInput = socketDonnee.getInputStream();
					
					FileOutputStream fis = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+fichierDemande.substring(0,fichierDemande.length()-1));
					byte fileContent[] = new byte[1];
					
					int n=0;
					while((n=myInput.read(fileContent))!=-1)
						fis.write(fileContent);
					//Log.e("ok ","lol");
					
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					fis.close();
					
					// mode passif
					operation = "QUIT";
					myWriter1.println(operation); //envoi du msg
					myWriter1.flush();
					resultat = myReader1.readLine();
					//Log.e(resultat,"lol");
					
					socketDonnee.close();
					socketControle.close();
					
				} catch (IOException e) {
					e.printStackTrace();
					//Log.e("Erreur socket","lol");
				}

		  }
		}
	
	public class listage extends Thread {
	      
		  private Socket socketControle;
		  private Socket socketDonnee;
		  private BufferedReader myReader1;
		  private PrintWriter myWriter1;
		  private String isFile = "";
		
		  public listage() {}
		  
		  public void run() {
  			try {
				
				socketControle = new Socket(adresseFtp, 21);
				 
				myReader1 = new BufferedReader(new InputStreamReader(socketControle.getInputStream()));
				myWriter1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketControle.getOutputStream())),true);
				
				String messageServeur = myReader1.readLine();
				//System.out.println(messageServeur);
				
				String operation="";
				String resultat="";
				
				// envoi login
				operation = "USER "+loginFtp;
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				resultat = myReader1.readLine();
				//System.out.println(resultat);
				
				// envoi pass
				operation = "PASS "+passFtp;
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				resultat = myReader1.readLine();
				//System.out.println(resultat);
				
				// changement de repertoire
				operation = "CWD "+repOuvrir;
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				resultat = myReader1.readLine();
				//System.out.println(resultat);
				
				// verification de repertoire
				operation = "PWD";
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				resultat = myReader1.readLine();
				////System.out.println(resultat);
				
				//on enregistre le repertoire courant
				repCourant = resultat.substring(resultat.indexOf("\"")+1,resultat.lastIndexOf("\""));
				
				// changement du mode
				operation = "TYPE "+modeFtp;
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				resultat = myReader1.readLine();
				////System.out.println(resultat);
				
				// mode passif
				operation = "PASV";
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				resultat = myReader1.readLine();
				////System.out.println(resultat);
				
				//traitement de la chaine de caractère
				String a = resultat;
				a=a.substring(a.indexOf("(",0)+1);
				String str[]=a.split(",");
				str[5]=str[5].substring(0,str[5].indexOf(").",0));
				String ipRecu="";
				int portRecu=0;
				
				for(int cpt=0;cpt<4;cpt++)
				{
					ipRecu+=str[cpt];
					if(cpt<3)
						ipRecu+=".";
				}
				
				portRecu=Integer.parseInt(str[4])*256;
				portRecu+=Integer.parseInt(str[5]);
				
				// recuperation de la liste
				operation = "LIST";
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				
				//reception des données
				
				socketDonnee = new Socket(ipRecu, portRecu);
				 
				InputStream myInput = socketDonnee.getInputStream();

				// envoi
				ByteArrayOutputStream fis=new ByteArrayOutputStream();
				byte fileContent[] = new byte[1];
				String res="";
				listeRecu = new ArrayList<String>();
				isFileRecu = new ArrayList<String>();
				int lec = 0;
				
				//reception et affichage de la liste
				
				do{
					do{
						myInput.read(fileContent);
						if(fileContent[0]!='\n')
							fis.write(fileContent);
					}while(fileContent[0]!='\n');
					
					if(fis.toString().length()>1)
						isFile = fis.toString().substring(0,1);
					
					res=fis.toString().replaceAll(".+[0-9]+:[0-9]+\\s","");  //regex
		
					if(lec++>=2)
					{
						listeRecu.add(res); //on ajoute le fichier/repertoire à la liste
						isFileRecu.add(isFile);
					}
					
					//System.out.println(res);
					
					fis.reset();
				}while(res.length()!=0);
				
				fis.close();
				
				if(lec>3)
				{
					listeRecu.remove(listeRecu.size()-1);
					isFileRecu.remove(listeRecu.size()-1);
				}
				
				
				// mode passif
				operation = "QUIT";
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				resultat = myReader1.readLine();
				//System.out.println(resultat);
				
				socketDonnee.close();
				socketControle.close();

			} catch (IOException e) {
				e.printStackTrace();
				//System.out.println("Erreur socket");
			}

		  }
		}
	
	public class gestion extends Thread {
	      
		  private Socket socketControle;
		  private BufferedReader myReader1;
		  private PrintWriter myWriter1;
		
		  public gestion() {}
		  
		  public void run() {
			try {
				
				socketControle = new Socket(adresseFtp, 21);
				 
				myReader1 = new BufferedReader(new InputStreamReader(socketControle.getInputStream()));
				myWriter1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketControle.getOutputStream())),true);
				
				String messageServeur = myReader1.readLine();
				//System.out.println(messageServeur);
				
				String operation="";
				String resultat="";
				
				// envoi login
				operation = "USER "+loginFtp;
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				resultat = myReader1.readLine();
				//System.out.println(resultat);
				
				// envoi pass
				operation = "PASS "+passFtp;
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				resultat = myReader1.readLine();
				//System.out.println(resultat);
				
				// changement de repertoire
				operation = "CWD "+repOuvrir;
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				resultat = myReader1.readLine();
				//System.out.println(resultat);
				
				// verification de repertoire
				operation = "PWD";
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				resultat = myReader1.readLine();
				//System.out.println(resultat);
				
				//on enregistre le repertoire courant
				repCourant = resultat.substring(resultat.indexOf("\"")+1,resultat.lastIndexOf("\""));
				
				if(commande.equals("DELE ")) //supprimer fichier
					operation = commande+fichierDemande;
				else if(commande.equals("MKD ")) //créer dossier
					operation = commande+aCreer;
				else if(commande.equals("RNFR ")) //renommage fichier
					operation = commande+fichierDemande;
				
				//envoi
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				
				if(commande.equals("RNFR ")) //on envoie le nouveau nom
				{
					myWriter1.println("RNTO "+newNom);
					
					myWriter1.flush();
					resultat = myReader1.readLine();
					//System.out.println(resultat);
				}
				
				// deconnexion
				operation = "QUIT";
				myWriter1.println(operation); //envoi du msg
				myWriter1.flush();
				resultat = myReader1.readLine();
				//System.out.println(resultat);
				
				socketControle.close();

			} catch (IOException e) {
				e.printStackTrace();
				//System.out.println("Erreur socket");
			}

		  }
		}
	
	@Override  
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
    super.onCreateContextMenu(menu, v, menuInfo);   
        menu.add(0, v.getId(), 0, "Télécharger"); 
        menu.add(0, v.getId(), 0, "Renommer"); 
        menu.add(0, v.getId(), 0, "Supprimer");
    }  
  
    @Override  
    public boolean onContextItemSelected(MenuItem item)
    {
    	if(item.getTitle().equals("Télécharger")){
        	
    		download b = new download();
		    b.start();
		    
		    while(!b.getState().toString().equals("TERMINATED")){}
		    
		    erreurToast("Fichier téléchargé avec succès");
        }
    	else if(item.getTitle().equals("Renommer"))
    	{
        	boiteDialogRenommerFichier();
        }
        else if(item.getTitle().equals("Supprimer"))
        {
        	commande="DELE ";
        	gestion d = new gestion();
        	d.start();
        	
        	while(!d.getState().toString().equals("TERMINATED")){}
        	
        	erreurToast("Le fichier a été supprimé");
        	creerListeFtp(repCourant);
        	commande="";
        	
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
      inflater.inflate(R.menu.explorer, menu);
      return true;
    }
    
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
      switch(item.getItemId())
      {
      	case R.id.creerFichierFtp:
      	boiteDialogNewFichier();
      	return true;
      
      	case R.id.creerRepertoireFtp:
        	boiteDialogNewDossier();
        	return true;
        	
        case R.id.quitterFtp:
        	setResult(2);
        	finish();
            return true;
      }
      return super.onOptionsItemSelected(item);
    }
    
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		//touche retour  
		if(keyCode == KeyEvent.KEYCODE_BACK) {
		    if(!repCourant.equals("/")) //on remonte si c'est possible
		    {
			    creerListeFtp("..");
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
			    	erreurToast("Appuyer une 2ème fois pour quitter le serveur");
		    	}
		    }
		    return true;
		 }
		return super.onKeyDown(keyCode, event);
	}

}
