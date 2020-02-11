/*
 * This library is licensed under version 3.0 of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * This software is distributed WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License 3.0 for more details.
 *
 * Copyright © 2020 The Axiom Foundation
 */

package tech.ndau.address;

import com.github.snksoft.crc.CRC;

import java.util.Arrays;

@SuppressWarnings("WeakerAccess")
public final class Checksum {

    /**
     * AUG_CCITT isn't defined as a default parameter set.
     */
    private static final CRC.Parameters AUG_CCITT = new CRC.Parameters(
            16, //width
            0x1021, // polynomial
            0x1d0f, // init
            false, // reflectIn
            false, //reflectOut
            0x0000 //finalXor
    );

    /**
     * This table computes ndau-style checksums.
     */
    private static final CRC TABLE = new CRC(AUG_CCITT);

    // suppress constructor: this is a utility class
    private Checksum() {
    }

    /**
     * Compute a 2-byte checksum of data.
     *
     * @param data data to check
     * @return checksum
     */
    public static byte[] checksum16(final byte[] data) {
        final long ck = Checksum.TABLE.calculateCRC(data);
        return new byte[]{
                (byte) ((ck & 0xff00) >>> 8),
                (byte) (ck & 0xff)
        };
    }

    /**
     * Check validates some data against its checksum.
     *
     * @param data  data to check
     * @param cksum expected checksum
     * @return true if the data produces the provided checksum
     */
    public static boolean check(final byte[] data, final byte[] cksum) {
        final byte[] ck = Checksum.checksum16(data);
        return Arrays.equals(ck, cksum);
    }
}
