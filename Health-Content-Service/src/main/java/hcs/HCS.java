package hcs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hcs.Security.Secured;
import hcs.Utils.PatientUtil;
import hcs.Utils.DatasetGroupUtil;
import hcs.Utils.DatasetUtil;
import hcs.Utils.JWTUtil;
import hcs.Utils.MPEGFileUtil;
import hcs.Utils.ProtectionUtil;
import java.io.IOException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import org.glassfish.jersey.media.multipart.FormDataParam;


/**
 * JAX-RS resource class that provides operations for authentication.
 *
 * @author Suren Konathala
 */
@Path("/v1")
public class HCS {
    private static String urlHCS = null;
    
    @GET
    @Path("/ping")
    public Response ping() {
        return Response.ok("Service online!").build();
    }

    public static void setHCSUrl(String url) {
        urlHCS = url;
    }
    
    @POST
    @Secured
    @Path("/addFile")
    public Response addMPEGFile(@HeaderParam("Authorization") String auth,
            @FormParam("file_name") String file_name) {
        long id;
        
        try {
            id = MPEGFileUtil.addMPEGFile(file_name, JWTUtil.getToken(auth));
        } catch (Exception e) {
            Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, e);
            return Response.serverError().build();
        }
        System.out.println("Returning to workflow...");
        return Response.ok(String.valueOf(id), MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Secured
    @Path("/addDatasetGroup")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addDG(@HeaderParam("Authorization") String auth, 
            @FormDataParam("dg_md") InputStream dg_md_FIS, 
            @FormDataParam("dt_md") InputStream dt_md_FIS, 
            @FormDataParam("patient_data") InputStream patient_data_FIS, 
            @FormDataParam("file_id") String file_id, 
            @FormDataParam("patient_pr") InputStream patient_pr_FIS, 
            @FormDataParam("dt_pr") InputStream dt_pr_FIS, 
            @FormDataParam("dg_pr") InputStream dg_pr_FIS) {
        long id = Long.parseLong(file_id);
        if(!db.consults.mpegC.exists(id)) return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        
        int dg_id = DatasetGroupUtil.addDG(JWTUtil.getToken(auth), dg_md_FIS, dg_pr_FIS, id, db.consults.mpegC.getMPEGFilePath(id));
        System.out.println("Added datagroup "+dg_id+" adding datasets...");
        if (dt_md_FIS != null) {
            int dt_id = 0;
            Response r = addDT(JWTUtil.getToken(auth), dt_md_FIS, dt_pr_FIS, patient_data_FIS, patient_pr_FIS, String.valueOf(dg_id), dt_id, file_id);
            //Response r = addDT(JWTUtil.getToken(auth), dt_md_FIS, dt_pr_FIS, null, null, String.valueOf(dg_id), dt_id, file_id);
            
            if(r.getStatus() != Response.Status.OK.getStatusCode()) {
                DatasetGroupUtil.deleteDG(dg_id, id,auth);
                return r;
            }
        }
        return Response.ok(String.valueOf(dg_id), MediaType.TEXT_PLAIN).build();
    }
    
    private Response addDT(String jwt, InputStream dt_md_FIS, InputStream dt_pr_FIS, InputStream patient_data_FIS, InputStream patient_pr_FIS, String dg_idS, Integer dt_id, String mpegfileS) {
        int dg_id = Integer.parseInt(dg_idS);
        long mpegfile = Long.parseLong(mpegfileS);
        if(db.consults.dgC.exists(dg_id, mpegfile)){
            if (dt_id == null) {
                dt_id = db.consults.dtC.getMaxID(dg_id,mpegfile)+1;
            }
            try {
                dt_id = DatasetUtil.addDT(jwt, dt_md_FIS, dt_pr_FIS, db.consults.dgC.getDGPath(dg_id, mpegfile), dg_id, dt_id, mpegfile);
                if (patient_data_FIS != null ) {
                    int patient_id = 1;
                    Response r = addPatient(jwt, patient_data_FIS, patient_pr_FIS, patient_id, String.valueOf(dt_id), dg_idS, mpegfileS);
                    if(r.getStatus() != Response.Status.OK.getStatusCode()) {
                        DatasetUtil.deleteDT(dt_id, dg_id, mpegfile, jwt);
                        return r;
                    }
                } else {
                    System.out.println("Patient està vació, no se añade nada");
                    return Response.ok(String.valueOf(dt_id), MediaType.TEXT_PLAIN).build();
                }
             
            } catch (Exception e) {
                Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, "Error adding Dataset to DB\n", e);
                return Response.serverError().build();
            }
            return Response.ok(String.valueOf(dt_id), MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }
    
    @POST
    @Secured
    @Path("/addDataset")
    public Response addDTAux(@HeaderParam("Authorization") String auth, 
            @FormDataParam("dt_md") InputStream dt_md_FIS, 
            @FormDataParam("dt_pr") InputStream dt_pr_FIS,
            @FormDataParam("patient_data") InputStream patient_data_FIS, 
            @FormDataParam("patient_pr") InputStream patient_pr_FIS,
            @FormDataParam("mpegfile") String mpegfileS,
            @FormDataParam("dg_id") String dg_idS) {
        if(dg_idS == null || mpegfileS == null)return Response.serverError().build();
        try {
            return addDT(JWTUtil.getToken(auth),dt_md_FIS,dt_pr_FIS,patient_data_FIS,patient_pr_FIS,dg_idS,null,mpegfileS);
        } catch (Exception e) {
            Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, "Error adding Dataset to DB\n"+e);
            return Response.serverError().build();
        }
    }
    
    private Response addPatient(String jwt, InputStream patient_data_FIS, InputStream patient_pr_FIS, Integer patient_id, String dt_idS, String dg_idS, String mpegfileS){
        int dg_id = Integer.parseInt(dg_idS), dt_id = Integer.parseInt(dt_idS);
        long mpegfile = Long.parseLong(mpegfileS);
        if(db.consults.dtC.exists(dt_id,dg_id, mpegfile)){
            if (patient_id == null) {
                patient_id = db.consults.patientC.getMaxID(dt_id,dg_id,mpegfile)+1;
            }
            try {
                patient_id = PatientUtil.addPatient(jwt, patient_data_FIS, patient_pr_FIS, db.consults.dtC.getDTPath(dt_id, dg_id, mpegfile), patient_id, dt_id, dg_id, mpegfile);
            } catch (Exception e) {
                Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, "Error adding AccessUnit to DB\n", e);
                return Response.serverError().build();
            }
            return Response.ok(String.valueOf(dt_id), MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }
    
    @POST
    @Secured
    @Path("/addPatient")
    public Response addPatientAux(@HeaderParam("Authorization") String auth,
            @FormDataParam("patient_data") InputStream patient_data_FIS, 
            @FormDataParam("patient_pr") InputStream patient_pr_FIS,
            @FormDataParam("mpegfile") String mpegfileS,
            @FormDataParam("dg_id") String dg_idS,
            @FormDataParam("dt_id") String dt_idS) {
        if(dg_idS == null || mpegfileS == null || dt_idS == null) return Response.serverError().build();
        try {
            return addPatient(JWTUtil.getToken(auth),patient_data_FIS,patient_pr_FIS,null,dt_idS,dg_idS,mpegfileS);
        } catch (Exception e) {
            Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, "Error adding Access Unit to DB\n"+e);
            return Response.serverError().build();
        }
    }
    
    @POST
    @Secured
    @Path("/addProtection")
    public Response addProtection(@HeaderParam("Authorization") String auth, 
            @FormDataParam("dg_pr") InputStream dg_pr_FIS, 
            @FormDataParam("dt_pr") InputStream dt_pr_FIS,
            @FormDataParam("keyType") String keyType,
            @FormDataParam("algType") String algType,
            @FormDataParam("keyName") String keyName,
            @FormDataParam("mpegfile") String mpegfileS,
            @FormDataParam("policy") String policy,
            @FormDataParam("dg_id") String dg_idS,
            @FormDataParam("dt_id") String dt_idS) {
        int dg_id = Integer.parseInt(dg_idS);
        long mpegfile = Long.parseLong(mpegfileS);
        
        if(dg_pr_FIS == null && dt_pr_FIS == null && keyType == null && algType == null && keyName == null) return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        int res = -1;
        if(dt_idS == null || dt_idS.isEmpty()){
            if(db.consults.dgC.hasProtection(dg_id, mpegfile)) return Response.status(Response.Status.NOT_MODIFIED).build();
            res = DatasetGroupUtil.addProtection(mpegfile, dg_id, dg_pr_FIS, keyType, algType, keyName, policy, JWTUtil.getToken(auth));
        }
        else {
            int dt_id = Integer.parseInt(dt_idS);
            if(db.consults.dtC.hasProtection(dt_id, dg_id, mpegfile)) return Response.status(Response.Status.NOT_MODIFIED).build();
            res = DatasetUtil.addProtection(mpegfile, dg_id, dt_id, dt_pr_FIS, keyType, algType, keyName, policy, JWTUtil.getToken(auth));
        }
        if(res == 0) return Response.ok("ok", MediaType.TEXT_PLAIN).build();
        else return Response.status(Response.Status.NOT_MODIFIED).build();
    }

    @POST
    @Secured
    @Path("/editDatasetGroup")
    public Response editDG(@HeaderParam("Authorization") String auth, 
            @FormDataParam("dg_md") InputStream dg_md_FIS, 
            @FormDataParam("dg_pr") InputStream dg_pr_FIS,
            @FormDataParam("mpegfile") String mpegfileS,
            @FormDataParam("dg_id") String dg_idS) {
        int dg_id = Integer.parseInt(dg_idS);
        long mpegfile = Long.parseLong(mpegfileS);
        if(db.consults.dgC.exists(dg_id, mpegfile)) {
            try {
                DatasetGroupUtil.editDG(JWTUtil.getToken(auth),dg_id,mpegfile,dg_md_FIS,dg_pr_FIS);
                return Response.ok("ok", MediaType.TEXT_PLAIN).build();
            } catch (Exception e) {
                Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, "Error editing DatasetGroup\n"+e);
                return Response.serverError().build();
            }
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }
    
    @POST
    @Secured
    @Path("/editDataset")
    public Response editDT(@HeaderParam("Authorization") String auth, 
            @FormDataParam("dt_md") InputStream dt_md_FIS, 
            @FormDataParam("dt_pr") InputStream dt_pr_FIS, 
            @FormDataParam("mpegfile") String mpegfileS,
            @FormDataParam("dg_id") String dg_idS,
            @FormDataParam("dt_id") String dt_idS) {
        if(dt_idS == null || dg_idS == null || mpegfileS == null) return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        long mpegfile = Integer.parseInt(mpegfileS);
        int dg_id = Integer.parseInt(dg_idS);
        int dt_id = Integer.parseInt(dt_idS);
        if(db.consults.dtC.exists(dt_id, dg_id, mpegfile)) {
            try {
                DatasetUtil.editDT(JWTUtil.getToken(auth), dt_id, dg_id, mpegfile, dt_md_FIS, dt_pr_FIS);
                return Response.ok("ok", MediaType.TEXT_PLAIN).build();
            } catch (Exception e) {
                Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, "Error editing Dataset\n{0}", e);
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }
    @POST
    @Secured
    @Path("/editPatient")
    public Response editPatient(@HeaderParam("Authorization") String auth, 
            @FormDataParam("dt_md") InputStream dt_md_FIS, 
            @FormDataParam("dt_pr") InputStream dt_pr_FIS, 
            @FormDataParam("mpegfile") String mpegfileS,
            @FormDataParam("dg_id") String dg_idS,
            @FormDataParam("dt_id") String dt_idS,
            @FormDataParam("patient_id") String patient_idS) {
        System.out.println("Estamos dentro del HCS.java");
        if(dt_idS == null || dg_idS == null || mpegfileS == null ||patient_idS == null) return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        System.out.println("Not null");
        long mpegfile = Integer.parseInt(mpegfileS);
        int dg_id = Integer.parseInt(dg_idS);
        int dt_id = Integer.parseInt(dt_idS);
        int patient_id = Integer.parseInt(patient_idS);
        if(db.consults.patientC.exists(patient_id, dt_id, dg_id, mpegfile)) {
            System.out.println("Exists");
            try {
                PatientUtil.editPatient(JWTUtil.getToken(auth), patient_id,dt_id, dg_id, mpegfile, dt_md_FIS, dt_pr_FIS);
                System.out.println("Edit Patient complete!");
                return Response.ok("ok", MediaType.TEXT_PLAIN).build();
            } catch (Exception e) {
                Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, "Error editing Patient\n{0}", e);
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }
    @DELETE
    @Secured
    @Path("/deleteFile")
    public Response deleteMPEGFile(@HeaderParam("Authorization") String auth, 
            @QueryParam("file_id") String file_id) {
        Long id = Long.valueOf(file_id);
        if (db.consults.mpegC.exists(id)){
            try {
                if (db.consults.mpegC.hasDG(id)) {
                    ResultSet rs = db.consults.mpegC.getDG(id);
                    while (rs.next()){
                            deleteDG(JWTUtil.getToken(auth), file_id, String.valueOf(rs.getInt("dg_id")));
                    }
                }
            } catch (SQLException ex) {
                    Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                MPEGFileUtil.deleteMPEGFile(id);
            } catch (Exception e) {
                Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, "Error deleting MPEGFile of DB\n"+e);
                return Response.serverError().build();
            }
            return Response.ok("ok", MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
    
    private int deleteKeys(long mpegfile, int dg_id, Integer dt_id){ //TODO: ???
        if(dt_id != null){
            
        } else {
            
        }
        return -1;
    }
    
    @DELETE
    @Secured
    @Path("/deleteDatasetGroup")
    public Response deleteDG(@HeaderParam("Authorization") String auth,
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("dg_id") String dg_idS) {
        int dg_id = Integer.parseInt(dg_idS);
        long mpegfile = Long.parseLong(mpegfileS);
        if(dg_idS == null || mpegfileS == null) return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        if (db.consults.dgC.exists(dg_id,mpegfile)){
            try {
                if (db.consults.dgC.hasDT(dg_id,mpegfile)) {
                    ResultSet rs = db.consults.dgC.getDT(dg_id,mpegfile);
                    while (rs.next()){
                        int dt_id = rs.getInt("dt_id");
                        deleteDT(auth, mpegfileS, dg_idS, String.valueOf(dt_id));
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                DatasetGroupUtil.deleteDG(dg_id,mpegfile,auth);
            } catch (Exception e) {
                Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, "Error deleting DatasetGroup of DB\n"+e);
                return Response.serverError().build();
            }
            return Response.ok("ok", MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }

    @DELETE
    @Secured
    @Path("/deleteDataset")
    public Response deleteDT(@HeaderParam("Authorization") String auth, 
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("dg_id") String dg_idS,
            @QueryParam("dt_id") String dt_idS) {
        long mpegfile = Long.parseLong(mpegfileS);
        int dg_id = Integer.parseInt(dg_idS);
        int dt_id = Integer.parseInt(dt_idS);
        if(dt_idS == null || dg_idS == null || mpegfileS == null) return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        if (db.consults.dtC.exists(dt_id, dg_id, mpegfile)){
            try {
                if (db.consults.dtC.hasPatient(dt_id,dg_id,mpegfile)) {
                    ResultSet rs = db.consults.dtC.getPatient(dt_id,dg_id,mpegfile);
                    while (rs.next()){
                        int patient_id = rs.getInt("patient_id");
                        deletePatient(auth, mpegfileS, dg_idS, dt_idS, String.valueOf(patient_id));
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                DatasetUtil.deleteDT(dt_id,dg_id,mpegfile,auth);
            } catch (Exception e) {
                Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, "Error deleting Dataset of DB\n"+e);
                return Response.serverError().build();
            }
            return Response.ok("ok", MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }
    
    @DELETE
    @Secured
    @Path("/deletePatient")
    public Response deletePatient(@HeaderParam("Authorization") String auth, 
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("dg_id") String dg_idS,
            @QueryParam("dt_id") String dt_idS,
            @QueryParam("patient_id") String patient_idS) {
        System.out.println("Valores que llegan al HCS.java: " + patient_idS + " (patient_id)");
        if(patient_idS == null || dt_idS == null || dg_idS == null || mpegfileS == null) return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        long mpegfile = Long.parseLong(mpegfileS);
        int dg_id = Integer.parseInt(dg_idS);
        int dt_id = Integer.parseInt(dt_idS);
        int patient_id = Integer.parseInt(patient_idS);
        if (db.consults.patientC.exists(patient_id, dt_id, dg_id, mpegfile)){
            try {
                if (db.consults.patientC.hasBlocks(patient_id,dt_id,dg_id,mpegfile)) {
                    ResultSet rs = db.consults.patientC.getBlocks(patient_id,dt_id,dg_id,mpegfile);
                    while (rs.next()){
                        int block_id = rs.getInt("block_id");
                        deleteBlock(auth, mpegfileS, dg_idS, dt_idS, patient_idS, String.valueOf(block_id));
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                PatientUtil.deletePatient(patient_id,dt_id,dg_id,mpegfile,auth);
            } catch (Exception e) {
                Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, "Error deleting Access Unit of DB\n"+e);
                return Response.serverError().build();
            }
            return Response.ok("ok", MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }
    
    @DELETE
    @Secured
    @Path("/deleteBlock")
    public Response deleteBlock(@HeaderParam("Authorization") String auth, 
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("dg_id") String dg_idS,
            @QueryParam("dt_id") String dt_idS,
            @QueryParam("patient_id") String patient_idS,
            @QueryParam("block_id") String block_idS) {
        if(block_idS == null || patient_idS == null || dt_idS == null || dg_idS == null || mpegfileS == null) return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        long mpegfile = Long.parseLong(mpegfileS);
        int dg_id = Integer.parseInt(dg_idS);
        int dt_id = Integer.parseInt(dt_idS);
        int patient_id = Integer.parseInt(patient_idS);
        int block_id = Integer.parseInt(block_idS);
        if (db.consults.patientC.blockExists(block_id, patient_id, dt_id, dg_id, mpegfile)){
            try {
                PatientUtil.deleteBlock(block_id,patient_id,dt_id,dg_id,mpegfile,auth);
            } catch (Exception e) {
                Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, "Error deleting Block of DB\n"+e);
                return Response.serverError().build();
            }
            return Response.ok("ok", MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }

    @GET
    @Secured
    @Path("/mpegfile")
    public Response getMPEGFile(@HeaderParam("Authorization") String auth, 
            @QueryParam("file_id") String file_id) {
        long id = Long.parseLong(file_id);
        if(db.consults.mpegC.exists(id)) {
            JsonObject jo = new JsonObject();
            jo.addProperty("name",db.consults.mpegC.getString(id, "name"));
            jo.addProperty("id",db.consults.mpegC.getMPEGFile(id));
            JsonArray ja = new JsonArray();
            if (db.consults.mpegC.hasDG(id)) {
                ResultSet rs = db.consults.mpegC.getDG(id);
                try {
                    while(rs.next()){
                        int dg_id = rs.getInt("dg_id");
                        JsonObject dg_jo = new JsonObject();
                        long[] res = au.AuthorizationUtil.authorized(urlHCS, "dg", file_id, String.valueOf(dg_id), null,null, auth, "GetHierarchy");
                        //boolean authorized = AuthorizationUtil.authorized2(urlHCS, "dg", file_id, String.valueOf(dg_id), null, auth, "GetIdDatasetGroup");
                        if (res != null && res[0] > 0 && (int) res[0] == dg_id) {
                            dg_jo.addProperty("id",file_id);
                            dg_jo.addProperty("dg_id",dg_id);
                            ja.add(dg_jo);
                        }
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            jo.add("dg",ja);
            return Response.ok(jo.get("dg").getAsJsonArray().toString(), MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }
    
    @GET
    @Secured
    @Path("/dg")
    public Response getDG(@HeaderParam("Authorization") String auth, 
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("dg_id") String dg_idS, 
            @QueryParam("resource") String resource) {
        int dg_id = Integer.parseInt(dg_idS);
        long mpegfile = Long.parseLong(mpegfileS);
        JsonObject jo = new JsonObject();
        if (!db.consults.dgC.exists(dg_id,mpegfile)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        switch (resource) {
            case "GetHierarchy":
                JsonArray ja = new JsonArray();
                if (db.consults.dgC.hasDT(dg_id,mpegfile)) {
                    ResultSet rs = db.consults.dgC.getDT(dg_id,mpegfile);
                    try {
                        while(rs.next()){
                            int dt_id = rs.getInt("dt_id");
                            JsonObject dt_jo = new JsonObject();
                            long[] res = au.AuthorizationUtil.authorized(urlHCS, "dt", mpegfileS, dg_idS, String.valueOf(dt_id), null, auth, "GetHierarchy");
//                            boolean authorized = AuthorizationUtil.authorized2(urlHCS, "dt", mpegfileS, dg_idS, String.valueOf(dt_id), auth, "GetIdDataset");
                            if (res != null && res[0] > 0 && (int) res[0] == dt_id) {
                                dt_jo.addProperty("id", mpegfile);
                                dt_jo.addProperty("dg_id",dg_id);
                                dt_jo.addProperty("dt_id",dt_id);
                                ja.add(dt_jo);
                            }
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                jo.add("dt", ja);
                break;
            case "GetMetadataContentDG":
                try {
                    jo.addProperty("data",DatasetGroupUtil.getMd(JWTUtil.getToken(auth), dg_id, mpegfile));
                } catch (IOException e) {
                    Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, e);
                    return Response.serverError().build();
                }
                break;
            case "GetProtectionDG":
                jo.addProperty("data",DatasetGroupUtil.getPr(dg_id, mpegfile));
                break;
            default:
                return Response.status(Response.Status.BAD_REQUEST).build();
        }
        //System.out.println("HCS: "+jo.get("dt").getAsJsonArray().toString());
        if(resource.equals("GetHierarchy")) return Response.ok(jo.get("dt").getAsJsonArray().toString()).build();
        else return Response.ok(jo.get("data").getAsString(), MediaType.APPLICATION_JSON).build();
    }
    
    @GET
    @Secured
    @Path("/dgEnc")
    public Response getEncDG(@HeaderParam("Authorization") String auth, 
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("dg_id") String dg_idS) {
        int dg_id = Integer.parseInt(dg_idS);
        long mpegfile = Long.parseLong(mpegfileS);
        JsonObject jo = new JsonObject();
        if (!db.consults.dgC.exists(dg_id,mpegfile)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        try {
            jo.addProperty("data",DatasetGroupUtil.getEncMd(JWTUtil.getToken(auth), dg_id, mpegfile));
        } catch (IOException e) {
            Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, e);
            return Response.serverError().build();
        } 
        return Response.ok(jo.get("data").getAsString(), MediaType.APPLICATION_JSON).build();
    }
    
    @GET
    @Secured
    @Path("/dt")
    public Response getDT(@HeaderParam("Authorization") String auth, 
            @QueryParam("dt_id") String dt_idS, 
            @QueryParam("dg_id") String dg_idS,
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("resource") String resource) {
        if(dt_idS == null || dg_idS == null || mpegfileS == null)return Response.status(Response.Status.BAD_REQUEST).build();
        long mpegfile = Long.parseLong(mpegfileS);
        int dg_id = Integer.parseInt(dg_idS);
        int dt_id = Integer.parseInt(dt_idS);
        JsonObject jo = new JsonObject();
        if (!db.consults.dtC.exists(dt_id, dg_id, mpegfile)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        switch (resource) {
            case "GetMetadataContent":
                try {
                    jo.addProperty("data",DatasetUtil.getMd(JWTUtil.getToken(auth),dt_id, dg_id, mpegfile));
                } catch (IOException e) {
                    Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, e);
                    return Response.serverError().build();
                }
                break;
            case "GetProtection":
                jo.addProperty("data",DatasetUtil.getPr(dt_id, dg_id, mpegfile));
                break;
            case "GetHierarchy":
                JsonArray ja = new JsonArray();
                if (db.consults.dtC.hasPatient(dt_id,dg_id,mpegfile)) {
                    ResultSet rs = db.consults.dtC.getPatient(dt_id,dg_id,mpegfile);
                    try {
                        while(rs.next()){
                            int patient_id = rs.getInt("patient_id");
                            JsonObject au_jo = new JsonObject();
                            au_jo.addProperty("id", mpegfile);
                            au_jo.addProperty("dg_id",dg_id);
                            au_jo.addProperty("dt_id",dt_id);
                            au_jo.addProperty("patient_id",patient_id);
                            ja.add(au_jo);
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                jo.add("au", ja);
                break;
            default:
                return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if(resource.equals("GetHierarchy")) return Response.ok(jo.get("au").getAsJsonArray().toString()).build();
        else return Response.ok(jo.get("data").getAsString(), MediaType.APPLICATION_JSON).build();
    }
    
    @GET
    @Secured
    @Path("/dtEnc")
    public Response getEncDT(@HeaderParam("Authorization") String auth, 
            @QueryParam("dt_id") String dt_idS, 
            @QueryParam("dg_id") String dg_idS,
            @QueryParam("mpegfile") String mpegfileS) {
        if(dt_idS == null || dg_idS == null || mpegfileS == null)return Response.status(Response.Status.BAD_REQUEST).build();
        long mpegfile = Long.parseLong(mpegfileS);
        int dg_id = Integer.parseInt(dg_idS);
        int dt_id = Integer.parseInt(dt_idS);
        JsonObject jo = new JsonObject();
        if (!db.consults.dtC.exists(dt_id, dg_id, mpegfile)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        try {
            jo.addProperty("data",DatasetUtil.getEncMd(JWTUtil.getToken(auth),dt_id, dg_id, mpegfile));
        } catch (IOException e) {
            Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, e);
            return Response.serverError().build();
        }
        return Response.ok(jo.get("data").getAsString(), MediaType.APPLICATION_JSON).build();
    }
    
    @GET
    @Secured
    @Path("/patient")
    public Response getPatient(@HeaderParam("Authorization") String auth, 
            @QueryParam("patient_id") String patient_idS,
            @QueryParam("dt_id") String dt_idS, 
            @QueryParam("dg_id") String dg_idS,
            @QueryParam("mpegfile") String mpegfileS,
            @QueryParam("resource") String resource) {
        if(patient_idS == null || dt_idS == null || dg_idS == null || mpegfileS == null)return Response.status(Response.Status.BAD_REQUEST).build();
        long mpegfile = Long.parseLong(mpegfileS);
        int dg_id = Integer.parseInt(dg_idS);
        int dt_id = Integer.parseInt(dt_idS);
        int patient_id = Integer.parseInt(patient_idS);
        JsonObject jo = new JsonObject();
        if (!db.consults.patientC.exists(patient_id, dt_id, dg_id, mpegfile)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        switch (resource) {
            case "data":
                try {
                    jo.addProperty("data",PatientUtil.getData(JWTUtil.getToken(auth),patient_id, dt_id, dg_id, mpegfile));
                } catch (IOException e) {
                    Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, e);
                    return Response.serverError().build();
                }
                break;
            case "protection":
                jo.addProperty("data",PatientUtil.getPr(patient_id, dt_id, dg_id, mpegfile));
                break;
            default:
                return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok(jo.get("data").getAsString(), MediaType.APPLICATION_JSON).build();
    }
    
    @GET
    @Secured
    @Path("/patientEnc")
    public Response getEncPatient(@HeaderParam("Authorization") String auth, 
            @QueryParam("patient_id") String patient_idS, 
            @QueryParam("dt_id") String dt_idS, 
            @QueryParam("dg_id") String dg_idS,
            @QueryParam("mpegfile") String mpegfileS) {
        if(patient_idS == null || dt_idS == null || dg_idS == null || mpegfileS == null)return Response.status(Response.Status.BAD_REQUEST).build();
        long mpegfile = Long.parseLong(mpegfileS);
        int dg_id = Integer.parseInt(dg_idS);
        int dt_id = Integer.parseInt(dt_idS);
        int patient_id = Integer.parseInt(patient_idS);
        JsonObject jo = new JsonObject();
        if (!db.consults.patientC.exists(patient_id, dt_id, dg_id, mpegfile)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        try {
            jo.addProperty("data",PatientUtil.getEncData(JWTUtil.getToken(auth),patient_id,dt_id, dg_id, mpegfile));
        } catch (IOException e) {
            Logger.getLogger(HCS.class.getName()).log(Level.SEVERE, null, e);
            return Response.serverError().build();
        }
        return Response.ok(jo.get("data").getAsString(), MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Secured
    @Path("/decrypt") //TODO: Validar xml
    public Response decrypt(@HeaderParam("Authorization") String auth, 
            @FormDataParam("cipher") InputStream cipher_FIS, 
            @FormDataParam("key") InputStream key_FIS, 
            @FormDataParam("pr") InputStream pr_FIS){
        if(cipher_FIS == null || key_FIS == null || pr_FIS == null) return Response.status(Response.Status.BAD_REQUEST).build();
        String res = new String(ProtectionUtil.decrypt(cipher_FIS, pr_FIS, key_FIS, auth));
        return Response.ok(res, MediaType.TEXT_PLAIN).build();
    }

}

