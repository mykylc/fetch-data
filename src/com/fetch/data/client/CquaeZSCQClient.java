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
 * 重庆联合产权交易所/知识产权
 * @author 
 *
 */
public class CquaeZSCQClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(10);
	
	private final static String domain = "http://www.cquae.com";
	private final static String webSite = "/Project?q=s&type=4&page=";
	private static int pageSize = 20;
	private static int currentPage = 1;
	private final String charset = "UTF-8";
	
	// 提取总条数的正则表达式
    private static final Pattern totalCountPattern = Pattern.compile("共有<b>(.*?)</b>条记录", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile(
    		"<div class=\"n2_List\" style=\"background:.*?;\">\\s+<a href=\"(.*?)\" class=\"P_List_A\" target=\"_blank\" id=\"A_.*?\" style=\"display:none;\" >.*?</a>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取name值
    private static final Pattern namePattern2 = Pattern.compile(
    		"<td class=\"P_S_t_t\">转让标的名称</td>\\s*<td colspan=\"3\">\\s*(.*?)\\s*</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取price
    private static final Pattern pricePattern2 = Pattern.compile(
    		"<td class=\"P_S_t_t\" style=\"width:240px;\">挂牌价格</td>\\s*<td  style=\"width:360px;\">\\s*(.*?)\\s*</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取listing_date end_date值
    private static final Pattern datePattern = Pattern.compile(
    		"<td class=\"P_S_t_t\" title=\".*?\">挂牌起始日期</td>        <td>\\s*(.*?)\\s*</td>\\s*<td class=\"P_S_t_t\" title=\".*?\">挂牌期满日期</td>        <td>\\s*(.*?)\\s*</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取location industry值
    private static final Pattern locationIndustryPattern = Pattern.compile(
    		"<td class=\"P_S_t_t\">标的所在地区</td>\\s*<td>\\s*(.*?)\\s*</td>\\s*<td class=\"P_S_t_t\">标的所属行业</td>\\s*<td>\\s*(.*?)\\s*</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<td  class=\"P_S_top_2_t\">项目名称</td>\\s*<td style=\"height:25px;\">&nbsp;&nbsp;<span title=\"(.*?)\"  style=\" white-space:nowrap; line-height:25px; height:25px;width:470px;overflow:hidden;text-overflow:ellipsis;display:inline-block;\">",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取price
    private static final Pattern pricePattern = Pattern.compile(
    		"<td  class=\"P_S_top_2_t\" title=\".*?\">挂牌价</td>\\s*<td>&nbsp;\\s*<span>(.*?)</span>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取listing_date值
    private static final Pattern listingDatePattern = Pattern.compile(
    		"<td  class=\"P_S_top_2_t\" title=\".*?\">挂牌开始日期</td>\\s*<td>&nbsp;\\s*(.*?)\\s*</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取end_date值
    private static final Pattern endDatePattern = Pattern.compile(
    		"<td  class=\"P_S_top_2_t\" title=\".*?\">挂牌期满日期</td>\\s*<td>&nbsp;\\s*(.*?)\\s*</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取project_type
    private static final Pattern projectTypePattern = Pattern.compile(
    		"<td  class=\"P_S_top_2_t\">业务类型</td>\\s*<td>&nbsp;\\s*(.*?)\\s*</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<div class=\"P_S_top_4_1\"><span>项目公告</span></div><div  class=\"P_S_top_4_2\">(.*?)</div><!--<a name=\"标的信息\">", 
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
			log.error("获取CquaeZSCQClient网站报错："+e.getMessage(), e);
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
		Matcher nameMatcher2 = namePattern2.matcher(content);
		if (nameMatcher2.find()) {
			log.debug("1");
			log.debug(nameMatcher2.group(1));
			data.setName(nameMatcher2.group(1));
			Matcher priceMatcher = pricePattern2.matcher(content);
			if (priceMatcher.find()) {
				log.debug(priceMatcher.group(1).replaceAll("\\s+", ""));
				data.setPrice(priceMatcher.group(1).replaceAll("\\s+", ""));
			}
			Matcher dateMatcher = datePattern.matcher(content);
			if (dateMatcher.find()) {
				log.debug(dateMatcher.group(1)+"="+dateMatcher.group(2));
				data.setListingDate(dateMatcher.group(1));
				data.setEndDate(dateMatcher.group(2));
			}
			Matcher locationIndustryMatcher = locationIndustryPattern.matcher(content);
			if (locationIndustryMatcher.find()) {
				log.debug(locationIndustryMatcher.group(1)+"="+locationIndustryMatcher.group(2));
				data.setLocation(locationIndustryMatcher.group(1));
				data.setIndustry(locationIndustryMatcher.group(2));
			}
		} else if(nameMatcher.find()){
			log.debug("2");
			log.debug(nameMatcher.group(1));
			data.setName(nameMatcher.group(1));
			Matcher priceMatcher = pricePattern.matcher(content);
			if (priceMatcher.find()) {
				log.debug(priceMatcher.group(1));
				data.setPrice(priceMatcher.group(1));
			}
			Matcher listingDateMatcher = listingDatePattern.matcher(content);
			if (listingDateMatcher.find()) {
				log.debug(listingDateMatcher.group(1));
				data.setListingDate(listingDateMatcher.group(1));
			}
			Matcher endDateMatcher = endDatePattern.matcher(content);
			if (endDateMatcher.find()) {
				log.debug(endDateMatcher.group(1));
				data.setEndDate(endDateMatcher.group(1));
			}
			Matcher projectTypeMatcher = projectTypePattern.matcher(content);
			if (projectTypeMatcher.find()) {
				log.debug(projectTypeMatcher.group(1).trim());
				data.setProjectType(projectTypeMatcher.group(1).trim());
			}
			
			Matcher descriptionMatcher = descriptionPattern.matcher(content);
			if (descriptionMatcher.find()) {
				log.debug(descriptionMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
				data.setDescription(descriptionMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			}
		}
		return data;
	}
	
}
