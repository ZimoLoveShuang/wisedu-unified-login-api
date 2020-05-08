package wiki.zimo.wiseduunifiedloginapi.helper;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class AESHelper {

    /**
     * 金智教务系统的加密实现 Java
     *
     * @param password
     * @param key
     * @return
     * @throws Exception
     */
    public static String encryptAES(String password, String key) throws Exception {
        String randomString = randomString(RANDOM_STRING_LENGTH);
        String randomIv = randomString(RANDOM_IV_LENGTH);
        // 金智的加密步骤
        // 1.随机的64位字符拼接在密码前面
        // 2.标准的AES-128-CBC加密
        // 3.将加密后的结果进行Base64编码
        // 4.随机iv并不影响加密和解密的结果，因此，固定或者随机都可以，但必须是16位
        String encrypt = Base64Encrypt(AESEncrypt(randomString + password, key, randomIv));

        // 解密步骤
        // 1.将密文串进行Base64解码
        // 2.标准的AES-128-CBC解密
        // 3.裁掉密码串前面的64随机字符串
        // 4.随机iv并不影响加密和解密的结果，因此，固定或者随机都可以，但必须是16位
//        String decrypt = decryptAES(encrypt, key, randomString(RANDOM_IV_LENGTH));
//        System.out.println(encrypt);
//        System.out.println(decrypt);
        return encrypt;
    }

    /**
     * 金智教务系统的解密实现 Java
     *
     * @param encrypt
     * @param key
     * @param iv
     * @return
     * @throws Exception
     */
    public static String decryptAES(String encrypt, String key, String iv) throws Exception {
        return AESDecrypt(Base64Decrypt(encrypt), key, iv).substring(RANDOM_STRING_LENGTH);
    }

    private static final String CIPHER_NAME = "AES/CBC/PKCS5Padding";
    private static final String CHARSETNAME = "UTF-8";
    private static final String AES = "AES";
    private static final String BASE_STRING = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678";
    private static final int RANDOM_IV_LENGTH = 16;
    private static final int RANDOM_STRING_LENGTH = 64;

    /**
     * AES加密
     *
     * @param data
     * @param key
     * @param iv
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private static byte[] AESEncrypt(String data, String key, String iv) throws NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(CIPHER_NAME);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(CHARSETNAME), AES);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(CHARSETNAME));
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        return cipher.doFinal(data.getBytes(CHARSETNAME));
    }

    /**
     * Base64编码
     *
     * @param data
     * @return
     */
    private static String Base64Encrypt(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Base64解码
     *
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     */
    private static byte[] Base64Decrypt(String data) throws UnsupportedEncodingException {
        return Base64.getDecoder().decode(data.getBytes(CHARSETNAME));
    }

    /**
     * AES解密
     *
     * @param data
     * @param key
     * @param iv
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    private static String AESDecrypt(byte[] data, String key, String iv) throws UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(CIPHER_NAME);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(CHARSETNAME), AES);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(CHARSETNAME));
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        return new String(cipher.doFinal(data), CHARSETNAME);
    }

    /**
     * 获取随机字符
     *
     * @param bits
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static String randomString(int bits) throws NoSuchAlgorithmException {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < bits; i++) {
            Random random = new Random();
            buffer.append(BASE_STRING.charAt(random.nextInt(BASE_STRING.length())));
        }
        return buffer.toString();
    }

    /*public static void main(String[] args) throws Exception {
        String key = "7RUJ5oYZWpq02evh";
        String data = "abcd123456";
        String s = encryptAES(data, key);
        System.out.println(s);
        System.out.println(decryptAES(s, key, randomString(RANDOM_IV_LENGTH)));
    }*/
}