package wiki.zimo.wiseduunifiedloginapi.helper;

import org.apache.commons.codec.binary.Hex;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAHelper {
    private static final String CIPHER_NAME = "RSA";
    private static final String CHARSETNAME = "UTF-8";
    private static final String PUBLICKEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxKVG9heYiIhkJ1phCgbrFgUnhVFb96uG1pfDZ8OxskJo1gc6c1LXrr2ALZt9Remllvmoak36drc/SjkibHAWJFvxiZfmBKzpYgBHHKTgsZgIMSGxJEL77ELEccOxkqC4lIMqjSEirOcqUXlx8MmyklDtwbhd0ZJ5TFlBgvFIqhwIDAQAB";

    public static String encrypt(String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        byte[] decode = Base64.decodeBase64(PUBLICKEY.getBytes(CHARSETNAME));
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance(CIPHER_NAME).generatePublic(new X509EncodedKeySpec(decode));
        Cipher cipher = Cipher.getInstance(CIPHER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return Base64.encodeBase64URLSafeString(cipher.doFinal(Base64.encodeBase64(data.getBytes(CHARSETNAME))));
    }

    private static final String CIPHER_NOPADDING = "RSA/ECB/NoPadding";
    private static final String MODULUS = "00f0d1b6305ea6256c768f30b6a94ef6c9fa2ee0b8eea2ea5634f821925de774ac60e7cfe9d238489be12551b460ef7943fb0fc132fdfba35fd11a71e0b13d9fe4fed9af90eb69da8627fab28f9700ceb6747ef1e09d6b360553f5385bb8f6315a3c7f71fa0e491920fd18c8119e8ab97d96a06d618e945483d39d83e3a2cf2567";
    private static final String EXPONENT = "010001";

    public static String encryptNoPadding(String data) throws NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {
        Cipher cipher = Cipher.getInstance(CIPHER_NOPADDING);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(MODULUS, 16), new BigInteger(EXPONENT, 16));
        PublicKey key = KeyFactory.getInstance(CIPHER_NAME).generatePublic(keySpec);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] doFinal = cipher.doFinal(new StringBuffer(data).reverse().toString().getBytes(CHARSETNAME));
        char[] hex = Hex.encodeHex(doFinal);
        return new String(hex);
    }

    public static String encrypt2(String data) throws Exception {
        return encryptNoPadding(encodeURIComponent(data));
    }

    private static String encodeURIComponent(String uri) throws UnsupportedEncodingException {
        return URLEncoder.encode(uri, CHARSETNAME)
                .replaceAll("\\+", "%20");
    }

    public static void main(String[] args) throws Exception {
//        System.out.println(encrypt("123456"));
        String data = "123456";
        System.out.println(encrypt2(data));
    }
}
