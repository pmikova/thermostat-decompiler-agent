/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.decompiler.agent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 *
 * @author pmikova
 */
public class InstrumentationProvider {

    private final Transformer transformer;
    private final Instrumentation instrumentation;

    InstrumentationProvider(Instrumentation inst, Transformer transformer) {
        this.transformer = transformer;
        this.instrumentation = inst;
    }

   private byte[] getClassBody(Class clazz) throws UnmodifiableClassException {
        transformer.allowToSaveBytecode();
        byte[] result;
        try{
            transformer.allowToSaveBytecode();
        instrumentation.retransformClasses(clazz);
        result = transformer.getLastValidResult();
        }
        catch (RuntimeException ex){
            throw new RuntimeException(ex);
        }
        
        transformer.denyToSaveBytecode();
        transformer.resetLastValidResult();
        return result;
    }

    public byte[] findClassBody(String className) throws UnmodifiableClassException {
        return getClassBody(findClass(className));

    }

    private Class findClass(String className) {
        Class[] classes = instrumentation.getAllLoadedClasses();
        for (Class classe : classes) {
            if (classe.getName().equals(className)) {
                return classe;
            }
        }
        throw new RuntimeException("Class " + className + " not found in loaded classes.");
    }

   public String[] getClassesNames() {
        Class[] loadedClasses = instrumentation.getAllLoadedClasses();
        String[] r = new String[1000/*loadedClasses.length*/];
        for (int i = 0; i < r.length; i++) {
            Class loadedClasse = loadedClasses[i];
            r[i] = loadedClasse.getName();

        }
        return r;
    }

}
