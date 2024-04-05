package httpserver.itf.impl;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import httpserver.itf.HttpRequest;
import httpserver.itf.HttpRicmletResponse;

public class HttpRicmletResponseImpl implements HttpRicmletResponse {
	
	protected HttpServer m_hs;
	protected PrintStream m_ps;
	protected HttpRequest m_req;
	Map<String, String> cookies = new LinkedHashMap<>();

	protected HttpRicmletResponseImpl(HttpServer hs, HttpRequest req, PrintStream ps) {
		m_hs = hs;
		m_req = req;
		m_ps = ps;
	}
	
	
	@Override
	public void setReplyOk() throws IOException {
		m_ps.println("HTTP/1.0 200 OK");
		for (Map.Entry<String, String> entry : cookies.entrySet()) {
			m_ps.println("Set-Cookie: " + entry.getKey() + "=" + entry.getValue() + "; Path=/\r\n");
		}
		if(m_req instanceof HttpRicmletRequestImpl) {
			Session sess = (Session)((HttpRicmletRequestImpl)m_req).getSession();
			if(sess != null) {
				m_ps.println("Set-Cookie: session-id="+sess.getId() + "; Path=/\r\n"); 
			}
		}
		m_ps.println("Date: " + new Date());
		m_ps.println("Server: ricm-http 1.0");
	}

	@Override
	public void setReplyError(int codeRet, String msg) throws IOException {
		m_ps.println("HTTP/1.0 "+codeRet+" "+msg);
		m_ps.println("Date: " + new Date());
		m_ps.println("Server: ricm-http 1.0");
		m_ps.println("Content-type: text/html");
		m_ps.println(); 
		m_ps.println("<HTML><HEAD><TITLE>"+msg+"</TITLE></HEAD>");
		m_ps.println("<BODY><H4>HTTP Error "+codeRet+": "+msg+"</H4></BODY></HTML>");
		m_ps.flush();
	}

	public void setContentLength(int length) {
		m_ps.println("Content-length: " + length);
	}

	public void setContentType(String type) {
		m_ps.println("Content-type: " + type);
	}

	@Override
	public PrintStream beginBody() throws IOException {
		m_ps.println(); 
		return m_ps;
	}

	@Override
	public void setCookie(String name, String value) {
		cookies.put(name, value);
	}

}
