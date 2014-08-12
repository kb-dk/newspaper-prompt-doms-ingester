package dk.statsbiblioteket.newspaper.promptdomsingester;

public class IngesterShutdownException extends RuntimeException {
    public IngesterShutdownException() {
    }

    public IngesterShutdownException(String message) {
        super(message);
    }

    public IngesterShutdownException(String message, Throwable cause) {
        super(message, cause);
    }

    public IngesterShutdownException(Throwable cause) {
        super(cause);
    }

    public IngesterShutdownException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
