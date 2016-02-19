package com.fetch.data.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.TraceMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartBase;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http客户端
 */
public class HttpClient {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private String url;
	private String method;
	private String charset;
	private Cookie[] cookie;
	private int connectTimeout = 1000 * 10;
	private int readTimeout = 1000 * 120;
	private Map<String,String> header = new LinkedHashMap<String,String>();
	private List<Entry<String,Object>> params = new ArrayList<Entry<String,Object>>();
	private RequestEntity requestEntity;
	
	public HttpClient(String url){
		this.url = url;
	}
	
	public HttpClient(String url,String method){
		this.url = url;
		this.method = method;
	}
	
	public HttpClient(String url,String method,Cookie[] cookie){
		this.url = url;
		this.method = method;
		this.cookie = cookie;
	}
	
	public String execute() throws Exception{
		HttpMethodBase method = null;
		StringBuffer responseStr = new StringBuffer();
		if("POST".equals(getMethod())){
			method = new PostMethod(getUrl());
		} else if("PUT".equals(getMethod())){
			method = new PutMethod(getUrl());
		} else if("DELETE".equals(getMethod())){
			method = new DeleteMethod(getUrl());
		} else if("HEAD".equals(getMethod())){
			method = new HeadMethod(getUrl());
		} else if("OPTIONS".equals(getMethod())){
			method = new OptionsMethod(getUrl());
		} else if("TRACE".equals(getMethod())){
			method = new TraceMethod(getUrl());
		} else {
			method = new GetMethod(getUrl());
		}
		//设置头信息
		method.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.13 (KHTML, like Gecko) Chrome/24.0.1284.2 Safari/537.13");
		method.setRequestHeader("Connection", "Keep-Alive");
		method.setRequestHeader("Accept-Charset", getCharset());
		for(Entry<String, String> entry: header.entrySet()) {
			method.setRequestHeader(entry.getKey(),entry.getValue());
		}
		
		//设置参数信息
		if("POST".equals(getMethod())) {
			String contentType = header.get("Content-Type");
			if(requestEntity != null){
				((PostMethod)method).setRequestEntity(requestEntity);
			} else if("multipart/form-data".equals(contentType)) {
				((PostMethod)method).setRequestEntity(new MultipartRequestEntity(getPartList(),method.getParams()));
			} else{
				((PostMethod)method).addParameters(getPairList());
			}
		} else {
			if(params.isEmpty()==false){
				method.setQueryString(getQueryString());
			}
		}
		org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
		HttpClientParams clientParams = client.getParams(); 
		clientParams.setContentCharset(getCharset()); 
		if(cookie != null && cookie.length > 0) {
			client.getState().addCookies(cookie);
		}
		HttpConnectionManagerParams params = client.getHttpConnectionManager().getParams();
		params.setConnectionTimeout(connectTimeout);
		params.setSoTimeout(readTimeout);
		int rescode = client.executeMethod(method);
		if (rescode != HttpStatus.SC_OK) {
			log.error(String.format("返回code：%s,responseBody：%s", rescode, method.getResponseBodyAsString()));
			throw new Exception();
		}
		cookie = client.getState().getCookies();//设置cookie
		
		InputStream resStream = method.getResponseBodyAsStream();  
        BufferedReader br = new BufferedReader(new InputStreamReader(resStream, getCharset()));  
        String resTemp = null;  
        while((resTemp = br.readLine()) != null){  
        	responseStr.append(resTemp);  
        }  
		return responseStr.toString();
	}
	
	
	/**
	 * 使用了该方法，之前设置的addParameter值会全部失效。
	 * @param requestEntity
	 */
	public void setRequestEntity(RequestEntity requestEntity){
		this.requestEntity = requestEntity;
	}

	public void clearParameter(){
		if(params != null) {
			params.clear();
		}
	}
	public void addHeader(String key,String value){
		header.put(key, value);
	}
	
	private Part[] getPartList() throws Exception{
		List<Part> data = new ArrayList<Part>();
		for(Entry<String,Object> entry : params) {
			if(entry.getKey() == null || entry.getValue() == null) {
				continue;
			}
			PartBase part = null;
			if(entry.getValue() instanceof File) {
				part = new FilePart(entry.getKey(), (File)entry.getValue());
			} else {
				part = new StringPart(entry.getKey(), String.valueOf(entry.getValue()));	  
			}
			data.add(part);
		}
		return data.toArray(new Part[data.size()]);
	}
	
	private NameValuePair[] getPairList() throws Exception{
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		for(Entry<String,Object> entry : params) {
			if(entry.getKey() == null || entry.getValue() == null) {
				continue;
			}
			data.add(new NameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
		}
		return data.toArray(new NameValuePair[data.size()]);
	}
	
	private String getQueryString(){
		List<String> data = new ArrayList<String>();
		for(Entry<String,Object> entry : params) {
			if(entry.getKey() != null && entry.getValue() != null) {
				data.add(entry.getKey()+"="+encodeURI(String.valueOf(entry.getValue())));
			}
		}
		return StringUtils.join(data, "&");
	}
   
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getMethod() {
		if(method == null || "".equals(method)) {
			method = "GET";
		}
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	public String getCharset() {
		if(charset == null || "".equals(charset)) {
			charset = "UTF-8";
		}
		return charset;
	}
	
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	public String encodeURI(String str){
		try {
			return URLEncoder.encode(str, getCharset());
		} catch (Exception e) {
			return str;
		}
	}
	
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public Cookie[] getCookie() {
		return cookie;
	}

	public void setCookie(Cookie[] cookie) {
		this.cookie = cookie;
	}
}
