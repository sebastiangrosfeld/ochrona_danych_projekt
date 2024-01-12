package ee.sebgros.secureapplication.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
@Slf4j
public class AESService {
	@Value("${ee.password.salt")
	private String salt;


	private SecretKey getKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
		SecretKey tmp = factory.generateSecret(spec);
		return new SecretKeySpec(tmp.getEncoded(), "AES");
	}

	public String encrypt(String key, String value)
			throws GeneralSecurityException {


		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, getKey(key),
				new IvParameterSpec(new byte[16]));
		return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
	}

	public String decrypt(String key, String baseEncrypted)
			throws GeneralSecurityException {
		byte[] encrypted = Base64.getDecoder().decode(baseEncrypted);


		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, getKey(key),
				new IvParameterSpec(new byte[16]));
		try {
			byte[] original = cipher.doFinal(encrypted);
			return new String(original, StandardCharsets.UTF_8);
		} catch (BadPaddingException e) {
			throw new GeneralSecurityException();
		}
	}
}
