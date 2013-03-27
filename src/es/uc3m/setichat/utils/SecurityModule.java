package es.uc3m.setichat.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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
	private Contact contact;
	
	public SecurityModule(Contact contact){
		this.contact = contact;
	}
	
	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	
	
	public byte[] encrypt(String content){
		
		sign(content);
		
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
	        keyGen.initialize(512);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.e("KEYPAIRGENERATOR", "ERROR CREATING KEYS");
		}
		
        KeyPair kp = keyGen.genKeyPair();
        
        PrivateKey privateKey = kp.getPrivate();
        PublicKey publicKey = kp.getPublic();
        
		Log.i("ORIGINAL MESSAGE", content);
		// Gen random key
		byte [] k = new byte[16];
		new Random().nextBytes(k);
		
		byte[] c = null;
		try {
			c = content.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
		Log.i("RSAKEY", "Length : "+RSA.length);
		Log.i("IVECTOR", "Length : "+cipher.getIV().length);
		byte [] xml = ArrayUtils.addAll(ArrayUtils.addAll(RSA, cipher.getIV()), encryptedMessage);
		Log.i("CONTENT", "SIZE: "+xml.length);
		Log.i("CODED MESSAGE XML", new String(xml));
		
		byte [] codedMessage = Base64.encodeToByte(xml, false);
		Log.i("base64 MESSAGE", new String(codedMessage));
		
		byte[]xml2 = Base64.decode(codedMessage);
		Log.i("CODED MESSAGE XML2", new String(xml2));
		
		Log.i("BASE64 CHECK","ARE EQUALS: "+ArrayUtils.isEquals(xml, xml2));
		
		// PRUEBA DE DESENCRIPTADO
		String prueba = decrypt(xml, privateKey);

		Log.i("ORIGINAL MESSAGE AFTER DECODING", prueba);
		
		Log.d("ENCRYPTION", "RESULT: "+prueba.equalsIgnoreCase(content));
		
		return xml;
	}
	
	private byte[] encryptRSA(byte[] key, PublicKey publicKey){
		//byte[] keyContact = getContact().getPublicKey();
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
		return result;
	}
	
	public String decrypt (byte[] bContent, PrivateKey privateKey){
		//TODO: Inicializar RSA_LENGTH y VECTOR_LENGTH con los valores correspondientes
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
		
		String result = null;
		try {
			result = new String(originalMessage, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	private byte[] decryptRSA(PrivateKey privateKey, byte[]rsaKey){
		//byte[] pKey = privateKey.getBytes();
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
		return result;
	}
	
	public boolean sign(String content){
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
	        keyGen.initialize(512);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.e("KEYPAIRGENERATOR", "ERROR CREATING KEYS");
		}
		
        KeyPair kp = keyGen.genKeyPair();
        
        PrivateKey privateKey = kp.getPrivate();
        PublicKey publicKey = kp.getPublic();
        
        byte[] bContent = null;
		try {
			bContent = content.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
        
        byte [] b64code = Base64.encodeToByte(signatureResult, false);
        
        // Verifying
        byte [] b64decode = Base64.decode(b64code);
        
        Log.d("SIGNING", "RESULT: "+ArrayUtils.isEquals(signatureResult, b64decode));
        
        try {
			sign.initVerify(publicKey);
	        sign.update(bContent);
		} catch (InvalidKeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // Return verification result
        boolean result = false;
		try {
			result = sign.verify(signatureResult);
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Log.d("SIGNING", ""+result);
		return result;
	}
}
