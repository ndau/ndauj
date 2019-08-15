package main.java.tech.ndau.b32;

// largely translated from https://golang.org/src/encoding/base32/base32.go?s=569:650#L13

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An Base32 is a radix 32 encoding/decoding scheme, defined by a 32-character
 * alphabet. The most common is the "base32" encoding introduced for SASL GSSAPI
 * and standardized in RFC 4648. The alternate "base32hex" encoding is used in
 * DNSSEC.
 */
public final class Base32 {
    private byte[] alphabet;
    private byte[] decodeMap;
    private byte padChar;
    private static int decodeMapSize = 256;

    private static String StdAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    /**
     * StdEncoding is the standard base32 encoding as defined in RFC 4648
     */
    public static Base32 StdEncoding = new Base32(StdAlphabet);

    public Base32(String alphabet) {
        this.padChar = '=';
        this.alphabet = alphabet.getBytes();

        if (this.alphabet.length != 32) {
            throw new IllegalArgumentException("encoding alphabet is not 32 bytes long");
        }

        decodeMap = new byte[decodeMapSize];
        Arrays.fill(this.decodeMap, (byte) 0xff);
        for (int i = 0; i < this.alphabet.length; i++) {
            this.decodeMap[this.alphabet[i]] = (byte) i;
        }
    }

    /**
     * EncodedLen returns the length in bytes of the base32 encoding of an input
     * buffer of length n.
     *
     * @return length of encoded
     */
    public static int EncodedLen(int n) {
        return (n + 4) / 5 * 8;
    }

    /**
     * DecodedLen returns the maximum length in bytes of the decoded data
     * corresponding to n bytes of base32-encoded data.
     *
     * @return length of decoded
     */
    public static int DecodedLen(int n) {
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
     */
    private List<Byte> Encode(List<Byte> src) {
        // shallow-copy the src so we don't clear the original
        src = new ArrayList<>(src);

        int size = Base32.EncodedLen(src.size());

        // create an output array of the appropriate size
        // we'd really kind of prefer that this be a direct byte array
        // so we can address particular bytes directly, as the go version
        // does, but the go code depends on go-specific slicing behavior which
        // really doesn't translate well into java
        List<Byte> out = new ArrayList<>(size);

        while (src.size() > 0) {
            byte[] b = new byte[8];

            // unpack 8x 5-bit source blocks into a 5-byte
            // destination quantum
            switch (src.size()) {
            default:
                b[7] = (byte) (src.get(4) & 0x1f);
                b[6] = (byte) (src.get(4) >> 5);
            case 4:
                b[6] |= (byte) ((src.get(3) << 3) & 0x1f);
                b[5] = (byte) ((src.get(3) >> 2) & 0x1f);
                b[4] = (byte) (src.get(3) >> 7);
            case 3:
                b[4] |= (byte) ((src.get(2) << 1) & 0x1f);
                b[3] = (byte) ((src.get(2) >> 4) & 0x1f);
            case 2:
                b[3] |= (byte) ((src.get(1) << 4) & 0x1f);
                b[2] = (byte) ((src.get(1) >> 1) & 0x1f);
                b[1] = (byte) ((src.get(1) >> 6) & 0x1f);
            case 1:
                b[1] |= (byte) ((src.get(0) << 2) & 0x1f);
                b[0] = (byte) (src.get(0) >> 3);
            }

            // encode 5-bit blocks using the base32 alphabet
            for (int i = 0; i < size; i++) {
                out.add(this.alphabet[b[i] & 31]);
            }

            // Pad the final quantum
            if (src.size() < 2) {
                out.add(this.padChar);
                out.add(this.padChar);
            }
            if (src.size() < 3) {
                out.add(this.padChar);
            }
            if (src.size() < 4) {
                out.add(this.padChar);
                out.add(this.padChar);
            }
            if (src.size() < 5) {
                out.add(this.padChar);
                break;
            }

            src = src.subList(5, src.size());
        }

        return out;
    }

    /**
     * EncodeToString encodes the supplied data as a string
     *
     * @param src input data
     * @return base32 encoding of the input
     */
    public String EncodeToString(byte[] src) {
        // there doesn't appear to be a straightforward way to convert byte[] into
        // ArrayList<Byte>,
        // for Reasons
        ArrayList<Byte> srcl = new ArrayList<Byte>(src.length);
        for (byte b : src) {
            srcl.add(b);
        }
        List<Byte> data = this.Encode(srcl);
        // of course, there's also no simple way to perform the transform in the other
        // direction either
        byte[] outa = new byte[data.size()];
        int i = 0;
        for (Byte b : data) {
            outa[i] = b;
            i++;
        }
        return new String(outa);
    }

    private List<Byte> Decode(List<Byte> src) throws CorruptInputError {
        int olen = src.size();
        boolean end = false;
        // shallow-copy the src
        src = new ArrayList<>(src);
        // prepare dest
        List<Byte> dst = new ArrayList<>(Base32.DecodedLen(src.size()));

        while (src.size() > 0 && !end) {
            // decode quantum using the base32 alphabet
            byte[] dbuf = new byte[8];
            int dlen = 8;

            for (int j = 0; j < 8;) {
                // we have reached the end and are missing padding
                if (src.size() == 0) {
                    throw new CorruptInputError(olen - src.size() - j);
                }

                byte in = src.get(0);
                src = src.subList(1, src.size());

                if (in == this.padChar && j >= 2 && src.size() < 8) {
                    // we've reached the end and there's padding
                    if (src.size() + j < 8 - 1) {
                        // not enough padding
                        throw new CorruptInputError(olen);
                    }
                    for (int k = 0; k < 8 - 1 - j; k++) {
                        if (src.size() > k && src.get(k) != this.padChar) {
                            // incorrect padding
                            throw new CorruptInputError(olen = src.size() + k - 1);
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
                        throw new CorruptInputError(olen - src.size() - 1);
                    }
                    break;
                }
                dbuf[j] = this.decodeMap[in];
                if (dbuf[j] == (byte) 0xff) {
                    throw new CorruptInputError(olen - src.size() - 1);
                }
                j++;
            }

            // pack 8 5-bit source blocks into 5 byte destination
            switch (dlen) {
            case 2:
                dst.add((byte) (dbuf[0] << 3 | dbuf[1] >> 2));
            case 4:
                dst.add((byte) (dbuf[1] << 6 | dbuf[2] << 1 | dbuf[3] >> 4));
            case 5:
                dst.add((byte) (dbuf[3] << 4 | dbuf[4] >> 1));
            case 7:
                dst.add((byte) (dbuf[4] << 7 | dbuf[5] << 2 | dbuf[6] >> 3));
            case 8:
                dst.add((byte) (dbuf[6] << 5 | dbuf[7]));
            }
        }

        return dst;
    }

    public byte[] DecodeString(String src) throws CorruptInputError {
        List<Byte> bytel = new ArrayList<>(src.length());
        for (byte b : src.getBytes()) {
            bytel.add(b);
        }
        bytel = this.Decode(bytel);
        byte[] out = new byte[bytel.size()];
        for (int i = 0; i < bytel.size(); i++) {
            out[i] = bytel.get(i);
        }
        return out;
    }
}
