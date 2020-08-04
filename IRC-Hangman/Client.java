import java.io.*;
import java.net.*;
import java.util.Scanner;

//Client represents the terminal / entity used to communicate with server
public class Client {

    //Declaration of variables.
    private static PrintWriter output;
    private static Socket socket;
    private static Scanner input;
    private static String channel = "#hangmanGame";
    private static String nickname;
    private static String username;
    private static String realname;


    public static void main(String[] args) throws IOException {

        //Default syntax for params. PLEASE USE THIS IF REQUIRED FOR MARKING
        //OTHERWISE, a scanner input is provided for ease.
        //String hostName = args[0];
        //int portNumber = Integer.parseInt(args[1]);

        //Scanner to input values by terminal for registering with IRC server
        Scanner in = new Scanner(System.in);

        //Environment variables for registering with IRC server.
        System.out.print("Enter your nickname: ");
        nickname = in.nextLine();

        System.out.print("Enter your username: ");
        username = in.nextLine();

        System.out.print("Enter your realname: ");
        realname = in.nextLine();

        System.out.print("Enter server host name: ");
        String hostName = in.nextLine();

        System.out.print("Enter server post number: ");
        int portNumber = Integer.parseInt(in.nextLine());

        System.out.print("Enter general channel E.g. help *(without #): ");
        String channelInitial = "#" + in.nextLine();

        //String hostName = "127.0.0.1";
        //int portNumber = 7777;

        //Initialisation of Socket to connect to IRC server.
        socket = new Socket(hostName, portNumber);
        output = new PrintWriter(socket.getOutputStream(), true);
        input = new Scanner(socket.getInputStream());

        //Registering bot in IRC by sending relevant information for IRC daemon
        sendMessage("NICK", nickname);
        sendMessage("USER", username + " 8 * :" + realname);

        //Setting up hangman game instance environment
        sendMessage("JOIN", channel);

        //People cannot find channel using list
        sendMessage("MODE", channel + " " + "+s");
        sendMessage("MODE", channel + " " + "+l 2");
        //Only invited people can join game
        sendMessage("MODE", channel + " " + "+i");
        sendMessage("TOPIC ", channel + " : PLAY HANGMAN - Type <!play> for information");

        //Moving to general chat where users may listen for <!invite> to create game instance.
        sendMessage("JOIN", channelInitial);
        sendMessage("PRIVMSG", channelInitial + " :" + "- <!invite> to to invited to new game instance");
        sendMessage("PRIVMSG", channelInitial + " :" + "- <!disconnect bot> to disconnect bot");
        //Listening to queries
        while (input.hasNext()) {

            String res = input.nextLine();
            System.out.println("<<< " + res);

            //Checking for invites and sending user to instanced game channel
            if (res.toLowerCase().contains("!invite")) {
                String userStart = ":";
                String userEnd = "!";

                res = res.substring(res.indexOf(userStart) + 1);
                res = res.substring(0, res.indexOf(userEnd));
                sendMessage("INVITE", res + " " + channel);
            }

            //Checking for server ping requests and responding with PONG to match AFK check
            if (res.startsWith("PING")) {
                System.out.println("PONG initiated");
                String ping = res.split(" ", 2)[1];
                sendMessage("PONG", channel + ": " + ping);
            }

            //Providing instructions for hangman game
            if (res.toLowerCase().contains("!play")) {
                sendMessage("PRIVMSG ", channel + " :" + "Welcome to hangman. Try to guess the word by typing a single letter each time.");
                sendMessage("PRIVMSG ", channel + " :" + "Please <!start> to begin.");
            }

            //Initiating hangman game
            if (res.toLowerCase().contains("!start")) {
                String userStart = ":";
                String userEnd = "!";

                res = res.substring(res.indexOf(userStart) + 1);
                res = res.substring(0, res.indexOf(userEnd));

                sendMessage("PRIVMSG", channel + " :" + "You have joined the game, " + res);
                new Hangman(socket, output, input, channel, res);
            }

            //Disconnect bot
            if (res.toLowerCase().contains("!disconnect bot")) {
                sendMessage("QUIT"," :" + "Gone fishing");
                if (res.contains("Your privileges are too low")){
                    sendMessage("PRIVMSG ", channel + " :" + "You do not have the privileges for this command. Please ensure that you receive OP status or are the first person to join this channel." + res);
                }
            }

            //Disconnect user
            if (res.toLowerCase().contains("!disconnect user")) {
                //Getting the start of user input and end of user input to identify bounds of user nickname
                String userStart = ":";
                String userEnd = "!";

                //User nickname is assigned and is sent on message to provide information on privileges.
                //However the case where the bot is not the OP does not occur as it is the first user to join an instanced game session
                res = res.substring(res.indexOf(userStart) + 1);
                res = res.substring(0, res.indexOf(userEnd));
                sendMessage("KICK", channel + " " + res + " :" + "Disconnected.");
                if (res.contains("Your privileges are too low")){
                    sendMessage("PRIVMSG ", channel + " :" + "You do not have the privileges for this command. Please ensure that you receive OP status or are the first person to join this channel." + res);
                }
             }
        }
        input.close();
        output.close();
        socket.close();
    }

    //Send message template which inputs protocol op and message and auto flushes
    private static void sendMessage(String op, String msg) {
        output.print(op + " " + msg + "\r\n");
        output.flush();
        System.out.print("Message flushed");
    }


}
