package com.emirates.dash.exceptions;
//for middleware runtime exception and throwable checked exceptions
public class DashMiddlewareException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		String code;
		String message;
		String source;
		
		
		public DashMiddlewareException(final String message) {
			super(message);
		}

		public DashMiddlewareException(final Throwable throwable) {
			super(throwable);
		}
		
		public DashMiddlewareException(String message, Throwable e) {
			super(message, e);
		}
		

		
		public DashMiddlewareException(String code,String message,String source) {
			this.code = code;
			this.message = message;
			this.source = source;
		}	
		
		public String getCode() {
			return code;
		}

		 

		public String getSource() {
			return source;
		}

		 

		public String getMessage() {
			return message;
		}

}
