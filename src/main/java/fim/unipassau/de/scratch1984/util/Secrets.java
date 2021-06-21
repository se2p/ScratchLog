package fim.unipassau.de.scratch1984.util;

import java.security.SecureRandom;

/**
 * Utility class for generating user secrets.
 */
public final class Secrets {

    /**
     * Cryptographically secure random generator.
     */
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generates the given number of random bytes in hexadecimal representation.
     *
     * @param numBytes The desired number of bytes to randomly generate.
     * @return {@code numBytes} random bytes as hexadecimal string of format {@code ([0-9a-f]{2})*}.
     */
    public static String generateRandomBytes(final int numBytes) {
        byte[] bytes = new byte[numBytes];
        random.nextBytes(bytes);
        return bytesToHex(bytes);
    }

    /**
     * Returns a hexadecimal string representation of the given byte array.
     *
     * @param bytes The bytes to be converted.
     * @return The string representation.
     */
    private static String bytesToHex(final byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
