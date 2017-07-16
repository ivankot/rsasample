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

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 *
 * @author Ivan
 */
public class KeyPair {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    /**
     *
     * @param privateKey
     * @param publicKey
     */
    public KeyPair(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }
    
    /**
     *
     * @return
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    /**
     *
     * @return
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
}
