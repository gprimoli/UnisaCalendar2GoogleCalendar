package Exception;

public class formatoDataException extends Exception {
    public formatoDataException(String message) {
        super(message);
    }

    public formatoDataException() {
        super("Formato Data RFC3339 errato!");
    }
}
