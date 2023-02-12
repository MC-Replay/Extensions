package mc.replay.extensions;

import java.io.Serial;

public final class InvalidExtensionException extends Exception {

    @Serial
    private static final long serialVersionUID = 4107205560990037966L;

    public InvalidExtensionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidExtensionException(Throwable cause) {
        super(cause);
    }

    public InvalidExtensionException(String message) {
        super(message);
    }
}