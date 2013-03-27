package es.uc3m.setichat.service;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.R;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import edu.gvsu.cis.masl.channelAPI.ChannelAPI;
import edu.gvsu.cis.masl.channelAPI.ChannelService;
import es.uc3m.setichat.activity.SeTIChatConversationActivity;
import es.uc3m.setichat.utils.ChatMessage;
import es.uc3m.setichat.utils.DatabaseManager;
import es.uc3m.setichat.utils.XMLParser;
import es.uc3m.setichat.utils.datamodel.Contact;
import es.uc3m.setichat.utils.datamodel.Conversation;

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

	
	public SeTIChatService() {
		Log.i("SeTIChat Service", "Service constructor");
	}
	
	
	  @Override
	  public void onCreate() {
	    super.onCreate();
	    
	    Log.i("SeTIChat Service", "Service created");
	    
	    // Read if registered from Preferences file
	    SharedPreferences settings = this.getSharedPreferences(	PREFERENCES_FILE, 0);
	    String phoneNumber = settings.getString("serviceKey", "");
	    if(phoneNumber.equalsIgnoreCase("")){
	    	Log.e("SERVICE", "Error starting service. Wrong key");
	    }
	    // SeTIChat connection is seted up in this step. 
	    // Mobile phone should be changed with the appropiate value
	    channel = new ChannelAPI();
		this.connect(phoneNumber);  
	    binder.onCreate(this);
	    
	    if(settings.getBoolean("registered", false) == true)
	    {
	    	ChatMessage cm = new ChatMessage();
			cm.setType(5);
			cm.setIdSource(settings.getString("sourceId", ""));
			cm.setIdDestination("setichat@appspot.com");
			cm.setEncrypted(false);
			cm.setSigned(false);
			cm.setIdMessage("2d46f3c49a2c6b7a2");			
			sendMessage(cm.toString());
	    }

	    
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
					 Log.i("TOKEN", key);
					 try {
							channel = new ChannelAPI("https://setichat.appspot.com", key, current); //Production Example /*tester847*/
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
					 Log.i("SendMessage", "Send: "+messages[0]);
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
			
			// Broadcast to Main Activity
			String intentKey = "es.uc3m.SeTIChat.CHAT_INTERNALMESSAGE";
			Intent openIntent = new Intent(intentKey);
			openIntent.setPackage("es.uc3m.setichat");
			// Add message to intent
			openIntent.putExtra("message", message);
			
			if(m.getType()==3){ //Contact response
				ArrayList<String[]> contacts = m.getContactList();
				DatabaseManager dbm = new DatabaseManager(getApplicationContext());
				for(int i = 0; i<contacts.size(); i++){
					String [] c = contacts.get(i);
					if(dbm.getContact(c[1])==null){
						Contact contact = new Contact(dbm.getContactsCount(), c[1], c[0]);
						dbm.addContact(contact);
					}
					
				}
				dbm.close();
			}else{
				if(m.getType()==4){ // If it is a chat message
					DatabaseManager dbm = new DatabaseManager(getApplicationContext());					
					Conversation conv = new Conversation(dbm.getConversationsCount(), message, Calendar.getInstance().getTime().toString(), m.getIdSource());
					dbm.addConversation(conv);
					dbm.close();
					
					if(!isForeground("es.uc3m.setichat.activity.SeTIChatConversationActivity")){
						showNotification(openIntent, conv.getidsource());
					}
				}
			}
			
			Context context = getApplicationContext();
			context.sendBroadcast(openIntent);
			
		}

		
		private void showNotification(Intent openIntent, String idsource){

			DatabaseManager dbm = new DatabaseManager(this);
			Contact cont = dbm.getContact(idsource);
			dbm.close();
			NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(this)
			        .setSmallIcon(R.drawable.alert_light_frame)
			        .setContentTitle("SeTIChat")
			        .setContentText("New Message From " + cont.getName());
			// Creates an explicit intent for an Activity in your app
			//Intent resultIntent = new Intent(this, ResultActivity.class);

			// The stack builder object will contain an artificial back stack for the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(SeTIChatConversationActivity.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(openIntent);
			PendingIntent resultPendingIntent =
			        stackBuilder.getPendingIntent(
			            0,
			            PendingIntent.FLAG_UPDATE_CURRENT
			        );
			mBuilder.setContentIntent(resultPendingIntent);
			NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// mId allows you to update the notification later on.
			mNotificationManager.notify(0, mBuilder.build());
		}
		
		
		/**
		 * 
		 * @param activityPath Path to the activity we want to know is working
		 * @return true or false if the activity is or is not in the foreground
		 */
		public boolean isForeground(String activityPath){
			ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			 List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1); 

			     ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
			   if(componentInfo.getClassName().equals(activityPath)) return true;
			return false;
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
