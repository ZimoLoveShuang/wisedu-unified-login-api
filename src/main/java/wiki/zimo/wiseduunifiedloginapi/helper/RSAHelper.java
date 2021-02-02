package wiki.zimo.wiseduunifiedloginapi.helper;

import org.apache.commons.codec.binary.Hex;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAHelper {
    private static final String CIPHER_NAME = "RSA";
    private static final String CHARSETNAME = "UTF-8";

    /**
     * 安徽建筑大学的加密过程
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt(String data) throws Exception {
        final String PUBLICKEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxKVG9heYiIhkJ1phCgbrFgUnhVFb96uG1pfDZ8OxskJo1gc6c1LXrr2ALZt9Remllvmoak36drc/SjkibHAWJFvxiZfmBKzpYgBHHKTgsZgIMSGxJEL77ELEccOxkqC4lIMqjSEirOcqUXlx8MmyklDtwbhd0ZJ5TFlBgvFIqhwIDAQAB";
        byte[] decode = Base64.decodeBase64(PUBLICKEY.getBytes(CHARSETNAME));
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance(CIPHER_NAME).generatePublic(new X509EncodedKeySpec(decode));
        Cipher cipher = Cipher.getInstance(CIPHER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return Base64.encodeBase64URLSafeString(cipher.doFinal(Base64.encodeBase64(data.getBytes(CHARSETNAME))));
    }


    /**
     * 昆明学院的加密过程
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt2(String data) throws Exception {
        data = encodeURIComponent(data);
        final String CIPHER_NOPADDING = "RSA/ECB/NoPadding";
        final String MODULUS = "00f0d1b6305ea6256c768f30b6a94ef6c9fa2ee0b8eea2ea5634f821925de774ac60e7cfe9d238489be12551b460ef7943fb0fc132fdfba35fd11a71e0b13d9fe4fed9af90eb69da8627fab28f9700ceb6747ef1e09d6b360553f5385bb8f6315a3c7f71fa0e491920fd18c8119e8ab97d96a06d618e945483d39d83e3a2cf2567";
        final String EXPONENT = "010001";
        Cipher cipher = Cipher.getInstance(CIPHER_NOPADDING);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(MODULUS, 16), new BigInteger(EXPONENT, 16));
        PublicKey key = KeyFactory.getInstance(CIPHER_NAME).generatePublic(keySpec);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] doFinal = cipher.doFinal(new StringBuffer(data).reverse().toString().getBytes(CHARSETNAME));
        char[] hex = Hex.encodeHex(doFinal);
        return new String(hex);
    }

    private static String encodeURIComponent(String uri) throws UnsupportedEncodingException {
        return URLEncoder.encode(uri, CHARSETNAME)
                .replaceAll("\\+", "%20");
    }

    private static String decodeURIComponent(String uri) throws UnsupportedEncodingException {
        return URLDecoder.decode(uri, CHARSETNAME)
                .replaceAll("%20", "\\+");
    }

    /**
     * 山东城市建设职业学院的加密过程
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt3(String data) throws Exception {
        data = encodeURIComponent(data);
        final String CIPHER_NOPADDING = "RSA/ECB/NoPadding";
        final String PUBLIC_EXPONENT = "010001";
        final String PRIVATE_EXPONENT = "413798867d69babed22e0dd3d4031c635f3e9dbca0fa50a32974a0e230787b7f7ba78caefbee828a051c690357a8cc31dba8efc738b4db22e887571ef1ec5a5a55b6d866f6a67527f6a7d78a127c9f687008bb540228b50aa2d1ca5a4ff71107234f936b611ac46432a26da9c302eaa7180820df70593353b3f8c0247fe97a45";
        final String MODULUS = "00b5eeb166e069920e80bebd1fea4829d3d1f3216f2aabe79b6c47a3c18dcee5fd22c2e7ac519cab59198ece036dcf289ea8201e2a0b9ded307f8fb704136eaeb670286f5ad44e691005ba9ea5af04ada5367cd724b5a26fdb5120cc95b6431604bd219c6b7d83a6f8f24b43918ea988a76f93c333aa5a20991493d4eb1117e7b1";
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(MODULUS, 16), new BigInteger(PUBLIC_EXPONENT, 16));
        PublicKey key = KeyFactory.getInstance(CIPHER_NAME).generatePublic(keySpec);
        Cipher cipher = Cipher.getInstance(CIPHER_NOPADDING);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] doFinal = cipher.doFinal(new StringBuffer(data).reverse().toString().getBytes(CHARSETNAME));
        char[] hex = Hex.encodeHex(doFinal);
        return new String(hex);
    }

    public static String decrypt3(String data) throws Exception {
        final String CIPHER_NOPADDING = "RSA/ECB/NoPadding";
        final String PUBLIC_EXPONENT = "010001";
        final String PRIVATE_EXPONENT = "413798867d69babed22e0dd3d4031c635f3e9dbca0fa50a32974a0e230787b7f7ba78caefbee828a051c690357a8cc31dba8efc738b4db22e887571ef1ec5a5a55b6d866f6a67527f6a7d78a127c9f687008bb540228b50aa2d1ca5a4ff71107234f936b611ac46432a26da9c302eaa7180820df70593353b3f8c0247fe97a45";
        final String MODULUS = "00b5eeb166e069920e80bebd1fea4829d3d1f3216f2aabe79b6c47a3c18dcee5fd22c2e7ac519cab59198ece036dcf289ea8201e2a0b9ded307f8fb704136eaeb670286f5ad44e691005ba9ea5af04ada5367cd724b5a26fdb5120cc95b6431604bd219c6b7d83a6f8f24b43918ea988a76f93c333aa5a20991493d4eb1117e7b1";
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(new BigInteger(MODULUS, 16), new BigInteger(PRIVATE_EXPONENT, 16));
        PrivateKey key = KeyFactory.getInstance(CIPHER_NAME).generatePrivate(keySpec);
        Cipher cipher = Cipher.getInstance(CIPHER_NOPADDING);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodeHex = Hex.decodeHex(data);
        byte[] doFinal = cipher.doFinal(decodeHex);
        return decodeURIComponent(new StringBuffer(new String(doFinal)).reverse().toString());
    }

    /**
     * 淮阴师范学院的加密过程
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static String encrypt4(String data) throws Exception {
        data = encodeURIComponent(data);
        final String CIPHER_NOPADDING = "RSA/ECB/NoPadding";
        final String MODULUS = "00852f8538afcddd152ddb550fe27f6af786fdddfbf3e9624628dfc91a4cb120257e38ba030300364bd706049519c9b5cd4b8600d688fea694c528c65156f273c413cb6271abd88dc067c5711db624d384a303d6ac7e0a322779683aff064fcbdc9600ea5bd46c89348e550575cbc470e78ef73269a0a7f3e4cd17cdc998fc9589";
        final String EXPONENT = "010001";
        Cipher cipher = Cipher.getInstance(CIPHER_NOPADDING);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(MODULUS, 16), new BigInteger(EXPONENT, 16));
        PublicKey key = KeyFactory.getInstance(CIPHER_NAME).generatePublic(keySpec);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] doFinal = cipher.doFinal(new StringBuffer(data).reverse().toString().getBytes(CHARSETNAME));
        char[] hex = Hex.encodeHex(doFinal);
        return new String(hex);
    }

    public static void main(String[] args) throws Exception {
//        System.out.println(encrypt("123456"));
        String data = "123456";
        String e = encrypt4(data);
        System.out.println(e);
    }
}
