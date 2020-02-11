/*
 * This library is licensed under version 3.0 of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * This software is distributed WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License 3.0 for more details.
 *
 * Copyright © 2020 The Axiom Foundation
 */

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
