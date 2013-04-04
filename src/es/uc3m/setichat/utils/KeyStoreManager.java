package es.uc3m.setichat.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.OpenableColumns;
import android.util.Log;

public class KeyStoreManager {
	
	private static final int KEYSIZE = 512;
	private static final String pkAlias = "privateKey";
	private final static String KEYSTORE_NAME = "SetiChatKeystore";
	private final static String CERTIFICATES_NAME = "setichat_certs";
	private final static char[] KEYSTORE_PASSWORD = "_C0WYgb1!+vd7>%LPh,19W`rYA\5-#zqk".toCharArray();
	private final static String PREFERENCES_FILE = "SeTiChat-Settings";
	
	public static int getKeysize() {
		return KEYSIZE;
	}

	public KeyStoreManager () {}
	
	public static KeyPair generateNewKeys(Context context){
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
		savePrivateKey(kp.getPrivate(),context);
		return kp;
	}
	
	public static void savePrivateKey(PrivateKey pk, Context context){
		/*try {
			String keyStorePassword = (new BufferedReader(new InputStreamReader(System.in))).readLine();
			KeyStore keystoreobj = KeyStore.getInstance("PKCS12");
			
			//File f = new File(KEYSTORE_NAME);
			//if(!f.exists()){
			//	FileOutputStream fOut = context.openFileOutput(KEYSTORE_NAME, Context.MODE_PRIVATE);
			//	OutputStreamWriter osw = new OutputStreamWriter(fOut); 
			//	osw.write("");
			//	osw.flush();
			//	osw.close();
				//fOut.flush();
				//fOut.close();				
			//}
			
			FileInputStream keystoreInputStream = new FileInputStream(KEYSTORE_NAME);
			keystoreobj.load(keystoreInputStream, KEYSTORE_PASSWORD);
			//keystoreobj.
			//keystoreInputStream.close();
				
			// Load the certificate chain (in X.509 DER encoding).
			//FileInputStream certificateStream = new FileInputStream(CERTIFICATES_NAME);
			//CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			//java.security.cert.Certificate[] chain = {};
			//chain = certificateFactory.generateCertificates(certificateStream).toArray(chain);
			//certificateStream.close();
			
			//Insert Key Entry to Keystore
			keystoreobj.setKeyEntry("privateKey", pk.getEncoded(),null);
			
			FileOutputStream keyStoreOutputStream = new FileOutputStream(KEYSTORE_NAME);
			keystoreobj.store(keyStoreOutputStream,keyStorePassword.toCharArray());
			keyStoreOutputStream.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}*/
		
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE, 0);
		SharedPreferences.Editor  editor = settings.edit();
		int length = pk.getEncoded().length;
		String prk = new String(Base64.encodeToByte(pk.getEncoded(),false));
		//editor.
		editor.putString("privateKey", prk);
		editor.commit();
			
			
	}
	
	public static PrivateKey getPrivateKey(Context context){
		// Load the private key (in PKCS#8 DER encoding).
		/*PrivateKey privateKey = null;
		try {
			File keyFile = new File(KEYSTORE_NAME);
			byte[] encodedKey = new byte[(int)keyFile.length()];
			FileInputStream keyInputStream = new FileInputStream(keyFile);
			keyInputStream.read(encodedKey);
			keyInputStream.close();
			KeyFactory rSAKeyFactory = KeyFactory.getInstance("RSA");
			privateKey = rSAKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		SharedPreferences settings = context.getSharedPreferences(PREFERENCES_FILE, 0);
		String prk = settings.getString("privateKey", null);
		byte [] pkb = Base64.decode(prk.getBytes());
	    EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(pkb);
	    KeyFactory generator;
	    PrivateKey privateKey = null;
		try {
			generator = KeyFactory.getInstance("RSA");
			privateKey = generator.generatePrivate(privateKeySpec);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return privateKey;
	 
	}
}
