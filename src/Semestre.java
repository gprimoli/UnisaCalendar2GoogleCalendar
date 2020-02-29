import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import Exception.*;

public class Semestre implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate start;
    private LocalDate end;

    private String corso;
    private int annoInCorso;
    private int resto;

    private ArrayList<LocalDate> startsWeek = new ArrayList<>();
    private int currentWeek;


    public Semestre(String inizio, String fine, String corso, int annoInCorso, int resto) throws formatoDataException {
        this.start = LocalDate.parse(formatData(inizio, "EN"));
        this.end = LocalDate.parse(formatData(fine, "EN"));
        this.corso = corso;
        this.annoInCorso = annoInCorso;
        this.resto = resto;
        currentWeek = 0;

        for (LocalDate temp = this.start; temp.isBefore(this.end); temp = temp.plusDays(1)){
            if(temp.getDayOfWeek() == DayOfWeek.MONDAY)
                startsWeek.add(temp);
        }

    }

    public String getCorso() {
        return corso;
    }

    public int getAnnoInCorso() {
        return annoInCorso;
    }

    public int getResto() {
        return resto;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public LocalDate getNextWeekStarts() throws semestreTerminatoException, annoInCorsoTerminatoException {
        LocalDate now = LocalDate.now();
        if(now.isAfter(start) && now.isBefore(end))
            while(now.isAfter(startsWeek.get(currentWeek).plusDays(4)) )
                if(currentWeek < startsWeek.size())
                    currentWeek++;
                else if(now.getMonthValue() < 6)
                    //Da Settembre a dicembre è 1 semestre.
                    //Quindi se ci troviamo da gennaio a giugno il 1 semestre è finito!
                    throw new semestreTerminatoException(1);
                else
                    //Altrimenti abbiamo terminato l'anno accademico.
                    throw new annoInCorsoTerminatoException(now.getYear()-1);
        return startsWeek.get(currentWeek);
    }

    public static String formatData(String raw, String type) throws formatoDataException {
        String[] temp = raw.split("-");
        if(temp.length != 3)
            throw new formatoDataException("Data con formato non valido (Es formato valido: 12-12-2020 || 2020-12-12)");

        if(type.compareToIgnoreCase("EN") == 0){
            if(temp[0].length() == 4) //Formato giusto (2020-12-12)
                return raw;
            return String.format("%s-%s-%s", temp[2], temp[1], temp[0]);
        }else if(type.compareToIgnoreCase("IT") == 0){
            if(temp[0].length() == 2) //Formato giusto (12-12-2020)
                return raw;
            return String.format("%s-%s-%s", temp[0], temp[1], temp[2]);
        }else{
            throw new formatoDataException("Type non valido (IT || EN)");
        }
    }
}
