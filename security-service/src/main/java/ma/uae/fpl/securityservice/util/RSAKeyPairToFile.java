package ma.uae.fpl.securityservice.util;

import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Support du TP : genere une paire de cles RSA (2048 bits) au format PEM.
 * A lancer une seule fois, puis copier publicKey.pem / privateKey.pem
 * dans src/main/resources/certs.
 */
public class RSAKeyPairToFile {

    public static void main(String[] args) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n"
                    + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(publicKey.getEncoded())
                    + "\n-----END PUBLIC KEY-----\n";
            String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n"
                    + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(privateKey.getEncoded())
                    + "\n-----END PRIVATE KEY-----\n";

            writeToFile("publicKey.pem", publicKeyPEM);
            writeToFile("privateKey.pem", privateKeyPEM);
            System.out.println("Cle publique et privee generees et stockees dans des fichiers.");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(String filePath, String content) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
