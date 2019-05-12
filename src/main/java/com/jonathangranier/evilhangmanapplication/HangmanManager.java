package com.jonathangranier.evilhangmanapplication;
/*  Student information for assignment:
 *
 *  On my honor, Jonathan Granier, this programming assignment is my own work
 *  and I have not provided this code to any other student.
 *
 *  Name: Jonathan Granier
 *  email address: jgranier99@gmail.com
 *  UTEID: jpg2778
 *  Section 5 digit ID: 51345
 *  Grader name: Shelby
 *  Number of slip days used on this assignment: 0
 */

// add imports as necessary

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Manages the details of EvilHangman. This class keeps
 * tracks of the possible words from a dictionary during
 * rounds of hangman, based on guesses so far.
 *
 */
public class HangmanManager {

    private Collection<String> dictionary; // immutable list of the words in the dictionary
    private ArrayList<String> words; // words that have been
    private ArrayList<Character> guessed;
    private int numGuesses;
    private int wordLen;
    private HangmanDifficulty diff;
    private String currentPattern;
    private int runCount;


    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * pre: words != null, words.size() > 0
     * @param words A set with the words for this instance of Hangman.
     * @param debugOn true if we should print out debugging to System.out.
     */
    public HangmanManager(Set<String> words, boolean debugOn) { // debug instance variable is not used, therefore I deleted it and am not assigning it to anything
        this(words);
    }

    /**
     * Create a new HangmanManager from the provided set of words and phrases.
     * Debugging is off.
     * pre: words != null, words.size() > 0
     * @param words A set with the words for this instance of Hangman.
     */
    public HangmanManager(Set<String> words) {
        if(words == null || words.size() == 0)
            throw new IllegalArgumentException("Violation of precondition: "
                    + "words != null && words.size() > 0");
        dictionary = Collections.unmodifiableCollection(words);
        guessed = new ArrayList<Character>();
        this.words = new ArrayList<String>(dictionary); // constructs with all the words in the given dictionary
        runCount = 1;
    }

    /**
     * Get the number of words in this HangmanManager of the given length.
     * pre: none
     * @param length The given length to check.
     * @return the number of words in the original Dictionary with the given length
     */
    public int numWords(int length) {
        int count = 0;
        for(String s: dictionary) {
            if(s.length() == length) {
                count++;
            }
        }
        return count;
    }


    /**
     * Get for a new round of Hangman. Think of a round as a complete game of Hangman.
     * @param wordLen the length of the word to pick this time. numWords(wordLen) > 0
     * @param numGuesses the number of wrong guesses before the player loses the round. numGuesses >= 1
     * @param diff The difficulty for this round.
     */
    public void prepForRound(int wordLen, int numGuesses, HangmanDifficulty diff) {
        if(numWords(wordLen) == 0 || numGuesses == 0 || diff == null)
            throw new IllegalArgumentException("Violation of precondition: "
                    + "numWords(numLen) > 0 && numGuesses >= 1 && dif != null");
        this.wordLen = wordLen;
        this.numGuesses = numGuesses;
        this.diff = diff;
        words.clear();
        guessed.clear();
        for(String s: dictionary) {
            if(s.length() == wordLen)
                words.add(s);
        }
        char[] blankPattern = new char[wordLen];
        Arrays.fill(blankPattern, '-');
        currentPattern = String.valueOf(blankPattern);
        runCount = 1;
    }


    /**
     * The number of words still possible (live) based on the guesses so far. Guesses will eliminate possible words.
     * @return the number of words that are still possibilities based on the original dictionary and the guesses so far.
     */
    public int numWordsCurrent() {
        return words.size();
    }


    /**
     * Get the number of wrong guesses the user has left in this round (game) of Hangman.
     * @return the number of wrong guesses the user has left in this round (game) of Hangman.
     */
    public int getGuessesLeft() {
        return numGuesses;
    }


    /**
     * Return a String that contains the letters the user has guessed so far during this round.
     * The String is in alphabetical order. The String is in the form [let1, let2, let3, ... letN].
     * For example [a, c, e, s, t, z]
     * @return a String that contains the letters the user has guessed so far during this round.
     */
    public String getGuessesMade() {
        Collections.sort(guessed);
        return guessed.toString();
    }


    /**
     * Check the status of a character.
     * @param guess The characater to check.
     * @return true if guess has been used or guessed this round of Hangman, false otherwise.
     */
    public boolean alreadyGuessed(char guess) {
        return guessed.contains(guess);
    }


    /**
     * Get the current pattern. The pattern contains '-''s for unrevealed (or guessed)
     * characters and the actual character for "correctly guessed" characters.
     * @return the current pattern.
     */
    public String getPattern() {
        return currentPattern;
    }

    public String getDifficulty() {
        if(diff == HangmanDifficulty.EASY)
            return "easiest";
        else if(diff == HangmanDifficulty.MEDIUM)
            return "medium";
        else
            return "hardest";
    }


