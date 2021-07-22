package util;

public class StringUtil {
    public static String repeatChar(char c, int times) {
        if (times == 0) return "";
        char sequence[] = new char[times];
        int times_div2 = times >> 1;
        sequence[0] = c;
        int length = 1;
        for (; length <= times_div2; length <<= 1) {
            System.arraycopy(sequence, 0, sequence, length, length);
        }
        System.arraycopy(sequence, 0, sequence, length, times - length);
        return new String(sequence);
    }
}
