package es.uc3m.setichat.utils;

public class ChatMessage {
	
	// Constructor
	public ChatMessage(){}
	
	public ChatMessage(String idSource, String idDestination, byte[] idMessage,
			boolean encrypted, boolean signed, byte type, String nick,
			String mobile, String[] mobileList, String[][] contactList,
			String chatMessage, byte responseCode, String responseMessage,
			String revokedMobile, boolean publicKey, String key,
			byte[] signature) {
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

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}
	
	public String toString(){
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message>";
		
		// Generate <header>
		String headerBlock = "<header>\n<message>\n";
		headerBlock+="<idSource>"+this.idSource+"</idSource>\n";
		headerBlock+="<idDestination>"+this.idDestination+"</idDestination>\n";
		headerBlock+="<idMessage>"+this.idMessage+"</idMessage>\n";
		headerBlock+="<type>"+this.type+"</type>\n";
		headerBlock+="<encrypted>"+this.encrypted+"</encrypted>\n";
		headerBlock+="<signed>"+this.signed+"</signed>\n";
		headerBlock+="</header>\n";
		
		// Generate <content>
		String contentBlock = "<content>\n";
		switch(this.type){
		case 1: // SignUp
			contentBlock += "<signup>\n";
			contentBlock += "<nick>"+this.nick+"</nick>\n<mobile>"+this.mobile+"</mobile>\n";
			contentBlock += "</signup>\n";
			break;
		case 2: // Contact Request
			contentBlock += "<mobileList>\n";
			for(String m:this.mobileList){
				contentBlock += "<mobile>"+m+"</mobile>\n";
			}
			contentBlock += "</mobileList>\n";
			break;
		case 3: // Contact Response
			contentBlock += "<contactList>\n";
			for(String[] m:this.contactList){
				contentBlock += "<contact>\n" +
						"<mobile>"+m[0]+"</mobile>\n" +
						"<nick>"+m[1]+"</nick>\n";
			}			
			contentBlock += "</contactList>\n";
			break;
		case 4: // Chat Message
			String mes = (String) ((this.encrypted) ? Base64.encodeToString(this.chatMessage.getBytes(), false): this.chatMessage);
			contentBlock += "<chatMessage>\n"+mes+"</chatMessage>\n";
			break;
		case 5: // Connection
			contentBlock += "<connection>\n</connection>\n";
			break;
		case 6: // Response
			contentBlock += "<response>\n";
			contentBlock += "<responseCode>"+this.responseCode+"</responseCode>\n<responseMessage>"+this.responseMessage+"</responseMessage>\n";
			contentBlock += "</response>\n";
			break;
		case 7: // Revocation
			contentBlock += "<revokedMobile>"+this.revokedMobile+"</revokedMobile>\n";
			break;
		case 8: // KeyRequest
			contentBlock += "<keyrequest>\n";
			contentBlock += "<type>"+(String) ((this.publicKey) ? "public" : "private") +"</type>\n<mobile>"+this.mobile+"</mobile>\n";
			contentBlock += "</keyrequest>\n";
			break;
		case 9: // Download
			contentBlock += "<download>\n";
			contentBlock += "<key>"+this.key+"</key><type>"+(String) ((this.publicKey) ? "public" : "private") +"</type>\n<mobile>"+this.mobile+"</mobile>\n";
			contentBlock += "</download>\n";
			break;
		case 10: // Upload
			contentBlock += "<upload>\n";
			contentBlock += "<key>"+this.key+"</key><type>"+(String) ((this.publicKey) ? "public" : "private") + "</type>\n";
			contentBlock += "</upload>\n";
			break;
		}
		
		contentBlock += "</content>\n";
		
		// Generate <signature>
		String signatureBlock = "<signature>n";
		signatureBlock += (String) ((this.signed) ? Base64.encodeToString(this.signature, false) : this.signature);
		signatureBlock += "\n</signature>";
		
		xml += headerBlock + contentBlock + signatureBlock+"\n</message>";
		return xml;
	}
}
