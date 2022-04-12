package appserver.satellite;

import appserver.job.Job;
import appserver.comm.ConnectivityInfo;
import appserver.job.UnknownToolException;
import appserver.comm.Message;
import static appserver.comm.MessageTypes.JOB_REQUEST;
import static appserver.comm.MessageTypes.REGISTER_SATELLITE;
import appserver.job.Tool;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.PropertyHandler;
import java.lang.reflect.InvocationTargetException;

/**
 * Class [Satellite] Instances of this class represent computing nodes that execute jobs by
 * calling the callback method of tool a implementation, loading the tool's code dynamically over a network
 * or locally from the cache, if a tool got executed before.
 *
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class Satellite extends Thread {

    private ConnectivityInfo satelliteInfo = new ConnectivityInfo();
    private ConnectivityInfo serverInfo = new ConnectivityInfo();
    private HTTPClassLoader classLoader = null;
    private Hashtable toolsCache = null;

    public Satellite(String satellitePropertiesFile, String classLoaderPropertiesFile, String serverPropertiesFile) {
        Scanner fileScanner = null;
        
        File satelliteFile = new File(satellitePropertiesFile);
        File serverFile =  new File(serverPropertiesFile);
        File classLoaderFile = new File(classLoaderPropertiesFile);
        
        String line;
        String[] splitLine;
        
        // read this satellite's properties and populate satelliteInfo object,
        // which later on will be sent to the server
        // ...
        
        // Get our satellite properties file
        try
        {
            fileScanner = new Scanner(satelliteFile);
        }
        catch (FileNotFoundException e)
        {
            Logger.getLogger(Satellite.class.getName())
                .log(Level.SEVERE, null, e);
            System.out.println("Failed to find satellite properties file.");
            System.exit(1);
        }
        
        // Iterate through the properties
        while (fileScanner.hasNext())
        {
            // Get a line
            line = fileScanner.nextLine();
            
            // Skip lines that start with # or are empty
            if (line.isBlank() || line.charAt(0) == '#' )
            {
                // Do nothing
            }
            else
            {
                // Split on tabs to get our label and value
                splitLine = line.split("\t", 2);
                
                // Set the appropriate fields
                if (splitLine[0].equals("NAME"))
                {
                    satelliteInfo.setName(splitLine[1]);
                }
                else if (splitLine[0].equals("PORT"))
                {
                    satelliteInfo.setPort(Integer.parseInt(splitLine[1]));
                }
                else
                {
                    System.out.println("Failed to parse satellite properties "
                            + "file.");
                    System.exit(1);
                }
            }
        }
        
        // Close scanner
        fileScanner.close();
        
        // read properties of the application server and populate serverInfo object
        // other than satellites, the as doesn't have a human-readable name, so leave it out
        // ...
        
        // Get our satellite properties file
        try
        {
            fileScanner = new Scanner(serverFile);
        }
        catch (FileNotFoundException e)
        {
            Logger.getLogger(Satellite.class.getName())
                .log(Level.SEVERE, null, e);
            System.out.println("Failed to find server properties file.");
            System.exit(1);
        }
        
        // Iterate through the properties
        while (fileScanner.hasNext())
        {
            // Get a line
            line = fileScanner.nextLine();
            
            // Skip lines that start with # or are empty
            if (line.isBlank() || line.charAt(0) == '#' )
            {
                // Do nothing
            }
            else
            {
                // This file is annoying and uses '=' surrounded by whitespace
                // not tabs for some reason
                splitLine = line.split("=", 2);
                
                // Get rid of the whitespace padding
                splitLine[0] = splitLine[0].strip();
                splitLine[1] = splitLine[1].strip();
                
                // Set the appropriate fields
                if (splitLine[0].equals("HOST"))
                {
                    serverInfo.setName(splitLine[1]);
                }
                else if (splitLine[0].equals("PORT"))
                {
                    serverInfo.setPort(Integer.parseInt(splitLine[1]));
                }
                else
                {
                    System.out.println("Failed to parse server properties "
                            + "file.");
                    System.exit(1);
                }
            }
        }
        
        // May as well set this. Why not
        serverInfo.setName("Application Server");
        
        // Close scanner
        fileScanner.close();
        
        // read properties of the code server and create class loader
        // -------------------
        // ...

        // Create our class loader
        classLoader = new HTTPClassLoader();
        
        // Get our code server properties file
        try
        {
            fileScanner = new Scanner(classLoaderFile);
        }
        catch (FileNotFoundException e)
        {
            Logger.getLogger(Satellite.class.getName())
                .log(Level.SEVERE, null, e);
            System.out.println("Failed to find satellite properties file.");
            System.exit(1);
        }
        
        // Iterate through the properties
        while (fileScanner.hasNext())
        {
            // Get a line
            line = fileScanner.nextLine();
            
            // Skip lines that start with # or are empty
            if (line.isBlank() || line.charAt(0) == '#' )
            {
                // Do nothing
            }
            else
            {
                // Split on tabs to get our label and value
                splitLine = line.split("\t", 2);
                
                // Set the appropriate fields
                if (splitLine[0].equals("HOST"))
                {
                    classLoader.host = splitLine[1];
                }
                else if (splitLine[0].equals("PORT"))
                {
                    classLoader.port = Integer.parseInt(splitLine[1]);
                }
                else if (splitLine[0].equals("DOC_ROOT"))
                {
                    classLoader.classRootDir = splitLine[1];
                }
                else
                {
                    System.out.println("Failed to parse class loader "
                            + "properties file.");
                    System.exit(1);
                }
            }
        }
        
        // Close scanner
        fileScanner.close();
        
        // create tools cache
        // -------------------
        // ...
        
        // Create our cache
        toolsCache = new Hashtable<String, Tool>();
        
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        Socket socket = null;
        
        // register this satellite with the SatelliteManager on the server
        // ---------------------------------------------------------------
        // ...
        
        // Skip this for now
        
        // create server socket
        // ---------------------------------------------------------------
        // ...
        
        try
        {
            serverSocket = new ServerSocket(satelliteInfo.getPort());
        }
        catch (IOException e)
        {
            Logger.getLogger(Satellite.class.getName())
                .log(Level.SEVERE, null, e);
            System.out.println("Failed to create server socket.");
            System.exit(1);
        }
        
        // start taking job requests in a server loop
        // ---------------------------------------------------------------
        // ...
        
        // Infinite loop for now I suppose
        while (true)
        {
            try
            {
                socket = serverSocket.accept();
            }
            catch (IOException e)
            {
                Logger.getLogger(Satellite.class.getName())
                    .log(Level.SEVERE, null, e);
                System.out.println("Failed to accept job.");
            }
            
            new SatelliteThread(socket, this).start();
        }
    }

    // inner helper class that is instanciated in above server loop and processes single job requests
    private class SatelliteThread extends Thread {

        Satellite satellite = null;
        Socket jobRequest = null;
        ObjectInputStream readFromNet = null;
        ObjectOutputStream writeToNet = null;
        Message message = null;

        SatelliteThread(Socket jobRequest, Satellite satellite) {
            this.jobRequest = jobRequest;
            this.satellite = satellite;
        }

        @Override
        public void run() {     
            Job requestedJob;
            Tool requestedTool;
            
            // Store our response to the server (probably return from tool)
            Object response = null;
            
            // setting up object streams
            // ...
            
            try
            {
                readFromNet = new ObjectInputStream(jobRequest.getInputStream());
                writeToNet = new ObjectOutputStream(jobRequest.getOutputStream());
                
                // Get our incoming message
                message = (Message) readFromNet.readObject();
            }
            catch (IOException e)
            {
                Logger.getLogger(SatelliteThread.class.getName())
                    .log(Level.SEVERE, null, e);
                System.out.println("Failed to read job.");
            }
            catch (ClassNotFoundException e)
            {
                Logger.getLogger(SatelliteThread.class.getName())
                    .log(Level.SEVERE, null, e);
                System.out.println("Couldn't find the Message class.");
            }
                
            // reading message
            // ...
                        
            switch (message.getType()) {
                case JOB_REQUEST:
                    // processing job request
                    // ...
                    
                    try
                    {
                        // Get our job and our tool
                        requestedJob = (Job) message.getContent();
                        requestedTool = getToolObject(requestedJob.getToolName());
                        
                        // Run our tool to get our result
                        response = requestedTool.go(requestedJob.getParameters());                        
                    }
                    catch (UnknownToolException e)
                    {
                        Logger.getLogger(SatelliteThread.class.getName())
                            .log(Level.SEVERE, null, e);
                        System.out.println("Unknown tool requested.");
                    }
                    catch (ClassNotFoundException e)
                    {
                        Logger.getLogger(SatelliteThread.class.getName())
                            .log(Level.SEVERE, null, e);
                        System.out.println("Class not found when getting tool or job.");
                    }
                    catch (InstantiationException e)
                    {
                        Logger.getLogger(SatelliteThread.class.getName())
                            .log(Level.SEVERE, null, e);
                        System.out.println("Failed to instantiate requested tool.");
                    }
                    catch (IllegalAccessException e)
                    {
                        Logger.getLogger(SatelliteThread.class.getName())
                            .log(Level.SEVERE, null, e);
                        System.out.println("Illegal access when requesting tool.");
                    }
                    
                    break;

                default:
                    System.err.println("[SatelliteThread.run] Warning: Message type not implemented");
            }
            
            // If we have something to send back to the client, send it
            if (response != null)
            {
                try
                {
                    writeToNet.writeObject(response);
                }
                catch (IOException e)
                {
                    Logger.getLogger(SatelliteThread.class.getName())
                        .log(Level.SEVERE, null, e);
                    System.out.println("Failed to send result to client.");
                }
            }
        }
    }

    /**
     * Aux method to get a tool object, given the fully qualified class string
     * If the tool has been used before, it is returned immediately out of the cache,
     * otherwise it is loaded dynamically
     */
    public Tool getToolObject(String toolClassString) throws UnknownToolException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        Tool toolObject = null;

        // ...
        
        // Check if we cached the tool and get it if we did
        if (toolsCache.containsKey(toolClassString))
        {
            toolObject = (Tool) toolsCache.get(toolClassString);
        }
        // If we haven't cached the tool attempt to get it from the server and 
        // if we succeed then add it to the cache
        else
        {
            try
            {
                // Request the tool
                toolObject = (Tool) classLoader.loadClass(toolClassString)
                        .getDeclaredConstructor().newInstance();
                // Add the tool to the cache
                toolsCache.put(toolClassString, toolObject);
            }
            catch (NoSuchMethodException e)
            {
                Logger.getLogger(SatelliteThread.class.getName())
                    .log(Level.SEVERE, null, e);
                System.out.println("Failed to get constructor on tool.");
            }
            catch (InvocationTargetException e)
            {
                Logger.getLogger(SatelliteThread.class.getName())
                    .log(Level.SEVERE, null, e);
                System.out.println("Failed to invoke constructor on tool.");
            }
        }
        
        // Give back the tool we got if we succeeded
        return toolObject;
    }

    public static void main(String[] args) {
        // start the satellite
        Satellite satellite = new Satellite(args[0], args[1], args[2]);
        satellite.run();
    }
}
