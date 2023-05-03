package mc.replay.extensions.exception;

import java.io.Serial;

public final class InvalidConfigurationException extends Exception {

    @Serial
    private static final long serialVersionUID = 4147095436237180303L;

    public InvalidConfigurationException(Throwable cause) {
        super(cause);
    }

    public InvalidConfigurationException(String message) {
        super(message);
    }
}