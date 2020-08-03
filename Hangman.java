import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Random;

public class Hangman {
    //Storing variables used witin the game
    private PrintWriter output;
    private Socket socket;
    private Scanner input;
    private String channel;
    private char value;
    Random random = new Random();
    //List of different words the user can guess
    String[] guesses = {
            "internet", "router", "packet", "server",
            "wireshark", "martin", "client", "phishing", "trojan"};
    private String user;

    //Constructing a hangman game with relevant IRC socket information
    public Hangman(Socket socket, PrintWriter output, Scanner input, String channel, String user) {
        this.output = output;
        this.socket = socket;
        this.input = input;
        this.channel = channel;
        this.user = user;
        startGame();
    }

    //Starting game
    private void startGame() {
        boolean playing = true;

        //Flag set to playing enables listeners to check that values and pass onto the game
        while (playing) {
            //Letting the user know that the game is starting
            sendMessage("PRIVMSG ", channel + " :" + "Hangman is starting...");
            int randomNumber = random.nextInt(guesses.length);

            //Converting a random word from the list to characters and then mapping  them as _.
            char[] randomWordToGuess = guesses[randomNumber].toCharArray();
            int ammountOfGuesses = randomWordToGuess.length; //total tries to guess a word.
            char[] playerGuess = new char[ammountOfGuesses]; // "_ _ _ _ _ _ _ _"
            for (int i = 0; i < playerGuess.length; i++) {
                playerGuess[i] = '_';
            }

            //Setting word not guessed to false.
            boolean wordGuessed = false;

            //Creating variable tries and incorrectTries to keep track of incorrect guesses and map them to drawing
            int tries = 1;
            int incorrectTries = 1;
            String[][] guessesStructure = {{" "}, {" "}, {" "}, {" "}, {" "}, {" "}};

            //Loop to draw characters based on current count
            while (!wordGuessed && tries != ammountOfGuesses) {

                //The possible patterns of hangman drawings mapped to incorrect tries
                if (incorrectTries == 1) {
                    guessesStructure[5][0] = "_____________";
                } else if (incorrectTries == 2) {
                    guessesStructure[1][0] = "   |         ";
                    guessesStructure[2][0] = "   |         ";
                    guessesStructure[3][0] = "   |         ";
                    guessesStructure[4][0] = "   |         ";
                    guessesStructure[5][0] = "___|_________";

                } else if (incorrectTries == 3) {
                    guessesStructure[0][0] = "_________________";
                    guessesStructure[1][0] = "   |         ";
                    guessesStructure[2][0] = "   |         ";
                    guessesStructure[3][0] = "   |         ";
                    guessesStructure[4][0] = "   |         ";
                    guessesStructure[5][0] = "___|_________";
                } else if (incorrectTries == 4) {
                    guessesStructure[0][0] = "_________________";
                    guessesStructure[1][0] = "   |        | ";
                    guessesStructure[2][0] = "   |         ";
                    guessesStructure[3][0] = "   |         ";
                    guessesStructure[4][0] = "   |         ";
                    guessesStructure[5][0] = "___|_________";

                } else if (incorrectTries == 5) {
                    guessesStructure[0][0] = "_________________";
                    guessesStructure[1][0] = "   |        | ";
                    guessesStructure[2][0] = "   |        O";
                    guessesStructure[3][0] = "   |         ";
                    guessesStructure[4][0] = "   |         ";
                    guessesStructure[5][0] = "___|_________";

                } else if (incorrectTries == 6) {
                    guessesStructure[0][0] = "_________________";
                    guessesStructure[1][0] = "   |        | ";
                    guessesStructure[2][0] = "   |        O";
                    guessesStructure[3][0] = "   |       \\|";
                    guessesStructure[4][0] = "   |         ";
                    guessesStructure[5][0] = "___|_________";

                } else if (incorrectTries == 7) {
                    guessesStructure[0][0] = "_________________";
                    guessesStructure[1][0] = "   |        | ";
                    guessesStructure[2][0] = "   |        O";
                    guessesStructure[3][0] = "   |       \\|/";
                    guessesStructure[4][0] = "   |         ";
                    guessesStructure[5][0] = "___|_________";

                } else if (incorrectTries == 8) {
                    guessesStructure[0][0] = "_________________";
                    guessesStructure[1][0] = "   |        | ";
                    guessesStructure[2][0] = "   |        O";
                    guessesStructure[3][0] = "   |       \\|/";
                    guessesStructure[4][0] = "   |       / ";
                    guessesStructure[5][0] = "___|_________";

                } else if (incorrectTries == 9) {
                    guessesStructure[0][0] = "_________________";
                    guessesStructure[1][0] = "   |        | ";
                    guessesStructure[2][0] = "   |        O";
                    guessesStructure[3][0] = "   |       \\|/";
                    guessesStructure[4][0] = "   |       / \\";
                    guessesStructure[5][0] = "___|_________";
                }

                //individually sending messages for drawing based on incorrect tries
                for (String[] item : guessesStructure) {
                    sendMessage("PRIVMSG ", channel + " :" + item[0]);
                }

                //Using for each loop to print each guessed item into the IRC
                String guess = "";
                for (int i = 0; i < playerGuess.length; i++) {
                    guess  += playerGuess[i] + " ";
                }

                //Logging guesses, amount of tries left and enter character message to IRC to let the user know about the game
                sendMessage("PRIVMSG ", channel + " :" + "Current guesses: " + guess);
                System.out.println("Current Guesses: ");
                sendMessage("PRIVMSG ", channel + " :" + "You have " + (ammountOfGuesses - tries) + " amount of tries left.");
                System.out.println("No of tries: ");
                sendMessage("PRIVMSG ", channel + " :" + "Enter a character: ");
                System.out.println("Enter character: ");

                //Fetching and storing the first character of any word inputted into IRC (little flexibility due to IRC considerations)
                String res = input.nextLine();
                System.out.println("Input value: " + res);
                this.value = res.split(" ")[3].charAt(1);

                //Outputs made selection
                System.out.println("Value for thingy: " + value);
                sendMessage("PRIVMSG ", channel + " :" + "You have selected: " + value);

                tries++;
                incorrectTries++;

                if (value == '-') {
                    wordGuessed = true;
                    playing = false;
                } else {
                    //Checking if guess matches the correct letter
                    for (int i = 0; i < randomWordToGuess.length; i++) {
                        if (randomWordToGuess[i] == value) {
                            playerGuess[i] = value;
                            incorrectTries--;
                        }
                    }
                    //Checking if the word is matched fully
                    if (isWordGuessed(playerGuess)) {
                        wordGuessed = true;
                        sendMessage("PRIVMSG ", channel + " :" + "Congratulations");
                    }
                }
            }
            //Result if the word is not correctly guessed
            if (!wordGuessed) {
                sendMessage("PRIVMSG ", channel + " :" + "You ran out of guesses");
                sendMessage("TOPIC ", channel + " :");
                sendMessage("PRIVMSG ", channel + " :" + "The word was " + guesses[randomNumber]);

            }
            playing = false;
        }
        //Game over
        sendMessage("PRIVMSG ", channel + " :" + "Game over");

        //Kick protocol being used
        sendMessage("KICK", channel + " " + user + " :" + "Good game.");
    }
    //Send message template which inputs protocol op and message and auto flushes
    private void sendMessage(String op, String msg) {
        output.print(op + " " + msg + "\r\n");
        output.flush();
        System.out.print("Message flushed");
    }


    //Checking if word is guessed
    public static boolean isWordGuessed(char[] array) {
        boolean condition = true;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == '_') {
                condition = false;
            }
        }
        return condition;
    }

}