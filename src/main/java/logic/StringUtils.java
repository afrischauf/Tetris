package logic;

import com.google.common.base.Strings;

/**
 * StringUtils class provides utility methods for manipulating strings.
 */
public class StringUtils {

    /**
     * Centers a string within a specified size by padding it with a given character.
     *
     * @param s    the string to be centered
     * @param size the size of the resulting centered string
     * @param pad  the character used for padding
     * @return the centered string
     */
    public static String center(String s, int size, char pad) {
        if (s == null || size <= s.length())
            return s;

        StringBuilder sb = new StringBuilder(size);
        sb.append(Strings.repeat(String.valueOf(pad), (size - s.length() / 2)));
        sb.append(s);
        while (sb.length() < size) {
            sb.append(pad);
        }
        return sb.toString();
    }
}
