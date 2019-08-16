package tech.ndau.address;

@SuppressWarnings("WeakerAccess")
public class InvalidAddress extends Exception {
    private static final long serialVersionUID = 3588428215022691968L;

    /**
     * Exception representing an invalid address.
     *
     * @param message why was this address invalid?
     */
    public InvalidAddress(final String message) {
        super(message);
    }

    /**
     * Wrap an InvalidAddress exception around an inner exception.
     *
     * @param e the origin exception.
     */
    public InvalidAddress(final Exception e) {
        super(e.toString());
    }
}
