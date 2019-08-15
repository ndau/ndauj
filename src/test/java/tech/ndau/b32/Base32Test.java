package tech.ndau.b32;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Base32Test {

    static class TestPair {
        String decoded;
        String encoded;

        TestPair(String decoded, String encoded) {
            this.decoded = decoded;
            this.encoded = encoded;
        }
    }

    static TestPair[] testPairs = {
            // RFC 4648 examples
            new TestPair("", ""),
            new TestPair("f", "MY======"),
            new TestPair("fo", "MZXQ===="),
            new TestPair("foo", "MZXW6==="),
            new TestPair("foob", "MZXW6YQ="),
            new TestPair("fooba", "MZXW6YTB"),
            new TestPair("foobar", "MZXW6YTBOI======"),

            // Wikipedia examples, converted to base32
            new TestPair("sure.", "ON2XEZJO"),
            new TestPair("sure", "ON2XEZI="),
            new TestPair("sur", "ON2XE==="),
            new TestPair("su", "ON2Q===="),
            new TestPair("leasure.", "NRSWC43VOJSS4==="),
            new TestPair("easure.", "MVQXG5LSMUXA===="),
            new TestPair("asure.", "MFZXK4TFFY======"),
            new TestPair("sure.", "ON2XEZJO"),

            // big
            new TestPair(
                    "Twas brillig, and the slithy toves",
                    "KR3WC4ZAMJZGS3DMNFTSYIDBNZSCA5DIMUQHG3DJORUHSIDUN53GK4Y="
            )
    };

    static Stream<TestPair> pairProvider() {
        return Stream.of(Base32Test.testPairs);
    }

    @ParameterizedTest
    @MethodSource("pairProvider")
    void encodeToString(TestPair pair) {
        String out = Base32.StdEncoding.EncodeToString(pair.decoded.getBytes());
        assertEquals(pair.encoded, out);
    }

    @ParameterizedTest
    @MethodSource("pairProvider")
    void decodeString(TestPair pair) throws CorruptInputError {
        byte[] out = Base32.StdEncoding.DecodeString(pair.encoded);
        assertEquals(pair.decoded.getBytes(), out);
    }
}
