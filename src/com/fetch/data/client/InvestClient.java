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
public class InvestClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	private final static String domain = "http://www.56invest.com";
	private final static String webSite = "/weiboweixin/list_232_";
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
    
    //提取projectType值
    private static final Pattern projectTypePattern = Pattern.compile(
    		"【(.*?)】",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取listingTime
    private static final Pattern listingTimePattern = Pattern.compile(
    		"<small>时间:</small>(.*?)<small>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取expected industry
    private static final Pattern expectedIndustryPattern = Pattern.compile(
    		"<td style=\".*?\">.*?期望标的公司所属行业.*?</td>\\s+<td style=\".*?\">(.*?)</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取expectedCapital
    private static final Pattern expectedCapitalPattern = Pattern.compile(
    		"<td style=\".*?\">.*?期望标的.*?资产规模.*?</td>\\s+<td style=\".*?\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取industryCaracteristics
    private static final Pattern industryCaracteristicsPattern = Pattern.compile(
    		"<td style=\".*?\">.*?期望拟购入.*?标的行业特性.*?</td>\\s+<td style=\".*?\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取expectedLocation值
    private static final Pattern expectedLocationPattern = Pattern.compile(
    		"<td style=\".*?\">.*?期望地域.*?</td>\\s+<td style=\".*?\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取expectedFinancialRatios值
    private static final Pattern expectedFinancialRatiosPattern = Pattern.compile(
    		"<td style=\".*?\">.*?期望标的财务经营状况.*?</td>\\s+<td style=\".*?\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取investmentMode
    private static final Pattern investmentModePattern = Pattern.compile(
    		"<td style=\".*?\">.*?投资方式.*?</td>\\s+<td style=\".*?\">(.*?)</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
    //提取name值
    private static final Pattern namePattern2 = Pattern.compile(
    		"<div class=\"con_tit\"><h2>(.*?)</h2>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取listingTime
    private static final Pattern listingTimePattern2 = Pattern.compile(
    		"<small>时间:</small>(.*?)<small>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取businessDescription
    private static final Pattern businessDescriptionPattern = Pattern.compile(
    		"<td style=\".*?\">((.*?主营业务及优势：)|(.*?主营业务.*?))</td>\\s+<td style=\".*?\">(.*?)</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取location值
    private static final Pattern locationPattern = Pattern.compile(
    		"<td style=\".*?\">.*?所在地区：</td>\\s+<td style=\".*?\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<td style=\".*?\">.*?企业基本状况.*?</td>\\s+<td style=\".*?\">(.*?)</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取financialRatios值
    private static final Pattern financialRatiosPattern = Pattern.compile(
    		"<td style=\".*?\">.*?财务经营状况.*?</td>\\s+<td style=\".*?\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取company Properites
    private static final Pattern companyProperitesPattern = Pattern.compile(
    		"<td style=\".*?\">.*?企业性质.*?</td>\\s+<td style=\".*?\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取financingMode
    private static final Pattern financingModePattern = Pattern.compile(
    		"<td style=\".*?\">.*?融资方式.*?</td>\\s+<td style=\".*?\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage + suffix;
			final Page page = getPage(url, totalCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			log.debug(page.getTotalCount()+"");
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
		log.debug(content);
		FetchData data = new FetchData();
		data.setPageUrl(pageUrl);
		if (pageUrl.equals("http://www.56invest.com/plus/view.php?aid=1688")) {
			return null;
		}
		Matcher nameMatcher = namePattern.matcher(content);
		if (nameMatcher.find()) {
			String name = nameMatcher.group(1);
			log.debug(name);
			data.setName(name);
			Matcher projectTypeMatcher = projectTypePattern.matcher(name);
			if (projectTypeMatcher.find()) {
				data.setProjectType(projectTypeMatcher.group(1));
			}
		}
		Matcher nameMatcher2 = namePattern2.matcher(content);
		if (nameMatcher2.find()) {
			String name = nameMatcher2.group(1);
			log.debug(name);
			data.setName(name);
			Matcher projectTypeMatcher = projectTypePattern.matcher(name);
			if (projectTypeMatcher.find()) {
				data.setProjectType(projectTypeMatcher.group(1));
			}
		}
		Matcher listingTimeMatcher = listingTimePattern.matcher(content);
		if (listingTimeMatcher.find()) {
			log.debug(listingTimeMatcher.group(1));
			data.setListingTime(listingTimeMatcher.group(1));
		}
		Matcher listingTimeMatcher2 = listingTimePattern2.matcher(content);
		if (listingTimeMatcher2.find()) {
			log.debug(listingTimeMatcher2.group(1));
			data.setListingTime(listingTimeMatcher2.group(1));
		}
		Matcher expectedIndustryMatcher = expectedIndustryPattern.matcher(content);
		if (expectedIndustryMatcher.find()) {
			log.debug(expectedIndustryMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setExpectedIndustry(expectedIndustryMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher expectedCapitalMatcher = expectedCapitalPattern.matcher(content);
		if (expectedCapitalMatcher.find()) {
			log.debug(expectedCapitalMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setExpectedCapital(expectedCapitalMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher industryCaracteristicsMatcher = industryCaracteristicsPattern.matcher(content);
		if (industryCaracteristicsMatcher.find()) {
			log.debug(industryCaracteristicsMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setExpectedIndustryCaracteristics(industryCaracteristicsMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher expectedLocationMatcher = expectedLocationPattern.matcher(content);
		if (expectedLocationMatcher.find()) {
			log.debug(expectedLocationMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setExpectedLocation(expectedLocationMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher expectedFinancialRatiosMatcher = expectedFinancialRatiosPattern.matcher(content);
		if (expectedFinancialRatiosMatcher.find()) {
			log.debug(expectedFinancialRatiosMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setExpectedFinancialRatios(expectedFinancialRatiosMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		Matcher investmentModeMatcher = investmentModePattern.matcher(content);
		if (investmentModeMatcher.find()) {
			log.debug(investmentModeMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setInvestmentMode(investmentModeMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		
		Matcher businessDescriptionMatcher = businessDescriptionPattern.matcher(content);
		if (businessDescriptionMatcher.find()) {
			log.debug(businessDescriptionMatcher.group(4).replaceAll("&mdash;", "-").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setBusinessDescription(businessDescriptionMatcher.group(4).replaceAll("&mdash;", "-").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
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
			data.setFinancialRatios(financialRatiosMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		Matcher companyProperitesMatcher = companyProperitesPattern.matcher(content);
		if (companyProperitesMatcher.find()) {
			log.debug(companyProperitesMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setCompanyProperites(companyProperitesMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher financingModeMatcher = financingModePattern.matcher(content);
		if (financingModeMatcher.find()) {
			log.debug(financingModeMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setFinancingMode(financingModeMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		return data;
	}
}
