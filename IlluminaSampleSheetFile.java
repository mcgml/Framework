package nhs.genetics.cardiff.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A class for parsing Illumina sample sheets
 *
 * @author  Matt Lyon
 * @version 1.0
 * @since   2015-01-26
 */
public class IlluminaSampleSheetFile {

    private static final Logger log = Logger.getLogger(IlluminaSampleSheetFile.class.getName());

    private File filePath;
    private int IEMFileVersion, cyclesRead1, cyclesRead2;
    private Date date = new Date();
    private String investigatorName, experimentName, workflow, application, assay, description, chemistry;
    private ArrayList<IlluminaSampleSheetRecord> sampleSheetRecords = new ArrayList<>();
    private HashMap<String, ArrayList<String>> sampleSheetSections = new HashMap<String, ArrayList<String>>();
    private HashMap<String, String> manifests = new HashMap<String, String>();
    private HashMap<String, String> settings = new HashMap<String, String>();
    private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

    public IlluminaSampleSheetFile(File filePath){
        this.filePath = filePath;
    }

    public void parseSampleSheet() throws IOException {

        String line, header = "";

        try (BufferedReader sampleSheetReader = new BufferedReader(new FileReader(filePath))){

            while ((line = sampleSheetReader.readLine()) != null) {

                //skip empty and comma lines
                if (line.equals("") || isCommaOnlyLine(line)) {
                    continue;
                }

                //check if line is a header; set header
                if (Pattern.matches("^\\[.*\\].*", line)){

                    String[] subs1 = line.split("^\\[");
                    String[] subs2 = subs1[1].split("\\]");
                    header = subs2[0].trim();

                    sampleSheetSections.put(subs2[0].trim(), new ArrayList<String>());
                    continue;
                }

                sampleSheetSections.get(header).add(line);
            }

            sampleSheetReader.close();
        }
    }
    public void populateSampleSheetValues(){

        if (sampleSheetSections.containsKey("Header")){

            for (String line : sampleSheetSections.get("Header")){
                String[] fields = line.split(",");

                if (fields.length > 1){
                    if (fields[0].trim().equals("IEMFileVersion")){
                        IEMFileVersion = Integer.parseInt(fields[1].trim());
                    } else if (fields[0].trim().equals("Investigator Name")){
                        investigatorName = fields[1].trim();
                    } else if (fields[0].trim().equals("Experiment Name")){
                        experimentName = fields[1].trim();
                    } else if (fields[0].trim().equals("Date")){

                        try {
                            date = format.parse(fields[1].trim());
                        } catch (Exception e){
                            log.log(Level.SEVERE, "Could not convert data string: " + e.getMessage());
                        }

                    } else if (fields[0].trim().equals("Workflow")){
                        workflow = fields[1].trim();
                    } else if (fields[0].trim().equals("Application")){
                        application = fields[1].trim();
                    } else if (fields[0].trim().equals("Assay")){
                        assay = fields[1].trim();
                    } else if (fields[0].trim().equals("Description")){
                        description = fields[1].trim();
                    } else if (fields[0].trim().equals("Chemistry")) {
                        chemistry = fields[1].trim();
                    }
                } else {
                    log.log(Level.WARNING, "Missing  value for " + fields[0] + " will be ignored.");
                }

            }

        } else {
            log.log(Level.SEVERE, "Sample sheet is malformed. Header section missing.");
            throw new RuntimeException();
        }

        if (sampleSheetSections.containsKey("Manifests")) {
            for (String line : sampleSheetSections.get("Manifests")) {
                String[] fields = line.split(",");

                if (fields.length > 1) {
                    manifests.put(fields[0].trim(), fields[1].trim());
                } else {
                    log.log(Level.WARNING, "Missing  value for " + fields[0] + " will be ignored.");
                }

            }
        }

        if (sampleSheetSections.containsKey("Reads")) {
            for (int n = 0; n < sampleSheetSections.get("Reads").size(); ++n) {
                String[] fields = sampleSheetSections.get("Reads").get(n).split(",");

                if (n == 0){
                    cyclesRead1 = Integer.parseInt(fields[0]);
                } else if (n == 1) {
                    cyclesRead2 = Integer.parseInt(fields[0]);
                }

            }
        }

        if (sampleSheetSections.containsKey("Settings")) {
            for (String line : sampleSheetSections.get("Settings")) {
                String[] fields = line.split(",");

                if (fields.length > 1) {
                    settings.put(fields[0].trim(), fields[1].trim());
                } else {
                    log.log(Level.WARNING, "Missing  value for " + fields[0] + " will be ignored.");
                }

            }
        }

        if (sampleSheetSections.containsKey("Data")) {
            String[] headers = sampleSheetSections.get("Data").get(0).split(",");

            for (int n = 1; n < sampleSheetSections.get("Data").size(); ++n){

                IlluminaSampleSheetRecord record = new IlluminaSampleSheetRecord(headers,  sampleSheetSections.get("Data").get(n), n);
                record.parseIlluminaSampleSheetRecord();

                sampleSheetRecords.add(record);
            }

        }

    }

    private static boolean isCommaOnlyLine(String line)
    {
        //identify is line is string of commas - Excel likes putting these in
        for (int i = 0; i < line.length(); i++){

            char c = line.charAt(i);

            if (c != ',')
            {
                return false;
            }
        }

        return true;
    }

    //getters
    public int getIEMFileVersion() {
        return IEMFileVersion;
    }
    public int getCyclesRead1() {
        return cyclesRead1;
    }
    public int getCyclesRead2() {
        return cyclesRead2;
    }
    public Date getDate() {
        return date;
    }
    public String getInvestigatorName() {
        return investigatorName;
    }
    public String getExperimentName() {
        return experimentName;
    }
    public String getWorkflow() {
        return workflow;
    }
    public String getApplication() {
        return application;
    }
    public String getAssay() {
        return assay;
    }
    public String getDescription() {
        return description;
    }
    public String getChemistry() {
        return chemistry;
    }
    public ArrayList<IlluminaSampleSheetRecord> getSampleSheetRecords() {
        return sampleSheetRecords;
    }
    public HashMap<String, String> getManifests() {
        return manifests;
    }
    public HashMap<String, String> getSettings() {
        return settings;
    }
    public String[] getSplitInvestigatorName(){
        String[] investigatorFields = investigatorName.split(":");

        for (int n = 0; n < investigatorFields.length; ++n){
            investigatorFields[n].trim(); //trim whitespace
            investigatorFields[n].toUpperCase(); //remove case sensitive
        }

        return investigatorFields;
    }

}



