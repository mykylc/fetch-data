package com.fetch.data.client;

import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.FetchData;
import com.fetch.data.domain.Page;
import com.fetch.data.tools.FetchDataThreadPool;

/**
 * 上海联合产权交易所/意向项目
 * @author 
 *
 */
public class SuaeeYXGPClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(10);
	
	private final static String domain = "http://www.suaee.com/suaee/portal/project/";
	private final static String webSite = "projlist.jsp?ptype=yxgp&cp=";
	private static int pageSize = 45;
	private static int currentPage = 1;
	private final String charset = "GBK";
	
	// 提取总条数的正则表达式
    private static final Pattern totalCountPattern = Pattern.compile("<b>本类项目共有 <font color=\"#ff3333\">(.*?)</font> 个</b>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile("<td width=\"50%\" align=\"left\"><a title=\".*?\" target=\"_black\" href=\"(.*?)\" class=\"proj\">.*?</a></td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<td width=\"150\" align=\"left\" bgcolor=\"\\#CAE8EA\">&nbsp;&nbsp;<b>标的名称</b></td>\\s+<td align=\"left\" bgcolor=\"\\#FAFAFA\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		" <td width=\"150\" align=\"left\" bgcolor=\"\\#CAE8EA\">&nbsp;&nbsp;<b>项目介绍</b></td>\\s+<td align=\"left\" bgcolor=\"\\#FAFAFA\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取end_date值
    private static final Pattern endDatePattern = Pattern.compile(
    		"<td width=\"150\" align=\"left\" bgcolor=\"\\#CAE8EA\">&nbsp;&nbsp;<b>有效期</b></td>\\s+<td align=\"left\" bgcolor=\"\\#FAFAFA\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取price
    private static final Pattern pricePattern = Pattern.compile(
    		"<td width=\"150\" align=\"left\" bgcolor=\"\\#CAE8EA\">&nbsp;&nbsp;<b>转让方式（意向）</b></td>\\s+<td align=\"left\" bgcolor=\"\\#FAFAFA\">(.*?)</td>",
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
			log.error("获取SuaeeYXGPClient网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
	@Override
	public FetchData parseObject(String content, String pageUrl) throws Exception {
		FetchData data = new FetchData();
		data.setPageUrl(pageUrl);
		data.setCountry("中国");
		log.debug(content);
		Matcher nameMatcher = namePattern.matcher(content);
        if (nameMatcher.find()) {
        	log.debug(nameMatcher.group(1));
        	data.setName(nameMatcher.group(1));
        }
        Matcher descriptionMatcher = descriptionPattern.matcher(content);
        if (descriptionMatcher.find()) {
        	log.debug(descriptionMatcher.group(1).replaceAll("&nbsp;", ""));
			data.setDescription(descriptionMatcher.group(1).replaceAll("&nbsp;", ""));
		}
        Matcher endDateMatcher = endDatePattern.matcher(content);
        if (endDateMatcher.find()) {
        	log.debug(endDateMatcher.group(1));
			data.setEndDate(endDateMatcher.group(1));
		}
        Matcher priceMatcher = pricePattern.matcher(content);
        if (priceMatcher.find()) {
        	log.debug(priceMatcher.group(1).replaceAll("&nbsp;", ""));
			data.setPrice(priceMatcher.group(1).replaceAll("&nbsp;", ""));
		}
		return data;
	}
	
}
