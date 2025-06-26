package db.modifiers;

import db.connManager.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class patientM {
    public static boolean insertPatient(int patient_id, String type, String protection, String path, String owner, String dni, String name, String street, int dt_id, Integer dg_id, Long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try {       
            PreparedStatement statement;
            String query = "INSERT INTO Patient (patient_id, type, path, owner, dni, name, street, protection, dt_id, dg_id, mpegfile) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(query);
            statement.setInt(1, patient_id);
            statement.setString(2, type);
            statement.setString(3, path);
            statement.setString(4, owner);
            statement.setString(5, dni);
            statement.setString(6, name);
            statement.setString(7, street);
            statement.setString(8, protection);
            statement.setInt(9, dt_id);
            statement.setInt(10, dg_id);
            statement.setLong(11, mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    public static boolean deletePatient(int patient_id, int dt_id, int dg_id, long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{       
            PreparedStatement statement;
            String query = "DELETE FROM Patient WHERE patient_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            statement = connection.prepareStatement(query); 
            statement.setInt(1,patient_id);
            statement.setInt(2,dt_id);
            statement.setInt(3,dg_id);
            statement.setLong(4,mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    public static boolean insertBlock(int block_id, int size, String path, String owner, int patient_id, int dt_id, Integer dg_id, Long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try {       
            PreparedStatement statement;
            String query = "INSERT INTO Block (block_id, size, path, owner, patient_id, dt_id, dg_id, mpegfile) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(query);
            statement.setInt(1, block_id);
            statement.setInt(2, size);
            statement.setString(3, path);
            statement.setString(4, owner);
            statement.setInt(5, patient_id);
            statement.setInt(6, dt_id);
            statement.setInt(7, dg_id);
            statement.setLong(8, mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
    
    public static boolean deleteBlock(int block_id, int patient_id, int dt_id, Integer dg_id, Long mpegfile){
        Connection connection = ConnectionManager.init_connection();
        try{       
            PreparedStatement statement;
            String query = "DELETE FROM Block WHERE block_id = ? AND patient_id = ? AND dt_id = ? AND dg_id = ? AND mpegfile = ?";
            statement = connection.prepareStatement(query); 
            statement.setInt(1,block_id);
            statement.setInt(2,patient_id);
            statement.setInt(3,dt_id);
            statement.setInt(4,dg_id);
            statement.setLong(5,mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }

        
    public static boolean updatePatient(int patient_id,String type,String path,String owner,String dni,String name,String street,String PR,int dt_id,int dg_id,long mpegfile) {
        Connection connection = ConnectionManager.init_connection();
        try {
            // 1) Construir dinámicamente la cláusula SET
            StringBuilder sb = new StringBuilder();
            boolean first = true;

            if (type   != null) { sb.append(first ? "" : ", ").append("type     = ?"); first = false; }
            if (path   != null) { sb.append(first ? "" : ", ").append("path     = ?"); first = false; }
            if (owner  != null) { sb.append(first ? "" : ", ").append("owner    = ?"); first = false; }
            if (dni    != null) { sb.append(first ? "" : ", ").append("dni      = ?"); first = false; }
            if (name   != null) { sb.append(first ? "" : ", ").append("name     = ?"); first = false; }
            if (street != null) { sb.append(first ? "" : ", ").append("street   = ?"); first = false; }
            if (PR     != null) { sb.append(first ? "" : ", ").append("PR       = ?"); first = false; }

            // Campos numéricos (si los consideras “opcionales” en la actualización)
            if (dt_id  != -1)  { sb.append(first ? "" : ", ").append("dt_id    = ?"); first = false; }
            if (dg_id  != -1)  { sb.append(first ? "" : ", ").append("dg_id    = ?"); first = false; }
            if (mpegfile != 0) { sb.append(first ? "" : ", ").append("mpegfile = ?"); first = false; }

            // Si no hay nada que actualizar
            if (first) {
                // No se pasó ningún parámetro a modificar
                return false;
            }

            // 2) Montar el UPDATE incluyendo el filtro por patient_id
            String sql = "UPDATE Patient SET " + sb.toString() + " WHERE patient_id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);

            // 3) Asignar los parámetros en el mismo orden de comprobación
            int idx = 1;
            if (type   != null) { stmt.setString(idx++, type); }
            if (path   != null) { stmt.setString(idx++, path); }
            if (owner  != null) { stmt.setString(idx++, owner); }
            if (dni    != null) { stmt.setString(idx++, dni); }
            if (name   != null) { stmt.setString(idx++, name); }
            if (street != null) { stmt.setString(idx++, street); }
            if (PR     != null) { stmt.setString(idx++, PR); }

            if (dt_id  != -1)  { stmt.setInt   (idx++, dt_id); }
            if (dg_id  != -1)  { stmt.setInt   (idx++, dg_id); }
            if (mpegfile != 0) { stmt.setLong  (idx++, mpegfile); }

            // Parámetro del WHERE
            stmt.setInt(idx, patient_id);

            // 4) Ejecutar y comprobar filas afectadas
            int updated = stmt.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            System.err.println("Error updating Patient: " + e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
  
    public static boolean updatePR(long mpegfile, int dg_id, int dt_id, int patient_id, String pr){
        Connection connection = ConnectionManager.init_connection();
        try{    
            
            PreparedStatement statement;
            String query = "UPDATE Patient set protection = ? where patient_id = ? and dt_id = ? and dg_id = ? and mpegfile = ?";
            statement = connection.prepareStatement(query); 
            statement.setString(1,pr);
            statement.setInt(2, patient_id);
            statement.setInt(3, dt_id);
            statement.setInt(4,dg_id);
            statement.setLong(5, mpegfile);
            statement.executeUpdate();
            ConnectionManager.close_connection(connection);
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }finally {
            if (connection != null) {
                ConnectionManager.close_connection(connection);
            }
        }
    }
    
    
}