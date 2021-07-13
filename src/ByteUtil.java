public class ByteUtil {

    public static String byte2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String tmp = null;

        for (byte b : bytes) {
            tmp = Integer.toHexString(0xFF & b);
            if (tmp.length() == 1) {
                tmp = "0" + tmp.toUpperCase();
            }
            sb.append(tmp.toUpperCase()).append(" ");
        }
        return sb.toString();
    }
}
