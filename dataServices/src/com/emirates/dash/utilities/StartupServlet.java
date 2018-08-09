package com.emirates.dash.utilities;


import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.log4j.Level;
import com.emirates.dash.engine.DataFlowOrchestrator;
import com.emirates.dash.exceptions.DashMiddlewareException;



public class StartupServlet implements Servlet {

	
	
	
	public void init(ServletConfig arg0) throws ServletException {
	
		try{
			DashLoggerUtil.initialize();
			DashLoggerUtil.log(Level.INFO,"Starting IBE Dashboard");
			DashProperties.initialize();
			DataFlowOrchestrator.getInstance().populateCachesOnStartup();
			DBDataAccessScheduler.startTimer();
			OverlayDataRefreshScheduler.startTimer();
			
			DashLoggerUtil.log(Level.INFO, "Startup Done");
		}catch(DashMiddlewareException e){
			e.printStackTrace();
		}
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}
}
