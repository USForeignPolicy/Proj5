import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Chat Server
 * A simple server that clients can connect to and
 * send fun thoughtful messages to each other
 *
 * @author Tobias Lind and Tristan Hargett L05
 * @version 04/24/2020

 */
final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private ArrayList<String> userArr = new ArrayList<>();
    public static String filePath = "badwords.txt";

    private ChatServer(int port) {
        this.port = port;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */

    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        int port = 1500;
        filePath = "badwords.txt";

        for (int i = 0; i < args.length; i++) {
            if (i == 0)
                port = Integer.parseInt(args[0]);
            if (i == 1)
                filePath = args[1];
        }
        ChatFilter CCP = new ChatFilter(filePath);
        //DISPLAYS ALL BANNED WORDS WHEN SERVER STARTS
        String bannedWordGreeting = "Banned words: ";
        for (String x : CCP.getBadArr()) {
            bannedWordGreeting += x + " ";
        }
        bannedWordGreeting = bannedWordGreeting.substring(0, bannedWordGreeting.length() - 1);
        System.out.println(bannedWordGreeting);
        ChatServer server = new ChatServer(1500);
        server.start();
    }


    //THIS IS THE BROADCAST FUNCTION: it prints to the terminal of the server,
    // and then sends a message to all connected clients. - WORKS
    private void broadcast(String message) {
        //Get current Date in string format
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        String tempDate = simpleDateFormat.format(new Date());

        //Print message to Server-Terminal
        System.out.println(message + " " + tempDate);


        //Sends message to ALL clients
        for (ClientThread x : clients) {
            x.writeMessage(message + " " + tempDate);
        }
    }


    //REMOVE METHOD - WORKS
    public static Object myObj = new Object();

    private void remove(int id) {
        synchronized (myObj) {
            for (int i = 0; i < clients.size(); i++) {
                int x = clients.get(i).id;
                if (x == id)
                    clients.remove(i);
            }
        }
    }

    //THIS ADDS TO THE ARRAY OF USERNAMES
    public void addUser(String name) {
        userArr.add(name);
    }

    //this checks if a username is in the UserArr
    public boolean checkUser(String name) {
        if (name.equals("Anonymous"))
            return true;
        for (String x : userArr) {
            if (x.equals(name))
                return false;
        }
        return true;
    }


    /**
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     *
     * @author Tristan H and Tobias L L05
     * @version 04/24/2020

     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        //WRITES MESSAGE, AND RETURNS TRUE OR RETURNS FALSE IF SOCKET NOT CONNECTED - WORKS

        private boolean writeMessage(String message) {
            synchronized (myObj) {
                if (socket.isConnected() == false)
                    return false;

                try {
                    sOutput.writeObject(message);
                } catch (IOException e) {
                    //THIS CATCHES THE SOCKET CLOSED EXCEPTION AND TELLS PEOPLE THAT THE USER HAS LEFT.
                }
                return true;
            }
        }


        //DIRECT MESSAGE
        public void directMessage(String message, String username) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            String tempDate = simpleDateFormat.format(new Date());

            boolean nameExists = false;
            int count = 0;
            for (ClientThread x : clients) {
                if (this.username.equals(username)) {
                    for (ClientThread y : clients) {
                        if (y.getUsername().equals(this.username) && count == 0) {
                            y.writeMessage("System: You cant send a message to yourself");
                            count++;
                            nameExists = true;
                        }
                    }
                } else if (x.getUsername().equals(username) && !x.getUsername().equals("Anonymous")) {
                    x.writeMessage("(PM)" + this.username + ": " + message + " " + tempDate);
                    nameExists = true;
                }
            }
  
            //messages back if the name in /msg doesnt exist
            if (nameExists == false) {
                for (ClientThread x : clients) {
                    if (x.getUsername().equals(this.username)) {
                        x.writeMessage("System: The person you are trying to message doesn't exist");
                    }
                }
            }
        }

        //THIS CLOSE CLASS DOES THE SAME AS LOGGING OUT - WORKS

        private void close() {
            try {
                sInput.close();
                sOutput.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        //This uses addUser and checkUser during startup to add appropriately to the arraylist and send an error if not.
        public boolean runChecks() {
            //ADDS USERNAME TO USERARR and broadcast that they have joined

            if (checkUser(getUsername())) {
                addUser(getUsername());
                broadcast(getUsername() + " has joined the chat!");
                return true;
            } else {
                writeMessage("Error, your username is not UNIQUE!");
                remove(id);
                close();
                return false;
            }

        }

        public String getUsername() {
            return username;
        }


        /*
         * This is what the client thread actually runs.
         */
        ChatFilter CCP = new ChatFilter(filePath);

        //DOWN HERE IS WHERE THE SERVER READS THE MESSAGES FROM CLIENTS AND ANSWERS

        @Override
        public void run() {
            // Read the username sent to you by client
            //Checks if username is unique
            if (runChecks() == false)
                return;


            while (true) {
                try {
                    cm = (ChatMessage) sInput.readObject();

                } catch (SocketException ez) {
                    broadcast("System: " + username + " has forcefully closed out of the chat!");
                    remove(id);
                    userArr.remove(username);
                    return;

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                String tempCheck = cm.getMessage();

                if (cm.getType() == 1) {
                    broadcast("System: " + username + " has logged out of the chat!");
                    remove(id);
                    userArr.remove(username);
                    close();
                    return;
                } else if (cm.getType() == 2) {
                    directMessage(CCP.filter(cm.getMessage()), cm.getRecipient());
                    //broadcast(username + ":! " + CCP.filter(cm.getMessage()));
                }
                //This has to be dead so the server doesnt respond to type 3 messages
                else if (cm.getType() == 3) {

                }

                //THIS PRINTS THE LISTS OF ALL PEOPLE ON SERVER EXCLUDING SENDER & Excluding Anonymous people

                else if (cm.getType() == 4) {
                    ArrayList<String> tempArr = new ArrayList<>();

                    for (ClientThread x : clients) {
                        if (!x.getUsername().equals(this.username) && !x.getUsername().equals("Anonymous")) {
                            tempArr.add(x.getUsername());
                        }
                    }
                    String temp = "";
                    for (String x : tempArr) {
                        temp += x + ", ";
                    }

                    if (temp.length() > 2)
                        temp = temp.substring(0, temp.length() - 2);

                    writeMessage(temp);
                } else {
                    broadcast(username + ": " + CCP.filter(cm.getMessage()));

                }
            }
        }
    }
}
