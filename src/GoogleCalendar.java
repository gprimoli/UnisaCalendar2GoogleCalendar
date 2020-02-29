import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import Exception.*;

public class GoogleCalendar {
    private static final String APPLICATION_NAME = "Unisa Calendar To Google Calendar";
    private static final String CREDENTIALS_FILE_PATH = "./resources/client_secret.json";
    private static final String TOKENS_DIRECTORY_PATH = "./resources/tokens";


    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private static NetHttpTransport HTTP_TRANSPORT;

    private Calendar calendario;

    public GoogleCalendar() throws IOException {
        HTTP_TRANSPORT = new NetHttpTransport();
        calendario = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public void inserisciEventi(ArrayList<Event> event) throws eventoDuplicatoException {
        try {
            if(!event.isEmpty()){
                String[] raw = event.get(0).getStart().getDateTime().toString().split("T");
                Events events = calendario.events().list("primary")
                        .setTimeMin(new DateTime(raw[0] + "T00:00:01+01:00"))
                        .setTimeMax(new DateTime(raw[0] + "T23:59:59+01:00"))
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                List<Event> items = events.getItems();
                if(!items.isEmpty())
                    if(items.get(0).getSummary().compareToIgnoreCase(event.get(0).getSummary()) == 0)
                        throw new eventoDuplicatoException();

                event.stream().forEach(e -> {
                    try {
                        calendario.events().insert("primary", e).execute();
                    } catch(IOException ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private static Credential getCredentials() throws IOException {
        InputStreamReader in = new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH));
        if (in == null) {
            throw new FileNotFoundException("File client_secret.json non trovato al seguente indirizzo: " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, in);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}