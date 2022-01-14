import java.util.Scanner;

import java.io.*;
import java.net.*;

class Client implements Runnable {
    private static String ip;
    private static int port;

    private Socket socket;

    Client() throws java.io.IOException {
        File properties = new File("properties.txt");
        Scanner fileReader;

        try {
            fileReader = new Scanner(properties);

            ip = fileReader.nextLine();
            port = fileReader.nextInt();

            fileReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("Failed to find properties file.");
            System.exit(1);
        }

        System.out.println("ip: " + ip + " port: " + port);

        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            System.err.println("Failed to create socket:\n" + e);
            System.exit(1);
        }

        DataOutputStream toServer = new DataOutputStream(socket.getOutputStream());
        ObjectOutputStream toServerObj = new ObjectOutputStream(toServer);

        DataInputStream fromServer = new DataInputStream(socket.getInputStream());
        ObjectInputStream fromServerObj = new ObjectInputStream(fromServer);

        NodeInfo info = new NodeInfo("ip", 0, "Hi");
        Message message = new Message(info, MessageTypes.JOIN);

        toServerObj.writeObject(message);
}

    public void run() {
        DataInputStream fromServer = null;
        DataOutputStream toServer = null;
        Scanner userInput = new Scanner(System.in);

        int input;
        int result;

        try {
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Error opening network streams:\n" + e);
            System.exit(1);
        }

        System.out.println("Give an int > 0.");
        System.out.println("Input: ");
        input = userInput.nextInt();

        try {
            System.out.println("Sending int " + input + " to server.");
            toServer.writeInt(input);
        } catch (IOException e) {
            System.err.println("Error writing int to server:\n" + e);
            System.exit(1);
        }

        try {
            result = fromServer.readInt();
            System.out.println("It took " + result + " step" + (result == 1 ? "" : "s") + ".");
        } catch (IOException e) {
            System.err.println("Error receiving result from server " + e);
            System.exit(1);
        }

        try {
            System.out.println("Closing socket to server.");
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket to server.");
        }
    }


    public static void main(String[] args) throws Exception {
        Client client = new Client();
//        client.
    }
}
