package com.fetch.data.client;

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
public class FdiSinfoClient extends FdiClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	private final static String webSite = "/bbsinfo/s_2_0_";
	private final static String param = ".html?style=1800000091-2-10000120&q=field39%5eoutward%20investment%20project&r=&t=ichk=0&starget=1";
	
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage + param;
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
							String urlPage = domain + webSite + nextPage + param;
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
			log.error("获取fdi sinfo网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
}
