package es.uc3m.setichat.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import es.uc3m.setichat.R;
import es.uc3m.setichat.service.SeTIChatService;
import es.uc3m.setichat.utils.ChatMessage;

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
	private Switch encrypt, sign;
	private TextView t_privateKey, t_publicKey;

	// Service, that may be used to access chat features
	private SeTIChatService mService;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
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
		encrypt = (Switch) getView().findViewById(R.id.encryptMessages);
		sign = (Switch) getView().findViewById(R.id.signMessages);
		
		// TextViews
		t_publicKey = (TextView) getView().findViewById(R.id.t_publickey);
		t_privateKey = (TextView) getView().findViewById(R.id.t_privatekey);
		
		// Show actual settings
		boolean [] savedSettings = getSettings();
		encrypt.setChecked(savedSettings[0]);
		sign.setChecked(savedSettings[1]);
		
		// Show actual keys
		String[] currentKeys = getKeys();
		t_publicKey.setText(currentKeys[0]);
		t_privateKey.setText(currentKeys[1]);
		
		// Create listeners for the buttons
		Button newKeys = (Button) getView().findViewById(R.id.getkey);
		Button saveButton = (Button) getView().findViewById(R.id.save_button);
		
		newKeys.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// Generate new keys
				String publicKey = "";
				String privateKey = "";
				
				// Store keys in preferences
				saveKeys(publicKey, privateKey);
				
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
				
				Log.i("SETTINGS", "Sent Upload message");
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
		
		// Create listeners for Signature switcher
		sign.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					// Check if there are keys, otherwise notify user and turn off the button
					String[] keys = getKeys();
					if(keys[0]==null){
						Toast.makeText(getActivity(), "Please first generate keys to allow messages signature", Toast.LENGTH_SHORT).show();
						sign.setChecked(false);
						Log.i("SETTINGS", "There aren't keys. Signature SWITCHED OFF");
					}
				}
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

	private void saveKeys(String pu, String pr){
		SharedPreferences settings = getActivity().getSharedPreferences(PREFERENCES_FILE, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("publickey", pu);
		editor.putString("privatekey", pr);
		editor.commit();
	}

	private String[] getKeys(){
		SharedPreferences settings = getActivity().getSharedPreferences(PREFERENCES_FILE, 0);
		return new String[]{settings.getString("publickey", null), settings.getString("privatekey", null)};
	}

	public String getSource(){
		SharedPreferences settings = getActivity().getSharedPreferences(PREFERENCES_FILE, 0);
		return settings.getString("sourceId", "");
	}
}
