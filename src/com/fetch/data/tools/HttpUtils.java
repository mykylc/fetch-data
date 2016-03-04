package com.fetch.data.tools;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	int needTryCount = 10;
	public String url;
	public String charset;
	public String method;
	private Map<String,String> header = new LinkedHashMap<String,String>();
	private Map<String,String> params = new LinkedHashMap<String,String>();
	public HttpUtils(String url, String charset){
		this.url = url;
		this.charset = charset;
	}
	public HttpUtils(String url, String charset, String method){
		this.url = url;
		this.charset = charset;
		this.method = method;
	}
	public HttpUtils(String url, String charset, String method, Map<String,String> header){
		this.url = url;
		this.charset = charset;
		this.method = method;
		this.header = header;
	}
	public HttpUtils(String url, String charset, String method, Map<String,String> header, Map<String,String> params){
		this.url = url;
		this.charset = charset;
		this.method = method;
		this.header = header;
		this.params = params;
	}
	
	public String execute(){
		String pageUrl = DBManager.getPageUrlMap().get(this.url);
		if (pageUrl!=null && !"".equals(pageUrl) && this.url.equals(pageUrl)) {
			return null;
		}
		for (; needTryCount > 0; needTryCount--) {
			try {
				HttpClient httpClient = new HttpClient(url);
				httpClient.setCharset(charset);
				if (method!=null&&!"".equals(method)) {
					httpClient.setMethod(method);
				}
				if (header.size()!=0 && !header.isEmpty()) {
					for(Entry<String, String> entry: header.entrySet()) {
						httpClient.addHeader(entry.getKey(),entry.getValue());
					}
				}
				if (params.size()!=0 && !params.isEmpty()) {
					for(Entry<String, String> entry: params.entrySet()) {
						httpClient.addParameter(entry.getKey(),entry.getValue());
					}
				}
				String result = httpClient.execute();
				if (result.indexOf("系统正在进行维护")!=-1) {
					log.error(String.format("[HttpUtils.execute][%s] error, needTryCount remain=%s ",  url, needTryCount));
				}else{
					return result;
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				log.error(String.format("[HttpUtils.execute][%s] error, needTryCount remain=%s ",  url, needTryCount));
			}
		}
		return null;
	}
}
