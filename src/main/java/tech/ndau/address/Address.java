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


import tech.ndau.b32.Base32;
import tech.ndau.b32.CorruptInputError;

public final class Address {
    @SuppressWarnings("WeakerAccess")
    /**
     * Length of an ndau address.
     */
    public static final int ADDR_LENGTH = 48;
    /**
     * prefix of all valid ndau addresses.
     */
    private static final String ADDR_PREFIX = "nd";
    /**
     * offset of kind in an ndau address.
     */
    private static final int KIND_OFFSET = Address.ADDR_PREFIX.length();
    /**
     * string representation of this address.
     */
    private String addr;

    /**
     * Create and validate an Address.
     *
     * @param addr the address to validate
     * @throws InvalidAddress when the address is not valid
     */
    public Address(final String addr) throws InvalidAddress {
        final String addrCopy = addr.toLowerCase();
        Address.validate(addrCopy);
        this.addr = addrCopy;
    }

    private static Pair<byte[], byte[]> splitAddrData(final byte[] addrData) {
        final byte[] data = new byte[addrData.length - 2];
        final byte[] cksum = new byte[2];

        System.arraycopy(addrData, 0, data, 0, addrData.length - 2);
        System.arraycopy(addrData, addrData.length - 2, cksum, 0, 2);
        return new Pair<>(data, cksum);
    }

    /**
     * Validate a string as an address.
     *
     * @param addr should be an ndau address
     * @throws InvalidAddress if the provided string is not a valid address.
     */
    public static void validate(final String addr) throws InvalidAddress {
        if (!addr.startsWith(ADDR_PREFIX)) {
            throw new InvalidAddress(
                    String.format("Address must begin with the prefix %s", ADDR_PREFIX));
        }
        if (addr.length() != ADDR_LENGTH) {
            throw new InvalidAddress(
                    String.format("Address must have length %d", ADDR_LENGTH));
        }
        try {
            Address.Kind.parse(addr.getBytes()[KIND_OFFSET]);
        } catch (final IllegalArgumentException e) {
            throw new InvalidAddress(e);
        }
        final byte[] h;
        try {
            h = Base32.NDAU_ENCODING.decodeString(addr);
        } catch (final CorruptInputError e) {
            throw new InvalidAddress(e);
        }
        final Pair<byte[], byte[]> pair = Address.splitAddrData(h);
        if (!Checksum.check(pair.x, pair.y)) {
            throw new InvalidAddress("checksum failure");
        }
    }

    @Override
    public String toString() {
        return this.addr;
    }

    /**
     * Get the kind of this address.
     *
     * @return the address's kind
     */
    public Kind kind() {
        return Kind.parse(this.addr.getBytes()[KIND_OFFSET]);
    }

    public enum Kind {
        User,
        Ndau,
        Endowment,
        Exchange,
        BPC,
        MarketMaker;

        /**
         * parse a kind.
         *
         * @param b a byte which might be a kind.
         * @return a kind.
         * @throws IllegalArgumentException when b was not a kind.
         */
        public static Kind parse(final byte b) throws IllegalArgumentException {
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
                    throw new IllegalArgumentException(
                            String.format("Unknown Kind byte %c", b));
            }
        }

        /**
         * Transform a kind into a byte.
         *
         * @return the byte value of the given kind.
         */
        public byte toByte() {
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
                    // this will never happen:
                    // we've covered every case in the possibility space
                    return 0x00;
            }
        }
    }

    private static class Pair<X, Y> {
        final X x;
        final Y y;

        Pair(final X x, final Y y) {
            this.x = x;
            this.y = y;
        }
    }
}
