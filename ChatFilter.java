import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Chat Filter
 * Sometimes people speak with a sailor's tongue, using terrible words
 * like the Dreaded IU and the Disgusting phrase "null_pointer_exceptions"
 * We don't like that, so we're censoring the people with this filter
 *
 * @author Tobias Lind and Tristan Hargett
 * @version 4/25/2020
 */
public class ChatFilter {

    ArrayList<String> badArr = new ArrayList<>();

    public ChatFilter(String badWordsFileName) {
        //dds all bad words to an ArrayList
        File file = new File(badWordsFileName);
        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                badArr.add(line);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    //THIS IS JUST A TESTING METHOD TO TEST BADWORDS
    public static void main(String[] args) {
        ChatFilter s = new ChatFilter("badwords.txt");

        System.out.println(s.filter("This iu is a test string hoosier that is meant to censor " +
                "Bloomington all the bad sleep words null_pointer_exceptions"));


    }


    //This checks variations of a string such as iu. iu? iu! IU. IU? etc....
    public boolean stringVar(String msg, String badWord) {

        if (msg.equals(badWord) || msg.equals(badWord + ".") ||
                msg.equals(badWord + "!") || msg.equals(badWord + "?") || msg.equals(badWord + ",")) {
            return true;
        }
        if (msg.equals(badWord.toUpperCase()) || msg.equals(badWord.toUpperCase() + ".")
                || msg.equals(badWord.toUpperCase() + "!") ||
                msg.equals(badWord.toUpperCase() + "?") ||
                msg.equals(badWord.toUpperCase() + ",")) {
            return true;
        }
        if (msg.equals(badWord.substring(0, 1).toUpperCase() + badWord.substring(1)) ||
                msg.equals(badWord.substring(0, 1).toUpperCase() + badWord.substring(1) + ".") ||
                msg.equals(badWord.substring(0, 1).toUpperCase() + badWord.substring(1) + "!") ||
                msg.equals(badWord.substring(0, 1).toUpperCase() + badWord.substring(1) + "?") ||
                msg.equals(badWord.substring(0, 1).toUpperCase() + badWord.substring(1) + ",")) {
            return true;
        }
        return false;
    }

    public String filter(String msg) {

        String[] messageArr = msg.split(" ");

        for (int i = 0; i < messageArr.length; i++) {
            for (int j = 0; j < badArr.size(); j++) {
                String temp = messageArr[i].substring(messageArr[i].length() - 1);
                if (!temp.equals(".") && !temp.equals(",") && !temp.equals("!") && !temp.equals("?"))
                    temp = "";
                if (stringVar(messageArr[i], badArr.get(j))) {
                    messageArr[i] = messageArr[i].replace(messageArr[i], numStars(badArr.get(j).length())) + temp;
                }
            }
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < messageArr.length; i++) {
            sb.append(messageArr[i] + " ");
        }

        msg = sb.toString();
        msg = msg.substring(0, msg.length() - 1);

        return msg;
    }


    //Returns a string of ****** with the same length as the given string
    public String numStars(int starLength) {
        String temp = "";
        for (int i = 0; i < starLength; i++)
            temp += "*";
        return temp;
    }


    //Returns the Array of Badwords
    public ArrayList<String> getBadArr() {
        return badArr;
    }

}
