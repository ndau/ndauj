package tech.ndau.address;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AddressTest {
    @Test
    void knownGoodKeyValidates() throws InvalidAddress {
        Address.Validate("ndadprx764ciigti8d8whtw2kct733r85qvjukhqhke3dka4");
    }

    @Test
    void knownGoodKeyConstructs() throws InvalidAddress {
        new Address("ndadprx764ciigti8d8whtw2kct733r85qvjukhqhke3dka4");
    }

    @Test
    void knownBadKeyDoesNotValidate() {
        assertThrows(InvalidAddress.class, () -> {
            Address.Validate("ndxdprx764ciigti8d8whtw2kct733r85qvjukhqhke3dka4");
        });
    }

    @Test
    void knownBadKeyDoesNotConstruct() {
        assertThrows(InvalidAddress.class, () -> {
            new Address("ndxdprx764ciigti8d8whtw2kct733r85qvjukhqhke3dka4");
        });
    }
}
