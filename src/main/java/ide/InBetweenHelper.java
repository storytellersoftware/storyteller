package ide;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a static helper method that will take in two strings and
 * will return a string that is lexically in between the two.
 * <p>
 * The strings can be thought of as numbers. There are two 'bases' that can
 * be used, base 10 and base 62. A base 10 string will only have the numeric
 * digits 0-9. A base 62 number can have 0-9, A-Z, and a-z (10 + 26 + 26).
 * We do this so that we can store relatively large 'numbers' in relatively
 * short strings. A five digit base 62 number, for example, has a range of
 * values from 0 to 62^5 or 916,132,832. Five base 62 digits is obviously
 * shorter than nine decimal digits. This should save some space when storing
 * these numbers in a database and allow them to be sorted.
 * <p>
 * Because these are strings and not real numbers, when they are compared
 * against each other they will be checked character by character until there
 * is a mismatch. So, the string "123" will come before "124". "257" will
 * come after "100" and so on.
 * <p>
 * Strings of different lengths have different properties than numbers with
 * different numbers of digits, however. The string "10", for example, lexically
 * comes before the string "9" because the computer will compare '1' to '9' as
 * the first thing it does. We must always keep this lexical comparison in
 * mind. The rules are:
 * - the computer will compare characters from the beginning of the two strings
 * until the compared characters are different. Once there is a difference
 * in the characters that pair of characters determines the lexical ordering
 * of the strings.
 * - The number of characters in a string does not represent the number of
 * significant digits in the 'number' like a purely numeric type would. In
 * other words, numeric 10 is greater than numeric 9 not because of the value
 * of the digits but because the number of digits is greater.
 */
public class InBetweenHelper {
    //default to the base 62 digits (0-9, A-Z, a-z)
    private static boolean useBase10Digits = false;

    //first value
    private static String DEFAULT_FIRST_VALUE = "5";

    /**
     * Pass in two string values and get a string that is lexically in between
     * the two.
     *
     * @param first  A string using either base 10 or base 62 digits. This value
     *               must lexically come before the second string.
     * @param second A string using either base 10 or base 62 digits. This value
     *               must lexically come after the first string.
     * @return A string using either base 10 or base 62 digits that is in
     * between first and second
     */
    public static String inbetween(String first, String second) {
        //a middle value in between the first and second
        String middleValue;

        //there is neither a first nor second, this is the first value
        if (first == null && second == null) {
            //set the middle value to the default first value
            middleValue = DEFAULT_FIRST_VALUE;
        } else if (first == null && second != null) //no first string, but there is a second
        {
            //this happens when adding to the very beginning to a non-empty document

            //get a value less than the second
            middleValue = decreaseStringBySmallestPossibleValue(second);
        } else if (first != null && second == null) //no second string, but there is a first
        {
            //this heppens when adding to the very end of a non-empty document

            //increase the first value
            middleValue = increaseStringBySmallestPossibleValue(first);
        } else //there is a first and a second string, find one in between
        {
            //make as small an increase as possible on the first string by
            //increasing the least significant digit (this may cause a carry
            //to occur on the more significant digits)
            middleValue = increaseStringBySmallestPossibleValue(first);

            //if making that small increase to the first number results in a number
            //that is greater than or equal to the second it means that we have
            //to extend the length of the first number rather than just increase it
            if (middleValue.compareTo(second) >= 0) {
                //extend the first number out to be longer than the second AND
                //1 least significant digit greater than the first
                //examples:
                //first: 123 second: 124 => 1231
                //first: 123 second: 1231 => 12301
                //first: 123 second: 1230001 => 12300001
                //first: 642 second: 1446 => 643
                middleValue = extendStringByAddingDigits(first, second);
            }
            //else- the middle value is in between the first and second
        }
        return middleValue;
    }

