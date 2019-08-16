package tech.ndau.address;

import com.github.snksoft.crc.CRC;

@SuppressWarnings("WeakerAccess")
public final class Checksum {

    private static final CRC table = new CRC(CRC.Parameters.CCITT);

    /**
     * Compute a 2-byte checksum of data
     *
     * @param data data to check
     * @return checksum
     */
    public static byte[] Checksum16(byte[] data) {
        long ck = Checksum.table.calculateCRC(data);
        return new byte[]{
                (byte) (ck >> 8 & 0xff), (byte) (ck & 0xff)
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
        return ck == cksum;
    }
}
