package org.xwiki.android.sync.exceptions;

import java.io.IOException;

public class NeedCaptchaException extends IOException {
    public NeedCaptchaException(String message) {
        super(message);
    }

    public NeedCaptchaException(String message, Throwable cause) {
        super(message, cause);
    }
}
