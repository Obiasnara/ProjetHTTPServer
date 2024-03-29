package httpserver.itf.impl;

import java.io.IOException;
import java.io.PrintStream;

import httpserver.itf.HttpRicmletResponse;

public class HttpRicmletResponseImpl implements HttpRicmletResponse {


	@Override
	public void setReplyOk() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReplyError(int codeRet, String msg) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentLength(int length) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setContentType(String type) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PrintStream beginBody() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCookie(String name, String value) {
		// TODO Auto-generated method stub
		
	}

}
