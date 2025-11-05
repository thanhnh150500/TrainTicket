package vn.ttapp.config;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;       // ✅ Dùng chuẩn JNDI
import java.sql.Connection;
import java.sql.SQLException;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author New User
 */
public class Db {
    private static volatile DataSource ds;

    private static DataSource lookupDataSource() throws NamingException {
        InitialContext ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:comp/env/jdbc/TrainDB");
    }

    private static DataSource getDataSource() throws NamingException {
        // Double-checked locking to lazily initialize the DataSource
        if (ds == null) {
            synchronized (Db.class) {
                if (ds == null) {
                    ds = lookupDataSource();
                }
            }
        }
        return ds;
    }

    public static Connection getConnection() throws SQLException {
        try {
            return getDataSource().getConnection();
        } catch (NamingException ne) {
            // Provide a clearer SQLException cause so callers can handle it
            SQLException sqlEx = new SQLException("JNDI DataSource not found: java:comp/env/jdbc/TrainDB");
            sqlEx.initCause(ne);
            throw sqlEx;
        }
    }
}
