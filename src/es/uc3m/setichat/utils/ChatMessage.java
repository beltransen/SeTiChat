package es.uc3m.setichat.utils;

import java.util.ArrayList;

import org.apache.commons.lang.RandomStringUtils;

import android.content.Context;
import es.uc3m.setichat.utils.datamodel.Contact;

public class ChatMessage {
	
	// Constructor
	public ChatMessage(){
		this.idMessage = getNewId();
	}
	
	private String getNewId() {
		// TODO Auto-generated method stub
		return RandomStringUtils.random(16);
	}

	public ChatMessage(String idSource, String idDestination, String idMessage,
			boolean encrypted, boolean signed, byte type, String nick,
			String mobile, String[] mobileList, ArrayList<String[]> contactList,
			String chatMessage, byte responseCode, String responseMessage,
			String revokedMobile, boolean publicKey, String key,
			byte[] signature, Context context) {
		super();
		this.idSource = idSource;
		this.idDestination = idDestination;
		this.encrypted = encrypted;
		this.signed = signed;
		this.type = type;
		this.nick = nick;
		this.mobile = mobile;
		this.mobileList = mobileList;
		this.contactList = contactList;
		this.chatMessage = chatMessage;
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.revokedMobile = revokedMobile;
		this.publicKey = publicKey;
		this.key = key;
		this.signature = signature;
		this.idMessage = getNewId();
		
		this.context = context;
	}

	public ChatMessage(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.idMessage = getNewId();
	}

	private Context context;
	
	
	// Header fields (Mandatory)
	private String idSource;
	private String idDestination;
	private String idMessage;
	private boolean encrypted;
	private boolean signed;
	private int type;
	
	// Content fields
	// Sign up
	private String nick;
	private String mobile;
	
	// Contact Request
	private String[] mobileList;
	
	// Contact Response
	private ArrayList<String[]> contactList;
	
	// Chat message
	private String chatMessage;
	
	// Connection (Empty)
	
	// Response
	private int responseCode;
	private String responseMessage;
	
	// Revocation
	private String revokedMobile;
	
	// Key Request
	private boolean publicKey;
	//mobile;
	
	// Download
	private String key;
	//type
	//mobile
	
	// Upload
	//key;
	//type
	
	// Signature
	private byte[] signature;

	/***************** Methods *********************/
	
	public String getIdSource() {
		return idSource;
	}

	public void setIdSource(String idSource) {
		this.idSource = idSource;
	}

	public String getIdDestination() {
		return idDestination;
	}

	public void setIdDestination(String idDestination) {
		this.idDestination = idDestination;
	}

	public String getIdMessage() {
		return idMessage;
	}

	public void setIdMessage(String idMessage) {
		this.idMessage = idMessage;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public boolean isSigned() {
		return signed;
	}

	public void setSigned(boolean signed) {
		this.signed = signed;
	}

	public int getType() {
		return type;
	}

	public void setType(int i) {
		this.type = i;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String[] getMobileList() {
		return mobileList;
	}

	public void setMobileList(String[] mobileList) {
		this.mobileList = mobileList;
	}

	public ArrayList<String[]> getContactList() {
		return contactList;
	}

	public void setContactList(ArrayList<String[]> contactList) {
		this.contactList = contactList;
	}

	public String getChatMessage() {
		return chatMessage;
	}

	public void setChatMessage(String chatMessage) {
		this.chatMessage = chatMessage;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int i) {
		this.responseCode = i;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getRevokedMobile() {
		return revokedMobile;
	}

	public void setRevokedMobile(String revokedMobile) {
		this.revokedMobile = revokedMobile;
	}

	public boolean isPublicKey() {
		return publicKey;
	}

	public void setPublicKey(boolean publicKey) {
		this.publicKey = publicKey;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public String toString(){
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message>";
		
		// Generate <header>
		String headerBlock = "<header>";
		headerBlock+="<idSource>"+this.idSource+"</idSource>";
		headerBlock+="<idDestination>"+this.idDestination+"</idDestination>";
		headerBlock+="<idMessage>"+this.idMessage+"</idMessage>";
		headerBlock+="<type>"+this.type+"</type>";
		headerBlock+="<encrypted>"+this.encrypted+"</encrypted>";
		headerBlock+="<signed>"+this.signed+"</signed>";
		headerBlock+="</header>";
		
		// Generate <content>
		String contentBlock = "<content>";
		switch(this.type){
		case 1: // SignUp
			contentBlock += "<signup>";
			contentBlock += "<nick>"+this.nick+"</nick><mobile>"+this.mobile+"</mobile>";
			contentBlock += "</signup>";
			break;
		case 2: // Contact Request
			contentBlock += "<mobileList>";
			for(String m:this.mobileList){
				contentBlock += "<mobile>"+m+"</mobile>";
			}
			contentBlock += "</mobileList>";
			break;
		case 3: // Contact Response
			contentBlock += "<contactList>";
			for(String[] m:this.contactList){
				contentBlock += "<contact>" +
						"<mobile>"+m[0]+"</mobile>" +
						"<nick>"+m[1]+"</nick>";
			}			
			contentBlock += "</contactList>";
			break;
		case 4: // Chat Message
			DatabaseManager dbm = new DatabaseManager(getContext());
			Contact c = dbm.getContact(this.idDestination);
			SecurityModule sm = new SecurityModule(c);
			this.chatMessage = ((this.encrypted) ? Base64.encodeToString(sm.encrypt(this.chatMessage, c), false): this.chatMessage);
			contentBlock += "<chatMessage>"+this.chatMessage+"</chatMessage>";
			break;
		case 5: // Connection
			contentBlock += "<connection></connection>";
			break;
		case 6: // Response
			contentBlock += "<response>";
			contentBlock += "<responseCode>"+this.responseCode+"</responseCode><responseMessage>"+this.responseMessage+"</responseMessage>";
			contentBlock += "</response>";
			break;
		case 7: // Revocation
			contentBlock += "<revokedMobile>"+this.revokedMobile+"</revokedMobile>";
			break;
		case 8: // Download
			contentBlock += "<download>";
			contentBlock += "<key>"+this.key+"</key><type>"+(String) ((this.publicKey) ? "public" : "private") +"</type><mobile>"+this.mobile+"</mobile>";
			contentBlock += "</download>";
			break;
		case 9: // Upload
			contentBlock += "<upload>";
			contentBlock += "<key>"+this.key+"</key><type>"+(String) ((this.publicKey) ? "public" : "private") + "</type>";
			contentBlock += "</upload>";
			break;
		case 10: // KeyRequest
			contentBlock += "<keyrequest>";
			contentBlock += "<type>"+(String) ((this.publicKey) ? "public" : "private") +"</type><mobile>"+this.mobile+"</mobile>";
			contentBlock += "</keyrequest>";
			break;
		}
		
		contentBlock += "</content>";
		
		String signatureBlock = "";
		// Generate <signature>
		if(this.signed){
			SecurityModule sm = new SecurityModule();
			String contentToSign= "<idDestination>"+this.idDestination+"</idDestination>"+"<idMessage>"+this.idMessage+"</idMessage>"+"<content>"+this.chatMessage+"</content>";
			String signature = sm.sign(contentToSign, context); 
			signatureBlock += "<signature>";
			signatureBlock += signature;
			signatureBlock += "</signature>";
		}
		
		
		xml += headerBlock + contentBlock + signatureBlock+"</message>";
		return xml;
	}

	
}
