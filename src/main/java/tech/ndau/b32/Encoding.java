package main.java.tech.ndau.b32;

// largely translated from https://golang.org/src/encoding/base32/base32.go?s=569:650#L13

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * An Encoding is a radix 32 encoding/decoding scheme, defined by a
 * 32-character alphabet. The most common is the "base32" encoding
 * introduced for SASL GSSAPI and standardized in RFC 4648.
 * The alternate "base32hex" encoding is used in DNSSEC.
 */
public final class Encoding {
    private byte[] alphabet;
    private byte[] decodeMap;
    private byte padChar;
    private static int decodeMapSize = 256;

    private static String StdAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    /**
     * StdEncoding is the standard base32 encoding as defined in RFC 4648
     */
    public static Encoding StdEncoding = new Encoding(StdAlphabet);


    public Encoding(String alphabet) {
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
     * EncodedLen returns the length in bytes of the base32 encoding of
     * an input buffer of length n.
     *
     * @return length of encoded
     */
    public static int EncodedLen(int n) {
        return (n + 4) / 5 * 8;
    }

    /**
     * Encode encodes src using this encoding, writing EncodedLen(src.length) bytes to dst.
     * <p>
     * The encoding pads the output to a multiple of 8 bytes, so Encode is not appropriate
     * for use on individual blocks of a large data stream.
     *
     * @param src source bytes
     */
    public List<Byte> Encode(List<Byte> src) {
        // shallow-copy the src so we don't clear the original
        src = new ArrayList<>(src);

        int size = Encoding.EncodedLen(src.size());

        // create an output array of the appropriate size
        List<Byte> out = new ArrayList<>(size);
        // create a shallow copy

        while (src.size() > 0) {
            byte[] b = new byte[8];

            // unpack 8x 5-bit source blocks into a 5-byte
            // destination quantum
            switch (src.size()) {
                default:
                    b[7] = (byte) (src.get(4) & 0x1f);
                    b[6] = src.get(4) >> 5;
                case 4:
                    b[6] |= (byte) ((src.get(3) << 3) & 0x1f);
                    b[5] = (byte) ((src.get(3) >> 2) & 0x1f);
                    b[4] = src.get(3) >> 7;
                case 3:
                    b[4] |= (byte) ((src.get(2) << 1) & 0x1f);
                    b[3] = (byte) ((src.get(2) >> 4) & 0x1f);
                case 2:
                    b[3] |= (byte) ((src.get(1) << 4) & 0x1f);
                    b[2] = (byte) ((src.get(1) >> 1) & 0x1f);
                    b[1] = (byte) ((src.get(1) >> 6) & 0x1f);
                case 1:
                    b[1] |= (byte) ((src.get(0) << 2) & 0x1f);
                    b[0] = src.get(0) >> 3;
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
     * @param src input data
     * @return base32 encoding of the input
     */
    public String EncodeToString(byte[] src) {
        // there doesn't appear to be a straightforward way to convert byte[] into ArrayList<Byte>,
        // for Reasons
        ArrayList<Byte> srcl = new ArrayList<Byte>(src.length);
        for (byte b : src) {
            srcl.add(b);
        }
        List<Byte> data = this.Encode(srcl);
        // of course, there's also no simple way to perform the transform in the other direction either
        byte[] outa = new byte[data.size()];
        int i = 0;
        for (Byte b: data) {
            outa[i] = b;
            i++;
        }
        return new String(outa);
    }
}
