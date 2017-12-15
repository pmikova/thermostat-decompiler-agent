/*
 * To change this license header, choose License Headers inputStream Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template inputStream the editor.
 */
package com.redhat.thermostat.decompiler.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

/**
 * This class handles the socket accepting and request processing from the decompiler
 * @author pmikova
 */
public class AgentActionListener extends Thread {

    private static AgentActionListener inited = null;

    public static final int DEFAULT_PORT = 5395;
    public static final String DEFAULT_ADRESS = "localhost";
    private ServerSocket theServerSocket;
    private InstrumentationProvider provider;
    private static String addressGiven;
    private static Integer portGiven;

    private AgentActionListener(InstrumentationProvider provider, ServerSocket serverSocket) {
        this.provider = provider;
        this.theServerSocket = serverSocket;
        setDaemon(true);
    }

    /**
     * This method is used to create an AgentActionListener object and start
     * listener thread
     * @param hostname host name to open communication with
     * @param port on which open socket
     * @param provider this is where instrumentation and transformer objects are stored
     * 
     * @return boolean true if ran correctly, else false
     */
    public static synchronized boolean initialize(String hostname, Integer port,
            InstrumentationProvider provider) {
        AgentActionListener.addressGiven = hostname;
        portGiven = port;
        if (inited == null) {
            ServerSocket initServerSocket = null;
            try {
                if (port == null) {
                    port = DEFAULT_PORT;
                }
                if (hostname == null) {
                    hostname = DEFAULT_ADRESS;
                }
                initServerSocket = new ServerSocket();
                initServerSocket.bind(new InetSocketAddress(hostname, port));
            } catch (IOException e) {
                System.err.println("Exception occured when opening the socket: "
                        + e);
                return false;
            }

            inited = new AgentActionListener(provider, initServerSocket);
            inited.start();
        }

        return true;
    }

    @Override
    public void run() {

        while (true) {
            if (theServerSocket.isClosed()) {
                return;
            }
            Socket temporarySocket = null;
            try {
                temporarySocket = theServerSocket.accept();
            } catch (IOException e) {
                if (!theServerSocket.isClosed()) {
                    System.err.println("The server socket is closed, killing the thread.");
                }
                return;
            }

            try {
                executeRequest(temporarySocket);
            } catch (Exception e) {
                System.err.println("Error when trying to execute the request. Exception: " + e);
                try {
                    temporarySocket.close();
                } catch (IOException e1) {
                    System.err.println("Error when trying to close the socket: " + e1);
                    //we can ignore this one too, since we are closing the socket anyway
                }
            }
        }
    }

    private void executeRequest(Socket socket) {
        InputStream is = null;
        try {
            is = socket.getInputStream();
        } catch (IOException e) {
            System.err.println("Error when opening the input stream of the socket. Exception: " + e);

            try {
                socket.close();
            } catch (IOException e1) {
               System.err.println("Error when closing the socket. Exception: " + e1);
            }
            return;
        }

        OutputStream os = null;
        try {
            os = socket.getOutputStream();
        } catch (IOException e) {
            System.err.println("Error when opening the output stream of the socket. Exception: " + e);

            try {
                socket.close();
            } catch (IOException e1) {
                System.err.println("Error when closing the socket. Exception: " + e1);
            }
            return;
        }
        BufferedReader inputStream = new BufferedReader(new InputStreamReader(is));
        BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(os));

        String line = null;
        try {
            line = inputStream.readLine();
        } catch (IOException e) {
            System.err.println("Exception occured during reading of the line: " + e);
        }
        try {
            if (null == line) {
                outputStream.write("ERROR\n");
                outputStream.flush();
            } else switch (line) {
                case "CLASSES":
                    getAllLoadedClasses(inputStream, outputStream);
                    break;
                case "BYTES":
                    sendByteCode(inputStream, outputStream);
                    break;
                default:
                    outputStream.write("ERROR\n");
                    outputStream.flush();
                    break;
            }
        } catch (IOException e) {
            System.err.println("Exception occured while trying to process the request:" + e);

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Exception occured while trying to close the socket:" + e);
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
        
        try {
            byte[] body = provider.findClassBody(className);
            String encoded = Base64.getEncoder().encodeToString(body);
            out.write("BYTES");
            out.newLine();
            out.write(encoded);
            out.newLine();
        } catch (Exception ex) {
            out.write("ERROR\n");
        }
        out.flush();
    }

}
