package tech.ndau.b32;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Base32Test {

    private static Stream<Arguments> testPairs() {
        return Stream.of(
                // RFC 4648 examples
                Arguments.of("", ""),
                Arguments.of("f", "MY======"),
                Arguments.of("fo", "MZXQ===="),
                Arguments.of("foo", "MZXW6==="),
                Arguments.of("foob", "MZXW6YQ="),
                Arguments.of("fooba", "MZXW6YTB"),
                Arguments.of("foobar", "MZXW6YTBOI======"),

                // Wikipedia examples, converted to base32
                Arguments.of("sure.", "ON2XEZJO"),
                Arguments.of("sure", "ON2XEZI="),
                Arguments.of("sur", "ON2XE==="),
                Arguments.of("su", "ON2Q===="),
                Arguments.of("leasure.", "NRSWC43VOJSS4==="),
                Arguments.of("easure.", "MVQXG5LSMUXA===="),
                Arguments.of("asure.", "MFZXK4TFFY======"),
                Arguments.of("sure.", "ON2XEZJO"),

                // big
                Arguments.of(
                        "Twas brillig, and the slithy toves",
                        "KR3WC4ZAMJZGS3DMNFTSYIDBNZSCA5DIMUQHG3DJORUHSIDUN53GK4Y="
                )
        );
    }

    private static Stream<Arguments> decodeCorrupt() {
        // -1 => no corruption
        return Stream.of(
                Arguments.of("", -1),
                Arguments.of("!!!!", 0),
                Arguments.of("x===", 0),
                Arguments.of("AA=A====", 2),
                Arguments.of("AAA=AAAA", 3),
                Arguments.of("MMMMMMMMM", 8),
                Arguments.of("MMMMMM", 0),
                Arguments.of("A=", 1),
                Arguments.of("AA=", 3),
                Arguments.of("AA==", 4),
                Arguments.of("AA===", 5),
                Arguments.of("AAAA=", 5),
                Arguments.of("AAAA==", 6),
                Arguments.of("AAAAA=", 6),
                Arguments.of("AAAAA==", 7),
                Arguments.of("A=======", 1),
                Arguments.of("AA======", -1),
                Arguments.of("AAA=====", 3),
                Arguments.of("AAAA====", -1),
                Arguments.of("AAAAA===", -1),
                Arguments.of("AAAAAA==", 6),
                Arguments.of("AAAAAAA=", -1),
                Arguments.of("AAAAAAAA", -1)
        );
    }

    private static Stream<Arguments> ndauPairs() {
        return Stream.of(
                Arguments.of(new byte[]{}, ""),
                Arguments.of(new byte[]{1, 2, 3, 4, 5}, "aebagbaf"),
                Arguments.of(new byte[]{0, 0, 0, 0, 0}, "aaaaaaaa"),
                Arguments.of(new byte[]{99, 100, 21, 0, 0}, "npubkaaa"),
                Arguments.of(new byte[]{99, 100, 21, (byte) 255, (byte) 255}, "npubm999"),
                Arguments.of(new byte[]{99, 100, 16, 0, 0}, "npubaaaa"),
                Arguments.of(new byte[]{99, 103, 31, (byte) 255, (byte) 255}, "npvt9999"),
                Arguments.of(new byte[]{(byte) 139, 100, 16, 0, 0}, "tpubaaaa"),
                Arguments.of(new byte[]{(byte) 139, 103, 31, (byte) 255, (byte) 255}, "tpvt9999")
        );
    }

    @ParameterizedTest
    @MethodSource("testPairs")
    void encodeToString(String decoded, String encoded) {
        String out = Base32.StdEncoding.EncodeToString(decoded.getBytes());
        assertEquals(encoded, out);
    }

    @ParameterizedTest
    @MethodSource("testPairs")
    void decodeString(String decoded, String encoded) throws CorruptInputError {
        byte[] out = Base32.StdEncoding.DecodeString(encoded);
        String s = new String(out);
        assertEquals(decoded, s);
    }

    @ParameterizedTest
    @MethodSource("decodeCorrupt")
    void decodeCorrupt(String encoded, int offset) {
        int caughtOffset = -1;
        try {
            Base32.StdEncoding.DecodeString(encoded);
        } catch (CorruptInputError e) {
            caughtOffset = e.getErrByte();
        }
        assertEquals(offset, caughtOffset);
    }

    @ParameterizedTest
    @MethodSource("ndauPairs")
    void ndauEncodeToString(byte[] decoded, String encoded) {
        String out = Base32.NdauEncoding.EncodeToString(decoded);
        assertEquals(encoded, out);
    }

    @ParameterizedTest
    @MethodSource("ndauPairs")
    void ndauDecodeString(byte[] decoded, String encoded) throws CorruptInputError {
        byte[] out = Base32.NdauEncoding.DecodeString(encoded);
        assertTrue(Arrays.equals(out, decoded));
    }
}
