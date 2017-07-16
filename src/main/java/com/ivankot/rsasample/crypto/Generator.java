/*
 * The MIT License
 *
 * Copyright 2017 Ivan.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
