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
 * eib
 * @author 
 *
 */
public class EibClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	private final static String domain = "http://www.eib.org";
	private final static String webSite = "/projects/pipeline/index.htm";
	private static int pageSize = 5;
	private static int currentPage = 1;
	private final String charset = "UTF-8";
	private List<String> urlList = new ArrayList<String>();
	
	// 提取总条数的正则表达式
    private static final Pattern totalCountPattern = Pattern.compile(
    		"<td class=\"hidden-phone\">.*?</td>                    <td>                                        <a href=\"(.*?)\">.*?</a>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<h2 lang=\".*?\">(.*?)</h2>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取listingDate
    private static final Pattern listingDatePattern = Pattern.compile(
    		"<li class=\"eventDate\"><span class=\"ui-icon ui-icon-calendar\">&#160;</span>(.*?)</li>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取agency值
    private static final Pattern agencyPattern = Pattern.compile(
    		"<div class=\"pipelineItemTitle\"><strong>(Promoter &#8211; Financial Intermediary|Promoteur &#8211; Interm&#233;diaire Financier)</strong></div>(.*?)</div>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取location值
    private static final Pattern locationPattern = Pattern.compile(
    		"<div class=\"pipelineItemTitle\"><strong>(Localisation|Location)</strong></div><ul class=\"offset1\" lang=\".*?\">(.*?)</ul>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<div class=\"pipelineItemTitle\"><strong>Description</strong></div><div class=\"searchable description offset1\" lang=\".*?\">(.*?)</div>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取industry
    private static final Pattern industryPattern = Pattern.compile(
    		"<div class=\"pipelineItemTitle\"><strong>(Sector|Secteur)\\(s\\)</strong></div><ul class=\"searchable offset1\" lang=\".*?\">(.*?)</ul>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取totalCost值
    private static final Pattern totalCostPattern = Pattern.compile(
    		"<div class=\"pipelineItemTitle\"><strong class=\"totalCost\">Total cost \\(Approximate amount\\)</strong></div><p class=\"searchable totalAmount offset1\">(.*?)</p><div class=\"pipelineItemTitle\">",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取status
    private static final Pattern statusPattern = Pattern.compile(
    		"<div class=\"pipelineItemTitle\"><strong>(Status|Statut)</strong></div><p class=\"offset1\">(.*?)</p>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite;
			final Page page = getAllUrlList(url, totalCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			log.debug(page.getTotalCount()+"");
			handleDataByUrlList(currentPage, charset);
			while (page.hasNextPage()) {
				semp.acquire();
				final int nextPage = page.getNextPage();
				page.setCurrentPage(nextPage);
				FetchDataThreadPool.exec.execute(new Runnable() {
					@Override
					public void run() {
						try {
							handleDataByUrlList(nextPage, charset);
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
			log.error("获取eib网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
    /**
	 * 获取所有的url
	 * @param url
	 * @param urlCountPattern
	 * @param pageSize
	 * @return Page
	 * @throws Exception
	 */
	public Page getAllUrlList(String url, Pattern urlCountPattern, int pageSize, String charset) throws Exception{
		try {
			HttpUtils httpUtils = new HttpUtils(url, charset);
			String result = httpUtils.execute();
			if (result==null) {
				return null;
			}
			log.debug(result);
			Matcher urlCountMatcher = urlCountPattern.matcher(result);
            while (urlCountMatcher.find()) {
                String hrefUrl = urlCountMatcher.group(1);
                if (hrefUrl.startsWith("http://")) {
            		urlList.add(hrefUrl);
            		log.debug(hrefUrl);
				} else {
					urlList.add(domain+hrefUrl);
					log.info(domain+hrefUrl);
				}
            }
            Page page = new Page();
            page.setPageSize(pageSize);
            page.setTotalCount(urlList.size());
            page.setPageCount();
            return page;
		} catch (Exception e) {
			log.error(String.format("[EibClient.getAllUrlList] url=%s; 获取page对象报错：%s", url, e.getMessage()), e);
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
	public void handleDataByUrlList(int pageIndex, String charset) throws Exception{
		try {
			List<String> urls = new ArrayList<String>();
			int size = urlList.size();
			int start = (pageIndex -1) * pageSize;
			int end = pageIndex * pageSize < size ? pageIndex * pageSize : size;
			for (int i = start; i < end; i++) {
				urls.add(urlList.get(i));
			}
            handlerData(urls, charset);
		} catch (Exception e) {
			log.error(String.format("[EibClient.handleDataByUrlList] 根据每页获取URL列表报错：%s", e.getMessage()), e);
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
		Matcher listingDateMatcher = listingDatePattern.matcher(content);
		if (listingDateMatcher.find()) {
			log.debug(listingDateMatcher.group(1));
			data.setListingDate(listingDateMatcher.group(1));
		}
		Matcher agencyMatcher = agencyPattern.matcher(content);
		if (agencyMatcher.find()) {
			log.debug(agencyMatcher.group(2).replaceAll("<[^>]+>", ""));
			data.setAgency(agencyMatcher.group(2).replaceAll("<[^>]+>", ""));
		}
		Matcher locationMatcher = locationPattern.matcher(content);
		if (locationMatcher.find()) {
			log.debug(locationMatcher.group(2).replaceAll("<[^>]+>", ""));
			data.setLocation(locationMatcher.group(2).replaceAll("<[^>]+>", ""));
		}
		
		Matcher descriptionMatcher = descriptionPattern.matcher(content);
		if (descriptionMatcher.find()) {
			log.debug(descriptionMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher industryMatcher = industryPattern.matcher(content);
		if (industryMatcher.find()) {
			log.debug(industryMatcher.group(2).replaceAll("<[^>]+>", ""));
			data.setIndustry(industryMatcher.group(2).replaceAll("<[^>]+>", ""));
		}
		Matcher totalCostMatcher = totalCostPattern.matcher(content);
		if (totalCostMatcher.find()) {
			log.debug(totalCostMatcher.group(1));
			data.setTotalCost(totalCostMatcher.group(1));
		}
		Matcher statusMatcher = statusPattern.matcher(content);
		if (statusMatcher.find()) {
			log.debug(statusMatcher.group(2).replaceAll("\\s+", "").replaceAll("&#160;", " "));
			data.setStatus(statusMatcher.group(2).replaceAll("\\s+", "").replaceAll("&#160;", " "));
		}
		
		return data;
	}
}
