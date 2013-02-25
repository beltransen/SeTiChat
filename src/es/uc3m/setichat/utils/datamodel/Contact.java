package es.uc3m.setichat.utils.datamodel;

public class Contact {

	private int id;
	private String idDestination;
	private String name;
		
	
	public Contact(int id, String idDestination, String name) {
		super();
		this.id = id;
		this.idDestination = idDestination;
		this.name = name;
	}
	
	public Contact() {
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIdDestination() {
		return idDestination;
	}
	public void setIdDestination(String idDestination) {
		this.idDestination = idDestination;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
