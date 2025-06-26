package hcs.Utils;

import static hcs.Utils.DatasetUtil.metadataUtil;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class PatientUtil {

    final static FileUtil f = new FileUtil();
    final static JWTUtil j = new JWTUtil();
    final static MetadataUtil metadataUtil = new MetadataUtil();
    
    private static ByteArrayOutputStream getByteArray(InputStream is){
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            
            return buffer;
        } catch (IOException ex) {
            Logger.getLogger(PatientUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static int addPatient(String jwt, InputStream patient_data_FIS, InputStream patient_pr_FIS, String dtPath, Integer patient_id, int dt_id, int dg_id, long mpegfile) throws ParserConfigurationException, SAXException {
        String owner = JWTUtil.getUID(jwt);
        Boolean hasP = false;
        String [] md;
        try {
            if (patient_data_FIS != null) {
                ByteArrayOutputStream patient_data = getByteArray(patient_data_FIS);
                ByteArrayOutputStream patient_pr = null;
                
                if(f.validateXML(new StreamSource(new ByteArrayInputStream(patient_data.toByteArray())), "Patient")){
                    md = metadataUtil.parsePatient(new String(patient_data.toByteArray()));
                    if(patient_pr_FIS != null) {
                        patient_pr = getByteArray(patient_pr_FIS);
                        if(patient_pr.toByteArray().length != 0 && FileUtil.validateXML(new StreamSource(new ByteArrayInputStream(patient_pr.toByteArray())),"patient_pr")){
                            hasP = true;
                        }
                    }

                    if(patient_id == null) patient_id = db.consults.patientC.getMaxID(dt_id,dg_id,mpegfile)+1;
                    String auPath = dtPath + File.separator + "patient_"+patient_id;
                    System.out.println("Inserting PATIENT "+patient_id+" to dataset "+dt_id);
                    String PR = null;
                    if(patient_pr_FIS != null && patient_pr != null) PR = new String(patient_pr.toByteArray());
                    
                    
                    //el md[0] debe contener el tipo de recurso fhir que se ha enviado.
                    if(db.modifiers.patientM.insertPatient(patient_id, md[0],PR,auPath,owner,md[1], md[2], md[3],dt_id,dg_id,mpegfile)){
                        db.modifiers.patientM.insertBlock(1, new String(patient_data.toByteArray()).length(), auPath + File.separator + "unaligned", owner, patient_id, dt_id, dg_id, mpegfile);
                        FileUtil.createDirectory(auPath);
                        if(hasP && patient_pr != null){
                            String[] res = ProtectionUtil.encryptPatient(mpegfile,dg_id,dt_id,patient_id,new String(patient_data.toByteArray()),PR,jwt,null);
                            String cipher = res[0];
    //                        FileUtil.createFile(dtPath + File.separator + "patient_data.txt", cipher);
    //                        db.modifiers.dtM.updatePR(mpegfile, dg_id, Integer.parseInt(md[0]), res[1]);
                            FileUtil.updateFile(auPath + File.separator + "patient_pr.xml", res[1]);
                            db.modifiers.patientM.updatePR(mpegfile, dg_id, dt_id, patient_id, res[1]);
                            FileUtil.createDirectory(auPath + File.separator + "unaligned");
                            FileUtil.updateFile(auPath + File.separator + "unaligned" + File.separator + String.valueOf(patient_id) + ".txt", cipher);
                        } else {
                            FileUtil.createDirectory(auPath + File.separator + "unaligned");
                            FileUtil.updateFile(auPath + File.separator + "unaligned" + File.separator + String.valueOf(patient_id) + ".txt", new String(patient_data.toByteArray()));
                        }
                        System.out.println("DTUtil: Added Patient to "+dt_id+" and created file.");
                        return patient_id;
                    } else Logger.getLogger(PatientUtil.class.getName()).log(Level.SEVERE, "Error inserting au to DB");
                }else Logger.getLogger(PatientUtil.class.getName()).log(Level.SEVERE, "Error in patient file! Empty file or Invalid format.");
            } else Logger.getLogger(PatientUtil.class.getName()).log(Level.SEVERE, "Error in au file! No file found.");
        } catch (IOException  ex) {
            Logger.getLogger(PatientUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    public static void editPatient(String jwt,int patient_id, int dt_id, int dg_id, long mpegfile, InputStream dt_md_FIS, InputStream dt_pr_FIS) {
        String owner = JWTUtil.getUID(jwt);
        boolean hasP = db.consults.patientC.hasProtection(patient_id,dt_id, dg_id, mpegfile);
        boolean pmod = false;
        System.out.println("editPatient del PatientUtil.java");
        String patientPath = db.consults.patientC.getPatientPath(patient_id,dt_id, dg_id, mpegfile);
        if (dt_md_FIS != null) {
            System.out.println("dentro del if dt_mdFIS != NULL");
            ByteArrayOutputStream patient_md = getByteArray(dt_md_FIS);
            ByteArrayOutputStream patient_pr = null;
            if (FileUtil.validateXML(new StreamSource(new ByteArrayInputStream(patient_md.toByteArray())),"Patient")) {
                System.out.println("xmlvalido");
                try {
                    String[] md = metadataUtil.parsePatient(new String(patient_md.toByteArray()));
                    System.out.println("[DEBUG] Parsed metadata fields: "
                                        + "dt_idS=" + md[0] + ", dni=" + md[1]
                                        + ", name=" + md[2] + ", street=" + md[3]);
                    if(dt_pr_FIS != null){
                        patient_pr = getByteArray(dt_pr_FIS);
                        if(FileUtil.validateXML(new StreamSource(new ByteArrayInputStream(patient_pr.toByteArray())),"patient_pr")) {hasP = true;pmod = true;}
                    }
                    String PR = null;
                    if(dt_pr_FIS != null && patient_pr != null) PR = dt_pr_FIS.toString();
                    if(db.modifiers.patientM.updatePatient(patient_id, md[0],PR,patientPath,owner,md[1], md[2], md[3],dt_id,dg_id,mpegfile)){
                        if(hasP && patient_pr != null && pmod){
                            String[] res = ProtectionUtil.encryptMD(mpegfile,dg_id,Integer.parseInt(md[0]),new String(patient_md.toByteArray()),jwt,null);
                            String cipher = res[0];
                            FileUtil.updateFile(patientPath + File.separator + "patient_md.xml", cipher);
                            db.modifiers.dtM.updatePR(mpegfile, dg_id, Integer.parseInt(md[0]), res[1]);
                            FileUtil.updateFile(patientPath+File.separator+"patient_pr.xml", res[1]);
                        }
                        else FileUtil.updateFile(patientPath+File.separator+"patient_md.xml",new String(patient_md.toByteArray()));
                    }
                } catch (ParserConfigurationException | IOException | SAXException ex) {
                    Logger.getLogger(DatasetUtil.class.getName()).log(Level.SEVERE, "HCS: editPatient error!", ex);
                }
            }
        }
    }
    
    
    
    public static void deletePatient(int patient_id, int dt_id, int dg_id, long mpegfile, String auth){
        String auPath = db.consults.patientC.getPatientPath(patient_id, dt_id, dg_id, mpegfile);
        FileUtil.deleteDirectory(auPath);
        db.modifiers.patientM.deletePatient(patient_id,dt_id,dg_id,mpegfile); 
    } 
    
    public static String getPr(int patient_id, int dt_id, int dg_id, long mpegfile) {
        return FileUtil.getFile(db.consults.patientC.getPatientPath(patient_id,dt_id, dg_id, mpegfile)+File.separator+"patient_pr.xml");
    }
    
    public static String getData(String jwt, int patient_id, int dt_id, int dg_id, long mpegfile) throws IOException {
        String patient_data = FileUtil.getFile(db.consults.patientC.getPatientPath(patient_id,dt_id,dg_id,mpegfile)+File.separator+"unaligned/"+patient_id+".txt");
        if(db.consults.patientC.hasProtection(patient_id, dt_id, dg_id, mpegfile)){
            return new String(ProtectionUtil.decryptPatient(mpegfile,dg_id,dt_id,patient_id,patient_data,jwt));
        }
        return FileUtil.getFile(db.consults.patientC.getPatientPath(patient_id, dt_id, dg_id, mpegfile) + File.separator + "unaligned/"+patient_id+".txt");
    }
    
    public static String getEncData(String jwt, int patient_id, int dt_id, int dg_id, long mpegfile) throws IOException {
        return FileUtil.getFile(db.consults.patientC.getPatientPath(patient_id, dt_id, dg_id, mpegfile) + File.separator + "unaligned/"+patient_id+".txt");
    }
    
    public static void deleteBlock(int block_id, int patient_id, int dt_id, int dg_id, long mpegfile, String auth){
        String blockPath = db.consults.patientC.getBlockPath(block_id, patient_id, dt_id, dg_id, mpegfile);
        FileUtil.deleteFile(blockPath + File.separator + block_id + ".txt");
        db.modifiers.patientM.deleteBlock(block_id,patient_id,dt_id,dg_id,mpegfile); 
    } 
    
//    public static int addProtection(long mpegfile, int dg_id, Integer dt_id, InputStream prXML_FIS, String keyType, String algType, String keyName, String policy, String jwt){
//        try {
//            String dt_mdPath = db.consults.dtC.getDTPath(dt_id, dg_id, mpegfile);
//            String dt_md = new String(Files.readAllBytes(Paths.get(dt_mdPath + File.separator + "dt_md.xml")));
//            String res[] = null;
//            if(prXML_FIS != null) {
//                ByteArrayOutputStream dt_pr = getByteArray(prXML_FIS);
//                if(f.validateXML(new StreamSource(new ByteArrayInputStream(dt_pr.toByteArray())), "dt_pr")) {
//                    db.modifiers.dtM.updatePR(mpegfile, dg_id, dt_id, new String(dt_pr.toByteArray()));
//                    res = ProtectionUtil.encryptMD(mpegfile,dg_id,dt_id,dt_md,jwt,null);
//                    FileUtil.updateFile(dt_mdPath + File.separator + "dt_md.xml",res[0]);
//                    db.modifiers.dgM.updatePR(mpegfile, dg_id, res[1]);
//                    FileUtil.updateFile(dt_mdPath + File.separator + "dt_pr.xml",res[1]);
//                    return 0;
//                }
//            }
//            res = ProtectionUtil.encryptMD(mpegfile,dg_id,dt_id,dt_md,jwt,new String[]{algType,keyName,keyType,policy});
//            FileUtil.updateFile(dt_mdPath + File.separator + "dt_md.xml",res[0]);
//            db.modifiers.dtM.updatePR(mpegfile, dg_id, dt_id, res[1]);
//            FileUtil.createFile(dt_mdPath + File.separator + "dt_pr.xml",res[1]);
//            return 0;
//        } catch (IOException ex) {
//            Logger.getLogger(ProtectionUtil.class.getName()).log(Level.SEVERE, "DTUtil: Error addingDTPR", ex);
//        }
//        return -1;  
//    }
}
