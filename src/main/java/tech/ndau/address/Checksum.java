package tech.ndau.address;

import com.github.snksoft.crc.CRC;

public final class Checksum {

    private static CRC table = new CRC(CRC.Parameters.CCITT);

    /**
     * Compute a 2-byte checksum of data
     *
     * @param data
     * @return checksum
     */
    public static byte[] Checksum16(byte[] data) {
        long ck = Checksum.table.calculateCRC(data);
        byte[] out = {
                (byte) (ck >> 8 & 0xff), (byte) (ck & 0xff)
        };
        return out;
    }

    /**
     * Check validates some data against its checksum
     *
     * @param data
     * @param cksum
     * @return true if the data produces the provided checksum
     */
    public static boolean Check(byte[] data, byte[] cksum) {
        byte[] ck = Checksum.Checksum16(data);
        return ck == cksum;
    }
}
