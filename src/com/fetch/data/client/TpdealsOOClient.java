package com.fetch.data.client;

import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.Page;
import com.fetch.data.tools.FetchDataThreadPool;

/**
 * Tpdeals other-opportunities
 * @author 
 *
 */
public class TpdealsOOClient extends TpdealsClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	private final static String domain = "http://www.tpdeals.co.uk";
	private final static String webSite = "/other-opportunities/?page=";
	private static int pageSize = 10;
	private static int currentPage = 1;
	private final String charset = "UTF-8";
    // 提取总页数的正则表达式
    private static final Pattern pageCountPattern = Pattern.compile(
    		"<li><a href=\"/businesses-for-sale.*?>(.*?)</a></li>\\s+<li class=\"next\">", 
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
			getUrlListByPage(url, urlListPattern, domain, charset);
			while (page.hasNextPage()) {
				semp.acquire();
				int next = page.getNextPage();
				final int nextPage = next - 1;
				page.setCurrentPage(next);
				FetchDataThreadPool.exec.execute(new Runnable() {
					@Override
					public void run() {
						try {
							String urlPage = domain + webSite + nextPage;
							getUrlListByPage(urlPage, urlListPattern, domain, charset);
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
			log.error("获取Tpdeals other-opportunities网站报错："+e.getMessage(), e);
			throw e;
		}
	}
}
