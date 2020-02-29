public class semestreTerminatoException extends Exception{
    public semestreTerminatoException(String message) {
        super(message);
    }

    public semestreTerminatoException(int semestre) {
        super("Hai terminato il " + semestre + "°");
    }
}
