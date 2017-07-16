/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
