import java.io.*;
import java.util.Scanner;

import Exception.*;

public class Main {
    private UnisaExtractor e;
    private File settings;

    public Main(){
        settings = new File("resources/settings.dat");
        boolean exit = false;
        while(!exit)
        if(settings.exists()){
            try{
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(settings));
                e = (UnisaExtractor) in.readObject();
                exit = true;
                in.close();
            }catch(ClassNotFoundException | FileNotFoundException e){
                System.out.println("File illegibile o non trovato\nEliminazione dei Settings");
                delSettings();

            } catch(IOException ex) {
                ex.printStackTrace();
                System.exit(-1);
            }
        }else{
            try{
                var sc = new Scanner(System.in);
                System.out.println("Inserisci prima l'anno poi il resto");
                settings.createNewFile();
                e = new UnisaExtractor("05121", sc.nextInt(), sc.nextInt());
                exit = true;
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream((settings)));
                out.writeObject(e);
                out.close();
            } catch(formatoDataException ex) {
                System.out.println("Sono stati fatti cambiamenti al sito... Ricontrolare il codice!");
                delSettings();
                System.exit(-1);
            } catch(IOException ex) {
                ex.printStackTrace();
                System.exit(-1);
            }
        }
    }

    public UnisaExtractor getExstractor() {
        return e;
    }

    public void delSettings(){
        if(settings.delete())
            System.out.println("Eliminazione effettuata con successo");
        else{
            System.out.println("Impossibile eliminare il File, procedere manualemnte");
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        var m = new Main();
        try{
            m.getExstractor().update();
        } catch(annoInCorsoTerminatoException ex) {
            System.out.println("Anno accademico terminato, aggiornare i settings");
            m.delSettings();
            System.exit(-1);
        } catch(eventoDuplicatoException ex) {
            System.out.println("Eventi già aggiunti per questa settimana");
        } catch(formatoDataException ex) {
            System.out.println("Sono stati fatti cambiamenti al sito... Ricontrolare il codice!");
            m.delSettings();
            System.exit(-1);
        }
        System.out.println("Fine!");
    }
}