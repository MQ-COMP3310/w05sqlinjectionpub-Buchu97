package workshop05code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class App {
    static {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to load logging config", e1);
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");

        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            logger.info("Wordle created and connected.");
        } else {
            logger.severe("Could not connect to the database.");
            return;
        }

        if (wordleDatabaseConnection.createWordleTables()) {
            logger.info("Wordle structures in place.");
        } else {
            logger.severe("Failed to create table structures.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if (line.matches("[a-z]{4}")) {
                    logger.info("Loaded valid word: " + line);
                    wordleDatabaseConnection.addValidWord(i, line);
                    i++;
                } else {
                    logger.severe("Invalid word in data.txt: " + line);
                }
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading from data.txt", e);
            System.out.println("Could not load the word list. Please try again.");
            return;
        }

        // Only game-related output from here
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a 4 letter word for a guess or q to quit: ");
            String guess = scanner.nextLine();

            while (!guess.equals("q")) {
                if (!guess.matches("[a-z]{4}")) {
                    logger.warning("Invalid user guess: " + guess);
                    System.out.println(
                            "Invalid input. Please enter a 4-letter word consisting only of lowercase letters a-z.\n");
                } else {
                    System.out.println("You've guessed '" + guess + "'.");

                    if (wordleDatabaseConnection.isValidWord(guess)) {
                        System.out.println("Success! It is in the list.\n");
                    } else {
                        System.out.println("Sorry. This word is NOT in the list.\n");
                    }
                }

                System.out.print("Enter a 4 letter word for a guess or q to quit: ");
                guess = scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.WARNING, "Error while reading user input", e);
            System.out.println("Something went wrong while reading input.");
        }
    }
}
