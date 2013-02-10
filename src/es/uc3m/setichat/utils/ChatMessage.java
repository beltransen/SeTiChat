package es.uc3m.setichat.utils;

public class ChatMessage {
	
	// Constructor
	public ChatMessage(){}
	
	public ChatMessage(String idSource, String idDestination, byte[] idMessage,
			boolean encrypted, boolean signed, byte type, String nick,
			String mobile, String[] mobileList, String[][] contactList,
			String chatMessage, byte responseCode, String responseMessage,
			String revokedMobile, boolean publicKey, String key,
			String signature) {
		super();
		this.idSource = idSource;
		this.idDestination = idDestination;
		this.idMessage = idMessage;
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
	}

	// Header fields (Mandatory)
	private String idSource;
	private String idDestination;
	private byte[] idMessage;
	private boolean encrypted;
	private boolean signed;
	private byte type;
	
	// Content fields
	// Sign up
	private String nick;
	private String mobile;
	
	// Contact Request
	private String[] mobileList;
	
	// Contact Response
	private String [][] contactList;
	
	// Chat message
	private String chatMessage;
	
	// Connection (Empty)
	
	// Response
	private byte responseCode;
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
	private String signature;

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

	public byte[] getIdMessage() {
		return idMessage;
	}

	public void setIdMessage(byte[] idMessage) {
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

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
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

	public String[][] getContactList() {
		return contactList;
	}

	public void setContactList(String[][] contactList) {
		this.contactList = contactList;
	}

	public String getChatMessage() {
		return chatMessage;
	}

	public void setChatMessage(String chatMessage) {
		this.chatMessage = chatMessage;
	}

	public byte getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(byte responseCode) {
		this.responseCode = responseCode;
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

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
}
