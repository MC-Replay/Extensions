package mc.replay.extensions.exception;

import java.io.Serial;

public final class ExtensionNotLoadedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4107205560990037966L;

    public ExtensionNotLoadedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExtensionNotLoadedException(Throwable cause) {
        super(cause);
    }

    public ExtensionNotLoadedException(String message) {
        super(message);
    }
}