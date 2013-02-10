package es.uc3m.setichat.activity;


import es.uc3m.setichat.activity.SeTIChatConversationActivity;
import es.uc3m.setichat.service.SeTIChatService;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


/**
 * This activity will show the list of contacts. 
 * If a contact is clicked, a new activity will be loaded with a conversation.
 *  
 * 
 * @author Guillermo Suarez de Tangil <guillermo.suarez.tangil@uc3m.es>
 * @author Jorge Blasco Al’s <jbalis@inf.uc3m.es>
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
        mService = ((MainActivity)activity).getService();
    }
    
    @Override
	public void onStop(){
    	super.onStop();
    	
    	
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	//Populate list with contacts.
    	//Ey, a more fancy layout could be used! You dare?!
        setListAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_activated_1, 
                new String[]{"Student 1 (100032456.100012597.100032599)", "Student 2 (100032451.100012591.100032591)", "Student 3 (100032453.100012593.100032593)", "Student 4 (100032454.100012594.100032594)", "Student 5 (100032455.100012595.100032595)", "Student 6 (100032456.100012596.100032596)", "Student 7 (100032457.100012597)"}));
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	// We need to launch a new activity to display
        // the dialog fragment with selected text.
        Intent intent = new Intent();
        intent.setClass(getActivity(), SeTIChatConversationActivity.class);
        intent.putExtra("index", position);           
        startActivity(intent);
    }
    
}
