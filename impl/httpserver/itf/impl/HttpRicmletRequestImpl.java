package httpserver.itf.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import httpserver.itf.HttpResponse;
import httpserver.itf.HttpRicmlet;
import httpserver.itf.HttpRicmletRequest;
import httpserver.itf.HttpRicmletResponse;
import httpserver.itf.HttpSession;

public class HttpRicmletRequestImpl extends HttpRicmletRequest {
	
	private static final Map<Class<?>, HttpRicmlet> singletons = new LinkedHashMap<>();
	
	Map<String, String> arguments;
	Map<String, String> cookies;
	
	public HttpRicmletRequestImpl(HttpServer hs, String method, String ressname, BufferedReader br) throws IOException {
		super(hs, method, ressname, br);
		// We create a "fake" query to parse it correctly
		this.arguments = splitQuery(new URL("https://localhost/"+ressname));
		Map<String, String> cookies = new LinkedHashMap<>();
		String line;
	    while ((line = br.readLine()).isEmpty() == false) {
	        if (line.startsWith("Cookie: ")) {
	        	System.out.println("COOOOKKIIIIIIE");
	            String[] cookieParts = line.substring("Cookie: ".length()).split(";")[0].split("=");
	            cookies.put(cookieParts[0], cookieParts[1]);
	        }
	    }
	    this.cookies = cookies;
	}

	
	public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
	    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
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
		// TODO Auto-generated method stub
		return null;
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
		HttpRicmlet getClassToCall =  getSingletonRicmlet(c);
		getClassToCall.doGet((HttpRicmletRequest) this, (HttpRicmletResponse) resp);
	}

}
