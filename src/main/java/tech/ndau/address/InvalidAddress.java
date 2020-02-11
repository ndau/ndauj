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
