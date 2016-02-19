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
 * 安徽省产权交易所/非国有股权
 * @author 
 *
 */
public class AaeeFGYClient extends AaeeClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(10);
	
	public final static String webSite = "list_item.jsp?colId=1359610664886003&strWebSiteId=1338175111067003&parentColId=1359610511716000&intCurPage=";
										  
	// 提取URL列表的正则表达式
	public static final Pattern urlListPattern = Pattern.compile(
    		"<div style=\"overflow:hidden;height:24px;line-height:22px\">   	       		       <a href=\"(.*?)\" target=\"_blank\" title=\".*?\">.*?</a>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		
	//提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<tr>     	<th>标的名称</th><td colspan=\"3\">(.*?)</td>  	</tr>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取price
    private static final Pattern pricePattern = Pattern.compile(
    		"<th width=\"110px\">挂牌价格（万元）</th>		<td>(.*?)</tr>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取listing_date end_date值
    private static final Pattern datePattern = Pattern.compile(
    		"<th>挂牌起始日期</th>		<td>(.*?)</td>		<th>挂牌期满日期</th>		<td>(.*?)</td>	</tr>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取location industry值
    private static final Pattern locationIndustryPattern = Pattern.compile(
    		"<th>标的所在地区</th>		<td>(.*?)</td>		<th>标的所属行业</th>		<td>(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage;
			final Page page = getPage(url, pageCountPattern, pageSize, charset);
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
			log.error("获取AaeeFGYClient网站报错："+e.getMessage(), e);
			throw e;
		}
	}
    
    @Override
   	public FetchData parseObject(String content, String pageUrl) throws Exception {
   		FetchData data = new FetchData();
   		data.setPageUrl(pageUrl);
   		data.setCountry("中国");
   		//log.debug(content);
   		Matcher nameMatcher = namePattern.matcher(content);
   		if (nameMatcher.find()) {
   			log.debug(nameMatcher.group(1).replaceAll("&nbsp;", ""));
   			data.setName(nameMatcher.group(1).replaceAll("&nbsp;", ""));
   		}
   		Matcher priceMatcher = pricePattern.matcher(content);
   		if (priceMatcher.find()) {
   			log.debug(priceMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
   			data.setPrice(priceMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
   		}
   		Matcher dateMatcher = datePattern.matcher(content);
   		if (dateMatcher.find()) {
   			log.debug(dateMatcher.group(1).replaceAll("&nbsp;", "")+"="+dateMatcher.group(2).replaceAll("&nbsp;", ""));
   			data.setListingDate(dateMatcher.group(1).replaceAll("&nbsp;", ""));
   			data.setEndDate(dateMatcher.group(2).replaceAll("&nbsp;", ""));
   		}
   		Matcher locationIndustryMatcher = locationIndustryPattern.matcher(content);
   		if (locationIndustryMatcher.find()) {
   			log.debug(locationIndustryMatcher.group(1).replaceAll("&nbsp;", "")+"="+locationIndustryMatcher.group(2).replaceAll("&nbsp;", ""));
   			data.setLocation(locationIndustryMatcher.group(1).replaceAll("&nbsp;", ""));
   			data.setIndustry(locationIndustryMatcher.group(2).replaceAll("&nbsp;", ""));
   		}
   		
   		return data;
   	}
	
}
