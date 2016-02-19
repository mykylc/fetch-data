package com.fetch.data.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	int needTryCount = 10;
	public String url;
	public String charset;
	public HttpUtils(String url, String charset){
		this.url = url;
		this.charset = charset;
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
				String result = httpClient.execute();
				if (result.indexOf("系统正在进行维护")!=-1) {
					log.error(String.format("[HttpUtils.execute][%s] error, needTryCount remain=%s ",  url, needTryCount));
				}else{
					return result;
				}
			} catch (Exception e) {
				log.error(String.format("[HttpUtils.execute][%s] error, needTryCount remain=%s ",  url, needTryCount));
			}
		}
		return null;
	}
}
