package httpserver.itf.impl;

import java.rmi.server.UID;
import java.util.concurrent.ConcurrentHashMap;

import httpserver.itf.HttpSession;

public class Session implements HttpSession {

	String id;
	
	ConcurrentHashMap<String, Object> session_informations;
	Long timer_to_destruct;
	public Long start_of_life;
	
	public Session() {
		System.out.println("New session");
		this.id = new UID().toString();
		session_informations = new ConcurrentHashMap<>();
		timer_to_destruct= System.currentTimeMillis();
		start_of_life=timer_to_destruct;
	}
	
	public Session(String UID) {
		System.out.println("New session");
		this.id = UID;
		session_informations = new ConcurrentHashMap<>();
		timer_to_destruct= System.currentTimeMillis();
		start_of_life=timer_to_destruct;
	}
	
	@Override
	public String getId() {
		return id.toString();
	}

	@Override
	public Object getValue(String key) {
		return session_informations.get(key);
	}

	@Override
	public void setValue(String key, Object value) {
		session_informations.put(key, value);
	}
	
	public void bump() {
		System.out.println("Bump");
		timer_to_destruct = System.currentTimeMillis();
	}
	
	public Long getLastUseValue() {
		return timer_to_destruct;
	}
}
