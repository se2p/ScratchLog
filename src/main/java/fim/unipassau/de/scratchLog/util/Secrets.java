/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */

package fim.unipassau.de.scratchLog.util;

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
