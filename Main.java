import javax.sound.midi.Soundbank;
import java.io.*;

public class Main {
    public static void main(String[] args){
        File settings = new File("resources/settings.dat");
        UnisaExtractor e;

        if (settings.exists()) {
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(settings));
                e = (UnisaExtractor) in.readObject();
                in.close();

                e.update();

                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream((settings)));
                out.writeObject(e);
                out.close();

            } catch(annoInCorsoTerminatoException ex) {
                System.out.println("Anno in corso terminato!");
                settings.delete();
            } catch(eventoDuplicatoException ex) {
                System.out.println("Evento già inserito!");
            } catch(formatoDataException ex) {
                System.out.println("Errori nelle date");
            } catch(IOException | ClassNotFoundException ex) {
                System.out.println("Impossibile caricare il file Settings!");
                settings.deleteOnExit();
                ex.printStackTrace();
            }
        }else{
            try {
                settings.createNewFile();
                e = new UnisaExtractor("05121", 2, 2);

                e.update();
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream((settings)));
                out.writeObject(e);
                out.close();
            } catch(formatoDataException ex) {
                System.out.println("Errori nelle date");
            } catch(annoInCorsoTerminatoException ex) {
                System.out.println("Anno in corso terminato!");
                settings.deleteOnExit();
            } catch(eventoDuplicatoException ex) {
                System.out.println("Evento già inserito!");
            } catch(IOException ex) {
                System.out.println("Impossibile caricare il file Settings!");
                settings.deleteOnExit();
            }
        }
        System.out.println("Fine!");
    }
}

// anno=2019&corso=05121&anno2=PDS0-2019-2%7C2&visualizzazione_orario=std&periodo_didattico=S2
// anno=2019&corso=2641&anno2=PDS0-2019-2%7C2&visualizzazione_orario=std&periodo_didattico=S2
