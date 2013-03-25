package es.uc3m.setichat.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.ArrayUtils;

import android.content.Context;
import es.uc3m.setichat.utils.datamodel.Contact;

public class SecurityModule {
	
	private final String PREFERENCES_FILE = "SeTiChat-Settings";
	private final String SERVER_NAME = "setichat@appspot.com";
	private Context context;
	private Contact contact;
	
	
	public SecurityModule(Contact contact){
		this.context = context;
		this.contact = contact;
	}
	
	public byte[] encrypt(String content){
		// Gen random key
		byte [] k = new byte[128];
		new Random().nextBytes(k);
		
		// Generate Random IV
		byte [] vector = new byte[16];
		new Random().nextBytes(vector);
		
		byte [] c = content.getBytes();
		byte [] encryptedMessage = null;
		
		// Cipher
		Cipher SymKeyCipher = null;
		SecretKeySpec aeskeySpec = new SecretKeySpec(k, "AES");
		try {
			SymKeyCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SymKeyCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
			encryptedMessage = SymKeyCipher.doFinal(c);
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
		byte [] RSA = encryptRSA(k);
		byte [] xml = ArrayUtils.addAll(ArrayUtils.addAll(RSA, vector), encryptedMessage);
		
		return xml;
	}
	
	private byte[] encryptRSA(byte[] key){
		byte[] keyContact =this.contact.getPublicKey();
		byte [] result = null;
		Cipher publicKeyCipher = null;
		SecretKeySpec rsakeySpec = new SecretKeySpec(keyContact, "RSA");
		try {
			publicKeyCipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
			publicKeyCipher.init(Cipher.ENCRYPT_MODE, rsakeySpec);
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
}
