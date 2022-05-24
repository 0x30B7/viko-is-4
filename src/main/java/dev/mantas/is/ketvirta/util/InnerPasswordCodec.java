package dev.mantas.is.ketvirta.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class InnerPasswordCodec {

    private static final int DES_SECRET_LENGTH = 24;
    private static final int DES_IV_LENGTH = 8;

    private InnerPasswordCodec() { }

    public static InnerPassword encrypt(String rawPassword) throws Exception {
        byte[] secret = generateSecret();
        byte[] iv = generateIv();

        Cipher encryptCipher = Cipher.getInstance("TripleDES/CBC/PKCS5Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret, "TripleDES"), new IvParameterSpec(iv));

        byte[] rawPasswordBytes = rawPassword.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedPassword = encryptCipher.doFinal(rawPasswordBytes);

        return new InnerPassword(secret, iv, encryptedPassword);
    }

    public static String decrypt(InnerPassword password) throws Exception {
        Cipher decryptCipher = Cipher.getInstance("TripleDES/CBC/PKCS5Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(password.secret, "TripleDES"), new IvParameterSpec(password.iv));
        byte[] encryptedPassword = decryptCipher.doFinal(password.encryptedPassword);
        return new String(encryptedPassword, StandardCharsets.UTF_8);
    }

    public static String serialize(InnerPassword password) {
        byte[] combined = new byte[password.secret.length + password.iv.length + password.encryptedPassword.length];
        System.arraycopy(password.secret, 0, combined, 0, password.secret.length);
        System.arraycopy(password.iv, 0, combined, password.secret.length, password.iv.length);
        System.arraycopy(password.encryptedPassword, 0, combined, password.secret.length + password.iv.length, password.encryptedPassword.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static InnerPassword deserialize(String password) {
        byte[] combined = Base64.getDecoder().decode(password.getBytes(StandardCharsets.UTF_8));
        return new InnerPassword(
                Arrays.copyOfRange(combined, 0, DES_SECRET_LENGTH),
                Arrays.copyOfRange(combined, DES_SECRET_LENGTH, DES_SECRET_LENGTH + DES_IV_LENGTH),
                Arrays.copyOfRange(combined, DES_SECRET_LENGTH + DES_IV_LENGTH, combined.length)
        );
    }

    private static byte[] generateSecret() {
        byte[] arr = new byte[DES_SECRET_LENGTH];
        new SecureRandom().nextBytes(arr);
        return arr;
    }

    private static byte[] generateIv() {
        byte[] arr = new byte[DES_IV_LENGTH];
        new SecureRandom().nextBytes(arr);
        return arr;
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class InnerPassword {

        private byte[] secret;
        private byte[] iv;
        private byte[] encryptedPassword;

    }

}
