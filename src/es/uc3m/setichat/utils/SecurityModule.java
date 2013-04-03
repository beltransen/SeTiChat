package es.uc3m.setichat.utils;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.ArrayUtils;

import android.util.Log;
import es.uc3m.setichat.utils.datamodel.Contact;

public class SecurityModule {
	
	private static final int RSA_LENGTH = 64;
	private static final int VECTOR_LENGTH = 16;
	private final String PREFERENCES_FILE = "SeTiChat-Settings";
	private final String SERVER_NAME = "setichat@appspot.com";
	private final String KEYSTORE_NAME = "settichat_keystore";
	private Contact contact;
	
	public SecurityModule(Contact contact){ // Needed for encryption
		this.contact = contact;
	}
	
	public SecurityModule(){}
	
	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public byte[] encrypt(String content, Contact contact){
		Log.i("ENCRYPTION", "ENCTRYPTED");
        PublicKey publicKey = null;
		try {
			byte [] key = contact.getPublicKey();
			if(key==null){return null;}
			publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(contact.getPublicKey()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
		// Gen random key
		byte [] k = new byte[16];
		new Random().nextBytes(k);
		
		byte[] c = Base64.decode(content);
		
		byte [] encryptedMessage = null;
		
		// Cipher initialization
		Cipher cipher = null;
		SecretKeySpec aeskeySpec = new SecretKeySpec(k, "AES");
		
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
			encryptedMessage = cipher.doFinal(c);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Encrypt key
		byte [] RSA = encryptRSA(k, publicKey);
		byte [] xml = ArrayUtils.addAll(ArrayUtils.addAll(RSA, cipher.getIV()), encryptedMessage);
		byte [] codedMessage = Base64.encodeToByte(xml, false);

		Log.i("ENCRYPTION", "FINISHED WITHOUT ERRORS");
		return codedMessage;
	}
	
	private byte[] encryptRSA(byte[] key, PublicKey publicKey){
		Log.i("RSA", "ENCRYPTING...");
		byte [] result = null;
		Cipher publicKeyCipher = null;
		try {
			publicKeyCipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
			publicKeyCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			result = publicKeyCipher.doFinal(key);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("RSA", "ENCTRYPTED...");
		return result;
	}
	
	public String decrypt (String content){
		Log.i("DECRYPTION", "STARTING...");
		
		byte [] bContent = Base64.decode(content);
        PrivateKey privateKey = KeyStoreManager.getPrivateKey(KEYSTORE_NAME);
        
		byte [] rsaKey = new byte [RSA_LENGTH];
		byte [] iv = new byte [VECTOR_LENGTH];
		byte [] aesContent = null;
		// Extract contents in different byte streams
		for(int i = 0; i < RSA_LENGTH; i++){
			rsaKey[i] = bContent[i];
		}
		for(int i = 0; i < VECTOR_LENGTH; i++){
			iv[i] = bContent[i+RSA_LENGTH];
		}
		
		aesContent = new byte[bContent.length-(RSA_LENGTH+VECTOR_LENGTH)];
		
		for(int i = 0; i < aesContent.length; i++){
			aesContent[i] = bContent[i+RSA_LENGTH + VECTOR_LENGTH];
		}
		
		// Start decryption
		byte [] aesDecryptedKey = decryptRSA(privateKey, rsaKey);
		
		Cipher cipher = null;
		SecretKeySpec aeskeySpec = new SecretKeySpec(aesDecryptedKey, "AES");
		
		byte [] originalMessage = null;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, aeskeySpec, new IvParameterSpec(iv));
			originalMessage = cipher.doFinal(aesContent);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("DECRYPTION", "FINISHED WITHOUT ERRORS");
		return Base64.encodeToString(originalMessage, false);
	}
	
	private byte[] decryptRSA(PrivateKey privateKey, byte[]rsaKey){
		Log.i("RSA", "DECRYPTING...");
		byte [] result = null;
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
			cipher.init (Cipher.DECRYPT_MODE, privateKey);
			result = cipher.doFinal(rsaKey);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("RSA", "DECRYPTED");
		return result;
	}
	
	public String sign(String content){
		Log.i("SIGNATURE", "SIGNING...");
		PrivateKey privateKey = KeyStoreManager.getPrivateKey(KEYSTORE_NAME);
		byte[] bContent = Base64.decode(content);
        // Signing
		Signature sign = null;
		byte [] signatureResult = null;
		try {
			sign = Signature.getInstance("SHA1withRSA");
	        sign.initSign(privateKey);
	        sign.update(bContent);
	        signatureResult = sign.sign();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String b64code = Base64.encodeToString(signatureResult, false);

		Log.i("SIGNATURE", "SIGNED");
        return b64code;
	}
	
	public boolean verifySignature (String content, byte[] b64signature){
		Log.i("SIGNATURE", "VERIFYING...");
		Signature sign = null;
		PublicKey publicKey = null;
		try {
			byte [] key = contact.getPublicKey();
			if(key==null){return false;}
			publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(contact.getPublicKey()));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
        // Verifying
        byte [] b64decode = Base64.decode(content);
        
        try {
        	sign = Signature.getInstance("SHA1withRSA");
			sign.initVerify(publicKey);
	        sign.update(b64decode);
		} catch (InvalidKeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // Return verification result
        boolean result = false;
		try {
			result = sign.verify(b64signature);
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.i("SIGNATURE", "RESULT: "+result);
		return result;
	}
}
