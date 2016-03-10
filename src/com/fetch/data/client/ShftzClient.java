package com.fetch.data.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fetch.data.domain.FetchData;
import com.fetch.data.domain.Page;
import com.fetch.data.tools.FetchDataThreadPool;
import com.fetch.data.tools.HttpUtils;

/**
 * shftz
 * @author 
 *
 */
public class ShftzClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	private final static String domain = "http://www.shftz.cn";
	private final static String webSite = "/Projects/GetProjectList";
	private final static String prefix = "http://www.shftz.cn/Projects/Detail?p=";
	private static int pageSize = 5;
	private static int currentPage = 1;
	private final String charset = "utf-8";
	
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<div class=\"articleheader\">\\s+<h2>(.*?)</h2>\\s+<div>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取listingTime
    private static final Pattern listingTimePattern = Pattern.compile(
    		"<div class=\"articleheader\">\\s+<h2>.*?</h2>\\s+<div>\\s+<span>(.*?)</span>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取country
    private static final Pattern countryPattern = Pattern.compile(
    		"<p>\\s+地&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;区：(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取industry
    private static final Pattern industryPattern = Pattern.compile(
    		"<p>\\s+行&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;业：(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern projectTypePattern = Pattern.compile(
    		"<p>\\s+项目类型：(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取projectCapitals
    private static final Pattern projectCapitalsPattern = Pattern.compile(
    		"<p>\\s+项目总金额：(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取price值
    private static final Pattern pricePattern = Pattern.compile(
    		"<p>\\s+拟吸引投资总金额：(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取endDate
    private static final Pattern endDatePattern = Pattern.compile(
    		"<p>\\s+有&nbsp;&nbsp;效&nbsp;&nbsp;期：(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<div class=\"img_title mt15\">\\s+<strong>项目内容描述</strong></div>\\s+<div class=\"txtContext\">(.*?)</div>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取projectEnvironment值
    private static final Pattern projectEnvironmentPattern = Pattern.compile(
    		"<div class=\"img_title mt15\">\\s+<strong>投资环境</strong></div>\\s+<div class=\"txtContext\">(.*?)</div>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取descOfInvestorConditions
    private static final Pattern descOfInvestorConditionsPattern = Pattern.compile(
    		"<div class=\"img_title mt15\">\\s+<strong>投资者条件</strong></div>\\s+<div class=\"txtContext\">(.*?)</div>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    @Override
	public void fetchData() throws Exception{
		try {
			final String url = domain + webSite ;
			final Page page = getPageCount(url);
			if (page == null) {
				return;
			}
			log.debug(page.getPageCount()+"");
			getUrlListByPage(url, currentPage);
			while (page.hasNextPage()) {
				semp.acquire();
				final int nextPage = page.getNextPage();
				page.setCurrentPage(nextPage);
				FetchDataThreadPool.exec.execute(new Runnable() {
					@Override
					public void run() {
						try {
							getUrlListByPage(url, nextPage);
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
			log.error("获取shftz网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
    public Page getPageCount(String url) throws Exception {
    	try {
	    	String result = getJsonData(url, "1");
	    	if (result==null) {
				return null;
			}
	    	JSONObject jsonObject = JSONObject.parseObject(result);
	        Page page = new Page();
			page.setPageCount(Integer.parseInt(jsonObject.getString("PageCount")));
			page.setPageSize(pageSize);
	        return page;
		} catch (Exception e) {
			log.error(String.format("[ShftzClient.getPage] url=%s; 获取page对象报错：%s", url, e.getMessage()), e);
			throw e;
		}     
    }
    
    private String getJsonData(String url, String pageNum){
    	Map<String,String> header = new LinkedHashMap<String,String>();
    	Map<String,String> params = new LinkedHashMap<String,String>();
    	header.put("Content-Type", "application/x-www-form-urlencoded");
    	params.put("trade", "行业");
    	params.put("page", pageNum);
    	params.put("m", "0");
    	params.put("manycitem", "");
    	params.put("key", "");
    	HttpUtils httpUtils = new HttpUtils(url, charset, "POST",header,params);
		String result = httpUtils.execute();
		return result;
    }
    
    /**
	 * 根据每页获取URL列表
	 * @param url
	 * @param urlListPattern
	 * @param domain
	 * @return void
	 * @throws Exception
	 */
	public void getUrlListByPage(String url, Integer pageNum) throws Exception{
		try {
			List<String> urlList = new ArrayList<String>();
			String result = getJsonData(url, pageNum.toString());
			if (result==null) {
				return;
			}
			log.debug(result);
			JSONObject jsonObject = JSONObject.parseObject(result);
			String data = jsonObject.getString("Data");
			
			JSONArray jsonArray = JSONArray.parseArray(data);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject json = (JSONObject)jsonArray.get(i);
				String hrefUrl = prefix + json.getString("id") + "&t=" + json.getString("trade");
				urlList.add(hrefUrl);
			}
            handlerData(urlList, charset);
		} catch (Exception e) {
			log.error(String.format("[ShftzClient.getUrlListByPage] url=%s; 根据每页获取URL列表报错：%s", url, e.getMessage()), e);
			throw e;
		}
	}
    
	@Override
	public FetchData parseObject(String content, String pageUrl) throws Exception {
		log.debug(content);
		FetchData data = new FetchData();
		data.setPageUrl(pageUrl);
		Matcher nameMatcher = namePattern.matcher(content);
		if (nameMatcher.find()) {
			log.debug(nameMatcher.group(1).replaceAll("\\s+", ""));
			data.setName(nameMatcher.group(1).replaceAll("\\s+", ""));
		}
		Matcher listingTimeMatcher = listingTimePattern.matcher(content);
		if (listingTimeMatcher.find()) {
			log.debug(listingTimeMatcher.group(1).replaceAll("\\s+", ""));
			data.setListingTime(listingTimeMatcher.group(1).replaceAll("\\s+", ""));
		}
		Matcher countryMatcher = countryPattern.matcher(content);
		if (countryMatcher.find()) {
			log.debug(countryMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
			data.setCountry(countryMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
		}
		
		Matcher industryMatcher = industryPattern.matcher(content);
		if (industryMatcher.find()) {
			log.debug(industryMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
			data.setIndustry(industryMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
		}
		Matcher projectTypeMatcher = projectTypePattern.matcher(content);
		if (projectTypeMatcher.find()) {
			log.debug(projectTypeMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
			data.setProjectType(projectTypeMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
		}
		
		Matcher projectCapitalsMatcher = projectCapitalsPattern.matcher(content);
		if (projectCapitalsMatcher.find()) {
			log.debug(projectCapitalsMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
			data.setProjectCapitals(projectCapitalsMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
		}
		Matcher priceMatcher = pricePattern.matcher(content);
		if (priceMatcher.find()) {
			log.debug(priceMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
			data.setPrice(priceMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
		}
		Matcher endDateMatcher = endDatePattern.matcher(content);
		if (endDateMatcher.find()) {
			log.debug(endDateMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
			data.setEndDate(endDateMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
		}
		Matcher descriptionMatcher = descriptionPattern.matcher(content);
		if (descriptionMatcher.find()) {
			String desc = descriptionMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "");
			log.debug(desc);
			data.setDescription(desc);
		}
		Matcher projectEnvironmentMatcher = projectEnvironmentPattern.matcher(content);
		if (projectEnvironmentMatcher.find()) {
			log.debug(projectEnvironmentMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setProjectEnvironment(projectEnvironmentMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		Matcher descOfInvestorConditionsMatcher = descOfInvestorConditionsPattern.matcher(content);
		if (descOfInvestorConditionsMatcher.find()) {
			log.debug(descOfInvestorConditionsMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescOfInvestorConditions(descOfInvestorConditionsMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		return data;
	}
}
