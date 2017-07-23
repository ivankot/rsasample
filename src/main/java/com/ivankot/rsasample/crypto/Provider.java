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
 * Provider is responsible for all cryptographic-related functionality within
 * the application. 
 * It encapsulates the following entites: Keychain and Cipher
 * @author Ivan
 */
public enum Provider {

    /**
     * Singleton instance of the provider
     */
    INSTANCE;

    /**
     * Provides access to the keychain tool
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
     * Provides access to the decoder tool
     * @return
     */
    public Cipher getDecoder() {
        return Cipher.DECRYPTOR;
    }

    /**
     * Keychain tool that utilizes Generator on its lower level in order to 
     * create a KeyPair and attach it to keychain (itself)
     */
    public enum Keychain {

        /**
         * Singleton instance of the class
         */
        INSTANCE;

        /**
         * Default (non-changeable) key size, 2048 is secure enough for 
         * almost any scenario
         */
        public static final int CRYPTO_KEY_SIZE = 2048;

        /**
         * Encryption algorithm; since this is based around RSA defaults to that
         */
        public static final String CRYTO_ALG = "RSA";

        /**
         * Creates and returns a KeyPair using the key size and algorithm
         * @return KeyPair key pair with public and private keys ready for use
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
     * Cipher class is responsible for encryption and decryption of content
     * supplied to it, relies on the RSA algorithm and utilizes the strategy
     * pattern to define course of action: either encrypt data or decrypt it
     */
    public enum Cipher {

        /**
         * Singleton Cipher instance set to encrypt data
         */
        ENCRYPTOR(Cipher.STRATEGY_ENCRYPT),

        /**
         * Singleton Cipher instance set to decrypt data
         */
        DECRYPTOR(Cipher.STRATEGY_DECRYPT);

        /**
         * Name of the algorithm used, since it's related to RSA it is hardcoded 
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
         * Gets the builder class for Cipher
         * @return Builder builder class
         */
        public Builder builder() {
            return builder.get(strategy);
        }

        /**
         * Builder class to configure the Ciper for a specific task
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
             * Gets a Builder with a pre-defined strategy, normally used internally
             * by the Cipher when it's configured through its constructor
             * @param strategy strategy to use, should be one of Cipher's public constants for strategy (STRATEGY_*)
             * @return Builder instance
             */
            public Builder get(String strategy) {
                this.strategy = strategy;
                return this;

            }

            /**
             * Sets the key to use for the action
             * @param key key to use
             * @return Builder instance
             */
            public Builder key(String key) {
                this.key = key;
                return this;
            }

            /**
             * Sets the input to use for the action
             * @param input input to use for the action
             * @return Builder instance
             */
            public Builder input(String input) {
                this.input = input;
                return this;
            }

            /**
             * Sets where to place output
             * @param output path to file or 'stdout' for output stream
             * @return Builder instance
             */
            public Builder output(String output) {
                this.output = output;
                return this;
            }

            /**
             * Sets whether the process needs to run as a daemon
             * @param background true/false for background mode
             * @return Builder instance
             */
            public Builder background(boolean background) {
                this.background = background;
                return this;
            }

            /**
             * Tells the app to be more verbose, at this point it doesn't do much
             * @param verbose whether the app is verbose or not
             * @return Builder instance
             */
            public Builder verbose(boolean verbose) {
                this.verbose = verbose;
                return this;
            }

            /**
             * An analogy to Java's own cipher, does the encryption/decryption
             * @return boolean result of the operation, always true for background
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
