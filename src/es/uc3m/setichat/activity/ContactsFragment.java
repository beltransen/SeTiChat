package es.uc3m.setichat.activity;

import java.util.List;

import es.uc3m.setichat.activity.SeTIChatConversationActivity;
import es.uc3m.setichat.service.SeTIChatService;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import es.uc3m.setichat.service.SeTIChatService;
import es.uc3m.setichat.utils.DatabaseManager;
import es.uc3m.setichat.utils.datamodel.Contact;

/**
 * This activity will show the list of contacts. If a contact is clicked, a new
 * activity will be loaded with a conversation.
 * 
 * 
 * @author Guillermo Suarez de Tangil <guillermo.suarez.tangil@uc3m.es>
 * @author Jorge Blasco Alís <jbalis@inf.uc3m.es>
 */
public class ContactsFragment extends ListFragment {

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
		// Populate list with contacts.
		// Ey, a more fancy layout could be used! You dare?!
		DatabaseManager dbm = new DatabaseManager(getActivity());
		//int i = dbm.getContactsCount();
		//Contact contact = new Contact(1,"idsource","Listed number 2");
		//dbm.addContact(contact);
		//int j = dbm.getContactsCount();
		List<Contact> list = dbm.getAllContacts();
		dbm.close();
		
		String [] results = new String[list.size()];
		int i = 0;
		for(Contact item : list){
			results[i] = item.getName();
			i++;
		}
		setListAdapter(new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_activated_1, results));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// We need to launch a new activity to display
		// the dialog fragment with selected text.
		Intent intent = new Intent();
		intent.setClass(getActivity(), SeTIChatConversationActivity.class);
		// Meter la informacion del número de teléfono en el intent para luego
		// reconocer los mensajes
		String mobile = l.getAdapter().getItem(position).toString();
		Log.i("Click on conversation", "Conversation: " + position + " opened");
		intent.putExtra("position", position);

		startActivity(intent);
	}

}
