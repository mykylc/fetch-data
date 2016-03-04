package com.fetch.data.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.FetchData;
import com.fetch.data.domain.Page;
import com.fetch.data.tools.FetchDataThreadPool;
import com.fetch.data.tools.HttpUtils;

/**
 * afdb
 * @author 
 *
 */
public class AfdbClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	private final static String domain = "http://www.afdb.org";
	private final static String webSite = "/en/projects-and-operations/project-portfolio/";
	private static int pageSize = 20;
	private final String charset = "UTF-8";
	
	private Map<String, String> map = new HashMap<String, String>();
	
	// 提取总条数的正则表达式
    private static final Pattern totalCountPattern = Pattern.compile("<div class=\"summary\"><div class=\"pagination\"><p>Displaying results <span>.*</span> to <span>.*</span> out of <span>(.*?)</span></p><ul>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile(
    		"</td> <td> <a href=\"(.*?)\" >.*?</a><br /> <p class=\"category\">", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取country industry值
    private static final Pattern countryIndustryPattern = Pattern.compile(
    		"<p class=\"category\"><span class=\"label\">Categories:</span> <span class=\"categories\">(.*?)</span></p> </td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<div class=\"csc-header\"><h1>(.*?)</h1></div>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Approval date
    private static final Pattern approvalDatePattern = Pattern.compile(
    		"<li>Approval date: <strong>(.*?)</strong></li>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取listing_date值
    private static final Pattern listingDatePattern = Pattern.compile(
    		"<li>Start date: <strong>(.*?)</strong></li>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取end_date值
    private static final Pattern endDatePattern = Pattern.compile(
    		"<li>Appraisal Date: <strong>(.*?)</strong></li>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取status
    private static final Pattern statusPattern = Pattern.compile(
    		"<li>Status: <strong>(.*?)<span class=\"hide\">.*?</span></strong></li>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Agency
    private static final Pattern agencyPattern = Pattern.compile(
    		"<li>Implementing Agency: <strong>(.*?)</strong></li>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取Location值
    private static final Pattern locationPattern = Pattern.compile(
    		"<li>Location: <strong>(.*?)</strong></li>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<div class=\"divider\"><hr /></div> <div class=\"csc-header\"><h2>Description</h2></div>(.*?)<div class=\"divider\"><hr /></div>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite;
			final Page page = getPage(url, totalCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			//log.debug(page.getPageCount()+"");
			getUrlListByPage2(url, urlListPattern, domain, charset);
			while (page.hasNextPage()) {
				semp.acquire();
				int next = page.getNextPage();
				final int nextPage = next - 1;
				page.setCurrentPage(next);
				FetchDataThreadPool.exec.execute(new Runnable() {
					@Override
					public void run() {
						try {
							String urlPage = domain + webSite + nextPage +"/";
							getUrlListByPage2(urlPage, urlListPattern, domain, charset);
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
			log.error("获取afdb网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
    /**
	 * 根据每页获取URL列表
	 * @param url
	 * @param urlListPattern
	 * @param domain
	 * @return void
	 * @throws Exception
	 */
	public void getUrlListByPage2(String url, Pattern urlListPattern, String domain, String charset) throws Exception{
		try {
			Set<String> setList = new HashSet<String>();
			List<String> urlList = new ArrayList<String>();
			HttpUtils httpUtils = new HttpUtils(url, charset);
			String result = httpUtils.execute();
			if (result==null) {
				return;
			}
			Matcher urlListmatcher = urlListPattern.matcher(result);
            while (urlListmatcher.find()) {
            	String hrefUrl = urlListmatcher.group(1);
            	if (hrefUrl.startsWith("http://") && setList.add(hrefUrl)) {
            		urlList.add(hrefUrl);
				} else if(setList.add(hrefUrl)){
					hrefUrl = domain + hrefUrl;
					urlList.add(hrefUrl);
					//log.debug(hrefUrl);
				}
            }
            List<String> countryIndustryList = new ArrayList<String>();
            Matcher countryIndustryMatcher = countryIndustryPattern.matcher(result);
            while (countryIndustryMatcher.find()) {
            	String countryIndustry = countryIndustryMatcher.group(1);
				//log.debug(countryIndustry.replaceAll("<[^>]+>", "").replaceAll("&#32;", "").replaceAll("&amp;", "&"));
				countryIndustryList.add(countryIndustry.replaceAll("<[^>]+>", "").replaceAll("&#32;", "").replaceAll("&amp;", "&"));
            }
            int urlSize = urlList.size();
            int countryIndustrySize = countryIndustryList.size();
			for (int i=0; i < urlSize; i++) {
            	if (urlSize==countryIndustrySize) {
            		map.put(urlList.get(i), countryIndustryList.get(i));
				} else {
					if (i < countryIndustrySize) {
						map.put(urlList.get(i), countryIndustryList.get(i));
					} else {
						map.put(urlList.get(i), "");
					}
				}
			}
            handlerData(urlList, charset);
		} catch (Exception e) {
			log.error(String.format("[AbstractClient.getUrlListByPage] url=%s; 根据每页获取URL列表报错：%s", url, e.getMessage()), e);
			throw e;
		}
	}
    
	@Override
	public FetchData parseObject(String content, String pageUrl) throws Exception {
		FetchData data = new FetchData();
		data.setPageUrl(pageUrl);
		String[] countryIndustry = map.get(pageUrl).split(",");
		StringBuffer sb = new StringBuffer();
		for (int i = 1; i < countryIndustry.length; i++) {
			sb.append(",<"+countryIndustry[i]+">");
		}
		data.setIndustry(sb.toString().replaceFirst(",", ""));
		data.setCountry(countryIndustry[0]);
		
		log.debug(content);
		Matcher nameMatcher = namePattern.matcher(content);
		if (nameMatcher.find()) {
			log.debug(nameMatcher.group(1));
			data.setName(nameMatcher.group(1));
		}
		
		Matcher approvalDateMatcher = approvalDatePattern.matcher(content);
		if (approvalDateMatcher.find()) {
			log.debug(approvalDateMatcher.group(1));
			data.setApprovalDate(approvalDateMatcher.group(1));
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
		
		Matcher statusMatcher = statusPattern.matcher(content);
		if (statusMatcher.find()) {
			log.debug(statusMatcher.group(1));
			data.setStatus(statusMatcher.group(1));
		}
		
		Matcher agencyMatcher = agencyPattern.matcher(content);
		if (agencyMatcher.find()) {
			log.debug(agencyMatcher.group(1));
			data.setAgency(agencyMatcher.group(1));
		}
		
		
		Matcher locationMatcher = locationPattern.matcher(content);
		if (locationMatcher.find()) {
			log.debug(locationMatcher.group(1));
			data.setLocation(locationMatcher.group(1));
		}
		
		Matcher descriptionMatcher = descriptionPattern.matcher(content);
		if (descriptionMatcher.find()) {
			log.debug(descriptionMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		return data;
	}
}
