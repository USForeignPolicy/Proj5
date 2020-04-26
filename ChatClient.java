import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

/**
 *
 * [Add your documentation here]
 *
 * @author your name and section
 * @version date
 */
final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private static boolean memeCheck = false;

    static boolean closeClient = false;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO made a method to close the client - WORKS
    public void closeClient()   {
        try {
            sInput.close();
            sOutput.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("You have logged out!");
            closeClient = true;
        }
    }

    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults
        String username = "Anonymous";
        String server_address = "localhost";
        int port = 1500;

        int count = 0;
        for(String x : args)    {
            if(count == 0) {
                username = x;
                count++;
            }
            if(count == 1) {
                username = x;
                count++;
            }
            if(count == 2) {
                username = x;
                count++;
            }
        }
        // Create your client and start it
        ChatClient client = new ChatClient(server_address, port, username);
        client.start();

        // Send an empty message to the server
        //client.sendMessage(new ChatMessage());

        //TODO THIS IS WHERE THE CLIENT SENDS MESSAGES
        Scanner myObj = new Scanner(System.in);


        //TODO THERE IS AN ERROR HERE FOR MESSAGE IF IT HAS NO PARAMETERS  - FIXED
        while(true) {
            String temp = myObj.nextLine();

            if(temp.length() >= 7 && temp.substring(0,7).equals("/logout"))    {
                    client.sendMessage(new ChatMessage(temp, 1));
                    memeCheck = true;
                    client.closeClient();
                    closeClient = true;
                    return;

            }
            else if(temp.length() >= 5 && temp.substring(0,5).equals("/list"))    {
                client.sendMessage(new ChatMessage(temp, 4));
            }
            else if(temp.length() >= 4 && temp.substring(0,4).equals("/msg"))    {
                String[] recept = {"", "", ""};
                String[] tempArr = temp.split(" ",3);
                for(int i = 0; i < tempArr.length; i++) {
                    recept[i] = tempArr[i];
                }
                if(recept[1].equals("") || recept[2].equals(""))    {
                    client.sendMessage(new ChatMessage("Error", 3));
                    System.out.println("System: Please use 3 arguments in the format /msg username message");
                }
                else {
                    client.sendMessage(new ChatMessage(recept[2], 2, recept[1]));
                }
            }
            else  {
                client.sendMessage(new ChatMessage(temp, 0));
            }

        }

    }


   /**
    * This is a private class inside of the ChatClient
    * It will be responsible for listening for messages from the ChatServer.
    * ie: When other clients send messages, the server will relay it to the client.
    *
    * @author your name and section
    * @version date
    */
    private final class ListenFromServer implements Runnable {
        public void run() {
            try {
                if(closeClient == true)
                    return;
                while(true || memeCheck != true) {
                    String msg = (String) sInput.readObject();
                    //might have to change this back to PRINT, made it PRINTLN since it looked ugly as fuck
                    System.out.println(msg);
                }
            } catch (IOException | ClassNotFoundException e) {
                if(closeClient == false)
                    e.printStackTrace();
                else
                    System.out.println("You have logged out!");
            }
        }
    }
}
