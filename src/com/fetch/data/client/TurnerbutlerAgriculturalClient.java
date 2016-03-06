package com.fetch.data.client;

import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.Page;
import com.fetch.data.tools.FetchDataThreadPool;

/**
 * Turnerbutler Agricultural
 * @author 
 *
 */
public class TurnerbutlerAgriculturalClient extends TurnerbutlerClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	private final static String domain = "http://www.turnerbutler.co.uk/";
	private final static String webSite = "businessesforsalelistingsector.php?keyword=Agricultural%20and%20Materials&id=";
	private static int pageSize = 10;
	private static int currentPage = 1;
	private final String charset = "UTF-8";
	
	// 提取总页数的正则表达式
    private static final Pattern pageCountPattern = Pattern.compile(
    		"<p style=\"text-align:right;font-size: 17px;padding: 5px;font-weight: bold; color:#125a96; background:#e2e2e2;background: #F3F3F3;border-radius: 5px;\"> Page 1 of(.*?)</p>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile(
    		"<h2 class=\"style12\" style=\" margin-top: -10px; font-size:17px;\" ><a href=\"(.*?)\" ><p style=\"text-align: left;\">.*?</p></a></h2>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage;
			final Page page = getPage2(url, pageCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			//log.info(page.getPageCount()+"");
			getUrlListByPage(url, urlListPattern, domain, charset);
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
			log.error("获取TurnerbutlerAgricultural网站报错："+e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public String getIndustry() {
		return "Agricultural and Materials";
	}

}
