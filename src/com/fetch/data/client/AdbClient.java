package com.fetch.data.client;

import java.util.ArrayList;
import java.util.List;
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
 * adb
 * @author 
 *
 */
public class AdbClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	private final static String domain = "";
	private final static String webSite = "http://www.adb.org/projects/search?keywords=&page=";
	private static int pageSize = 25;
	private static int currentPage = 0;
	private final String charset = "UTF-8";
	
	// 提取总条数的正则表达式
    private static final Pattern totalCountPattern = Pattern.compile(
    		"<li class=\"pager-last last\"><a href=\"/projects/search\\?keywords=&amp;page=(.*?)\"></a></li>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile(
    		"<td >            <a href=\"(.*?)\"><span class=\"meta\">.*?<span class=\"products\">", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<tr><td>Project Name</td><td>(.*?)</td></tr>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Country
    private static final Pattern countryPattern = Pattern.compile(
    		"<tr><td>Country</td><td>(.*?)</td></tr>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取status
    private static final Pattern statusPattern = Pattern.compile(
    		"<tr><td>Project Status</td><td>(.*?)</td></tr>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取projectType值
    private static final Pattern projectTypePattern = Pattern.compile(
    		"<tr><td>Project Type / Modality of Assistance</td><td>(.*?)</td></tr>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取source of funding值
    private static final Pattern sourceOfFundingPattern = Pattern.compile(
    		"<tr><td>Technical Assistance Special Fund</td><td align=\"right\">(.*?)</td></tr>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取Approval date
    private static final Pattern approvalDatePattern = Pattern.compile(
    		"<strong>Approval Date</strong>\\s+<ul class=\"field-items inline\">\\s+<li class=\"field-item\"><span class=\"date-display-single\" property=\"dc\\:date\" datatype=\"xsd\\:dateTime\" content=\".*?\">(.*?)</span></li>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取industry
    private static final Pattern industryPattern = Pattern.compile(
    		"<tr><td>Sector / Subsector</td><td>(.*?)</td></tr>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<tr><td>Description</td><td>(.*?)</td></tr>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage;
			final Page page = getPage2(url, totalCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			log.debug(page.getPageCount()+"");
			getUrlListByPage(url, urlListPattern, domain, charset);
			while (page.hasNextPage()) {
				semp.acquire();
				int next = page.getNextPage();
				final int nextPage = next - 1;
				page.setCurrentPage(next);
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
				if (semp.availablePermits()==20) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("获取adb网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
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
			log.debug(result);
			Matcher pageCountMatcher = pageCountPattern.matcher(result);
            while (pageCountMatcher.find()) {
                String pageCount = pageCountMatcher.group(1);
                Page page = new Page();
                page.setPageSize(pageSize);
                page.setPageCount(Integer.parseInt(pageCount)+1);
                return page;
            }
		} catch (Exception e) {
			log.error(String.format("[AdbClient.getPage] url=%s; 获取page对象报错：%s", url, e.getMessage()), e);
			throw e;
		}
		return null;
	} 
    
	/**
	 * 根据每页获取URL列表
	 * @param url
	 * @param urlListPattern
	 * @param domain
	 * @return void
	 * @throws Exception
	 */
	public void getUrlListByPage(String url, Pattern urlListPattern, String domain, String charset) throws Exception{
		try {
			List<String> urlList = new ArrayList<String>();
			HttpUtils httpUtils = new HttpUtils(url, charset);
			String result = httpUtils.execute();
			if (result==null) {
				return;
			}
			Matcher urlListmatcher = urlListPattern.matcher(result);
            while (urlListmatcher.find()) {
            	String hrefUrl = urlListmatcher.group(1);
            	if (hrefUrl.startsWith("http://")) {
            		urlList.add(hrefUrl+"#project-pds");
				}
            }
            handlerData(urlList, charset);
		} catch (Exception e) {
			log.error(String.format("[AdbClient.getUrlListByPage] url=%s; 根据每页获取URL列表报错：%s", url, e.getMessage()), e);
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
			log.debug(nameMatcher.group(1));
			data.setName(nameMatcher.group(1));
		}
		Matcher countryMatcher = countryPattern.matcher(content);
		if (countryMatcher.find()) {
			log.debug(countryMatcher.group(1));
			data.setCountry(countryMatcher.group(1));
		}
		Matcher statusMatcher = statusPattern.matcher(content);
		if (statusMatcher.find()) {
			log.debug(statusMatcher.group(1));
			data.setStatus(statusMatcher.group(1));
		}
		
		Matcher projectTypeMatcher = projectTypePattern.matcher(content);
		if (projectTypeMatcher.find()) {
			log.debug(projectTypeMatcher.group(1));
			data.setProjectType(projectTypeMatcher.group(1));
		}
		
		Matcher sourceOfFundingMatcher = sourceOfFundingPattern.matcher(content);
		if (sourceOfFundingMatcher.find()) {
			log.debug(sourceOfFundingMatcher.group(1));
			data.setSourceOfFunding(sourceOfFundingMatcher.group(1));
		}
		
		Matcher approvalDateMatcher = approvalDatePattern.matcher(content);
		if (approvalDateMatcher.find()) {
			log.debug(approvalDateMatcher.group(1));
			data.setApprovalDate(approvalDateMatcher.group(1));
		}
		
		
		Matcher industryMatcher = industryPattern.matcher(content);
		if (industryMatcher.find()) {
			log.debug(industryMatcher.group(1).replaceAll("<p>", ",<").replaceAll("</p>", ">").replaceAll("<[^<^>]+>", "").replaceFirst(",", ""));
			data.setIndustry(industryMatcher.group(1).replaceAll("<p>", ",<").replaceAll("</p>", ">").replaceAll("<[^<^>]+>", "").replaceFirst(",", ""));
		}
		
		Matcher descriptionMatcher = descriptionPattern.matcher(content);
		if (descriptionMatcher.find()) {
			log.debug(descriptionMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		return data;
	}
}