    // pre: !alreadyGuessed(ch)
    // post: return a tree map with the resulting patterns and the number of
    // words in each of the new patterns.
    // the return value is for testing and debugging purposes
    /**
     * Update the game status (pattern, wrong guesses, word list), based on the give
     * guess.
     * @param guess pre: !alreadyGuessed(ch), the current guessed character
     * @return return a tree map with the resulting patterns and the number of
     * words in each of the new patterns.
     * The return value is for testing and debugging purposes.
     */
    public TreeMap<String, Integer> makeGuess(char guess) {
        if(alreadyGuessed(guess))
            throw new IllegalStateException("Violation of precondition: "
                    + "!alreadyGuessed(guess)");
        guessed.add(guess);
        Map<String, ArrayList<String>> activeMap = createActiveMap(guess);
        TreeMap<String, Integer> debugMap = createDebugMap(activeMap);
        String newPattern = newPatternBasedOnDiff(activeMap);
        if(currentPattern.equals(newPattern))
            numGuesses--;
        else
            currentPattern = newPattern;
        words = activeMap.get(newPattern);
        return debugMap;
    }

    // creates a map consisting of the possible patterns and their correlating words
    private Map<String, ArrayList<String>> createActiveMap(char guess) {
        Map<String, ArrayList<String>> activeMap = new TreeMap<String, ArrayList<String>>();
        for(String s: words) {
            char[] temp = s.toCharArray();
            char[] pattern = currentPattern.toCharArray();
            for(int i = 0; i < wordLen; i++) {
                if(temp[i] == guess)
                    pattern[i] = guess;
            }
            String key = String.valueOf(pattern);
            if(!activeMap.containsKey(key)) {
                ArrayList<String> blank = new ArrayList<String>();
                blank.add(s);
                activeMap.put(key, blank);
            }
            else
                activeMap.get(key).add(s);
        }
        return activeMap;
    }

    // creates the map strictly meant for debugging. <key = pattern, value = number of values>
    private TreeMap<String, Integer> createDebugMap(Map<String, ArrayList<String>> activeMap) {
        TreeMap<String, Integer> debugMap = new TreeMap<String, Integer>();
        for(String key: activeMap.keySet())
            debugMap.put(key, activeMap.get(key).size());
        return debugMap;
    }

    // finds the second hardest pattern every 4th time when diff == HangmanDifficulty.MEDIUM
    // and every 2nd time when diff == HangmanDifficulty.EASY
    private String newPatternBasedOnDiff(Map<String, ArrayList<String>> activeMap) {
        String key= newPattern(activeMap); // the highest key unless overridden to the second highest
        if(activeMap.size() != 1 && diff != HangmanDifficulty.HARD) {
            if(diff == HangmanDifficulty.MEDIUM) {
                if(runCount % 4 == 0)
                    activeMap.remove(key);
            }
            else { // diff == HangmanDifficulty.EASY
                if(runCount % 2 == 0)
                    activeMap.remove(key);
            }
            key = newPattern(activeMap);
        }
        runCount++;
        return key;
    }

    // helper method for makeGuess. finds the hardest pattern when there are multiple keys tied for number of values
    private String newPattern(Map<String, ArrayList<String>> activeMap) {
        ArrayList<String> keyOptions = findNextKeyOptions(activeMap);
        if(keyOptions.size() == 1)
            return keyOptions.get(0);
        int mostEmptyCount = 0;
        int[] emptyCount = new int[keyOptions.size()]; // used to see if lexicographic sorting is necessary
        int index = 0;
        for(String s: keyOptions) {
            int tempCount = 0;
            for(int i = 0; i < s.length(); i++) {
                if(s.charAt(i) == '-')
                    tempCount++;
            }
            if(tempCount > mostEmptyCount)
                mostEmptyCount = tempCount;
            emptyCount[index] = tempCount;
            index++;
        }
        Arrays.sort(emptyCount);
        boolean multipleKeys = emptyCount[emptyCount.length - 1] == emptyCount[emptyCount.length - 2]; // true if there are at least two elements with same emptyCount
        if(multipleKeys) {
            lexicographicalOrdering(keyOptions);
        }
        return keyOptions.get(0);

    }

    // helper method for newPattern. Used in case of ties in both number of values and number of empty characters.
    // lexicographically sorts tied strings
    private void lexicographicalOrdering(ArrayList<String> keyOptions) {
        for(int i = 0; i < keyOptions.size(); i++) {
            for(int j = 0; j < keyOptions.size(); j++) {
                if(i != j) {
                    int compare = keyOptions.get(i).compareTo(keyOptions.get(j));
                    if(compare < 0) {
                        keyOptions.remove(j);
                        j--;
                    }
                }
            }
        }
    }

    // helper method for newPattern to find all the key options with the most values
    private ArrayList<String> findNextKeyOptions(Map<String, ArrayList<String>> activeMap) {
        int mostValues = 0;
        ArrayList<String> keys = new ArrayList<String>();
        for(String key: activeMap.keySet()) {
            if(activeMap.get(key).size() > mostValues)
                mostValues = activeMap.get(key).size();
        }
        for(String key: activeMap.keySet()) {
            if(activeMap.get(key).size() == mostValues)
                keys.add(key);
        }
        return keys;
    }

    /**
     * Return the secret word this HangmanManager finally ended up picking for this round.
     * If there are multiple possible words left one is selected at random.
     * <br> pre: numWordsCurrent() > 0
     * @return return the secret word the manager picked.
     */
    public String getSecretWord() {
        if(numWordsCurrent() <= 0)
            throw new IllegalStateException("Violation of precondition: "
                    + "numWordsCurrent() > 0");
        return(words.get((int)(Math.random() * words.size())));
    }
}