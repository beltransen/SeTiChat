package es.uc3m.setichat.service;



import java.io.IOException;


import edu.gvsu.cis.masl.channelAPI.ChannelAPI;
import edu.gvsu.cis.masl.channelAPI.ChannelService;
import es.uc3m.setichat.utils.ChatMessage;
import es.uc3m.setichat.utils.XMLParser;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

/**
 * This service is used to connecto to the SeTIChat server. 
 * It should remain running even if the app is not in the foreground
 *  
 * 
 * @author Guillermo Suarez de Tangil <guillermo.suarez.tangil@uc3m.es>
 * @author Jorge Blasco Al’s <jbalis@inf.uc3m.es>
 */

public class SeTIChatService extends Service implements ChannelService {
	
	// Used to communicate with the server
	ChannelAPI channel;
	
	// Used to bind activities
	private final SeTIChatServiceBinder binder=new SeTIChatServiceBinder();
	
	// Needed variables
	private boolean signedUp;
	private final String PREFERENCES_FILE = "SeTiChat-Settings";
	private final String phoneNumber = "100276600.100277382";

	
	public SeTIChatService() {
		Log.i("SeTIChat Service", "Service constructor");
	}
	
	
	  @Override
	  public void onCreate() {
	    super.onCreate();
	    Log.i("SeTIChat Service", "Service created");
	    
	    // Read if registered from Preferences file
	    SharedPreferences settings = this.getSharedPreferences(	PREFERENCES_FILE, 0);
		signedUp = settings.getBoolean("registered", false);
		if(!signedUp){ // Persist phone number into settings to be used in signup process in main activity
			SharedPreferences.Editor setEditor = settings.edit();
			setEditor.putString("serviceKey", phoneNumber);
			setEditor.commit();
		}
	    
	    // SeTIChat connection is seted up in this step. 
	    // Mobile phone should be changed with the appropiate value
	    channel = new ChannelAPI();
		this.connect(phoneNumber);  
	    binder.onCreate(this);
//	    XMLParser.XMLtoMessage("<?xml version='1.0' encoding='UTF-8'?>"
//	    		+ "<message>"
//	    		+ "<header>" 
//	    		+ "<idSource>FEAD…FAA3</idSource>" 
//	    		+ "<idDestination>100012345.100056789</idDestination>" 
//	    		+ "<idMessage>2d46f3c49a2c6b7a2</idMessage>" 
//	    		+ "<type>4</type>" 
//	    		+ "<encrypted>false</encrypted>" 
//	    		+ "<signed>false</signed>" 
//	    		+ "</header>" 
//	    		+ "<content>" 
//	    		+ "<chatMessage>" 
//	    		+ "Hi there!!!"
//	    		+ "</chatMessage>" 
//	    		+ "</content>" 
//	    		+ "</message>");
	    
		 
	  }

	  @Override
	  public IBinder onBind(Intent intent) {
		  Log.i("SeTIChat Service", "Service binded");
		  return(binder);
	  }

	  @Override
	  public void onDestroy() {
	    super.onDestroy();
	    Log.i("SeTIChat Service", "Service destrotyed");
	    // When the service is destroyed, the connection is closed 
	    try {
			channel.close();
		} catch (Exception e){
			System.out.println("Problem Closing Channel");
		}
	    binder.onDestroy();    
	  }
	  

		//Methods exposed to service binders
		// Login user, send message, update public key, etc.
	  
	  	// All of them are implemented with AsyncTask examples to avoid UI Thread blocks.
		 public void connect(String key){
			 final SeTIChatService current = this;
			 class ChannelConnect extends AsyncTask<String, String, String> {
			    
				 protected String doInBackground(String... keys) {
					 Log.i("Service connect", "Connect test");
					 String key = keys[0];
					 try {
							channel = new ChannelAPI("http://setichat.appspot.com", key, current); //Production Example
							channel.open();
							
						} catch (Exception e){
							System.out.println("Something went wrong...");
							Log.i("Service connect", "Error connecting..."+e.getLocalizedMessage());
						}
					 return "ok";
			     }

			     protected void onProgressUpdate(String... progress) {
			         //setProgressPercent(progress[0]);
			     }

			     protected void onPostExecute(String result) {
			         //
			     }
			 }
			 new ChannelConnect().execute(key,key,key);
		 }

		 
		 public void sendMessage(String message){
			 
			 
			 class SendMessage extends AsyncTask<String, String, String> {
				 protected String doInBackground(String... messages) {
					 Log.i("SendMessage", "send message test");
					 String message = messages[0];
					 try {
							channel.send(message, "/chat");
						} catch (IOException e) {
							System.out.println("Problem Sending the Message");
						}
					 return "ok";
			     }

			     protected void onProgressUpdate(String... progress) {
			         //setProgressPercent(progress[0]);
			     }

			     protected void onPostExecute(String result) {
			    	// TODO Auto-generated method stub
			    	
			     }
				 
				 
			 }
			 new SendMessage().execute(message,message,message);
		 }

		 
		 // Callback method for the Channel API. This methods are called by ChannelService when some kind 
		 // of event happens
		 
		 
		 /**
		  *  Called when the client is able to correctly establish a connection to the server. In this case,
		  *  the main activity is notified with a Broadcast Intent.
		  */
		@Override
		public void onOpen() {
			Log.i("onOpen", "Channel Opened");
			String intentKey = "es.uc3m.SeTIChat.CHAT_OPEN";
			Intent openIntent = new Intent(intentKey);
			// ÀWhy should we set a Package?
			openIntent.setPackage("es.uc3m.setichat");
			Context context = getApplicationContext();
			context.sendBroadcast(openIntent);  
		}

		/**
		  *  Called when the client receives a chatMessage. In this case,
		  *  the main activity is notified with a Broadcast Intent.
		  */
		@Override
		public void onMessage(String message) {
			Log.i("onMessage", "Message received :"+message);
			// Extract message type (server or user) to decide handler
			ChatMessage m = XMLParser.XMLtoMessage(message);
			
			// TODO Auto-generated method stub
			String intentKey = "";
			if(!signedUp){
				intentKey = "es.uc3m.SeTIChat.SIGN_UP";
			}else{
				intentKey = "es.uc3m.SeTIChat.CHAT_INTERNALMESSAGE";
			}
			
			
			Intent openIntent = new Intent(intentKey);
			openIntent.setPackage("es.uc3m.setichat");
			// Add message to intent
			openIntent.putExtra("message", message);
			
			Context context = getApplicationContext();
			context.sendBroadcast(openIntent);  
			
		}


		@Override
		public void onClose() {
			// Called when the connection is closed
			
		}


		@Override
		public void onError(Integer errorCode, String description) {
			// Called when there is an error in the connection
			
		}
	  
}
