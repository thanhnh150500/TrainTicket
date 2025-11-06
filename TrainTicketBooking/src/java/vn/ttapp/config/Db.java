package vn.ttapp.config;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.tomcat.jdbc.pool.DataSource;

public class Db {
    private static DataSource ds;
    static {
        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:comp/env/jdbc/TrainDB");
        } catch ( NamingException e) {
            throw new RuntimeException("JNDI DataSource not found", e);
        }
    }
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
