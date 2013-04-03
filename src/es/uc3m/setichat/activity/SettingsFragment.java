package es.uc3m.setichat.activity;

import java.security.KeyPair;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import es.uc3m.setichat.R;
import es.uc3m.setichat.service.SeTIChatService;
import es.uc3m.setichat.utils.Base64;
import es.uc3m.setichat.utils.ChatMessage;
import es.uc3m.setichat.utils.KeyStoreManager;

/**
 * This activity will show the list of contacts. If a contact is clicked, a new
 * activity will be loaded with a conversation.
 * 
 * 
 * @author Guillermo Suarez de Tangil <guillermo.suarez.tangil@uc3m.es>
 * @author Jorge Blasco Al’s <jbalis@inf.uc3m.es>
 */
public class SettingsFragment extends Fragment {

	private final String PREFERENCES_FILE = "SeTiChat-Settings";
	private final String SERVER_NAME = "setichat@appspot.com";
	private final String KEYSTORE_NAME = "settichat_keystore";
	private Switch encrypt, sign;

	// Service, that may be used to access chat features
	private SeTIChatService mService;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mService = ((MainActivity) activity).getService();
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Populate with options
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View myFragmentView = inflater.inflate(R.layout.activity_settings, container, false);
		
		// Switchers
		encrypt = (Switch) myFragmentView.findViewById(R.id.encryptMessages);
		sign = (Switch) myFragmentView.findViewById(R.id.signMessages);
		
		// Show actual settings
		boolean [] savedSettings = getSettings();
		encrypt.setChecked(savedSettings[0]);
		sign.setChecked(savedSettings[1]);
		
		// Create listeners for the buttons
		Button newKeys = (Button) myFragmentView.findViewById(R.id.getkey);
		Button saveButton = (Button) myFragmentView.findViewById(R.id.save_button);
		
		newKeys.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				KeyPair kp = KeyStoreManager.generateNewKeys(KEYSTORE_NAME);
				// Cast PublicKey to String to send message
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
				
				Log.i("SETTINGS", "Sent UploadKeys message");
			}
		});
		
		saveButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// Collect settings 
				boolean encryption = encrypt.isChecked();
				boolean signature = sign.isChecked();
				
				// Save settings
				saveSettings(encryption, signature);
				
				Log.i("SETTINGS", "Saved settings");
				
				// Notify user
				Toast.makeText(getActivity(), "Settings have been saved", Toast.LENGTH_SHORT).show();
			}
		});
		
		return myFragmentView;
	}
	
	private void saveSettings(boolean e, boolean s){
		SharedPreferences settings = getActivity().getSharedPreferences(PREFERENCES_FILE, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("encryption", e);
		editor.putBoolean("signature", s);
		editor.commit();
	}
	
	private boolean[] getSettings(){
		SharedPreferences settings = getActivity().getSharedPreferences(PREFERENCES_FILE, 0);
		return new boolean[]{settings.getBoolean("encryption", false), settings.getBoolean("signature", false)};
	}

	public String getSource(){
		SharedPreferences settings = getActivity().getSharedPreferences(PREFERENCES_FILE, 0);
		return settings.getString("sourceId", "");
	}
}
