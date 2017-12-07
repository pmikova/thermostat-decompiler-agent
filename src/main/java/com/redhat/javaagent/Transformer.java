/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.javaagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

/**
 *
 * @author pmikova
 */
public class Transformer implements ClassFileTransformer{
    
    protected final Instrumentation inst;
    private static ClassHandler handler;

    public Transformer(Instrumentation inst)
            throws Exception
    {
        this.inst = inst;
        this.handler = ClassHandler.getInstance();
    }
    
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        
        handler.addClassByteCode(className, classfileBuffer);
        return null;
    }
    
    public Class[] getLoadedClasses(){
        return inst.getAllLoadedClasses();
    }
    
    public String retransformClass(Class classe) throws UnmodifiableClassException{
        inst.addTransformer(this, true);
        inst.retransformClasses(classe);  
        inst.removeTransformer(this);
        return classe.getName();
        
    }
    
}
