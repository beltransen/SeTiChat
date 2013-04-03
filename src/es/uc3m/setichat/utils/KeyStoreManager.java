package es.uc3m.setichat.utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import android.util.Log;

public class KeyStoreManager {
	
	private static final int KEYSIZE = 512;
	private static final String pkAlias = "privateKey";
	
	
	public static int getKeysize() {
		return KEYSIZE;
	}

	public KeyStoreManager () {}
	
	public static KeyPair generateNewKeys(String keystore){
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
	        keyGen.initialize(KEYSIZE);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.e("KEYPAIRGENERATOR", "ERROR CREATING KEYS");
		}
		
        KeyPair kp = keyGen.genKeyPair();
		savePrivateKey(kp.getPrivate(), keystore);
		return kp;
	}
	
	private static void savePrivateKey(PrivateKey pk, String keystore){
		
	}
	
	public static PrivateKey getPrivateKey(String keystore){
		return null;
	}
}
