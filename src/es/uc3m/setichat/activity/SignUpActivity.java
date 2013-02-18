package es.uc3m.setichat.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
	
	private SeTIChatService mService;
	private BroadcastReceiver chatMessageReceiver;
	
	private boolean DEBUG = false;
	
	EditText nick, phone;
	Button button;
	private final String SERVER_NAME = "aplicacion@appspot.com";
	
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mService = SeTIChatServiceBinder.getService();

			DEBUG = true;
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

		if (mService == null) {
			// Binding the activity to the service to get shared objects
			if (DEBUG)
				Log.d("SignUpActivity", "Binding activity");
			bindService(new Intent(SignUpActivity.this,
					SeTIChatService.class), mConnection,
					Context.BIND_AUTO_CREATE);
		}
		
		setContentView(R.layout.activity_sign_up);
		
		nick = (EditText) findViewById(R.id.nick);
		phone = (EditText) findViewById(R.id.phone);
		
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String nickname = nick.getText().toString();
				String phoneNumber = phone.getText().toString();
				// Check both fields have been filled
				if(!nickname.equalsIgnoreCase("") || !phoneNumber.equalsIgnoreCase("")){
					// Show error message
					Log.i("SIGNUP", "Error in Sign Up. Nick and Phone fields are mandatory");
				}
				signUp(nickname,phoneNumber);
			}	
		});
		
		chatMessageReceiver = new BroadcastReceiver (){
			@Override
		    public void onReceive(Context context, Intent intent) {
				// Get message from intent and cast into an object
				ChatMessage mes = XMLParser.XMLtoMessage(intent.getStringExtra("message"));
				
				// Check message code
				if(mes.getResponseCode()==201){
					// Persist random number received from server as sourceId
					
					Log.i("SIGNUP", "Signed up successfully");
				}else{
					// Show error message
					// Toast
					
					Log.e("SIGNUP", "Error in Sign Up process: "+mes.getResponseCode());
				}
				
				
		    }
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_sign_up, menu);
		return true;
	}
	
	
	
	public void signUp(String nick, String phone){
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
		
		// Send message to server
		mService.sendMessage(mes.toString());
	}
}
