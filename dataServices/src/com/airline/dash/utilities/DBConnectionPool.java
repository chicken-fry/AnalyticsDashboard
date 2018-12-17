package com.emirates.dash.utilities;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.log4j.Level;
import com.emirates.dash.exceptions.DashMiddlewareException;


public class DBConnectionPool {

	
	/**
	 * getConnection returns a connection-object to connect to the database
	 * 
	 * @return Connection : the connection-object to connect to the DB
	 */
	
	private static DataSource ds = null;
	
	private static void getDS(){
		DashLoggerUtil.log(Level.DEBUG,"inside getDS");
//		Properties ctxprop = new Properties();
//		InitialContext ctx = null;
		
		Context context = null; 
//		Service service = (Service) context.lookup( "java:app/service/" + ServiceImpl.class.getSimpleName() );
		
//		ctxprop.put(Context.INITIAL_CONTEXT_FACTORY, DashProperties
//				.getProperty(DashConstants.INITIAL_CONTEXT_FACTORY));
		
			try {
				context = new InitialContext();
//				ctx = new InitialContext(ctxprop);
				ds = (DataSource) context.lookup(DashProperties.getProperty(
						DashConstants.JNDI_NAME_MW).toString());
				DashLoggerUtil.log(Level.DEBUG, " JNDI :"+DashProperties.getProperty(
						DashConstants.JNDI_NAME_MW).toString());
			} catch (Exception e) {
				DashLoggerUtil.log(Level.ERROR, e);
				throw new DashMiddlewareException(e);
			}
	}
	
	public static Connection getConnection() {
		DashLoggerUtil.log(Level.DEBUG,"inside getConnection");
		if(ds == null){
			DashLoggerUtil.log(Level.DEBUG,"ds is null");
			getDS();
		}
		if(ds!=null){
			DashLoggerUtil.log(Level.DEBUG,"ds not null now");
		}
		Connection con = null;

		try {
			con = ds.getConnection();
			return con;
		} catch (Exception e) {
			ds = null;
			throw new DashMiddlewareException(e);
		}

	}

	/**
	 * release the connection to the pool
	 * 
	 * @return void
	 */
	public static void releaseConnection(Connection con) {
		try {
			if (con != null) {
				con.close();
			}
		} catch (Exception ex) {
			DashLoggerUtil.log(Level.ERROR, ex);
		}
	}

	/**
	 * Release the db connections to the pool
	 * 
	 * @param rs
	 * @param st
	 * @param con
	 */
	public static void releaseConnection(ResultSet rs, Statement st,
			Connection con) {
		try {
			try{
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
			}catch(Throwable e){
				DashLoggerUtil.log(Level.ERROR, e);
			}
			if (con != null) {
				con.close();
			}
		} catch (Exception ex) {
			DashLoggerUtil.log(Level.ERROR, ex);
		}
	}
}
