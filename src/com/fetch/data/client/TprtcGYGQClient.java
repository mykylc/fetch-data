package com.fetch.data.client;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.Page;
import com.fetch.data.tools.FetchDataThreadPool;

/**
 * 天津产权交易中心/国有股权
 * @author 
 *
 */
public class TprtcGYGQClient extends TprtcClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private final static String domain = "http://xinxipingtai.tprtc.com:8080/transaction";
	private final static String webSite = "/display/gygpproAll.jsp?dec_url=..%2Fdisplay%2FgygpproAll.jsp&Submit=GO&Page=";
	// 提取总条数的正则表达式
    public static final Pattern totalCountPattern = Pattern.compile(
    		"<td colspan=\"5\" height=\"25\" align=\"center\">共(.*?)条.*?<input name=\"Page\" type=\"text\"", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    public static final Pattern urlListPattern = Pattern.compile(
    		"<td class=\"font03\" height=\"25\"><a title=\".*?\" target=\"_blank\" href=\"\\.\\.(.*?)\" class=\"font03\" >.*?</a>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage;
			final Page page = getPage(url, totalCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			getUrlListByPage(url, urlListPattern, domain, charset);
			while (page.hasNextPage()) {
				semp.acquire();
				final int nextPage = page.getNextPage();
				page.setCurrentPage(nextPage);
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
				if (semp.availablePermits()==10) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("获取TprtcGYGQClient网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
}
