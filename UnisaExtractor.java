import com.google.api.services.calendar.model.Event;
import com.jayway.jsonpath.JsonPath;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;

public class UnisaExtractor implements Serializable{
    private static final long serialVersionUID = 1L;

    private Semestre semestre;
    private int nSemestre;

//    private LocalDate lastUpdate;
    private LocalDate nextUpdate;
    private String urlComune;

    public UnisaExtractor(String corso, int annoInCorso, int resto) throws formatoDataException {
        initSemestre(corso, annoInCorso, resto);//Inizializza anche UrlComune
        nextUpdate = LocalDate.now();
    }

    public void update() throws formatoDataException, eventoDuplicatoException, annoInCorsoTerminatoException {
        //Da finire! (-_-')
        if(LocalDate.now().equals(nextUpdate)){
            try{
                new GoogleCalendar().inserisciEventi(downloadEvents());
            }catch(IOException e){
                System.out.println("Problemi con GoogleCalendar");
                e.printStackTrace();
            }
        }else{
            System.out.println("Download per questa settimana già effettuato!");
        }
    }

    private ArrayList<Event> downloadEvents() throws formatoDataException, annoInCorsoTerminatoException {
        try {
            String url = "https://easycourse.unisa.it/AgendaStudenti/ec_download_ical_grid.php?" + urlComune
                    + "cal&date=" + Semestre.formatData(semestre.getNextWeekStarts().toString(), "IT");

            File f = File.createTempFile("tmp", null);
            f.deleteOnExit();

            download(f, url);

            nextUpdate = semestre.getNextWeekStarts().plusDays(4);
            return new IcsConverter(f).toArrayList();

        }catch(semestreTerminatoException e) {
            //Sono a gennaio fino a maggio
            LocalDate now = LocalDate.now();
            if (now.getMonthValue() > 1 && now.getDayOfMonth() > 15){
                nextUpdate = LocalDate.now().plusDays(1);
                initSemestre(semestre.getCorso(), semestre.getAnnoInCorso(), semestre.getResto());
            }else
                nextUpdate = LocalDate.of(now.getYear(), 2, 16);
        }catch(IOException e){
            System.out.println("Impossibile creare e/o leggere il file temporaneo!");
            e.printStackTrace();
        }
        //Inguardabile questo return null... Consigli?
        return null;
    }

    private void initSemestre(String corso, int annoInCorso, int resto) throws formatoDataException {
        LocalDate now = LocalDate.now();
        StringBuilder temp = new StringBuilder(50);
        StringBuilder url = new StringBuilder("https://corsi.unisa.it/unisa-rescue-page/ajax/case/calendarioOrariFull?");

        //PSD0- È solo per informatica!
        //anno=2019&corso=07121&anno2=unico%7C1&visualizzazione_orario=std&periodo_didattico=S2
        int anno;
        if(now.getMonthValue() > 8){
            anno = now.getYear();
            nSemestre = 1;
        }else{
            anno = now.getYear()-1;
            nSemestre = 2;
        }
        temp.append("anno=")
                .append(anno)
                .append("&corso=")
                .append(corso)
                .append("&anno2=PDS0-")
                .append(anno)
                .append("-")
                .append(resto)
                .append("%7C")
                .append(annoInCorso)
                .append("&visualizzazione_orario=")
                .append("std&periodo_didattico=S").append(nSemestre);


        url.append(temp);
        urlComune = temp.toString();

        try {
            File f = File.createTempFile("tmp", null);
            f.deleteOnExit();

            download(f, url.toString());

            String raw = format(new String(Files.readAllBytes(Paths.get(f.getPath()))));




            semestre = new Semestre(JsonPath.read(raw, "$.dataInizio"), JsonPath.read(raw, "$.dataFine"), corso, annoInCorso, resto);
        }catch(IOException e){
            System.out.println("Impossibile creare e/o leggere il file temporaneo!");
            e.printStackTrace();
        }
    }


    public static void download(File destinazione, String url) {
        try {
            URL finalUrl = new URL(url);
            BufferedInputStream bis = new BufferedInputStream(finalUrl.openStream());
            FileOutputStream fis = new FileOutputStream(destinazione);

            byte[] buffer = new byte[2048];
            int count;
            while ((count = bis.read(buffer, 0, 2048)) != -1)
                fis.write(buffer, 0, count);
            fis.close();
            bis.close();

        }catch(IOException e){
            System.out.println("Impossibile scaricare il file");
            e.printStackTrace();
        }
    }


    private static String format(String raw) {
        StringBuilder temp = new StringBuilder(150);
        StringBuilder finalString = new StringBuilder(55);
        finalString.append("{");
        for (int i = 0, y = 0; i < raw.length(); i++) {
            if (raw.charAt(i) == '{' || raw.charAt(i) == '}' || raw.charAt(i) == ',' || raw.charAt(i) == ';') {
                if (temp.toString().contains("dataInizio") || temp.toString().contains("dataFine"))
                    if (y++ == 0)
                        finalString.append(temp).append(",");
                    else {
                        finalString.append(temp).append("}");
                        return finalString.toString();
                    }
                temp.delete(0, temp.length());
            } else {
                temp.append(raw.charAt(i));
            }
        }
        return finalString.toString();
    }
}
