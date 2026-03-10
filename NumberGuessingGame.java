import java.util.Random;
import java.util.Scanner;

/**
 * =====================================================
 *         NUMBER GUESSING GAME - Java Console App
 *         Beginner-Friendly Mini Project
 * =====================================================
 * Features:
 *  - Random number generation (1–100)
 *  - 7 attempts per round
 *  - Hints: too high / too low
 *  - Score system (more points for fewer attempts)
 *  - Multiple rounds with play-again option
 * =====================================================
 */
public class NumberGuessingGame {

    // ── Constants ──────────────────────────────────────
    static final int MIN_NUMBER   = 1;
    static final int MAX_NUMBER   = 100;
    static final int MAX_ATTEMPTS = 7;

    // Shared objects (created once, reused every round)
    static Scanner scanner = new Scanner(System.in);
    static Random  random  = new Random();

    // ── Entry Point ────────────────────────────────────
    public static void main(String[] args) {
        printWelcomeBanner();

        int totalScore = 0;
        int roundNumber = 0;

        // Keep playing rounds until the user says "no"
        do {
            roundNumber++;
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║        ROUND  " + roundNumber + "              ║");
            System.out.println("╚══════════════════════════════╝");

            int scoreThisRound = playOneRound();
            totalScore += scoreThisRound;

            System.out.println("\n  ★  Score this round : " + scoreThisRound);
            System.out.println("  ★  Total score      : " + totalScore);

        } while (askPlayAgain());

        // Goodbye message
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║   Thanks for playing!  Goodbye! 👋   ║");
        System.out.printf( "║   Your FINAL score is : %-13d║%n", totalScore);
        System.out.println("╚══════════════════════════════════════╝\n");

        scanner.close();
    }

    // ── Play One Full Round ────────────────────────────
    /**
     * Generates a secret number and lets the player guess
     * up to MAX_ATTEMPTS times.
     *
     * @return points earned this round (0 if not guessed)
     */
    static int playOneRound() {

        // Generate the secret number
        int secretNumber = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;

        System.out.println("\n  I'm thinking of a number between "
                + MIN_NUMBER + " and " + MAX_NUMBER + ".");
        System.out.println("  You have " + MAX_ATTEMPTS + " attempts. Good luck!\n");

        int attemptsUsed = 0;
        boolean guessedCorrectly = false;

        // ── Guessing Loop ──
        while (attemptsUsed < MAX_ATTEMPTS) {

            int remainingAttempts = MAX_ATTEMPTS - attemptsUsed;
            System.out.println("  Attempts remaining: " + remainingAttempts);

            int guess = readIntFromUser("  Enter your guess: ");
            attemptsUsed++;

            // ── Compare guess ──
            if (guess == secretNumber) {
                System.out.println("\n  🎉 Congratulations! You guessed the number: " + secretNumber);
                guessedCorrectly = true;
                break;

            } else if (guess > secretNumber) {
                System.out.println("  ↓  Your guess is HIGHER than the number. Try lower!\n");

            } else {
                System.out.println("  ↑  Your guess is LOWER than the number. Try higher!\n");
            }
        }

        // ── Round result ──
        if (!guessedCorrectly) {
            System.out.println("\n  ✗  Out of attempts! The number was: " + secretNumber);
            return 0;  // No points for failing
        }

        return calculateScore(attemptsUsed);
    }

    // ── Score Calculation ──────────────────────────────
    /**
     * Awards more points the fewer attempts were used.
     *
     * Scoring table:
     *   1 attempt  → 100 pts
     *   2 attempts →  90 pts
     *   3 attempts →  80 pts
     *   4 attempts →  70 pts
     *   5 attempts →  50 pts
     *   6 attempts →  30 pts
     *   7 attempts →  10 pts
     *
     * @param attemptsUsed number of guesses taken
     * @return points earned
     */
    static int calculateScore(int attemptsUsed) {
        switch (attemptsUsed) {
            case 1:  return 100;
            case 2:  return 90;
            case 3:  return 80;
            case 4:  return 70;
            case 5:  return 50;
            case 6:  return 30;
            default: return 10;  // 7 attempts
        }
    }

    // ── Ask Play Again ─────────────────────────────────
    /**
     * Prompts the user to play another round.
     *
     * @return true if the user wants to play again
     */
    static boolean askPlayAgain() {
        System.out.print("\n  Would you like to play again? (yes / no): ");
        String answer = scanner.nextLine().trim().toLowerCase();

        // Accept "y", "yes" as yes; anything else is no
        return answer.equals("yes") || answer.equals("y");
    }

    // ── Safe Integer Input ─────────────────────────────
    /**
     * Reads a valid integer from the user in the range [MIN_NUMBER, MAX_NUMBER].
     * Loops until valid input is provided, preventing crashes on bad input.
     *
     * @param prompt text to display to the user
     * @return a valid integer entered by the user
     */
    static int readIntFromUser(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);

                if (value < MIN_NUMBER || value > MAX_NUMBER) {
                    System.out.println("  ⚠  Please enter a number between "
                            + MIN_NUMBER + " and " + MAX_NUMBER + ".");
                } else {
                    return value;  // Valid input — return it
                }

            } catch (NumberFormatException e) {
                System.out.println("  ⚠  Invalid input. Please enter a whole number.");
            }
        }
    }

    // ── Welcome Banner ─────────────────────────────────
    /**
     * Prints a decorative welcome message when the game starts.
     */
    static void printWelcomeBanner() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      NUMBER GUESSING GAME  🎮            ║");
        System.out.println("║  Guess the secret number between 1–100   ║");
        System.out.println("║  You get " + MAX_ATTEMPTS + " attempts per round.          ║");
        System.out.println("║  Fewer guesses = More points!            ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }
}