/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.thermostat.decompiler.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.instrument.UnmodifiableClassException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pmikova
 */
public class AgentActionListener extends Thread {

    private static AgentActionListener theActionListener = null;

    public static int DEFAULT_PORT = 9091;
    public static String DEFAULT_HOST = "localhost";
    private ServerSocket theServerSocket;
    private InstrumentationProvider provider;
    private static String hostnameGiven;
    private static Integer portGiven;

    private AgentActionListener(InstrumentationProvider p, ServerSocket ss) {
        provider = p;
        theServerSocket = ss;
        setDaemon(true);
    }

    public static synchronized boolean initialize(String hostname, Integer port, InstrumentationProvider prov) {
        AgentActionListener.hostnameGiven = hostname;
        portGiven = port;
        if (theActionListener == null) {
            ServerSocket ss = null;
            try {
                if (hostname == null) {
                    hostname = DEFAULT_HOST;
                }
                if (port == null) {
                    port = DEFAULT_PORT;
                }
                ss = new ServerSocket();
                ss.bind(new InetSocketAddress(hostname, port));
                //logger.log(Level.FINE, "TransformListener() : accepting requests on " + hostname + ":" + port);
            } catch (IOException e) {
                //logger.log(Level.WARNING, "TransformListener() : unexpected exception opening server socket " + e);
                //Helper.errTraceException(e);
                return false;
            }

            theActionListener = new AgentActionListener(prov, ss);

            theActionListener.start();
        }

        return true;
    }

    public synchronized boolean terminate() {

        if (theActionListener != null) {
            try {
                theServerSocket.close();
                //Helper.verbose("TransformListener() :  closing port " + DEFAULT_PORT);

            } catch (IOException e) {
                // ignore -- the thread should exit anyway
            }
            try {
                theActionListener.join();
            } catch (InterruptedException e) {
                // ignore
            }

            theActionListener = null;
            theServerSocket = null;
        }

        return true;
    }

    @Override
    public void run()
    {

        while (true) {
            if (theServerSocket.isClosed()) {
                return;
                }
            Socket socket = null;
            try {
                socket = theServerSocket.accept();
            } catch (IOException e) {
                if (!theServerSocket.isClosed()) {
                    //Helper.err("TransformListener.run : exception from server socket accept " + e);
                    //Helper.errTraceException(e);
                }
                return;
            }

            //Helper.verbose("TransformListener() : handling connection on port " + socket.getLocalPort());
            try {
                handleConnection(socket);
            } catch (Exception e) {
                //Helper.err("TransformListener() : error handling connection on port " + socket.getLocalPort());
                try {
                    socket.close();
                } catch (IOException e1) {
                    // do nothing
                }
            }
        }
    }

    private void handleConnection(Socket socket) {
        InputStream is = null;
        try {
            is = socket.getInputStream();
        } catch (IOException e) {
            // oops. cannot handle this
            //Helper.err("TransformListener.run : error opening socket input stream " + e);
            //Helper.errTraceException(e);

            try {
                socket.close();
            } catch (IOException e1) {
                //Helper.err("TransformListener.run : exception closing socket after failed input stream open" + e1);
                //Helper.errTraceException(e1);
            }
            return;
        }

        OutputStream os = null;
        try {
            os = socket.getOutputStream();
        } catch (IOException e) {
            // oops. cannot handle this
            //Helper.err("TransformListener.run : error opening socket output stream " + e);
            //Helper.errTraceException(e);

            try {
                socket.close();
            } catch (IOException e1) {
                //Helper.err("TransformListener.run : exception closing socket after failed output stream open" + e1);
                //Helper.errTraceException(e1);
            }
            return;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));

        String line = null;
        try {
            line = in.readLine();
            System.out.println("line");
        } catch (IOException e) {
            e.printStackTrace();
            //Helper.err("TransformListener.run : exception " + e + " while reading command");
            //Helper.errTraceException(e);
        }
        try {
            if (line == null) {

                System.out.println("error");
                out.write("ERROR\n");
                out.flush();
            } else if (line.equals("CLASSES")) {
                System.out.println("classes");
                getAllLoadedClasses(in, out);
            } else if (line.equals("BYTES")) {
                sendByteCode(in, out);
            } else {
                out.write("ERROR\n");
                out.flush();
            }
        } catch (Exception e) {
            //logger.log(Level.WARNING, "TransformListener.run : exception " + e + " processing command " + line);

        } finally {
            try {
                socket.close();
            } catch (IOException e1) {
                //Helper.err("TransformListener.run : exception closing socket " + e1);
                //Helper.errTraceException(e1);
            }
        }
    }

    private void getAllLoadedClasses(BufferedReader in, BufferedWriter out) throws IOException {
        out.write("CLASSES");
        out.newLine();
        String[] classList = provider.getClassesNames();
        for (String classe : classList) {
            out.write(classe);
            out.newLine();
        }
        out.flush();
    }

    private void sendByteCode(BufferedReader in, BufferedWriter out) throws IOException {
        String className = in.readLine();
        if (className == null) {
            out.write("ERROR\n");
            out.flush();
            return;
        }
        out.write("BYTES");
        out.newLine();
        try {
            byte[] body = provider.findClassBody(className);
            String encoded = Base64.getEncoder().encodeToString(body);
            out.write(encoded);
            out.newLine();
        } catch (Exception ex) {
            out.write("ERROR\n");
        }
        out.flush();
    }

}