    /**
     * This method increases the passed in string by the smallest possible
     * value by attempting to increase the least significant digit by one.
     * If increasing the last digit causes a carry, the next most significant
     * digit will have to be increased. This might cause a carry also, and so
     * on.
     *
     * @param num A base 10 or base 62 string to be increased
     * @return A larger base 10 or base 62 value than the passed in num
     */
    public static String increaseStringBySmallestPossibleValue(String num) {
        //a string that comes after the passed in one
        String retVal;

        //create a character array from the passed in number
        List<Character> digits = createCharacterArray(num);

        //if the string is filled with all of the most significant digits
        //we don't want to add and carry over on all the digits.
        //For example, in a base 10 number like 99999, we can't carry over to
        //100000 because this would kill our lexical ordering ('1' comes before
        //'9'). In a base 62 number like zzzzz adding and carrying will also
        //create 100000 which is not lexically after zzzzz. So, instead we
        //extend the original number by a digit. We append a '1' to the string
        //to make it lexically greater than the passed in string.
        if (allLargestDigits(digits)) {
            //extend the number out one additional digit
            //99999 => 999991 (base 10)
            //zzzzz => zzzzz1 (base 62)
            retVal = new String(num + '1');
        } else //we can increase the least significant digit and carry over
        {
            //position of the last digit
            int posOfLastDigit = num.length() - 1;

            //indicates how long we need to continue increasing digits
            boolean needToCarry = true;

            //while we need to increase the last digit
            while (needToCarry) {
                //get the last digit
                char lastDigit = digits.get(posOfLastDigit);

                //get the next available digit
                lastDigit = increaseDigit(lastDigit);

                //replace the last digit with the new one
                digits.set(posOfLastDigit, lastDigit);

                //if after increasing the last digit we wrapped around to zero,
                //this means we have to carry over in the next most significant
                //digit
                if (lastDigit == '0') {
                    //move backward to the next most significant digit
                    posOfLastDigit--;
                } else //no need to carry or continue on increasing the last digit
                {
                    needToCarry = false;
                }
            }

            //create a string with the list of characters
            retVal = createStringFromList(digits);
        }

        //return the new string
        return retVal;
    }

    private static boolean allLargestDigits(List<Character> digits) {
        //assume all the characters are all the largest digit ('9' for base 10 and 'z' for base 62)
        boolean retVal = true;

        //go through each character
        for (Character c : digits) {
            //if we are using base 10
            if (useBase10Digits) {
                //if we find a non-9
                if (c != '9') {
                    //they are not all 9's
                    retVal = false;
                    break;
                }
            } else //base 62
            {
                //if we find a non-z
                if (c != 'z') {
                    //they are not all z's
                    retVal = false;
                    break;
                }
            }
        }

        return retVal;
    }

    private static char increaseDigit(char digit) {
        char retVal;

        //if the number is base 10
        if (useBase10Digits) {
            //if it is the last possible digit
            if (digit == '9') {
                //wrap around to the first digit
                retVal = '0';
            } else //not the last
            {
                //just add one to get the next digit
                retVal = (char) (digit + 1);
            }
        } else //base 62
        {
            //if it is the last possible numeric digit
            if (digit == '9') {
                //wrap around to the first uppercase digit
                retVal = 'A';
            } else if (digit == 'Z') //last uppercase digit
            {
                //wrap around to the first lowercase digit
                retVal = 'a';
            } else if (digit == 'z') //last lowercase digit
            {
                //wrap around to the the smallest numeric digit
                retVal = '0';
            } else //not the last of any group
            {
                //just add one to get the next digit
                retVal = (char) (digit + 1);
            }
        }
        return retVal;
    }

    private static String extendStringByAddingDigits(String first, String second) {
        //the first and second are close, find out how many more digits there
        //are in the second compared to the first
        int secondsAdditionalDigits = second.length() - first.length();

        //build up a string with the first numbers and some number of 0's and a single 1 at the end
        StringBuilder builder = new StringBuilder();
        //start with the first number
        builder.append(first);

        //for each additional digit in the second
        for (int i = 0; i < secondsAdditionalDigits; i++) {
            //add a 0
            builder.append("0");
        }

        //add a one to make it larger than first but smaller than second
        builder.append("1");

        return builder.toString();
    }

