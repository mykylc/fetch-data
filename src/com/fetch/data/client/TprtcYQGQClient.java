package com.fetch.data.client;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.Page;
import com.fetch.data.tools.FetchDataThreadPool;

/**
 * 天津产权交易中心/央企股权
 * @author 
 *
 */
public class TprtcYQGQClient extends TprtcClient{
	
protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private final static String domain = "http://xinxipingtai.tprtc.com:8080";
	private final static String webSite = "/transaction/display/zhongyangtprtcAllN.jsp?Submit=GO&Page=";
	
	// 提取总条数的正则表达式
    public static final Pattern totalCountPattern = Pattern.compile(
    		"<td height=\"25\" colspan=\"5\" align=\"center\">共(.*?)条.*?<input name=\"Page\" type=\"text\"", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    public static final Pattern urlListPattern = Pattern.compile(
    		"<tr align=\"left\" onMouseOver=\"javascript:this.bgColor='#ffffff' ;this.style.cursor='hand'\" onMouseOut=\"javascript:this.bgColor=''\" onClick=\"javascript:window.open\\('(.*?)'\\)\">", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage;
			final Page page = getPage(url, totalCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			log.debug(page.getTotalCount()+"");
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
			log.error("获取TprtcYQGQClient网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
}
