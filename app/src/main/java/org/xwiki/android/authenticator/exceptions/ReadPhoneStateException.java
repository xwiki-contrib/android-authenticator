package org.xwiki.android.authenticator.exceptions;

public class ReadPhoneStateException extends SecurityException {
    public ReadPhoneStateException() {
    }

    public ReadPhoneStateException(String s) {
        super(s);
    }

    public ReadPhoneStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReadPhoneStateException(Throwable cause) {
        super(cause);
    }
}
