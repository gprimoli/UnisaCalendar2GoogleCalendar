public class eventoDuplicatoException extends Exception {
    public eventoDuplicatoException(String message) {
        super(message);
    }

    public eventoDuplicatoException() {
        super("Evento già presente all'interno del calendario!");
    }
}
