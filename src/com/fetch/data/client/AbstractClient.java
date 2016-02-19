package com.fetch.data.client;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.dao.FetchDataDao;
import com.fetch.data.domain.FetchData;
import com.fetch.data.domain.Page;
import com.fetch.data.tools.HttpUtils;

public abstract class AbstractClient {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	public abstract void fetchData() throws Exception;
	
	public abstract FetchData parseObject(String content, String pageUrl) throws Exception;
	
	/**
	 * 获取page对象
	 * @param url
	 * @param totalCountPattern
	 * @param pageSize
	 * @return Page
	 * @throws Exception
	 */
	public Page getPage(String url, Pattern totalCountPattern, int pageSize, String charset) throws Exception{
		try {
			HttpUtils httpUtils = new HttpUtils(url, charset);
			String result = httpUtils.execute();
			if (result==null) {
				return null;
			}
			//log.info(result);
			Matcher totalCountMatcher = totalCountPattern.matcher(result);
            while (totalCountMatcher.find()) {
                String totalCount = totalCountMatcher.group(1);
                Page page = new Page();
                page.setPageSize(pageSize);
                page.setTotalCount(Integer.parseInt(totalCount));
                page.setPageCount();
                return page;
            }
		} catch (Exception e) {
			log.error(String.format("[AbstractClient.getPage] url=%s; 获取page对象报错：%s", url, e.getMessage()), e);
			throw e;
		}
		return null;
	} 
	
	/**
	 * 根据每页获取URL列表
	 * @param url
	 * @param urlListPattern
	 * @param domain
	 * @return void
	 * @throws Exception
	 */
	public void getUrlListByPage(String url, Pattern urlListPattern, String domain, String charset) throws Exception{
		try {
			List<String> urlList = new ArrayList<String>();
			HttpUtils httpUtils = new HttpUtils(url, charset);
			String result = httpUtils.execute();
			if (result==null) {
				return;
			}
			log.debug(result);
			Matcher urlListmatcher = urlListPattern.matcher(result);
            while (urlListmatcher.find()) {
            	String hrefUrl = urlListmatcher.group(1);
            	if (hrefUrl.startsWith("http://")) {
            		urlList.add(hrefUrl);
            		log.debug(hrefUrl);
				} else {
					urlList.add(domain+hrefUrl);
					log.debug(domain+hrefUrl);
				}
            }
            handlerData(urlList, charset);
		} catch (Exception e) {
			log.error(String.format("[AbstractClient.getUrlListByPage] url=%s; 根据每页获取URL列表报错：%s", url, e.getMessage()), e);
			throw e;
		}
	}
	
	public void handlerData(List<String> urlList, String charset) throws Exception{
		if (urlList!=null && urlList.size()>0) {
			FetchDataDao fetchDataDao = new FetchDataDao();
			for (String url : urlList) {
				FetchData fetchData = getContent(url, charset);
				if (fetchData!=null) {
					fetchDataDao.insert(fetchData);
				}
			}
		}
	}
	
	
	/**
	 * 获取文章内容
	 * @param url
	 * @return FetchData
	 * @throws Exception
	 */
	public FetchData getContent(String url, String charset) throws Exception{
		try {
			HttpUtils httpUtils = new HttpUtils(url, charset);
			String content = httpUtils.execute();
			if (content!=null) {
				return parseObject(content, url);
			}
			return null;
		} catch (Exception e) {
			log.error(String.format("[AbstractClient.getContent] url=%s; 获取文章内容报错：%s", url, e.getMessage()), e);
			throw e;
		}
	}
	
}
