/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.decompiler.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;

/**
 *
 * @author pmikova
 */
public class Transformer implements ClassFileTransformer {

    private boolean allowToSaveBytecode = false;
    private HashMap<String, byte[]> results = new HashMap<>();

    // I really do not like to throw exception here, in premain, the jvm will crash, in agentmain, it will be ignored...
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (allowToSaveBytecode) {

            results.put(className, classfileBuffer);
                    }

        return null;
    }

    public byte[] getResult(String name) {
        return results.get(name);
    }

    public void resetLastValidResult() {
        results = new HashMap<>();
    }

    public void allowToSaveBytecode() {
        allowToSaveBytecode = true;
    }

    public void denyToSaveBytecode() {
        allowToSaveBytecode = false;
    }
}
