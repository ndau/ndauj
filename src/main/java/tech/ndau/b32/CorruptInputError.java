package tech.ndau.b32;

@SuppressWarnings("WeakerAccess")
public final class CorruptInputError extends Exception {
    private static final long serialVersionUID = 4709708771572879783L;

    private final int errByte;

    /**
     * The input data was corrupt.
     *
     * @param errByte The first byte at which invalid data was found.
     */
    public CorruptInputError(final int errByte) {
        super(String.format("illegal base32 data at input byte %d", errByte));
        this.errByte = errByte;
    }

    /**
     * get the first byte at which invalid data was found.
     *
     * @return the first byte at which invalid data was found.
     */
    public int getErrByte() {
        return this.errByte;
    }
}
