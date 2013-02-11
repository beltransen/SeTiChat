package es.uc3m.setichat.utils;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


//import ChatMessage;

public class XMLParser {

	public static ChatMessage XMLtoMessage (String message){
		
		ChatMessage messageobj = new ChatMessage();
		
		try {
			
			// Header fields (Mandatory)
			Document doc = loadXMLFromString(message);
			NodeList n1 = doc.getElementsByTagName("header");
			Element header = (Element)n1.item(0);

			messageobj.setIdSource(getTextValue(header,"idSource"));
			messageobj.setIdDestination(getTextValue(header,"idDestination"));
			messageobj.setIdMessage(getTextValue(header,"idMessage").getBytes());
			messageobj.setEncrypted(getTextValue(header,"encrypted").equalsIgnoreCase("true"));
			messageobj.setSigned(getTextValue(header,"signed").equalsIgnoreCase("true"));
			messageobj.setType((byte) getIntValue(header, "type"));
			
			
			// Content
			NodeList n2 = doc.getElementsByTagName("content");
			Element content = (Element)n2.item(0);
			
			//Sign up content node
			NodeList nsignup = content.getElementsByTagName("signup");
			if(0 != nsignup.getLength())
			{
				Element signup = (Element) nsignup.item(0);
				messageobj.setNick(getTextValue(signup, "nick"));
				messageobj.setMobile((getTextValue(signup, "mobile")));
			}
			
			//Contact Request node
			NodeList ncreq = content.getElementsByTagName("mobileList");
			if(0 != ncreq.getLength())
			{	
				String [] mobileList = new String [2];
				Element mobile1 = (Element) ncreq.item(0);
				mobileList[0] = mobile1.getNodeValue();
				Element mobile2 = (Element) ncreq.item(1);
				mobileList[1] = mobile2.getNodeValue();
				
				messageobj.setMobileList(mobileList);				
			}
			
			//Contact Response node
			NodeList ncresp = content.getElementsByTagName("contactList");
			if(0 != ncresp.getLength())
			{	
				Element contactList = (Element) ncresp.item(0);
				Element cresp = (Element) contactList.getElementsByTagName("contact").item(0);
				messageobj.setNick(getTextValue(cresp, "nick"));
				messageobj.setMobile((getTextValue(cresp, "mobile")));			
			}
			
			//Chat Message
			NodeList nchatmess = content.getElementsByTagName("chatMessage");
			if(0 != nchatmess.getLength())
			{	
				Element chatmessage = (Element) nchatmess.item(0);
				//messageobj.setMobile(chatmessage.getNodeValue());	
				messageobj.setChatMessage(chatmessage.getTextContent());
			}
			
			//Connection
			NodeList ncon = content.getElementsByTagName("connection");
			if(0 != ncon.getLength())
			{	
				Element chatmessage = (Element) ncon.item(0);
				messageobj.setMobile(chatmessage.getNodeValue());			
			}
			
			//Response
			NodeList nresponse = content.getElementsByTagName("response");
			if(0 != nresponse.getLength())
			{	
				Element response = (Element) nresponse.item(0);
				messageobj.setResponseCode((byte) getIntValue(response, "responseCode"));
				messageobj.setResponseMessage(getTextValue(response, "responseMessage"));
			}	
			
			//Revocation
			NodeList nrevoc = content.getElementsByTagName("revokedMobile");
			if(0 != nrevoc.getLength())
			{	
				Element revoc = (Element) nrevoc.item(0);
				messageobj.setRevokedMobile(revoc.getNodeValue());				
			}	
			
			//Key Request
			NodeList nkeyreq = content.getElementsByTagName("keyrequest");
			if(0 != nkeyreq.getLength())
			{	
				Element keyreq = (Element) nkeyreq.item(0);
				// FALTA TYPE PARA KEY REQUEST
				messageobj.setMobile(getTextValue(keyreq, "mobile"));				
			}				
			
			//Download
			NodeList ndownload = content.getElementsByTagName("download");
			if(0 != ndownload.getLength())
			{	
				Element down = (Element) ndownload.item(0);
				// FALTA TYPE PARA DOWNLOAD
				messageobj.setKey(getTextValue(down, "key"));		
				messageobj.setMobile(getTextValue(down, "mobile"));
			}	
			
			//Upload
			NodeList nupload = content.getElementsByTagName("upload");
			if(0 != nupload.getLength())
			{	
				Element down = (Element) nupload.item(0);
				// FALTA TYPE PARA DOWNLOAD
				messageobj.setKey(getTextValue(down, "key"));		
			}				
			
			
			///////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////

			// Signature			
			NodeList n3 = doc.getElementsByTagName("signature");
			if(n3.getLength() != 0)
			{
				Element signature = (Element)n3.item(0);
				messageobj.setSignature(getTextValue(signature, "signature").getBytes());
			}



			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
				
		return messageobj;
	}
	
    public static Document loadXMLFromString(String xml) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
    
    

    private static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}


	/**
	 * Calls getTextValue and returns a int value
	 */
	private static int getIntValue(Element ele, String tagName) {
		//in production application you would catch the exception
		return Integer.parseInt(getTextValue(ele,tagName));
	}
}

