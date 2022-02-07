package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Identifier {
    private MessageDigest digester;
    private final String algorithm;

    public Identifier(String algorithm) {
        this.algorithm = algorithm;
        try {
            digester = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
    }

    public void read(byte[] data) {
        digester.update(data);
    }

    public String getUniqueId() {
        StringBuilder stringBuilder = new StringBuilder();
        byte[] bytes = digester.digest();
        for (byte b : bytes) {
            String binToHex = String.format("%02x", b);
            stringBuilder.append(binToHex);
        }
        return stringBuilder.toString();
    }

    public String getAlgorithm() {
        return algorithm;
    }

}
