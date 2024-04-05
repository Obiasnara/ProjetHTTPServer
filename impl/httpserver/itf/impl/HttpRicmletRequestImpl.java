package httpserver.itf.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.rmi.server.UID;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import httpserver.itf.HttpResponse;
import httpserver.itf.HttpRicmlet;
import httpserver.itf.HttpRicmletRequest;
import httpserver.itf.HttpRicmletResponse;
import httpserver.itf.HttpSession;

public class HttpRicmletRequestImpl extends HttpRicmletRequest {
	
	private static final Map<Class<?>, HttpRicmlet> singletons = new LinkedHashMap<>();
	
	ConcurrentHashMap<String, String> arguments;
	ConcurrentHashMap<String, String> cookies;
	
	public HttpRicmletRequestImpl(HttpServer hs, String method, String ressname, BufferedReader br) throws IOException {
		super(hs, method, ressname, br);
		// We create a "fake" query to parse it correctly
		this.arguments = splitQuery(new URL("https://localhost/"+ressname));
		
		ConcurrentHashMap<String, String> cookies = new ConcurrentHashMap<>();
		
		String line;
	    while ((line = br.readLine()).isEmpty() == false) {
	    	if (line.startsWith("Cookie: ")) {
	            String[] cookiesParts = line.substring("Cookie: ".length()).split(";");
	            for (String cookiePart : cookiesParts) {
	                String[] cookie = cookiePart.split("=");
	                if (cookie.length == 2) {
	                    cookies.put(cookie[0].trim(), cookie[1].trim());
	                }
	            }
	        }
	    }
	    this.cookies = cookies;
	}

	
	public static ConcurrentHashMap<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
		ConcurrentHashMap<String, String> query_pairs = new ConcurrentHashMap<String, String>();
	    String query = url.getQuery();
	    if(query == null) {
	    	return null;
	    }
	    String[] pairs = query.split("&");
	    for (String pair : pairs) {
	        int idx = pair.indexOf("=");
	        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
	    }
	    return query_pairs;
	}
	
	@Override
	public HttpSession getSession() {
		// Handle the session
	    String id = this.cookies.get("session-id");
	    if(id != null) {
	    	Session s = (Session) this.m_hs.sessions.get(id);
	    	if(s != null) {
	    		s.bump();
	    		return s;
	    	}
	    	Session new_session = new Session(id);
		    this.m_hs.sessions.put(new_session.getId(), new_session);
		    return new_session;
	    }
	    Session new_session = new Session();
	    this.m_hs.sessions.put(new_session.getId(), new_session);
	    return new_session;
	}

	@Override
	public String getArg(String name) {
		
		return arguments == null ? null : arguments.get(name);
	}

	@Override
	public String getCookie(String name) {
		return cookies == null ? null : cookies.get(name);
	}
	// Synchronized to prevent duplication
	public synchronized HttpRicmlet getSingletonRicmlet(Class<?> c) {
		if (!singletons.containsKey(c)) {
            try {
                HttpRicmlet instance = (HttpRicmlet) c.getDeclaredConstructor().newInstance();
                singletons.put(c, instance);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create singleton for class " + c.getName(), e);
            }
        }
        return singletons.get(c);
	}
	
	@Override
	public void process(HttpResponse resp) throws Exception {
		String ricmletPath = m_ressname.substring("/ricmlets/".length());
		ricmletPath = ricmletPath.replace("/", ".");
		if(arguments != null) { // If args remove ?* 
			int int_index = ricmletPath.indexOf("?");
			ricmletPath = ricmletPath.substring(0, int_index);
		}
		String clsname = ricmletPath;
		Class<?> c = Class.forName(clsname);
		HttpRicmlet getClassToCall = getSingletonRicmlet(c);
		getClassToCall.doGet((HttpRicmletRequest) this, (HttpRicmletResponse) resp);
	}

}
