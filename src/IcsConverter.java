import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import Exception.*;

public class IcsConverter {

    private File icsPath;

    public IcsConverter(File icsPath) {
        this.icsPath = icsPath;
    }

    public IcsConverter(String icsPath) {
        this.icsPath = new File(icsPath);
    }

    public ArrayList<Event> toArrayList() throws FileNotFoundException, formatoDataException {
        ArrayList<Event> list = new ArrayList<>();
        Scanner in = new Scanner(icsPath);

        while(in.hasNextLine()){
            String temp = in.nextLine();
            if (temp.contains("BEGIN:VEVENT")){
                temp = in.nextLine();
                Event e = new Event();
                for(int i=0; !temp.contains("END:VEVENT"); temp = in.nextLine(), i++){
                    String[] splitted = temp.split(":");
                    if(splitted[0].contains("DTSTART")){
                        e.setStart(new EventDateTime().setDateTime(new DateTime(IcsConverter.formatRFC3339(splitted[1]) + "+01:00")));
                    }else if(splitted[0].contains("DTEND")){
                        e.setEnd(new EventDateTime().setDateTime(new DateTime(IcsConverter.formatRFC3339(splitted[1]) + "+01:00")));
                    }else if(splitted[0].contains("SUMMARY")){
                        e.setSummary(splitted[1]);
                    }else if(splitted[0].contains("DESCRIPTION")){
                        if(splitted[1].contains("Aula")){
                            String[] subsplit = splitted[1].split("Aula");
                            e.setDescription(subsplit[0].substring(e.getSummary().length()));
                            e.setLocation("Aula " + subsplit[1].substring(0, subsplit[1].length() - 8));
                        }else {
                            String[] subsplit = splitted[1].split("Laboratorio");
                            e.setDescription(subsplit[0].substring(e.getSummary().length()));
                            e.setLocation("Laboratorio " + subsplit[1].substring(0, subsplit[1].length() - 8));
                        }
                    }
                }
                list.add(e.setReminders(new Event.Reminders().setUseDefault(false)));
            }
        }
        in.close();
        return list;
    }

    public String toJson() throws FileNotFoundException {
        Scanner in = new Scanner(icsPath);
        StringBuilder json = new StringBuilder(2100);
        json.append("{ \"event\":[");
        while (in.hasNextLine()){
            String temp = in.nextLine();
            if (temp.contains("BEGIN:VEVENT")){
                json.append("{");
                for (temp = in.nextLine(); !temp.contains("END:VEVENT"); temp = in.nextLine()){
                    String[] split = temp.split(":");
                    json.append("\"").append(split[0]).append("\":").append("\"").append(split[1]).append("\"");
                    if(split[0].contains("DESCRIPTION"))
                        json.append("},");
                    else
                        json.append(",");
                }
            }
        }
        in.close();
        return json.deleteCharAt(json.length()-1).append("]}").toString();
    }

    public static String formatRFC3339(String raw) throws formatoDataException {
        //Es 20200224T090000 to 2020-02-24T09:00:00
        if(raw.length() != 15)
            throw new formatoDataException();
        String[] splitted = raw.split("T");
        return String.format("%s-%s-%sT%s:%s:%s",
                splitted[0].substring(0,4),
                splitted[0].substring(4,6),
                splitted[0].substring(6,8),
                splitted[1].substring(0,2),
                splitted[1].substring(2,4),
                splitted[1].substring(4,6)
                );
    }
}
