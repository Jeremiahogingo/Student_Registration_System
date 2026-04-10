import db.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class TestDB {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (conn != null) {
            System.out.println("SUCCESS: Connected to database!");
        } else {
            System.out.println("FAILED: Could not connect.");
        }
    }
}
