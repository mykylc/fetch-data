package com.fetch.data.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.FetchData;
import com.fetch.data.domain.Page;
import com.fetch.data.tools.HttpUtils;

/**
 * fdi
 * @author 
 *
 */
public abstract class FdiClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	public final static String domain = "http://project.fdi.gov.cn";
	public static int pageSize = 25;
	public static int currentPage = 1;
	public final String charset = "gb2312";
	
	// 提取总条数的正则表达式
	public static final Pattern pageCountPattern = Pattern.compile(
    		"<FONT style=\"FONT-SIZE: 11pt\" face=Arial>.*?&nbsp;results&nbsp;&nbsp;&nbsp;Page&nbsp;1&nbsp;of&nbsp;(.*?)&nbsp;&nbsp;<a href=", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
	public static final Pattern urlListPattern = Pattern.compile(
    		"<TD class=zsyzlb-fmk1 width=340><A style=\"TEXT-DECORATION: none\" href=\"(.*?)\"><FONT color=#333333>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<TBODY><TR><TD class=xmnrmk2>(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取listingDate
    private static final Pattern listingDatePattern = Pattern.compile(
    		"<TR><TD class=xmnrmk1>Date</TD><TD class=xmnrmk2><SCRIPT>\\s+document.write\\(formatDate\\('(.*?)'\\)\\)\\s+</SCRIPT></TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取projectType值
    private static final Pattern projectTypePattern = Pattern.compile(
    		"<TR><TD class=xmnrmk1>Project Type</TD><TD class=xmnrmk2>(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Investment Mode
    private static final Pattern investmentModePattern = Pattern.compile(
    		"<TD class=xmnrmk1><BR>Investment Mode</TD><TD class=xmnrmk2>(.*?)</TD>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取industry
    private static final Pattern industryPattern = Pattern.compile(
    		"<TD class=xmnrmk1>Industry</TD><TD class=xmnrmk2>(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取Location值
    private static final Pattern locationPattern = Pattern.compile(
    		"<TD class=xmnrmk1>Location</TD><TD class=xmnrmk2>(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取Project Advantages
    private static final Pattern projectAdvantagesPattern = Pattern.compile(
    		"<TD class=xmnrmk1>Project Advantages</TD><TD class=xmnrmk2>(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取Project validity period
    private static final Pattern validityPeriodPattern = Pattern.compile(
    		"<TD class=xmnrmk1>Project validity period</TD><TD class=xmnrmk2>(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Project properties
    private static final Pattern projectPropertiesPattern = Pattern.compile(
    		"<TD class=xmnrmk1>Project properties</TD><TD class=xmnrmk2>(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Project capitals
    private static final Pattern projectCapitalsPattern = Pattern.compile(
    		"<TD class=xmnrmk1>Total amount of project capitals</TD><TD class=xmnrmk2>(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取price
    private static final Pattern pricePattern = Pattern.compile(
    		"<TD class=xmnrmk1>Total amount of investment to be attracted</TD><TD class=xmnrmk2>(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Expected annual revenue
    private static final Pattern expectedAnnualRevenuePattern = Pattern.compile(
    		"<TD class=xmnrmk1>Expected annual sales revenue</TD><TD class=xmnrmk2>(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Expected payback period
    private static final Pattern expectedPaybackPeriodPattern = Pattern.compile(
    		"<TD class=xmnrmk1>Expected investment payback period</TD><TD class=xmnrmk2>(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Description of environment protection
    private static final Pattern protectionPattern = Pattern.compile(
    		"<TD class=xmnrmk1>Description of environment protection</TD><TD class=xmnrmk2>(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Description of investor conditions
    private static final Pattern conditionsPattern = Pattern.compile(
    		"<TD class=xmnrmk1>Description of investor conditions</TD><TD class=xmnrmk2>(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<TD class=xmnrmk1>Description of project contents</TD><TD class=xmnrmk2>(.*?)</TD>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    /**
	 * 获取page对象
	 * @param url
	 * @param pageCountPattern
	 * @param pageSize
	 * @return Page
	 * @throws Exception
	 */
	public Page getPage2(String url, Pattern pageCountPattern, int pageSize, String charset) throws Exception{
		try {
			HttpUtils httpUtils = new HttpUtils(url, charset);
			String result = httpUtils.execute();
			if (result==null) {
				return null;
			}
			//log.debug(result);
			Matcher pageCountMatcher = pageCountPattern.matcher(result);
            while (pageCountMatcher.find()) {
                String pageCount = pageCountMatcher.group(1);
                Page page = new Page();
                page.setPageSize(pageSize);
                page.setPageCount(Integer.parseInt(pageCount));
                return page;
            }
		} catch (Exception e) {
			log.error(String.format("[FdiClient.getPage] url=%s; 获取page对象报错：%s", url, e.getMessage()), e);
			throw e;
		}
		return null;
	} 
    
	@Override
	public FetchData parseObject(String content, String pageUrl) throws Exception {
		log.info(content);
		FetchData data = new FetchData();
		data.setPageUrl(pageUrl);
		Matcher nameMatcher = namePattern.matcher(content);
		if (nameMatcher.find()) {
			log.debug(nameMatcher.group(1));
			data.setName(nameMatcher.group(1));
		}
		Matcher listingDateMatcher = listingDatePattern.matcher(content);
		if (listingDateMatcher.find()) {
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//小写的mm表示的是分钟  
			Date date = sdf.parse(listingDateMatcher.group(1));
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			log.info(sdf.format(date));
			data.setListingDate(sdf.format(date));
		}
		Matcher projectTypeMatcher = projectTypePattern.matcher(content);
		if (projectTypeMatcher.find()) {
			log.debug(projectTypeMatcher.group(1));
			data.setProjectType(projectTypeMatcher.group(1));
		}
		
		Matcher investmentMatcher = investmentModePattern.matcher(content);
		if (investmentMatcher.find()) {
			log.debug(investmentMatcher.group(1).replaceAll("\\s+", ""));
		}
		Matcher industryMatcher = industryPattern.matcher(content);
		if (industryMatcher.find()) {
			log.debug(industryMatcher.group(1).replaceAll("<[^>]+>", ""));
			data.setIndustry(industryMatcher.group(1).replaceAll("<[^>]+>", ""));
		}
		
		Matcher locationMatcher = locationPattern.matcher(content);
		if (locationMatcher.find()) {
			log.debug(locationMatcher.group(1));
			data.setLocation(locationMatcher.group(1));
		}
		
		Matcher projectAdvantagesMatcher = projectAdvantagesPattern.matcher(content);
		if (projectAdvantagesMatcher.find()) {
			log.debug(projectAdvantagesMatcher.group(1));
		}
		Matcher validityPeriodMatcher = validityPeriodPattern.matcher(content);
		if (validityPeriodMatcher.find()) {
			log.debug(validityPeriodMatcher.group(1));
		}
		Matcher projectPropertiesMatcher = projectPropertiesPattern.matcher(content);
		if (projectPropertiesMatcher.find()) {
			log.debug(projectPropertiesMatcher.group(1));
		}
		Matcher projectCapitalsMatcher = projectCapitalsPattern.matcher(content);
		if (projectCapitalsMatcher.find()) {
			log.debug(projectCapitalsMatcher.group(1).replaceAll("&nbsp;", " "));
		}
		Matcher priceMatcher = pricePattern.matcher(content);
		if (priceMatcher.find()) {
			log.debug(priceMatcher.group(1).replaceAll("&nbsp;", " "));
			data.setPrice(priceMatcher.group(1).replaceAll("&nbsp;", " "));
		}
		
		Matcher expectedAnnualRevenueMatcher = expectedAnnualRevenuePattern.matcher(content);
		if (expectedAnnualRevenueMatcher.find()) {
			log.debug(expectedAnnualRevenueMatcher.group(1).replaceAll("&nbsp;", ""));
		}
		
		Matcher expectedPaybackPeriodMatcher = expectedPaybackPeriodPattern.matcher(content);
		if (expectedPaybackPeriodMatcher.find()) {
			log.debug(expectedPaybackPeriodMatcher.group(1).replaceAll("&nbsp;", ""));
		}
		Matcher protectionMatcher = protectionPattern.matcher(content);
		if (protectionMatcher.find()) {
			log.debug(protectionMatcher.group(1).replaceAll("<[^>]+>", "").replaceAll("&nbsp;", ""));
		}
		Matcher conditionsMatcher = conditionsPattern.matcher(content);
		if (conditionsMatcher.find()) {
			log.debug(conditionsMatcher.group(1).replaceAll("<[^>]+>", "").replaceAll("&nbsp;", ""));
		}
		
		Matcher descriptionMatcher = descriptionPattern.matcher(content);
		if (descriptionMatcher.find()) {
			log.debug(descriptionMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		return data;
	}
}
