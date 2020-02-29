package Exception;

public class annoInCorsoTerminatoException extends Exception{
    public annoInCorsoTerminatoException(String message) {
        super(message);
    }

    public annoInCorsoTerminatoException(int annoInCorso) {
        super("Hai terminato l'anno accademico " + annoInCorso + "/" + (annoInCorso+1) + " aggiorna i settings");
    }
}
