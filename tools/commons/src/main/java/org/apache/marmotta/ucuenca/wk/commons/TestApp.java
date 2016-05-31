package org.apache.marmotta.ucuenca.wk.commons;

public final class TestApp {
	
	private static TestApp instance;
	public static TestApp getInstance()throws Exception {
		return instance == null ? new TestApp():instance; 
	}
	
	private TestApp() {
		
	}

}
