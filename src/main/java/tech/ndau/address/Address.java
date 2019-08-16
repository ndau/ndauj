package tech.ndau.address;


import tech.ndau.b32.Base32;
import tech.ndau.b32.CorruptInputError;

public final class Address {
    @SuppressWarnings("WeakerAccess")
    public static final int AddrLength = 48;
    private static final String addrPrefix = "nd";
    private static final int kindOffset = Address.addrPrefix.length();
    public static int MinDataLength = 12;
    private static int hashTrim = 26;
    private String addr;

    /**
     * Create and validate an Address
     *
     * @param addr the address to validate
     */
    public Address(String addr) throws InvalidAddress {
        addr = addr.toLowerCase();
        Address.Validate(addr);
        this.addr = addr;
    }

    private static Pair<byte[], byte[]> splitAddrData(byte[] addrData) {
        byte[] data = new byte[addrData.length - 2];
        byte[] cksum = new byte[2];

        System.arraycopy(addrData, 0, data, 0, addrData.length - 2);
        System.arraycopy(addrData, addrData.length - 2, cksum, 0, 2);
        return new Pair<>(data, cksum);
    }

    /**
     * Validate a string as an address
     *
     * @param addr should be an ndau address
     * @throws InvalidAddress if the provided string is not a valid address.
     */
    public static void Validate(String addr) throws InvalidAddress {
        if (!addr.startsWith(addrPrefix)) {
            throw new InvalidAddress(String.format("Address must begin with the prefix %s", addrPrefix));
        }
        if (addr.length() != AddrLength) {
            throw new InvalidAddress(String.format("Address must have length %d", AddrLength));
        }
        try {
            Address.Kind.Parse(addr.getBytes()[kindOffset]);
        } catch (IllegalArgumentException e) {
            throw new InvalidAddress(e);
        }
        byte[] h;
        try {
            h = Base32.NdauEncoding.DecodeString(addr);
        } catch (CorruptInputError e) {
            throw new InvalidAddress(e);
        }
        Pair<byte[], byte[]> pair = Address.splitAddrData(h);
        if (!Checksum.Check(pair.x, pair.y)) {
            throw new InvalidAddress("checksum failure");
        }
    }

    @Override
    public String toString() {
        return this.addr;
    }

    public Kind kind() {
        return Kind.Parse(this.addr.getBytes()[kindOffset]);
    }

    public enum Kind {
        User,
        Ndau,
        Endowment,
        Exchange,
        BPC,
        MarketMaker;

        @SuppressWarnings("WeakerAccess")
        public static Kind Parse(byte b) throws IllegalArgumentException {
            switch (b) {
                case 'a':
                    return User;
                case 'n':
                    return Ndau;
                case 'e':
                    return Endowment;
                case 'x':
                    return Exchange;
                case 'b':
                    return BPC;
                case 'm':
                    return MarketMaker;
                default:
                    throw new IllegalArgumentException(String.format("Unknown Kind byte %c", b));
            }
        }

        public byte Byte() {
            switch (this) {
                case User:
                    return 'a';
                case Ndau:
                    return 'n';
                case Endowment:
                    return 'e';
                case Exchange:
                    return 'x';
                case BPC:
                    return 'b';
                case MarketMaker:
                    return 'm';
                default:
                    // this will never happen; we've covered every case in the possibility space
                    return 0x00;
            }
        }
    }

    private static class Pair<X, Y> {
        final X x;
        final Y y;

        Pair(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }
}
