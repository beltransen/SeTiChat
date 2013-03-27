package es.uc3m.setichat.utils;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import es.uc3m.setichat.utils.datamodel.Conversation;

public class Quicksort  {
	  private List<Conversation> dates;
	  private int length;

	  public List<Conversation> sortByDate(List<Conversation> conv) {
	    // Check for empty or null array
	    if (conv ==null || conv.size()==0){
	      return null;
	    }
	    this.dates = conv;
	    this.length = conv.size();
	    List<Conversation> result = null;
		try {
			result = quicksort(0, length - 1);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return result;
	  }

	  private List<Conversation> quicksort(int low, int high) throws ParseException {
	    int i = low, j = high;
	    // Get the pivot element from the middle of the list
	    Calendar pivot = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
	    pivot.setTime(sdf.parse(dates.get(low + (high-low)/2).getDate()));// all done
	    
	    //Date pivot = Date.valueOf(dates.get(low + (high-low)/2).getDate());

	    // Divide into two lists
	    while (i <= j) {
	      // If the current value from the left list is smaller then the pivot
	      // element then get the next element from the left list
	    	Calendar aux = Calendar.getInstance();
	    	aux.setTime(sdf.parse(dates.get(i).getDate()));
	      while (aux.compareTo(pivot) < 0) {
	        i++;
	        aux.setTime(sdf.parse(dates.get(i).getDate()));
	      }
	      // If the current value from the right list is larger then the pivot
	      // element then get the next element from the right list
	    	
	    	aux.setTime(sdf.parse(dates.get(j).getDate()));
	      while (aux.compareTo(pivot) > 0) {
	        j--;
	        aux.setTime(sdf.parse(dates.get(j).getDate()));
	      }

	      // If we have found a values in the left list which is larger then
	      // the pivot element and if we have found a value in the right list
	      // which is smaller then the pivot element then we exchange the
	      // values.
	      // As we are done we can increase i and j
	      if (i <= j) {
	        exchange(i, j);
	        i++;
	        j--;
	      }
	    }
	    // Recursion
	    if (low < j)
	      return quicksort(low, j);
	    if (i < high)
	      return quicksort(i, high);
	    
	    return dates;
	  }

	  private void exchange(int i, int j) {
	    Conversation temp = dates.get(i);
	    dates.set(i, dates.get(j));
	    dates.set(j, temp);
	  }
	} 
