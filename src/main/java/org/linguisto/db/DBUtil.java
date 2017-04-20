package org.linguisto.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DBUtil {
	public static final Logger log = Logger.getLogger(DBUtil.class.getName());
    private static DataSource ds;
    private static String dataSourceName = "";

    /**
     * Empty Constructor
     */
    protected DBUtil() {
        // empty!
    }
    
    public static synchronized void setDataSourceName(final String dataSourceName) {
        DBUtil.dataSourceName = dataSourceName;
    }
    
    private static synchronized String getDataSourceName() {
        return DBUtil.dataSourceName;
    }

    private static DataSource getDataSource() throws SQLException {
        try {
            InitialContext ctx = new InitialContext();
            return (DataSource) ctx.lookup("java:/" + getDataSourceName());
        } catch (NamingException e) {
            throw new SQLException("Fatal: Unable to locate DataSource");
        }
    }
    
    public static Connection getConnectionFromPool() throws SQLException {
        if (ds == null) {
            ds = getDataSource();
        }
        return ds.getConnection();
    }
    
    /** Make connection to database.
     */
    public static Connection getConnection(String user, String password, String dbUrl, String jdbcDriver){
        Connection con = null;
        try{
            java.sql.Driver drv = (java.sql.Driver)Class.forName(jdbcDriver).newInstance();
            DriverManager.registerDriver(drv);
            try {
                if(user != null) {
                    con = DriverManager.getConnection(dbUrl,user,password);
                } else {
                    con = DriverManager.getConnection(dbUrl);
                }
                log.info("Connected to DB "+dbUrl);
                con.setAutoCommit(false);
            } catch(SQLException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        } catch(Exception e) {
            log.log(Level.SEVERE, "Can't load driver "+jdbcDriver+"\nException "+e.getClass().getName()+
            		" in makeConnection : "+e.getMessage(), e);
        }
        return con;
    }

    /**
     * execute the Query with Date result.
     * @param ce Connection
     * @param sqlQuery Query
     * @return Date result
     * @throws SQLException 
     */
    public static Date exeDateQuery(Connection ce, String sqlQuery) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = ce.prepareStatement(sqlQuery);

            rs = ps.executeQuery();
            if (rs.next()) {
                String strDate = rs.getString("DATUM");

                int tag = Integer.parseInt(strDate.substring(0, 2));
                int monat = Integer.parseInt(strDate.substring(3, 5));
                int jahr = Integer.parseInt(strDate.substring(6, 10));
                int stunde = Integer.parseInt(strDate.substring(11, 13));
                int minute = Integer.parseInt(strDate.substring(14, 16));
                int sekunde = Integer.parseInt(strDate.substring(17, 19));

                Calendar calDate = Calendar.getInstance();
                calDate.clear();
                calDate.set(jahr, monat - 1, tag, stunde, minute, sekunde);
                return calDate.getTime();
            }
            return null;
        } catch (SQLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } finally {
            rs.close();
            ps.close();
        }
    }

    /**
     * execute the Query with Long result.
     * @param ce Connection
     * @param sqlQuery Query
     * @return long result
     * @throws SQLException 
     */
    public static long exeLongQuery(Connection ce, String sqlQuery) throws SQLException {
        PreparedStatement s = null;
        ResultSet rs = null;
        try {
            s = ce.prepareStatement(sqlQuery);
            rs = s.executeQuery();

            if (rs.next()) {
                return rs.getLong(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } finally {
            try {
                rs.close();
                s.close();
            } catch (Exception e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public static List<Long> execListLongQuery(Connection ce, String sql, List<String> params) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Long> ret = new ArrayList<Long>();
        try {
            ps = ce.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i+1, params.get(i));
            }
            rs = ps.executeQuery();

            while (rs.next()) {
                ret.add(rs.getLong(1));
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (Exception e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return ret;
    }

    public static long exeLongPreparedStatement(PreparedStatement ps) throws SQLException {
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw e;
        } finally {
            try {
                rs.close();
            } catch (Exception e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
    
    /**
     * Closes the SQL Statement and handles null check and exception handling.
     * @param statement The Statement to close.
     */
    public final static void closeStatement(final Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "DaoDB: Exception closing Statement: " + e.getMessage(), e);
        }
    }

    /**
     * Closes the ResultSet and handles null check and exception handling.
     * @param resultSet The ResultSet to close
     */
    public final static void closeResultSet(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "DaoDB: Exception closing ResultSet: " + e.getMessage(), e);
        }
    }

    public final static void closeConnection(final Connection con) {
        try {
            if (con != null) {
                rollback(con);
                con.close();
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "DaoDB: Exception closing Connection: " + e.getMessage(), e);
        }
    }

    public final static void rollback(final Connection con) {
        if (con == null) {
            return;
        }
        try {
        	if (!con.getAutoCommit()) {
                con.rollback();
        	}
        } catch (Exception e) {
            log.log(Level.WARNING, "DaoDB: Rollback failed: " + e.getMessage(), e);
        }
    }
}
