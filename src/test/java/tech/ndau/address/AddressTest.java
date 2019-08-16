package tech.ndau.address;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AddressTest {
    @Test
    void knownGoodKeyValidates() throws InvalidAddress {
        Address.validate("ndadprx764ciigti8d8whtw2kct733r85qvjukhqhke3dka4");
    }

    @Test
    void knownGoodKeyConstructs() throws InvalidAddress {
        new Address("ndadprx764ciigti8d8whtw2kct733r85qvjukhqhke3dka4");
    }

    @Test
    void knownBadKeyDoesNotValidate() {
        assertThrows(InvalidAddress.class, () -> Address.validate("ndxdprx764ciigti8d8whtw2kct733r85qvjukhqhke3dka4"));
    }

    @Test
    void knownBadKeyDoesNotConstruct() {
        assertThrows(InvalidAddress.class, () -> new Address("ndxdprx764ciigti8d8whtw2kct733r85qvjukhqhke3dka4"));
    }
}
