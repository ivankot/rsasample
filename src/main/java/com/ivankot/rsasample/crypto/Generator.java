package com.ivankot.rsasample.crypto;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

/**
 *
 * @author Ivan
 */
public class Generator {

    /**
     *
     */
    public static final String PUB_KEY_NAME = "public.key";

    /**
     *
     */
    public static final String PRIVATE_KEY_NAME = "private.key";

    private static final String ERR_COULD_NOT_CREATE_KP = "Could not create Key Pair";

    private final Provider provider;

    private String lastError = null;

    /**
     *
     * @param provider
     */
    public Generator(Provider provider) {
        this.provider = provider;
    }

    /**
     *
     * @return
     */
    public String getLastError() {
        return lastError;
    }

    /**
     *
     * @return
     */
    public boolean generate() {
        boolean result = false;
        KeyPair kp = Provider.INSTANCE.getKeychain().generateKeyPair();
        Path publicPath = Paths.get(PUB_KEY_NAME);
        Path privatePath = Paths.get(PRIVATE_KEY_NAME);

        if (null != kp) {

            if (writeKey(kp.getPublicKey().getEncoded(), publicPath) 
                    && writeKey(kp.getPrivateKey().getEncoded(), privatePath)) {
                result = true;
            }

        } else {
            setError(ERR_COULD_NOT_CREATE_KP);
        }

        return result;
    }

    private void setError(String message) {
        lastError = message;
    }

    private boolean writeKey(byte[] encoded, Path path) {
        boolean result = false;
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            String encodedKey = Base64.getEncoder().encodeToString(encoded);
            writer.write(encodedKey);
            result = true;
        } catch (IOException ex) {
            lastError = ex.getMessage();
        }
        return result;
    }

}
