package httpserver.itf.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import httpserver.itf.HttpRequest;
import httpserver.itf.HttpResponse;

/*
 * This class allows to build an object representing an HTTP static request
 */
public class HttpStaticRequest extends HttpRequest {
	static final String DEFAULT_FILE = "index.html";

	public HttpStaticRequest(HttpServer hs, String method, String ressname) throws IOException {
		super(hs, method, ressname);
	}
	
	public void process(HttpResponse resp) throws Exception {
		
		
		File[] files = this.m_hs.getFolder().listFiles();
	    File file_requested = null;
	    												// This trick is used because I set ./Files as resource directory
	    if(this.m_ressname == null || this.m_ressname.equals("") || this.m_ressname.equals("/") || this.m_ressname.equals("/"+this.m_hs.getFolder().getPath()+"/")) {
	    	this.m_ressname = this.m_hs.getFolder().getPath()+"/"+DEFAULT_FILE;
	    } else {
		    if(this.m_ressname.contains(this.m_hs.getFolder().getName())) {
	    		this.m_ressname = "."+this.m_ressname;
		    } else  {
		    	this.m_ressname.replace(".", "");
		    	this.m_ressname = this.m_hs.getFolder().toString() + this.m_ressname;
		    }
	    }
	    
	    // Find the file equal to DEFAULT_FILE
	    if (files != null) {
	        for (File file : files) {
        		if (this.m_ressname.equals(file.getPath())) {
	            	file_requested = file;
	                break;
	            }
	        }
	    }
	    // Check if file exists
	    if (file_requested != null && file_requested.exists()) {
	    	resp.setReplyOk();
			resp.setContentType(HttpRequest.getContentType(this.m_ressname));
			
	    	// File found, read its content and write to the response
	        InputStream is = new FileInputStream(file_requested);
	        resp.setContentLength((int)file_requested.length());
	        PrintStream ps = resp.beginBody();
	        if(is != null) {
	        	try {
	        		ps.write(is.readAllBytes());
	        	} catch (Exception e) {
	        		is.close();
	        		resp.setReplyError(500, "Error while reading : " + DEFAULT_FILE);
	        	}
	        }
	        
	    } else {
	        // File not found, set appropriate response status
	        resp.setReplyError(404, "File not found");
	    }
	}

}
