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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * @author Ivan
 */
public enum Provider {

    /**
     *
     */
    INSTANCE;

    /**
     * Provides access to keychain tool
     *
     * @return Keychain keychain tool used
     */
    public Keychain getKeychain() {
        return Keychain.INSTANCE;
    }

    /**
     * Provides access to the encoder tool
     *
     * @return Encoder encoder tool instance
     */
    public Cipher getEncoder() {
        return Cipher.ENCRYPTOR;
    }

    /**
     *
     * @return
     */
    public Cipher getDecoder() {
        return Cipher.DECRYPTOR;
    }

    /**
     *
     */
    public enum Keychain {

        /**
         *
         */
        INSTANCE;

        /**
         *
         */
        public static final int CRYPTO_KEY_SIZE = 2048;

        /**
         *
         */
        public static final String CRYTO_ALG = "RSA";

        /**
         *
         * @return
         */
        public KeyPair generateKeyPair() {
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance(CRYTO_ALG);
                generator.initialize(CRYPTO_KEY_SIZE);
                java.security.KeyPair kp = generator.generateKeyPair();
                return new KeyPair(kp.getPrivate(), kp.getPublic());
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Provider.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

    }

    /**
     *
     */
    public enum Cipher {

        /**
         *
         */
        ENCRYPTOR(Cipher.STRATEGY_ENCRYPT),

        /**
         *
         */
        DECRYPTOR(Cipher.STRATEGY_DECRYPT);

        /**
         *
         */
        public static final String CRYPTO_ALG = "RSA";

        private static final String STRATEGY_ENCRYPT = "encrypt";
        private static final String STRATEGY_DECRYPT = "decrypt";

        private final String strategy;
        private final Builder builder = new Builder();

        private Cipher(String strategy) {
            this.strategy = strategy;
        }
        
        /**
         *
         * @return
         */
        public Builder builder() {
            return builder.get(strategy);
        }

        /**
         *
         */
        public class Builder {

            private static final String OUTPUT_STDOUT = "stdout";

            private String strategy;
            private String key;
            private String input;
            private String output = OUTPUT_STDOUT;
            private boolean background = false;
            private boolean verbose = false;

            /**
             *
             * @param strategy
             * @return
             */
            public Builder get(String strategy) {
                this.strategy = strategy;
                return this;

            }

            /**
             *
             * @param key
             * @return
             */
            public Builder key(String key) {
                this.key = key;
                return this;
            }

            /**
             *
             * @param input
             * @return
             */
            public Builder input(String input) {
                this.input = input;
                return this;
            }

            /**
             *
             * @param output
             * @return
             */
            public Builder output(String output) {
                this.output = output;
                return this;
            }

            /**
             *
             * @param background
             * @return
             */
            public Builder background(boolean background) {
                this.background = background;
                return this;
            }

            /**
             *
             * @param verbose
             * @return
             */
            public Builder verbose(boolean verbose) {
                this.verbose = verbose;
                return this;
            }

            /**
             *
             * @return
             */
            public boolean doFinal() {
                if (background) {
                    Thread thread = new Thread(() -> {
                        cipher();
                    });
                    thread.setDaemon(true);
                    thread.start();
                    return true;
                }
                return cipher();
            }

            private byte[] encrypt(byte[] key, byte[] input) {
                byte[] encrypted = null;
                try {
                    PrivateKey privateKey = KeyFactory.getInstance(CRYPTO_ALG).generatePrivate(new PKCS8EncodedKeySpec(key));
                    javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CRYPTO_ALG);
                    cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, privateKey);
                    encrypted = cipher.doFinal(input);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                    Logger.getLogger(Provider.class.getName()).log(Level.SEVERE, null, ex);
                }
                return encrypted;
            }

            private byte[] decrypt(byte[] key, byte[] input) {
                byte[] decrypted = null;
                try {
                    PublicKey publicKey = KeyFactory.getInstance(CRYPTO_ALG).generatePublic(new X509EncodedKeySpec(key));
                    javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CRYPTO_ALG);
                    cipher.init(javax.crypto.Cipher.DECRYPT_MODE, publicKey);
                    decrypted = cipher.doFinal(input);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
                    Logger.getLogger(Provider.class.getName()).log(Level.SEVERE, null, ex);
                }
                return decrypted;

            }

            private byte[] readKey(Path keyPath) {
                byte[] keyBytes = null;
                try {
                    List<String> lines = Files.readAllLines(keyPath);
                    keyBytes = Base64.getDecoder().decode(String.join("", lines));
                } catch (IOException ex) {
                    Logger.getLogger(Provider.class.getName()).log(Level.SEVERE, null, ex);
                }
                return keyBytes;
            }

            private boolean cipher() {
                boolean result = false;
                Path inputPath = Paths.get(input);
                Path outputPath = Paths.get(output);
                Path keyPath = Paths.get(key);
                
                if (Files.exists(keyPath) && Files.exists(inputPath)
                        && (output.equals(OUTPUT_STDOUT)
                        || (Files.exists(outputPath) && Files.isWritable(outputPath))
                        || (!Files.exists(outputPath) && Files.isWritable(outputPath.getParent())))) {
                    try {
                        byte[] keyBytes = readKey(keyPath);
                        byte[] inputBytes = Files.readAllBytes(inputPath);
                        byte[] encodedBytes = (strategy.equals(STRATEGY_ENCRYPT))
                                ? encrypt(keyBytes, inputBytes)
                                : decrypt(keyBytes, inputBytes);

                        if (output.equals(OUTPUT_STDOUT)) {
                            
                            String cipheredString = strategy.equals(STRATEGY_ENCRYPT)
                                    ? Base64.getEncoder().encodeToString(encodedBytes)
                                    : new String(encodedBytes);
                            
                            System.out.println(cipheredString);
                            
                        } else {
                            Files.write(outputPath, encodedBytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        }
                        result = true;
                    } catch (IOException ex) {
                        Logger.getLogger(Provider.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

                return result;
            }

        }

    }

}
