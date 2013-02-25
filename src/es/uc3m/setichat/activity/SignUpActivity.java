package es.uc3m.setichat.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import es.uc3m.setichat.R;
import es.uc3m.setichat.service.SeTIChatService;
import es.uc3m.setichat.service.SeTIChatServiceBinder;
import es.uc3m.setichat.utils.ChatMessage;
import es.uc3m.setichat.utils.XMLParser;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class SignUpActivity extends Activity {
	
	EditText nick, phone;
	Button button;

	private boolean DEBUG = false;
	private SeTIChatService mService;
	private BroadcastReceiver chatMessageReceiver;
	
	
	private final String PREFERENCES_FILE = "SeTiChat-Settings";
	private final String SERVER_NAME = "setichat@appspot.com";
	

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mService = SeTIChatServiceBinder.getService();

			DEBUG = true;

			render();

		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.

			if (DEBUG)
				Log.d("SeTIChatConversationActivity",
						"onServiceDisconnected: un-bounding the service");

			mService = null;
			Toast.makeText(SignUpActivity.this, "Disconnected", // R.string.local_service_disconnected,
					Toast.LENGTH_SHORT).show();
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 chatMessageReceiver = new BroadcastReceiver() {
			    @Override
			    public void onReceive(Context context, Intent intent) {
			    	ChatMessage mes = XMLParser.XMLtoMessage(intent.getStringExtra("message"));
					// Check message code
					if(mes.getResponseCode()==201){
						SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
						SharedPreferences.Editor setEditor = settings.edit();
						setEditor.putBoolean("registered", true);
						// Persist random number received from server as sourceId
						String sourceId = mes.getResponseMessage();
						setEditor.putString("sourceId", sourceId);
						setEditor.commit();
						Log.i("SIGNUP", "Signed up successfully");
						
						// Return control to main activity
						backToMain(RESULT_OK);
					}else{
						backToMain(RESULT_CANCELED);
					}
				}
		};
		IntentFilter chatMessageFilter = new IntentFilter();
		chatMessageFilter.addAction("es.uc3m.SeTIChat.SIGN_UP");
		//chatMessageFilter.addCategory("main");
		registerReceiver(chatMessageReceiver, chatMessageFilter);
		
		if (mService == null) {
			// Binding the activity to the service to get shared objects
			if (DEBUG)
				Log.d("SeTIChatConversationActivity", "Binding activity");
			bindService(new Intent(SignUpActivity.this,
					SeTIChatService.class), mConnection,
					Context.BIND_AUTO_CREATE);
		}else{
			render();
		}
		
		
	}

	protected void backToMain(int code) {
		// TODO Auto-generated method stub
		setResult(code);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_sign_up, menu);
		return true;
	}
	
	@Override
	public void onStop(){
		super.onStop();
		unbindService(mConnection);
		unregisterReceiver(chatMessageReceiver);
	}
	
	public void render(){
		setContentView(R.layout.activity_sign_up);
		
		nick = (EditText) findViewById(R.id.nick);
		phone = (EditText) findViewById(R.id.phone);
		button = (Button) findViewById(R.id.sign_up_button);
		
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String nickname = nick.getText().toString();
				String phoneNumber = phone.getText().toString();
				// Check both fields have been filled
				if(nickname.equalsIgnoreCase("") || phoneNumber.equalsIgnoreCase("")){
					// Show error message
					Log.e("SIGNUP", "Error in Sign Up. Nick and Phone fields are mandatory");
				}
				
				signUp(nickname, phoneNumber);
			}	
		});
	}
	
	private void signUp(String nick, String phone){
		// Check typed phone with real one (key used in service due to server restrictions)
		SharedPreferences settings = getSharedPreferences(PREFERENCES_FILE, 0);
		String realPhone = settings.getString("serviceKey", null);
		if(realPhone==null){
			// Error
			Log.e("SIGNUP", "Error retrieving service key");
		}
		
		if(!phone.equalsIgnoreCase(realPhone)){
			Log.i("SIGNUP", "Wrong phone number");
			Toast.makeText(getApplicationContext(), "Wrong phone number, check you type your real phone number.", Toast.LENGTH_SHORT).show();
			// Restart SignUp Activity
			Intent signUp = new Intent();
			signUp.setClass(getApplicationContext(), SignUpActivity.class);
			startActivityForResult(signUp, 1);
		}else{
			// Save typed phone to start the service
			SharedPreferences.Editor setEditor = settings.edit();
			setEditor.putString("nick", nick);
			setEditor.putString("serviceKey", phone);
			setEditor.commit();
			
			
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
		}
	}
	
}
