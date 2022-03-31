package br.unicamp.fnjv.wasis.api.utils.crypto;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 Cryptographic Hash Algorithm
 */
public class SHA256 {

    private static MessageDigest algorithmDigest;
    private static Charset defaultCharset;

    static {
        try {
            algorithmDigest = MessageDigest.getInstance("SHA-256");
            defaultCharset = StandardCharsets.UTF_8;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the SHA-256 hash from a file.
     *
     * @param file
     *
     * @return hashedFile
     */
    public static String getHashFromFile(File file) throws IOException {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        byte[] encodedHash = algorithmDigest.digest(fileContent);

        return bytesToHex(encodedHash);
    }

    /**
     * Get the SHA-256 hash from a file.
     *
     * @param file
     *
     * @return hashedFile
     */
    public static String getHashFromFile(MultipartFile file) throws IOException {
        byte[] encodedHash = algorithmDigest.digest(file.getBytes());

        return bytesToHex(encodedHash);
    }

    /**
     * Get the SHA-256 hash from a message.
     *
     * @param message
     *
     * @return hashedMessage
     */
    public static String getHashFromMessage(String message) {
        byte[] encodedHash = algorithmDigest.digest(message.getBytes(defaultCharset));

        return bytesToHex(encodedHash);
    }

    /**
     * Get the hashed value in hexadecimal.
     *
     * @param hash
     *
     * @return hexString
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);

        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);

            if (hex.length() == 1) {
                hexString.append("0");
            }

            hexString.append(hex);
        }

        return hexString.toString();
    }

}