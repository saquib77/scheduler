package com.scheduler.scheduler.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

@Service
public class AESCryptographyUtils {

    private final Logger logger = LoggerFactory.getLogger(AESCryptographyUtils.class);

    private String encryptionKey;
    private SecretKeySpec key;
    /**
     * Possible value are : 128,192,256
     */
    private String keyLength;
    private String iv;

    @Autowired
    public AESCryptographyUtils(@Value("${aes.encryption.key:k92hk3770}") final String encryptionKey,
                                @Value("${aes.iv:8c78aj@rj0ed03js}") final String iv,
                                @Value("${aes.key.length:256}") final String keyLength) {
        this.keyLength = keyLength;
        this.encryptionKey = encryptionKey;
        this.iv = iv;

    }

    @PostConstruct
    public void initIt() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-" + keyLength);
            byte[] key = md.digest(encryptionKey.getBytes("UTF-8"));
            this.key = new
                    SecretKeySpec(key, "AES");
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            logger.error("error while initilizing Cryptography", e);
        }

    }

    public String encrypt(String text) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(iv.getBytes("UTF-8")));
            return Base64.encodeBytes(cipher.doFinal(text.getBytes("UTF-8")));
        } catch (Exception e) {
            logger.error("error while encrypt ", e);
            throw new RuntimeException(e);
        }
    }


    public String decrypt(String text) {
        String decrypted = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            System.out.println(" key =" + this.key);
            cipher.init(Cipher.DECRYPT_MODE, this.key, new IvParameterSpec(iv.getBytes("UTF-8")));
            decrypted = new String(cipher.doFinal(Base64.decode(text)), "UTF-8");
        } catch (Exception e) {
            logger.error("error while decrypt ", e);
            throw new RuntimeException(e);
        }
        return decrypted;
    }


    private static String secretKey = "asiu8ui94reghb765rde3wsdfgnhytrdf";
    private static String salt = "ssshhhhhhhhhhh!!!!";

    public String encryptStr(String strToEncrypt) {
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return java.util.Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }


    public String decryptt(String strToDecrypt) {
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(java.util.Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }


}
