/*
 * This library is licensed under version 3.0 of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * This software is distributed WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License 3.0 for more details.
 *
 * Copyright © 2019 Oneiro NA, Inc.
 */

package tech.ndau.b32;

// largely translated from https://golang.org/src/encoding/base32/base32.go

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An Base32 is a radix 32 encoding/decoding scheme, defined by a 32-character
 * alphabet. The most common is the "base32" encoding introduced for SASL GSSAPI
 * and standardized in RFC 4648. The alternate "base32hex" encoding is used in
 * DNSSEC.
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
public final class Base32 {
    /**
     * NdauEncoding is base32 encoding with a custom alphabet.
     * <p>
     * It consists of the lowercase alphabet and digits, without
     * l, 1, 0, and o. When decoding, we accept either case.
     */
    public static final Base32 NDAU_ENCODING;

    private static final int DECODE_MAP_SIZE = 256;
    private static final String STD_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    /**
     * StdEncoding is the standard base32 encoding as defined in RFC 4648.
     */
    public static final Base32 STD_ENCODING = new Base32(STD_ALPHABET);
    private static final String NDAU_ALPHABET = "abcdefghijkmnpqrstuvwxyz23456789";

    static {
        NDAU_ENCODING = new Base32(NDAU_ALPHABET);
        NDAU_ENCODING.foldLowercase = true;
    }

    private byte[] alphabet;
    private byte[] decodeMap;
    private byte padChar;
    private boolean foldLowercase = false;

    /**
     * Create a base32 codec from an alphabet.
     *
     * @param alphabet the alphabet to use. Must be 32 bytes long. Each byte must be a valid character.
     */
    public Base32(final String alphabet) {
        this.padChar = '=';
        this.alphabet = alphabet.getBytes();

        if (this.alphabet.length != 32) {
            throw new IllegalArgumentException("encoding alphabet is not 32 bytes long");
        }

        this.decodeMap = new byte[DECODE_MAP_SIZE];
        Arrays.fill(this.decodeMap, (byte) 0xff);
        for (int i = 0; i < this.alphabet.length; i++) {
            this.decodeMap[this.alphabet[i]] = (byte) i;
        }
    }

    /**
     * EncodedLen returns the length in bytes of the base32 encoding of an input
     * buffer of length n.
     *
     * @param n length of source data
     * @return length of encoded
     */
    public static int encodedLen(final int n) {
        return (n + 4) / 5 * 8;
    }

    /**
     * DecodedLen returns the maximum length in bytes of the decoded data
     * corresponding to n bytes of base32-encoded data.
     *
     * @param n length of encoded string
     * @return length of decoded data
     */
    public static int decodedLen(final int n) {
        return n / 8 * 5;
    }

    /**
     * Encode encodes src using this encoding, writing EncodedLen(src.length) bytes
     * to dst.
     * <p>
     * The encoding pads the output to a multiple of 8 bytes, so Encode is not
     * appropriate for use on individual blocks of a large data stream.
     *
     * @param src source bytes
     * @return a list of encoded bytes
     */
    private List<Byte> encode(final List<Byte> src) {
        // shallow-copy the src so we don't clear the original
        List<Byte> s = new ArrayList<>(src);

        final int size = Base32.encodedLen(s.size());

        // create an output array of the appropriate size
        // we'd really kind of prefer that this be a direct byte array
        // so we can address particular bytes directly, as the go version
        // does, but the go code depends on go-specific slicing behavior which
        // really doesn't translate well into java
        final List<Byte> out = new ArrayList<>(size);

        while (s.size() > 0) {
            final byte[] b = new byte[8];

            // unpack 8x 5-bit source blocks into a 5-byte
            // destination quantum
            switch (s.size()) {
                default:
                    b[7] = (byte) (s.get(4) & 0x1f);
                    b[6] = (byte) ((s.get(4) & 0xff) >>> 5);
                case 4:
                    b[6] |= (byte) ((s.get(3) << 3) & 0x1f);
                    b[5] = (byte) (((s.get(3) & 0xff) >>> 2) & 0x1f);
                    b[4] = (byte) ((s.get(3) & 0xff) >>> 7);
                case 3:
                    b[4] |= (byte) ((s.get(2) << 1) & 0x1f);
                    b[3] = (byte) (((s.get(2) & 0xff) >>> 4) & 0x1f);
                case 2:
                    b[3] |= (byte) ((s.get(1) << 4) & 0x1f);
                    b[2] = (byte) (((s.get(1) & 0xff) >>> 1) & 0x1f);
                    b[1] = (byte) (((s.get(1) & 0xff) >>> 6) & 0x1f);
                case 1:
                    b[1] |= (byte) ((s.get(0) << 2) & 0x1f);
                    b[0] = (byte) ((s.get(0) & 0xff) >>> 3);
            }

            // encode 5-bit blocks using the base32 alphabet
            for (int i = 0; i < Math.min(size, 8); i++) {
                out.add(this.alphabet[b[i] & 31]);
            }

            // Pad the final quantum
            if (s.size() < 2) {
                out.set(out.size() - 6, this.padChar);
                out.set(out.size() - 5, this.padChar);
            }
            if (s.size() < 3) {
                out.set(out.size() - 4, this.padChar);
            }
            if (s.size() < 4) {
                out.set(out.size() - 3, this.padChar);
                out.set(out.size() - 2, this.padChar);
            }
            if (s.size() < 5) {
                out.set(out.size() - 1, this.padChar);
                break;
            }

            s = s.subList(5, s.size());
        }

        return out;
    }

