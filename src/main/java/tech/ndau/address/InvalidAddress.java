package tech.ndau.address;

@SuppressWarnings("WeakerAccess")
public class InvalidAddress extends Exception {
    private static final long serialVersionUID = 3588428215022691968L;

    public InvalidAddress(String message) {
        super(message);
    }

    public InvalidAddress(Exception e) {
        super(e.toString());
    }
}
