package dev.mantas.is.ketvirta.model.database;

import dev.mantas.is.ketvirta.util.InnerPasswordCodec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

public class DatabaseEncryption {

    private static final int AES_IV_LENGTH = 16;

    public static byte[] encrypt(String dbData, InnerPasswordCodec.InnerPassword masterPassword) throws Exception {
        String masterSalt = "master";
        byte[] iv = generateIv();

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getKeyFromPassword(masterPassword, masterSalt), new IvParameterSpec(iv));
        byte[] e = cipher.doFinal(dbData.getBytes(StandardCharsets.UTF_8));
        // Concat IV - as per https://stackoverflow.com/questions/44694994/storing-iv-when-using-aes-asymmetric-encryption-and-decryption
        byte[] eWithIV = ByteBuffer.allocate(e.length + iv.length).put(e).put(iv).array();

        return eWithIV;
    }

    public static String decrypt(String dbDataEncrypted, InnerPasswordCodec.InnerPassword masterPassword) throws Exception {
        byte[] combinedData = Base64.getDecoder().decode(dbDataEncrypted.getBytes());
        byte[] data = Arrays.copyOfRange(combinedData, 0, combinedData.length - AES_IV_LENGTH);
        byte[] iv = Arrays.copyOfRange(combinedData, combinedData.length - AES_IV_LENGTH, combinedData.length);

        String masterSalt = "master";

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, getKeyFromPassword(masterPassword, masterSalt), new IvParameterSpec(iv));
        byte[] d = cipher.doFinal(data);

        return new String(d, StandardCharsets.UTF_8);
    }

    private static SecretKey getKeyFromPassword(InnerPasswordCodec.InnerPassword password, String salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(InnerPasswordCodec.decrypt(password).toCharArray(), salt.getBytes(), 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private static byte[] generateIv() {
        byte[] iv = new byte[AES_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

}
