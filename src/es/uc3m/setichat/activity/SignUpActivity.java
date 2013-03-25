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
		render();
	}
	
	protected void backToMain(int code, Intent data) {
		// TODO Auto-generated method stub
		setResult(code, data);
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
					Toast.makeText(getApplicationContext(), "Fill both nick and pass to register", Toast.LENGTH_SHORT).show();
					backToMain(RESULT_CANCELED, null);
				}
				Intent data = new Intent();
				data.putExtra("nickname", nickname);
				data.putExtra("phone", phoneNumber);
				backToMain(RESULT_OK, data);
			}	
		});
	}
}
