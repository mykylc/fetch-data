package com.fetch.data.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.Page;
import com.fetch.data.tools.FetchDataThreadPool;
import com.fetch.data.tools.HttpUtils;

/**
 * Tpdeals businesses-sought
 * @author 
 *
 */
public class TpdealsBSClient extends TpdealsClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	private final static String domain = "http://www.tpdeals.co.uk";
	private final static String webSite = "/businesses-sought/?page=";
	private static int pageSize = 10;
	private static int currentPage = 1;
	private final String charset = "UTF-8";
    // 提取总页数的正则表达式
    private static final Pattern pageCountPattern = Pattern.compile(
    		"<li><a href=\"/businesses-sought.*?>(.*?)</a></li>\\s+<li class=\"next\">", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile(
    		"<h3><a href=\"(.*?)\">.*?</a></h3>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage;
			final Page page = getPage2(url, pageCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			log.debug(page.getPageCount()+"");
			getUrlListByPage2(url, urlListPattern, domain, charset);
			while (page.hasNextPage()) {
				semp.acquire();
				int next = page.getNextPage();
				final int nextPage = next;
				page.setCurrentPage(next);
				FetchDataThreadPool.exec.execute(new Runnable() {
					@Override
					public void run() {
						try {
							String urlPage = domain + webSite + nextPage;
							getUrlListByPage2(urlPage, urlListPattern, domain, charset);
						} catch (Exception e) {
							log.error("启动线程报错："+e.getMessage(), e);
						} finally {
							semp.release();
						}
					}
				});
			}
			while (true) {
				if (semp.availablePermits()==20) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("获取Tpdeals businesses-sought网站报错："+e.getMessage(), e);
			throw e;
		}
	}
    /**
	 * 根据每页获取URL列表
	 * @param url
	 * @param urlListPattern
	 * @param domain
	 * @return void
	 * @throws Exception
	 */
	public void getUrlListByPage2(String url, Pattern urlListPattern, String domain, String charset) throws Exception{
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
            	hrefUrl = hrefUrl.replaceAll("&#233;", "%C3%A9");
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
}
