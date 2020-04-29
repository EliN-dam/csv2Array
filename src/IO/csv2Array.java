/* https://people.sc.fsu.edu/~jburkardt/data/csv/csv.html
 * https://community.broadcom.com/communities/community-home/digestviewer/viewthread?MID=754315
 * https://stackoverflow.com/questions/4617935/is-there-a-way-to-include-commas-in-csv-columns-without-breaking-the-formatting
 */
package IO;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.FileWriter;
import java.io.BufferedWriter;
/**
 *
 * @author zelda
 */
public class csv2Array {
    
    private File file;
    private int nRecords;
    private int nFields;
    private int[] sizes;
    
    public csv2Array(String path) {
        if (path.matches("^(http?|ftp).*$"))
            this.download(path);
        else
            this.file = new File(path);
        this.nRecords = this.countRecords();
        this.nFields = this.countFields();
        this.sizes = new int[nFields + 1];
    }
    
    public int getnRecords(){
        return this.nRecords;
    }
    
    public int getnFields(){
        return this.nFields;
    }
    
    public int[] getLongitudes(){
        return this.sizes;
    }
    
    public int countRecords(){
        int count = 0;
        if (this.file.exists()){
            if (this.file.canRead()){
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(this.file));
                    String line = reader.readLine();
                    while ((line != null) && (!line.isEmpty())){
                        line = reader.readLine();
                        count++;
                    }
                    reader.close();
                    return count;
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return count;
    }
    
    public int countFields(){
        int count = 1;
        if (this.file.exists()){
            if (this.file.canRead()){
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(this.file));
                    String line = reader.readLine();
                    for (int i = 0; i < line.length() - 1; i++){
                        if (line.charAt(i) == ',')
                            count++;
                    }
                    reader.close();
                    return count;
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return count;
    }
    
    public void countLengths(String[] line){
        for (int i = 0; i < line.length; i++){
            if (this.sizes[i + 1] < line[i].length())
                this.sizes[i + 1] = line[i].length();
        }
    }
    
    public String[][] load(){
        if (this.file.exists()){
            if (this.file.canRead()){
                String[][] data = new String[nRecords][nFields];
                int count = 0;
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(this.file));
                    String line = reader.readLine();
                    while ((line != null) && (!line.isEmpty())){
                        line = line.trim();
                        data[count] = line.split("\\s*,\\s*(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                        /*\\s*        // Ignore the whitespaces before the comma
                         *,           // Split on comma
                         *\\s*        // Ignore the white spaces after the comma
                         *(?=         // Followed by
                         *   (?:      // Start a non-capture group
                         *     [^"]*  // 0 or more non-quote characters
                         *     "      // 1 quote
                         *     [^"]*  // 0 or more non-quote characters
                         *     "      // 1 quote
                         *   )*       // 0 or more repetition of non-capture group (multiple of 2 quotes will be even)
                         *   [^"]*    // Finally 0 or more non-quotes
                         *   $        // Till the end  (This is necessary, else every comma will satisfy the condition)
                         *      )
                        */
                        this.countLengths(data[count]);
                        line = reader.readLine();
                        count++;                
                    }
                    for (int i = 1; i < this.sizes.length; i++)
                            this.sizes[0] += this.sizes[i];
                    reader.close();
                    return data;
                } catch (FileNotFoundException e){
                    System.out.println("Archivo no encontrado.");
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    public void download(String url){
        String destinationPath = "files" + File.separator + "download" + File.separator;
        File folder = new File(destinationPath);
        if (!folder.exists())
            folder.mkdirs();
        try {
            InputStream in = URI.create(url).toURL().openStream();
            String filename = url.substring(url.lastIndexOf('/') + 1);
            Files.copy(in, Paths.get(destinationPath + filename), StandardCopyOption.REPLACE_EXISTING);
            this.file = new File(destinationPath + filename);
        } catch (IOException e) {
            System.out.println("El file no se ha podido descargar.");
        }
    }
    
    public void printCSV(){
        String[][] data = this.load();
        if (data != null) {
            int fileLength = this.file.getName().length();
            /* (this.nFields * 2) -> 2 espacios adicionales a cada lado para cada dato
             * (this.nFields - 1) -> el espacio para cada columna separadora. */
            int totalSize = this.sizes[0] + (this.nFields * 2) + (this.nFields - 1);
            String interline = "+";
            for (int i = 1; i < this.sizes.length; i++)
                interline += new String(new char[this.sizes[i]]).replace('\0', '=') + "==" + "+"; 
            System.out.println("+" + new String(new char[totalSize]).replace('\0', '=') + "+");
            System.out.printf("|%" + (totalSize - fileLength) / 2 +
                              "s%" + fileLength +
                              "s%" + ((totalSize - fileLength) + 1) / 2 + "s|" +
                              System.lineSeparator(), 
                              "", this.file.getName(), "");
            System.out.println("+" + new String(new char[totalSize]).replace('\0', '=') + "+");
            for (int i = 0; i < data.length; i++){
                System.out.print("|");
                for (int j = 0; j < data[i].length; j++){
                    int longitudR = data[i][j].length();
                    System.out.printf("%" + (this.sizes[j + 1] - longitudR + 2) / 2 +
                                      "s%-" + longitudR +
                                      "s%" + (this.sizes[j + 1] - longitudR + 3)  / 2 + "s|",
                                      "", data[i][j], "");
                }
                System.out.println();
                if (i != data.length - 1)
                    System.out.println(interline);
            }
            System.out.println("+" + new String(new char[totalSize]).replace('\0', '=') + "+");
        } else {
            System.out.println("Archivo no encontrado.");
        }
    }
    
    public void save(String path){
        String[][] data = this.load();
        if (data != null){
            String destinationPath = path.trim().substring(0, path.lastIndexOf('/')) + File.separator;
            File folder = new File(destinationPath);
            if (!folder.exists())
                folder.mkdirs();
            File saved = new File(path);
            if (!saved.exists()){
                try {
                    saved.createNewFile();
                } catch (IOException e){
                    System.out.println("No se ha podido crear el archivo.");
                }
            }
            if (saved.exists()){
                if (saved.canWrite()){
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(saved));
                        String record;
                        for (int i = 0; i < data.length; i++){
                            record = "";
                            for (int j = 0; j < data[i].length; j++){
                                record += data[i][j] + ",";
                            }
                            record = record.substring(0, record.length() - 1) + System.lineSeparator();
                            writer.append(record);
                        }
                        writer.close();         
                    } catch (FileNotFoundException e){
                        System.out.println("Archivo no encontrado.");
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    public static void main(String[] args) {
        csv2Array records = new csv2Array("https://people.sc.fsu.edu/~jburkardt/data/csv/oscar_age_female.csv");
        records.printCSV();
        records.save("files/prueba.csv");
    }
}