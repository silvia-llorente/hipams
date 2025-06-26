package db.modifiers;

import db.connManager.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class mpegM {    
    public static boolean insertMpegFile(long id, String name, String path, String owner){
        Connection connection = ConnectionManager.init_connection();
        try {
            PreparedStatement statement;
            String query = "INSERT INTO MPEGFile (ID, NAME, PATH, OWNER) VALUES (?, ?, ?, ?)";
            statement = connection.prepareStatement(query);
            statement.setLong(1, id);
            statement.setString(2, name);
            statement.setString(3, path);
            statement.setString(4, owner);
            statement.executeUpdate();
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
    
    public static boolean deleteMpegFile(long id) {
        Connection connection = ConnectionManager.init_connection();
        try{       
            PreparedStatement statement;
            String query = "DELETE FROM MPEGFile WHERE id = ?";
            statement = connection.prepareStatement(query); 
            statement.setLong(1,id);
            statement.executeUpdate();
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