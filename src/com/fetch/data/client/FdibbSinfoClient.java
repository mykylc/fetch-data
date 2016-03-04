package com.fetch.data.client;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.Page;
import com.fetch.data.tools.FetchDataThreadPool;

/**
 * fdi
 * @author 
 *
 */
public class FdibbSinfoClient extends FdiClient{
	
	final Semaphore semp = new Semaphore(20);
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	private final static String webSite = "/bbsinfo/s_2_0_";
	private final static String param = ".html?style=1800000091-2-10000115&q=field39%5einvestment&r=&t=ichk=0&starget=1";
	public static Set<String> set = new HashSet<String>(); 
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage + param;
			final Page page = getPage2(url, pageCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			log.debug(page.getPageCount()+"");
			getUrlListByPage2(url, urlListPattern, domain, charset);
			while (page.hasNextPage()) {
				semp.acquire();
				final int nextPage = page.getNextPage();
				page.setCurrentPage(nextPage);
				FetchDataThreadPool.exec.execute(new Runnable() {
					@Override
					public void run() {
						try {
							String urlPage = domain + webSite + nextPage + param;
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
			log.error("获取fdi bbSinfo网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
}
