package es.uc3m.setichat.utils.datamodel;

import java.sql.Date;

public class Conversation {
	 
    //private variables
    private int _id;
    private String _text;
    private String _date;


	String _idsource;
 
    // Empty constructor
    public Conversation(){
 
    }
    // constructor
    public Conversation(int id, String name, String date, String idsource){
        this._id = id;
        this._text = name;
        this._date = date;
        this._idsource = idsource;
    }
 

    // getting ID
    public int getID(){
        return this._id;
    }
 
    // setting id
    public void setID(int id){
        this._id = id;
    }
 
    // getting name
    public String getText(){
        return this._text;
    }
 
    // setting name
    public void setText(String name){
        this._text = name;
    }
 
    // getting phone number
    public String getDate(){
        return this._date;
    }
 
    // setting phone number
    public void setDate(String phone_number){
        this._date = phone_number;
    }
    
    public String getidsource() {
		return _idsource;
	}
	public void setidsource(String _idsource) {
		this._idsource = _idsource;
	}
}
