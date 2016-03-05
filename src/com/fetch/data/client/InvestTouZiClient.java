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
 * invest 投资需求
 * @author 
 *
 */
public class InvestTouZiClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	private final static String domain = "http://www.56invest.com";
	private final static String webSite = "/weiboweixin/binggouxuqiu/list_249_";
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
    //提取industry
    private static final Pattern industryPattern = Pattern.compile(
    		"<td style=\"width:177px;\">.*?期望标的公司所属行业</td>\\s+<td style=\"width:391px;\">(.*?)</td>	", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取capital
    private static final Pattern capitalPattern = Pattern.compile(
    		"<td style=\"width:177px;\">				&nbsp; 期望标的资产规模<br />\\s+&nbsp;</td>\\s+<td style=\"width:391px;\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取industryCaracteristics
    private static final Pattern industryCaracteristicsPattern = Pattern.compile(
    		"<td style=\"width:177px;\">				&nbsp; 期望拟购入标的行业特性</td>\\s+<td style=\"width:391px;\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取location值
    private static final Pattern locationPattern = Pattern.compile(
    		"<td style=\"width:177px;\">				&nbsp; 期望地域</td>\\s+<td style=\"width:391px;\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取financialRatios值
    private static final Pattern financialRatiosPattern = Pattern.compile(
    		"<td style=\"width:177px;height:75px;\">\\s+&nbsp; 期望标的财务经营状况<br />\\s+&nbsp;</td>\\s+<td style=\"width:391px;height:75px;\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取investmentMode
    private static final Pattern investmentModePattern = Pattern.compile(
    		"<td style=\"width:177px;\">\\s+&nbsp; 投资方式</td>\\s+<td style=\"width:391px;\">(.*?)</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage + suffix;
			final Page page = getPage(url, totalCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			log.info(page.getTotalCount()+"");
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
			log.error("获取invest投资需求网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
	@Override
	public FetchData parseObject(String content, String pageUrl) throws Exception {
		log.info(content);
		FetchData data = new FetchData();
		data.setPageUrl(pageUrl);
		data.setProjectType("投资需求");
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
		Matcher industryMatcher = industryPattern.matcher(content);
		if (industryMatcher.find()) {
			log.info(industryMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setIndustry(industryMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher capitalMatcher = capitalPattern.matcher(content);
		if (capitalMatcher.find()) {
			log.debug(capitalMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher industryCaracteristicsMatcher = industryCaracteristicsPattern.matcher(content);
		if (industryCaracteristicsMatcher.find()) {
			log.debug(industryCaracteristicsMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher locationMatcher = locationPattern.matcher(content);
		if (locationMatcher.find()) {
			log.debug(locationMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setLocation(locationMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher financialRatiosMatcher = financialRatiosPattern.matcher(content);
		if (financialRatiosMatcher.find()) {
			log.debug(financialRatiosMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		Matcher investmentModeMatcher = investmentModePattern.matcher(content);
		if (investmentModeMatcher.find()) {
			log.debug(investmentModeMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		return data;
	}
}
