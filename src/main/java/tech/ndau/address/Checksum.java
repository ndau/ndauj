package tech.ndau.address;

import com.github.snksoft.crc.CRC;

import java.util.Arrays;

@SuppressWarnings("WeakerAccess")
public final class Checksum {

    // we use AUG_CCITT here, which isn't defined as a default parameter set
    private static final CRC.Parameters AUG_CCITT = new CRC.Parameters(
            16, //width
            0x1021, // polynomial
            0x1d0f, // init
            false, // reflectIn
            false, //reflectOut
            0x0000 //finalXor
    );
    private static final CRC table = new CRC(AUG_CCITT);

    /**
     * Compute a 2-byte checksum of data
     *
     * @param data data to check
     * @return checksum
     */
    public static byte[] Checksum16(byte[] data) {
        long ck = Checksum.table.calculateCRC(data);
        return new byte[]{
                (byte) ((ck & 0xff00) >>> 8), (byte) (ck & 0xff)
        };
    }

    /**
     * Check validates some data against its checksum
     *
     * @param data  data to check
     * @param cksum expected checksum
     * @return true if the data produces the provided checksum
     */
    public static boolean Check(byte[] data, byte[] cksum) {
        byte[] ck = Checksum.Checksum16(data);
        return Arrays.equals(ck, cksum);
    }
}
