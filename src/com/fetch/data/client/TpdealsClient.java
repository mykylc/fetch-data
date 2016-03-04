package com.fetch.data.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.FetchData;
import com.fetch.data.domain.Page;
import com.fetch.data.tools.HttpUtils;

/**
 * Tpdeals
 * @author 
 *
 */
public abstract class TpdealsClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<h1>(.*?)</h1>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取projectType
    private static final Pattern projectTypePattern = Pattern.compile(
    		"<h5><span class=\"blue bold\">Category:</span>(.*?)</h5>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取industry
    private static final Pattern industryPattern = Pattern.compile(
    		"<h5><span class=\"blue bold\">Sector:</span>(.*?)</h5>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取location值
    private static final Pattern locationPattern = Pattern.compile(
    		"<h5><span class=\"blue bold\">Location:</span>(.*?)</h5>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取turnover值
    private static final Pattern turnoverPattern = Pattern.compile(
    		"<h5><span class=\"blue bold\">Turnover:</span> &#163;10.0m</h5>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<div class=\"rte\">(.*?)</div>    <div class=\"links\">", 
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
			//log.info(result);
			Matcher pageCountMatcher = pageCountPattern.matcher(result);
            while (pageCountMatcher.find()) {
                String[] pageCounts = pageCountMatcher.group(1).split("<li>");
                String pageCountStr = pageCounts[pageCounts.length-1];
                Page page = new Page();
                page.setPageSize(pageSize);
                page.setPageCount(Integer.parseInt(pageCountStr.replaceAll("<[^>]+>", "")));
                return page;
            }
		} catch (Exception e) {
			log.error(String.format("[TpdealsClient.getPage] url=%s; 获取page对象报错：%s", url, e.getMessage()), e);
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
		Matcher projectTypeMatcher = projectTypePattern.matcher(content);
		if (projectTypeMatcher.find()) {
			log.debug(projectTypeMatcher.group(1));
			data.setProjectType(projectTypeMatcher.group(1));
		}
		Matcher industryMatcher = industryPattern.matcher(content);
		if (industryMatcher.find()) {
			log.debug(industryMatcher.group(1).replaceAll("&amp;", "&"));
			data.setIndustry(industryMatcher.group(1).replaceAll("&amp;", "&"));
		}
		Matcher locationMatcher = locationPattern.matcher(content);
		if (locationMatcher.find()) {
			log.debug(locationMatcher.group(1));
			data.setLocation(locationMatcher.group(1));
		}
		Matcher turnoverMatcher = turnoverPattern.matcher(content);
		if (turnoverMatcher.find()) {
			log.debug(turnoverMatcher.group(1).replaceAll("&#163;", ""));
			data.setTurnover(turnoverMatcher.group(1).replaceAll("&#163;", ""));
		}
		Matcher descriptionMatcher = descriptionPattern.matcher(content);
		if (descriptionMatcher.find()) {
			log.debug(descriptionMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		return data;
	}
}
