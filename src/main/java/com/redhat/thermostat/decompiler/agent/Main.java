/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.decompiler.agent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author pmikova
 */
public class Main {

    private static final String ADDRESS = "address:";
    private static final String PORT = "port:";
    private static String hostname;
    private static Integer port;
    


    /**
     * @param agentArgs
     * @param inst
     * @throws java.lang.instrument.IllegalClassFormatException
     * @throws java.lang.instrument.UnmodifiableClassException
     * @throws java.io.IOException
     */
    public static void premain(String agentArgs, Instrumentation inst) throws IllegalClassFormatException, UnmodifiableClassException, IOException, Exception {
        Socket dummy = new Socket();
        Transformer transformer = new Transformer();
        inst.addTransformer(transformer);
        InstrumentationProvider p  = new InstrumentationProvider(inst, transformer);
        
         if (agentArgs != null) {
            String[] argsArray = agentArgs.split(",");
            for (String arg : argsArray) {
                if (arg.startsWith(ADDRESS)) {
                    hostname = arg.substring(ADDRESS.length(), arg.length());
                    

                } else if (arg.startsWith("port:")) {
                    try {
                        port = Integer.valueOf(arg.substring(PORT.length(), arg.length()));
                        if (port <= 0) {
                            System.err.println("Invalid port specified [" + port + "]");
                            port = null;

                        }
                        } catch (Exception e) {
                        System.err.println("Invalid port specified [" + arg + "]. Cause: " + e);
                    }
                }}}

        boolean listenerStarted = AgentActionListener.initialize(hostname, port, p);               
        
        }
    


    public static void agentmain(String args, Instrumentation inst) throws Exception {
        premain(args, inst);
    }
    
    
}
