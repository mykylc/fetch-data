package com.fetch.data.client;

import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.FetchData;

/**
 * 天津产权交易中心
 * @author 
 *
 */
public abstract class TprtcClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(10);
	
	public static int pageSize = 25;
	public static int currentPage = 1;
	public final String charset = "gb2312";
	
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<td width=\"11%\" class=\"xmtd1\">\\s*标的名称\\s*</td>\\s*<td colspan=\"3\" class=\"xmtd2\">\\s*(.*?)\\s*</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取price
    private static final Pattern pricePattern = Pattern.compile(
    		"<td width=\"11%\" class=\"xmtd1\">\\s*挂牌价格\\s*</td>\\s*<td width=\"40%\" class=\"xmtd2\">\\s*(.*?)\\s*</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取listing_date end_date值
    private static final Pattern datePattern = Pattern.compile(
    		"<td class=\"xmtd1\">\\s*挂牌起始日期\\s*</td>\\s*<td class=\"xmtd2\">\\s*(.*?)\\s*</td>"
    		+ "\\s*<td class=\"xmtd1\">\\s*挂牌期满日期\\s*</td>\\s*<td class=\"xmtd2\">\\s*(.*?)\\s*</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取location industry值
    private static final Pattern locationIndustryPattern = Pattern.compile(
    		"<td class=\"xmtd1\">\\s*标的所在地区\\s*</td>\\s*<td class=\"xmtd2\">\\s*(.*?)\\s*</td>\\s*<td class=\"xmtd1\">\\s*标的所属行业\\s*</td>\\s*<td class=\"xmtd2\">\\s*(.*?)\\s*</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
	@Override
	public FetchData parseObject(String content, String pageUrl) throws Exception {
		FetchData data = new FetchData();
		data.setPageUrl(pageUrl);
		data.setCountry("中国");
		Matcher nameMatcher = namePattern.matcher(content);
		if (nameMatcher.find()) {
			log.debug(nameMatcher.group(1).replaceAll("&nbsp;", ""));
      		data.setName(nameMatcher.group(1).replaceAll("&nbsp;", ""));
		}
		Matcher priceMatcher = pricePattern.matcher(content);
		if (priceMatcher.find()) {
			log.debug(priceMatcher.group(1));
			data.setPrice(priceMatcher.group(1));
		}
		Matcher dateMatcher = datePattern.matcher(content);
		if (dateMatcher.find()) {
			log.debug(dateMatcher.group(1).replaceAll("&nbsp;", "")+"="+dateMatcher.group(2).replaceAll("&nbsp;", "").replaceAll("\\s*", ""));
			data.setListingDate(dateMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("\\s*", ""));
			data.setEndDate(dateMatcher.group(2).replaceAll("&nbsp;", "").replaceAll("\\s*", ""));
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
