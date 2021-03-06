package es.uc3m.setichat.activity;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;
import es.uc3m.setichat.R;
import es.uc3m.setichat.service.SeTIChatService;
import es.uc3m.setichat.service.SeTIChatServiceBinder;
import es.uc3m.setichat.utils.Base64;
import es.uc3m.setichat.utils.ChatMessage;
import es.uc3m.setichat.utils.DatabaseManager;
import es.uc3m.setichat.utils.KeyStoreManager;
import es.uc3m.setichat.utils.XMLParser;
import es.uc3m.setichat.utils.datamodel.Contact;

/**
 * This is the main activity and its used to initialize all the SeTIChat features. 
 * It configures the three tabs used in this preliminary version of SeTIChat.
 * It also start the service that connects to the SeTIChat server.
 * 
 * @author Guillermo Suarez de Tangil <guillermo.suarez.tangil@uc3m.es>
 * @author Jorge Blasco Alis <jbalis@inf.uc3m.es>
 */

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	
	// Service used to access the SeTIChat server
	private SeTIChatService mService;
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	
	// Receivers that wait for notifications from the SeTIChat server
	private BroadcastReceiver openReceiver;
	private BroadcastReceiver chatMessageReceiver;
	
	// Needed variables
	private boolean signedUp, haveKeys;
	private final String PREFERENCES_FILE = "SeTiChat-Settings";
	private final String SERVER_NAME = "setichat@appspot.com";
	private final String KEYSTORE_NAME = "settichat_keystore";
	private static ContentResolver cr;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
		signedUp = settings.getBoolean("registered", false);
		
		// Create and register broadcast receivers
		IntentFilter openFilter = new IntentFilter();
		openFilter.addAction("es.uc3m.SeTIChat.CHAT_OPEN");

		 openReceiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	Context context1 = getApplicationContext();
				CharSequence text = "SeTIChatConnected";
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context1, text, duration);
				toast.show();
		    }
		  };

		  registerReceiver(openReceiver, openFilter);
		  
		  chatMessageReceiver = new BroadcastReceiver() {
			    @Override
			    public void onReceive(Context context, Intent intent) {
			    	// Check which type of message has arrive
		    		Toast toast = Toast.makeText(context, "Message from server", Toast.LENGTH_SHORT);
					toast.show();
					ChatMessage mes = XMLParser.XMLtoMessage(intent.getStringExtra("message"));
					SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
					signedUp = settings.getBoolean("registered", false);
					if(!signedUp){
						if(mes.getResponseCode()==201){
							SharedPreferences.Editor setEditor = settings.edit();
							setEditor.putBoolean("registered", true);
							// Persist random number received from server as sourceId
							String sourceId = mes.getResponseMessage();
							setEditor.putString("sourceId", sourceId);
							setEditor.commit();
							Log.i("SIGNUP", "Signed up successfully");
						}else{
							Log.e("SIGNUP", "Response message shows errors...");
						}
						startMainActivity();
					}else if(mes.getResponseCode()==3){ // ContactResponse
						((ContactsFragment) getFragmentManager().findFragmentById(0)).refreshContactList();
					}else{
						// Add idDestination type to the intent 
						intent.setAction("es.uc3m.SeTIChat.CHAT_MESSAGE");
						System.out.println(intent.getCategories());
						// Broadcast message
						context.sendBroadcast(intent); 
					}
				}
		  };
			  
		IntentFilter chatMessageFilter = new IntentFilter();
		chatMessageFilter.addAction("es.uc3m.SeTIChat.CHAT_INTERNALMESSAGE");
		
		//chatMessageFilter.addCategory("main");
		registerReceiver(chatMessageReceiver, chatMessageFilter);
		cr = getContentResolver();
		
		if(signedUp){
			try{
		        // Make sure the service is started.  It will continue running
		        // until someone calls stopService().  The Intent we use to find
		        // the service explicitly specifies our service component, because
		        // we want it running in our own process and don't want other
		        // applications to replace it.
		        startService(new Intent(MainActivity.this,
		                SeTIChatService.class));
		        bindService(new Intent(MainActivity.this,
						SeTIChatService.class), mConnection,
						Context.BIND_AUTO_CREATE);
		        

	        }catch(Exception e){
	    		Log.d("MainActivity", "Unknown Error", e);

		        stopService(new Intent(MainActivity.this,
		                SeTIChatService.class));
	        }
		}else{ // Show sign up screen
			Intent signUp = new Intent();
			signUp.setClass(getApplicationContext(), SignUpActivity.class);
			startActivityForResult(signUp, 1);
		}
		
		
	}
	
	private void startMainActivity() {
		// Generate keys at first time system is booted
		SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
		haveKeys = settings.getBoolean("generatedKeys", false);
		KeyPair kp = null;
		
		if(!haveKeys){
			Log.i("KEYS", "FIRST BOOT. NO KEYS FOUND. GENERATING...");
			kp = KeyStoreManager.generateNewKeys(this);
			String publicKey = Base64.encodeToString(kp.getPublic().getEncoded(), false);
			// Create Upload message
			ChatMessage mes = new ChatMessage();
			mes.setIdSource(getSource());
			mes.setIdDestination(SERVER_NAME);
			mes.setType(9);
			mes.setEncrypted(false);
			mes.setSigned(false);
			mes.setKey(publicKey);
			mes.setPublicKey(true);
			
			// Send Upload message to server
			mService.sendMessage(mes.toString());
			
			Log.i("PRIVATE", kp.getPrivate().toString());
			Log.i("PUBLIC", kp.getPublic().toString());
			
			
			
			// Set flag in settings for further boots
			SharedPreferences.Editor edit = settings.edit();
			edit.putBoolean("generatedKeys", true);
			edit.commit();
			
			Log.i("KEYS", "KEY GENERATION FINISHED WITHOUT ERRORS. USER NOW READY FOR SECURITY FEATURES");

		}
		
		
		// Check for new contacts
		ArrayList<String[]> contactList = getContacts();
		String[] mobileList = new String[contactList.size()]; 
		
		for(int i = 0; i<contactList.size(); i++){
			mobileList[i] = contactList.get(i)[1];
		}
		
		ChatMessage contactRequest = new ChatMessage();
		contactRequest.setIdSource(getSource());
		contactRequest.setIdDestination(SERVER_NAME);
		contactRequest.setType(2);
		contactRequest.setEncrypted(false);
		contactRequest.setSigned(false);
		
		contactRequest.setMobileList(mobileList);
		
		String message =  contactRequest.toString();
		//"<?xml version=\"1.0\" encoding=\"UTF-8\"?> <message><header><idSource>0616C4EF3D430243C3D34E9E68C60BC1</idSource> <idDestination>setichat@appspot.com</idDestination> <idMessage>2d46f3c49a2c6b7a2</idMessage> <type>2</type><encrypted>false</encrypted><signed>false</signed></header> <content><mobileList> <mobile>100277382.100276644</mobile> <mobile>100309236.100309238</mobile> </mobileList></content> </message>";
		
		// Send contact request
		mService.sendMessage(message);
		
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// Set up the action bar to show tabs.
		// For each of the sections in the app, add a tab to the action bar.
		// TabListener<ContactsFragment> t1 = new TabListener<ContactsFragment>(this, getResources().getString(R.string.title_section1), ContactsFragment.class );
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section1).setTabListener(getTabListener()));
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section2).setTabListener(getTabListener()));
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section3).setTabListener(getTabListener()));
		Log.i("Activity", "onCreate");

		setContentView(R.layout.activity_main);
		
		connectToServer();
	}

	@Override
	  public void onDestroy() {
	    super.onDestroy();
        // We stop the service if activity is destroyed
	    if(mService != null){
	    	stopService(new Intent(MainActivity.this,
	                SeTIChatService.class));
	    }
	    // We also unregister the receivers to avoid leaks.
	    if(chatMessageReceiver != null){
	        unregisterReceiver(chatMessageReceiver);
	    }
	    if(openReceiver != null){
	        unregisterReceiver(openReceiver);
	    }
	 }
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		Log.v("MainActivity", "onResume: Resuming activity...");
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==1 && resultCode==RESULT_OK){
			String nickname = data.getStringExtra("nickname");
			String phone = data.getStringExtra("phone");
			signUp(nickname, phone);
		}else{
			// Show error message saying something was wrong
			Toast.makeText(getApplicationContext(), "Error during Sign Up process. Please try again", Toast.LENGTH_SHORT).show();
			Log.e("SIGNUP", "Error signing up. Restarting process...");
			// Restart SignUp Activity
			Intent signUp = new Intent();
			signUp.setClass(getApplicationContext(), SignUpActivity.class);
			startActivityForResult(signUp, 1);
		}
	}


	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current tab position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current tab position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, show the tab contents in the
		// container view.
		ContactsFragment fragment = new ContactsFragment();
		switch(tab.getPosition()){
		case 0: // Contacts
			getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
			break;
		case 1: // Recent conversations
			RecentConversationFragment recent = new RecentConversationFragment();
			getFragmentManager().beginTransaction().replace(R.id.container, recent).commit();
			break;
		case 2: // Settings
			SettingsFragment settings = new SettingsFragment();
			getFragmentManager().beginTransaction().replace(R.id.container, settings).commit();
			break;
		}
		
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	
	public void update(){
		
	}
	
	  public void showException(Throwable t) {
		    AlertDialog.Builder builder=new AlertDialog.Builder(this);

		    builder
		      .setTitle("Exception!")
		      .setMessage(t.toString())
		      .setPositiveButton("OK", null)
		      .show();
	  }
	  
	  /** Defines callbacks for service binding, passed to bindService() */
	    private ServiceConnection mConnection = new ServiceConnection() {

	        @Override
	        public void onServiceConnected(ComponentName className,
	                IBinder service) {
	            // We've bound to LocalService, cast the IBinder and get LocalService instance
	        	Log.i("Service Connection", "Estamos en onServiceConnected");
	            SeTIChatServiceBinder binder = (SeTIChatServiceBinder) service;
	            mService = binder.getService();
	            
	            // Check User is signed up
	    		// If it is user's first time, register
	    		if(!signedUp){
	    			SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
	    			String nick = settings.getString("nick", "");
	    			String phone = settings.getString("serviceKey", "");
	    			// Create message for server
	    			ChatMessage mes = new ChatMessage();
	    			// Header
	    			mes.setIdSource(phone);
	    			mes.setIdDestination(SERVER_NAME);
	    			mes.setType(1);
	    			mes.setEncrypted(false);
	    			mes.setSigned(false);
	    			// Data
	    			mes.setNick(nick);
	    			mes.setMobile(phone);
	    			
	    			String m = mes.toString();
	    			// Send message to server*/
	    			mService.sendMessage(m);
	    			
	    			Log.i("SIGNUP", "Sign up message sent: "+m);
	    		}else{
	    			startMainActivity();
	    		}
	            
	        }
	        @Override
	        public void onServiceDisconnected(ComponentName arg0) {
	        	
	        }
	    };
	    

	public SeTIChatService getService() {
		// TODO Auto-generated method stub
		return mService;
	}
	
	//SeTIChatServiceDelegate Methods
	
	public void showNotification(String message){
		Context context = getApplicationContext();
		CharSequence text = message;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	
	/**
	 * Method that retrieves all contacts and retrieves name
	 * and phone for each one of them.  May be extended to retrieve
	 * more information
	 * 
	 * @return A list of String[]
	 * 
	 * Each String[] has
	 * String[0] - Contact Name
	 * String[1] - Contact Phone Number
	 */
	public ArrayList<String[]> getContacts() {
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		
		ArrayList<String []> resultlist = new ArrayList<String[]>();
		int num  =cur.getCount();
		if (num > 0) {
		    while (cur.moveToNext()) {
		    	String [] result = new String[2];
		    // read id
		        String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
		        /** Read name **/
		        result [0] = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		        /** Phone Number **/
		        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
		        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
		        if(pCur.moveToNext()){
		        	//Get first phone.  Extend to get all types of phones for the same contact?
			        result [1] = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));		        
			        //String typeStr = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
			        
			        resultlist.add(result);
		        }
		        pCur.close();
		        
		        
		    }
		}
		
		return resultlist;
	}
	
	public String getSource(){
		SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
		return settings.getString("sourceId", "");
	}
	
	private void signUp(String nick, String phone){
		// Check typed phone with real one (key used in service due to server restrictions)
		SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
		
		// Save typed phone to start the service
		SharedPreferences.Editor setEditor = settings.edit();
		setEditor.putString("nick", nick);
		setEditor.putString("serviceKey", phone);
		setEditor.commit();
		
		// Start service
		try{
	        // Make sure the service is started.  It will continue running
	        // until someone calls stopService().  The Intent we use to find
	        // the service explicitly specifies our service component, because
	        // we want it running in our own process and don't want other
	        // applications to replace it.
	        startService(new Intent(MainActivity.this,
	                SeTIChatService.class));
	        bindService(new Intent(MainActivity.this,
					SeTIChatService.class), mConnection,
					Context.BIND_AUTO_CREATE);
        }catch(Exception e){

    		Log.d("MainActivity", "Unknown Error", e);

	        stopService(new Intent(MainActivity.this,
	                SeTIChatService.class));
        }
	}
	
	private void connectToServer(){
		ChatMessage m = new ChatMessage();
		m.setIdSource(getSource());
		m.setIdDestination(SERVER_NAME);
		m.setType(5);
		m.setEncrypted(false);
		m.setSigned(false);
		
		String message = m.toString();
		
		mService.sendMessage(message);
	}
	
	private TabListener getTabListener (){
		return this;
	}

}
