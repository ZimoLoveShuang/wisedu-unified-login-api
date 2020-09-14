package wiki.zimo.wiseduunifiedloginapi.helper;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
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

    /*public static void main(String[] args) throws Exception {
        System.out.println(encrypt("123456"));
    }*/
}
