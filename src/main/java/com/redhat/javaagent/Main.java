/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.javaagent;

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

    public static ClassHandler classHandler;
    public static ArrayList<Class> classesToFind;
    private static final String ADDRESS = "address:";
    private static final String PORT = "port:";
    private static String hostname;
    private static Integer port;

    public static ClassHandler getHandler() {
        return Main.classHandler;
    }

    /**
     * @param agentArgs
     * @param inst
     * @throws java.lang.instrument.IllegalClassFormatException
     * @throws java.lang.instrument.UnmodifiableClassException
     * @throws java.io.IOException
     */
    public static void premain(String agentArgs, Instrumentation inst) throws IllegalClassFormatException, UnmodifiableClassException, IOException, Exception {
       // create a socket so we can be sure it is loaded before the transformer gets created. otherwise
        // we seem to hit a deadlock when trying to instrument socket
        Transformer transformer = new Transformer(inst);
        
        
        //parse the arguments
        System.out.println("ok");
        
         if (agentArgs != null) {
            // args are supplied separated by ',' characters
            String[] argsArray = agentArgs.split(",");
            // we accept extra jar files to be added to the boot/sys classpaths
            // script files to be scanned for rules
            // listener flag which implies use of a retransformer
            for (String arg : argsArray) {
                if (arg.startsWith(ADDRESS)) {
                    System.out.println("adresa");
                    hostname = arg.substring(ADDRESS.length(), arg.length());

                } else if (arg.startsWith("port:")) {
                    try {
                        port = Integer.valueOf(arg.substring(PORT.length(), arg.length()));
                        System.out.println("port");
                        if (port <= 0) {
                            System.err.println("Invalid port specified [" + port + "]");
                            port = null;

                        }
                        } catch (Exception e) {
                        System.err.println("Invalid port specified [" + arg + "]. Cause: " + e);
                    }
                }}}

        Socket dummy = new Socket();
        boolean listenerStarted = AgentActionListener.initialize(hostname, port, transformer);               
        
        }
    


    public static void agentmain(String args, Instrumentation inst) throws Exception {
        premain(args, inst);
    }
    
    
}
