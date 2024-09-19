package cartridge.exceptions;

public class InvalidCartridgeException extends RuntimeException {
    public InvalidCartridgeException(String message) {
        super(message);
    }

    public InvalidCartridgeException(String message, Throwable cause) {
        super(message, cause);
    }
}
