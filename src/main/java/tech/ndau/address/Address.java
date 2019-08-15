package tech.ndau.address;


public final class Address {
    private String addr;

    public enum Kind {
        User,
        Ndau,
        Endowment,
        Exchange,
        BPC,
        MarketMaker;

        public static Kind Parse(byte b) throws IllegalArgumentException {
            switch(b) {
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
            switch(this) {
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

    private static String addrPrefix = "nd";
    private static int kindOffset = Address.addrPrefix.length();
    private static int hashTrim = 26;
    public static int AddrLength = 48;
    public static int MinDataLength = 12;

    /**
     * Create and validate an Address
     *
     * @param addr the address to validate
     */
    public Address(String addr) throws InvalidAddress {
        addr = addr.toLowerCase();
        if (! addr.startsWith(addrPrefix)) {
            throw new InvalidAddress(String.format("Address must begin with the prefix %s", addrPrefix));
        }
        if (addr.length() != AddrLength) {
            throw new InvalidAddress(String.format("Address must have length %d", AddrLength));
        }
        Address.Kind kind;
        try {
            kind = Address.Kind.Parse(addr.getBytes()[kindOffset]);
        } catch(IllegalArgumentException e) {
            throw new InvalidAddress(e.toString());
        }

    }

    public String toString() {
        return this.addr;
    }
}
