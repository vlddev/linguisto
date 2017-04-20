/*
#######################################################################
#
#  Linguisto Portal
#
#  Copyright (c) 2017 Volodymyr Vlad
#
#######################################################################
*/

package org.linguisto.learn.db;

import java.sql.*;
import java.util.logging.Logger;

public class DAO {
	
	private static final Logger	logger = Logger.getLogger(DAO.class.getName());

    /** Make connection to database.
     */
    public static Connection getConnection(String user, String password, String dbUrl, String jdbcDriver){
        Connection con = null;
        try{
            Driver drv = (Driver)Class.forName(jdbcDriver).newInstance();
            DriverManager.registerDriver(drv);
            try{
                if(user != null)
                    con = DriverManager.getConnection(dbUrl,user,password);
                else
                    con = DriverManager.getConnection(dbUrl);
                System.out.println("Connected to DB "+dbUrl);
                con.setAutoCommit(false);
            }catch(SQLException e){
                e.printStackTrace();
            }
        }catch(Exception e){
            System.out.println("Can't load driver "+jdbcDriver);
            System.out.println("Exception "+e.getClass().getName()+
            " in makeConnection : "+e.getMessage());
        }
        return con;
    }
	
    /**
     * Closes the SQL Statement and handles null check and exception handling.
     * @param statement The Statement to close.
     */
    public static void closeStatement(final Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (Exception e) {
            logger.warning("DaoDB: Exception closing Statement: " + e.getMessage());
        }
    }

	/**
     * Closes the ResultSet and handles null check and exception handling.
     * @param resultSet The ResultSet to close
     */
    public static void closeResultSet(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (Exception e) {
            logger.warning("DaoDB: Exception closing ResultSet: " + e.getMessage());
        }
    }

    public static void closeConnection(final Connection con) {
        try {
            if (con != null) {
                if (!con.getAutoCommit())
                    rollback(con);
                con.close();
            }
        } catch (Exception e) {
            logger.warning("DaoDB: Exception closing Connection: " + e.getMessage());
        }
    }

    public static void rollback(final Connection con) {
        if (con == null) {
            return;
        }
        try {
            con.rollback();
        } catch (Exception e) {
        	logger.warning("DaoDB: Rollback failed: " + e.getMessage());
        }
    }
}
