/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.decompiler.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 *
 * @author pmikova
 */
public class Transformer implements ClassFileTransformer {

    private boolean allowToSaveBytecode = false;
    private byte[] lastValidResult;

    // I really do not like to throw exception here, in premain, the jvm will crash, in agentmain, it will be ignored...
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (allowToSaveBytecode) {
            if (lastValidResult!=null){
                throw new RuntimeException("last valid result was not used!");
            }
            lastValidResult = classfileBuffer;
        }

        return null;
    }

    public byte[] getLastValidResult() {
        return lastValidResult;
    }

    public void resetLastValidResult() {
        this.lastValidResult = null;
    }

    public void allowToSaveBytecode() {
        allowToSaveBytecode = true;
    }

    public void denyToSaveBytecode() {
        allowToSaveBytecode = false;
    }
}