    /**
     * EncodeToString encodes the supplied data as a string.
     *
     * @param src input data
     * @return base32 encoding of the input
     */
    public String encodeToString(final byte[] src) {
        // there doesn't appear to be a straightforward way to convert byte[] into
        // ArrayList<Byte>,
        // for Reasons
        final ArrayList<Byte> srcl = new ArrayList<>(src.length);
        for (final byte b : src) {
            srcl.add(b);
        }
        final List<Byte> data = this.encode(srcl);
        // of course, there's also no simple way to perform the transform in the other
        // direction either
        final byte[] outa = new byte[data.size()];
        int i = 0;
        for (final Byte b : data) {
            outa[i] = b;
            i++;
        }
        return new String(outa);
    }

    private List<Byte> decode(final List<Byte> src) throws CorruptInputError {
        final int olen = src.size();
        boolean end = false;
        // shallow-copy the src
        List<Byte> s = new ArrayList<>(src);
        // prepare dest
        final List<Byte> dst = new ArrayList<>(Base32.decodedLen(s.size()));

        while (s.size() > 0 && !end) {
            // decode quantum using the base32 alphabet
            final byte[] dbuf = new byte[8];
            int dlen = 8;

            for (int j = 0; j < 8; j++) {
                // we have reached the end and are missing padding
                if (s.size() == 0) {
                    throw new CorruptInputError(olen - s.size() - j);
                }

                final byte in = s.get(0);
                s = s.subList(1, s.size());

                if (in == this.padChar && j >= 2 && s.size() < 8) {
                    // we've reached the end and there's padding
                    if (s.size() + j < 8 - 1) {
                        // not enough padding
                        throw new CorruptInputError(olen);
                    }
                    for (int k = 0; k < 8 - 1 - j; k++) {
                        if (s.size() > k && s.get(k) != this.padChar) {
                            // incorrect padding
                            throw new CorruptInputError(olen - s.size() + k - 1);
                        }
                    }
                    dlen = j;
                    end = true;
                    // 7, 5 and 2 are not valid padding lengths, and so 1, 3 and 6 are not
                    // valid dlen values. See RFC 4648 Section 6 "Base 32 Base32" listing
                    // the five valid padding lengths, and Section 9 "Illustrations and
                    // Examples" for an illustration for how the 1st, 3rd and 6th base32
                    // src bytes do not yield enough information to decode a dst byte.
                    if (dlen == 1 || dlen == 3 || dlen == 6) {
                        throw new CorruptInputError(olen - s.size() - 1);
                    }
                    break;
                }
                dbuf[j] = this.decodeMap[in];
                if (dbuf[j] == (byte) 0xff) {
                    throw new CorruptInputError(olen - s.size() - 1);
                }
            }

            // pack 8 5-bit source blocks into 5 byte destination
            final byte[] suffix = new byte[5]; // bytes to append to the dest
            int dcnt = 0; // how many need appending
            switch (dlen) {
                case 8:
                    suffix[4] = (byte) (dbuf[6] << 5 | dbuf[7]);
                    dcnt++;
                case 7:
                    suffix[3] = (byte) (dbuf[4] << 7 | dbuf[5] << 2 | (dbuf[6] & 0xff) >>> 3);
                    dcnt++;
                case 5:
                    suffix[2] = (byte) (dbuf[3] << 4 | (dbuf[4] & 0xff) >>> 1);
                    dcnt++;
                case 4:
                    suffix[1] = (byte) (dbuf[1] << 6 | dbuf[2] << 1 | (dbuf[3] & 0xff) >>> 4);
                    dcnt++;
                case 2:
                    suffix[0] = (byte) (dbuf[0] << 3 | (dbuf[1] & 0xff) >>> 2);
                    dcnt++;
            }

            for (int i = 0; i < dcnt; i++) {
                dst.add(suffix[i]);
            }
        }

        return dst;
    }

    /**
     * Decode a string into the bytes that it represents.
     *
     * @param src base32-encoded data.
     * @return the decoded bytes.
     * @throws CorruptInputError if src did not in fact encode base32 data with this encoder.
     */
    public byte[] decodeString(String src) throws CorruptInputError {
        if (this.foldLowercase) {
            src = src.toLowerCase();
        }
        List<Byte> bytel = new ArrayList<>(src.length());
        for (final byte b : src.getBytes()) {
            bytel.add(b);
        }
        bytel = this.decode(bytel);
        final byte[] out = new byte[bytel.size()];
        for (int i = 0; i < bytel.size(); i++) {
            out[i] = bytel.get(i);
        }
        return out;
    }
}