    /**
     * This method find a lexically smaller string than the passed in one.
     * <p>
     * The basic algorithm is to look at the least significant digit and see
     * if it is a zero. If the least significant digit is not a zero then
     * we just decrease that digit by one. "237" => "236"
     * If the least significant digit is a zero then we remove that digit
     * and get the smallest possible value for the smaller string. "230" => "22"
     *
     * @param num The string that will be lexically larger than the return value
     * @return A value that is lexically smaller than the passed in num
     */
    public static String decreaseStringBySmallestPossibleValue(String num) {
        //a string that comes before the passed in one
        String retVal;

        //create a character array from the passed in number
        List<Character> digits = createCharacterArray(num);

        //get the last digit
        int posOfLastDigit = num.length() - 1;
        char lastDigit = digits.get(posOfLastDigit);

        //if the last digit is not a zero we can safely subtract one from it
        if (lastDigit != '0') {
            //replace the last digit with one less than it
            digits.set(posOfLastDigit, decreaseDigit(lastDigit));

            //create a new string with the new digits
            retVal = createStringFromList(digits);
        } else // the last digit is a 0
        {
            //chop off the last digit
            String smallerString = num.substring(0, posOfLastDigit);

            //subtract one from it (recursively)
            retVal = decreaseStringBySmallestPossibleValue(smallerString);
        }

        //now check if all the remaining characters are zero, assume they are all 0's
        boolean allZeros = true;

        //go through all the characters until we reach a non-'0'
        for (char c : retVal.toCharArray()) {
            //if its not a 0
            if (c != '0') {
                allZeros = false;
                //stop looking
                break;
            }
        }

        //if all the characters are zero or there are no more characters
        if (allZeros || retVal.length() == 0) {
            //pad the original string with a zero in the most significant digit
            retVal = "0" + num;
        }

        return retVal;
    }

    /**
     * Takes a list of characters and builds up a string
     *
     * @param digits List of characters
     * @return String with those characters
     */
    private static String createStringFromList(List<Character> digits) {
        //rebuild the string
        StringBuilder builder = new StringBuilder();

        //add the digits to a builder
        for (int i = 0; i < digits.size(); i++) {
            builder.append(digits.get(i));
        }

        //return the new string
        return builder.toString();
    }

    /**
     * Breaks a string into a list of characters
     *
     * @param num A string to be broken up
     * @return The list of characters in the string
     */
    private static List<Character> createCharacterArray(String num) {
        //create a list of the characters that make up the number
        List<Character> digits = new ArrayList<Character>();

        //get each character
        for (Character c : num.toCharArray()) {
            //add it to the list
            digits.add(c);
        }

        return digits;
    }

    /**
     * Decrease a digit by one. Any value but 0 will be decreased whether it is
     * a base 10 or 62 number. 0 is not allowed (an exception will be thrown).
     *
     * @param digit The digit to be decreased
     * @return A value one smaller than the passed in digit
     */
    private static char decreaseDigit(char digit) {
        char retVal;

        //base 10 number
        if (useBase10Digits) {
            //if it is a non-0 digit
            if (digit >= '1' && digit <= '9') {
                //subtract one from the digit
                retVal = (char) (digit - 1);
            } else {
                throw new RuntimeException("Cannot decrease a 0");
            }
        } else //base 62 number
        {
            if (digit == 'a') //first lowercase digit
            {
                //wrap around to the the last upper case digit
                retVal = 'Z';
            } else if (digit == 'A') //first uppercase digit
            {
                //wrap around to the last numeric digit
                retVal = '9';
            } else if (digit >= '1' && digit <= '9')//non-0 numeric digit
            {
                //subtract one from the digit
                retVal = (char) (digit - 1);
            } else {
                throw new RuntimeException("Cannot decrease a 0");
            }
        }

        return retVal;
    }

    //change to base 10 digits or not
    public static void setUseBase10Digits(boolean v) {
        useBase10Digits = v;
    }
}