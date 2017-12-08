/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.nativeagent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author pmikova
 */
public class ClassHandler {

    private static ClassHandler singleton;
    public String path = "/home/pmikova/Desktop/files/";


    public static Class[] loadedClasses;
    // class names separated with ; separator
    public ArrayList<String> loadedClassesNames;
    
    public HashMap<String, byte[]> transformedClasses;

    private ClassHandler() {
        loadedClassesNames = new ArrayList<>();
        transformedClasses = new HashMap<>();

    }
    public byte[] getByteArray(String name){
        return transformedClasses.get(name);
    }
    
    // here buggy behaviour might appear
    public void addClassByteCode(String name, byte[] bytecode){       
        String slashedName = name.replace("/", ".");
        transformedClasses.put(slashedName, bytecode);
    }
    
    public ArrayList<String> getClassesNames(){
        return loadedClassesNames;
    }

    public static ClassHandler getInstance() {
        if (singleton == null) {
            singleton = new ClassHandler();
        }
        return singleton;
    }

    public Class[] getLoadedClasses() {
        
        return ClassHandler.loadedClasses;
    }
    
    public void setLoadedClasses(Class[] classes){
        clearClassBuffer();
        this.loadedClasses = classes;
        convertToString();
        
    }
    
    public void convertToString(){
        ArrayList<String> classBuffer = new ArrayList<>();
        for (Class classe : loadedClasses) {
            String name = classe.getName();
            classBuffer.add(name);
            
        }
        this.loadedClassesNames = classBuffer;
    }


    public void clearClassBuffer() {
        ClassHandler.loadedClasses = null;
    }

    
    public Class findClass(String name){
        for (Class classe : loadedClasses) {
            if (classe.getName().equals(name)){
                return classe;
            }           
        }
        return null;
    }
    
    public void cleanUp(){
        clearClassBuffer();
        this.loadedClassesNames = null;
        this.transformedClasses = null;
        
        
    }
}
