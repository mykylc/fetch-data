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
 * invest 融资需求
 * @author 
 *
 */
public class InvestRongZiClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	private final static String domain = "http://www.56invest.com";
	private final static String webSite = "/weiboweixin/rongzixuqiu/list_248_";
	private final static String suffix = ".html";
	private static int pageSize = 14;
	private static int currentPage = 1;
	private final String charset = "UTF-8";
	
	// 提取总条数的正则表达式
    private static final Pattern totalCountPattern = Pattern.compile(
    		"<span class=\"pageinfo\">共 <strong>.*?</strong>页<strong>(.*?)</strong>条</span>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile(
    		"&nbsp;&nbsp;<a title=\".*?\" href=\"(.*?)\">.*?</a></li>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<div class=\"con_tit\"><h2>(.*?)</h2>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取listingDate
    private static final Pattern listingDatePattern = Pattern.compile(
    		"<small>时间:</small>(.*?)<small>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取businessDescription
    private static final Pattern businessDescriptionPattern = Pattern.compile(
    		"<td style=\"width:121px;\">((\\s+主营业务及优势：)|(\\s+主营业务：))</td>\\s+<td style=\"width:448px;\">(.*?)</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取location值
    private static final Pattern locationPattern = Pattern.compile(
    		"<td style=\"width:121px;\">\\s+所在地区：</td>\\s+<td style=\"width:448px;\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<td style=\"width:121px;\">\\s+企业基本状况</td>\\s+<td style=\"width:448px;\">(.*?)</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取financialRatios值
    private static final Pattern financialRatiosPattern = Pattern.compile(
    		"<td style=\"width:121px;\">\\s+财务经营状况：</td>\\s+<td style=\"width:448px;\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取company type
    private static final Pattern companyTypePattern = Pattern.compile(
    		"<td style=\"width:121px;\">\\s+企业性质</td>\\s+<td style=\"width:448px;\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取financingMode
    private static final Pattern financingModePattern = Pattern.compile(
    		"<td style=\"width:121px;\">\\s+融资方式：</td>\\s+<td style=\"width:448px;\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage + suffix;
			final Page page = getPage(url, totalCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			//log.debug(page.getPageCount()+"");
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
							String urlPage = domain + webSite + nextPage + suffix;
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
			log.error("获取invest融资需求网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
	@Override
	public FetchData parseObject(String content, String pageUrl) throws Exception {
		log.debug(content);
		FetchData data = new FetchData();
		data.setPageUrl(pageUrl);
		data.setProjectType("融资需求");
		Matcher nameMatcher = namePattern.matcher(content);
		if (nameMatcher.find()) {
			log.debug(nameMatcher.group(1));
			data.setName(nameMatcher.group(1));
		}
		Matcher listingDateMatcher = listingDatePattern.matcher(content);
		if (listingDateMatcher.find()) {
			log.debug(listingDateMatcher.group(1));
			data.setListingDate(listingDateMatcher.group(1));
		}
		Matcher businessDescriptionMatcher = businessDescriptionPattern.matcher(content);
		if (businessDescriptionMatcher.find()) {
			log.info(businessDescriptionMatcher.group(4).replaceAll("&mdash;", "-"));
		}
		
		Matcher locationMatcher = locationPattern.matcher(content);
		if (locationMatcher.find()) {
			log.debug(locationMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setLocation(locationMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		Matcher descriptionMatcher = descriptionPattern.matcher(content);
		if (descriptionMatcher.find()) {
			log.debug(descriptionMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		Matcher financialRatiosMatcher = financialRatiosPattern.matcher(content);
		if (financialRatiosMatcher.find()) {
			log.debug(financialRatiosMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		Matcher companyTypeMatcher = companyTypePattern.matcher(content);
		if (companyTypeMatcher.find()) {
			log.debug(companyTypeMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setCompanyType(companyTypeMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher financingModeMatcher = financingModePattern.matcher(content);
		if (financingModeMatcher.find()) {
			log.debug(financingModeMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		return data;
	}
}
