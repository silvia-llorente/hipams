package db.consults;

import db.connManager.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.rowset.*;

public class patientC {

    public static boolean exists(int patient_id, int dt_id, int dg_id, long mpegfile) {
        Connection connection = ConnectionManager.init_connection();

        try {
            String query = "SELECT dt_id FROM Patient WHERE patient_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, patient_id);
            statement.setInt(2, dt_id);
            statement.setInt(3, dg_id);
            statement.setLong(4, mpegfile);
            ResultSet rs = statement.executeQuery();

            boolean exists = false;
            if (rs.next()) {
                exists = true;
            }            
            return exists;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }finally{
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }

    public static String getPatientPath(int patient_id, int dt_id, int dg_id, long mpegfile) {
        Connection connection = ConnectionManager.init_connection();

        try {
            String query = "SELECT path FROM Patient WHERE patient_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, patient_id);
            statement.setInt(2, dt_id);
            statement.setInt(3, dg_id);
            statement.setLong(4, mpegfile);
            ResultSet rs = statement.executeQuery();

            String path = null;
            if (rs.next()) {
                path = rs.getString("path");
            }
            return path;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            
            return null;
        }finally{
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }

    public static int getMaxID(int dt_id, int dg_id, long mpegfile) {
        Connection connection = ConnectionManager.init_connection();

        try {
            String query = "SELECT MAX(patient_id) FROM Patient WHERE dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, dt_id);
            statement.setInt(2, dg_id);
            statement.setLong(3, mpegfile);
            ResultSet rs = statement.executeQuery();

            int ret = 1;
            if (rs.next()) {
                ret = rs.getInt("MAX(patient_id)");
            }
            return ret;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return 1;
        }finally{if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }

    public static boolean hasProtection(int patient_id, int dt_id, int dg_id, long mpegfile) {
        Connection connection = ConnectionManager.init_connection();
        try {
            String query = "SELECT protection FROM Patient WHERE patient_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, patient_id);
            statement.setInt(2, dt_id);
            statement.setInt(3, dg_id);
            statement.setLong(4, mpegfile);
            ResultSet rs = statement.executeQuery();

            String hasP = null;
            if (rs.next()) {
                hasP = rs.getString("protection");
            }
            return hasP != null && !hasP.isEmpty();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            
            return false;
        }finally{
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }

    public static ResultSet getOwnBlocs(String owner) {
        Connection connection = ConnectionManager.init_connection();

        try {
            String query = "SELECT patient_id FROM Patient WHERE owner = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, owner);
            ResultSet rs = statement.executeQuery();

            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(rs);

            return crs;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            
            return null;
        }finally{
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }

    public static String getPR(long mpegfile, int dg_id, int dt_id, int patient_id) {
        Connection connection = ConnectionManager.init_connection();
        try {
            String query = "SELECT protection FROM Patient WHERE patient_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, patient_id);
            statement.setInt(2, dt_id);
            statement.setInt(3, dg_id);
            statement.setLong(4, mpegfile);
            ResultSet rs = statement.executeQuery();

            String hasP = null;
            if (rs.next()) {
                hasP = rs.getString("protection");
            }

            return hasP;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            
            return null;
        }finally{
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }

    public static boolean hasBlocks(int patient_id, int dt_id, int dg_id, long mpegfile) {
        Connection connection = ConnectionManager.init_connection();

        try {
            String query = "SELECT block_id FROM Block WHERE patient_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, patient_id);
            statement.setInt(2, dt_id);
            statement.setInt(3, dg_id);
            statement.setLong(4, mpegfile);
            ResultSet rs = statement.executeQuery();

            boolean exists = false;
            if (rs.next()) {
                exists = true;
            }

            return exists;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            
            return false;
        }finally{
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }

    public static ResultSet getBlocks(int patient_id, int dt_id, int dg_id, long mpegfile) {
        Connection connection = ConnectionManager.init_connection();

        try {
            String query = "SELECT block_id FROM Block WHERE patient_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, patient_id);
            statement.setInt(2, dt_id);
            statement.setInt(3, dg_id);
            statement.setLong(4, mpegfile);
            ResultSet rs = statement.executeQuery();

            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(rs);

            return crs;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            
            return null;
        }finally{
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }

    public static boolean blockExists(int block_id, int patient_id, int dt_id, int dg_id, long mpegfile) {
        Connection connection = ConnectionManager.init_connection();
        try {
            String query = "SELECT block_id FROM Block WHERE block_id = ? AND patient_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, block_id);
            statement.setInt(2, patient_id);
            statement.setInt(3, dt_id);
            statement.setInt(4, dg_id);
            statement.setLong(5, mpegfile);
            ResultSet rs = statement.executeQuery();

            boolean exists = false;
            if (rs.next()) {
                exists = true;
            }

            return exists;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            
            return false;
        }finally{
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }

    public static String getBlockPath(int block_id, int patient_id, int dt_id, int dg_id, long mpegfile) {
        Connection connection = ConnectionManager.init_connection();
        try {
            String query = "SELECT path FROM Block WHERE block_id = ? AND patient_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, block_id);
            statement.setInt(2, patient_id);
            statement.setInt(3, dt_id);
            statement.setInt(4, dg_id);
            statement.setLong(5, mpegfile);
            ResultSet rs = statement.executeQuery();

            String path = null;
            if (rs.next()) {
                path = rs.getString("path");
            }
            return path;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            
            return null;
        }finally{
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
    
    public static ResultSet findByMetadata(String dni, String name){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select patient_id,dt_id, dg_id, mpegfile from Patient where";
            String operator = "AND";
            Boolean first = true;
            String op;
            List <String> params = new ArrayList<>();
            if (dni != null && !dni.equals("")) {
                query = query.concat(" dni LIKE ?");                
                first = false;
                params.add(dni);
            }
            if (name != null && !name.equals("")) {
                op = (first) ? "" : operator; // fiquem AND o OR si abans ja hem ficat algun altre atribut
                query = query.concat(op+" DESCRIPTION LIKE ?");                
                first = false;
                params.add(name);
            }
            
            
            // Si no se añadieron filtros, hacer un SELECT * (sin WHERE)
            if (params.isEmpty()) {
                query = "SELECT * FROM Patient"; // Aquí hacemos un SELECT completo
            }
            
            PreparedStatement statement = connection.prepareStatement(query);
            for (int w = 0; w < params.size(); w++) statement.setString(w+1, "%"+params.get(w)+"%");
            ResultSet rs = statement.executeQuery();
            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(rs);

            return crs;
        } catch (SQLException e){
            System.err.println(e.getMessage());
            return null;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
    public static String getString(int patient_id, String field, int dg_id,int dt_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{
            String query = "select "+field+" from Patient where patient_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            PreparedStatement statement = connection.prepareStatement(query);    
            statement.setInt(1, patient_id);
            statement.setInt(2, dt_id);
            statement.setInt(3, dg_id);
            statement.setLong(4, mpegfile);
            ResultSet rs = statement.executeQuery();
            String retField = null;
            if(rs.next()) retField = rs.getString(field);
            return retField;
        } catch (SQLException e){
            System.err.println(e.getMessage());
            return null;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
}
