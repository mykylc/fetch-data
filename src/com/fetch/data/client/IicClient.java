package com.fetch.data.client;

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
 * Iic
 * @author 
 *
 */
public class IicClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	private final static String domain = "http://www.iic.org";
	private final static String webSite = "/en/projects/results?keys=&country=All&approvaldate%5bvalue%5d%5byear%5d=&page=";
	private static int pageSize = 20;
	private static int currentPage = 0;
	private final String charset = "UTF-8";
	
	// 提取总页数的正则表达式
    private static final Pattern pageCountPattern = Pattern.compile(
    		"<li class=\"pager-last last\"><a title=\"Go to last page\" href=\"/en/projects/results\\?keys=&amp;country=All&amp;approvaldate\\[value\\]\\[year\\]=&amp;page=(.*?)\">", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile(
    		"<td class=\"views-field views-field-title\" >            <a href=\"(.*?)\">.*?</a>          </td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<p><label class=\"label-inline\">Project Name:&nbsp;</label>(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Country
    private static final Pattern countryPattern = Pattern.compile(
    		"<p><label class=\"label-inline\">Country:&nbsp;</label>(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取industry
    private static final Pattern industryPattern = Pattern.compile(
    		"<p><label class=\"label-inline\">Sector:&nbsp;</label>(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取Approval date
    private static final Pattern approvalDatePattern = Pattern.compile(
    		"<p><label class=\"label-inline\">Approval Date:&nbsp;</label>(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取cost值
    private static final Pattern costPattern = Pattern.compile(
    		"<p><label class=\"label-inline\">Total Project Cost:&nbsp;</label>(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<span class=\"fieldset-legend\">Project</span></legend><div class=\"fieldset-wrapper\">(.*?)<div id=\"node_project_full_group_project_basicinfo\"class = \"common-field-group project-field-group group-project-basicinfo field-group-div\">", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);   
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage;
			final Page page = getPage2(url, pageCountPattern, pageSize, charset);
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
			log.error("获取Iic网站报错："+e.getMessage(), e);
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
			log.error(String.format("[IicClient.getPage] url=%s; 获取page对象报错：%s", url, e.getMessage()), e);
			throw e;
		}
		return null;
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
			log.debug(countryMatcher.group(1).replaceAll("<[^>]+>", ""));
			data.setCountry(countryMatcher.group(1).replaceAll("<[^>]+>", ""));
		}
		Matcher industryMatcher = industryPattern.matcher(content);
		if (industryMatcher.find()) {
			log.debug(industryMatcher.group(1).replaceAll("<[^>]+>", ""));
			data.setIndustry(industryMatcher.group(1).replaceAll("<[^>]+>", ""));
		}
		Matcher approvalDateMatcher = approvalDatePattern.matcher(content);
		if (approvalDateMatcher.find()) {
			log.debug(approvalDateMatcher.group(1).replaceAll("<[^>]+>", ""));
			data.setApprovalDate(approvalDateMatcher.group(1).replaceAll("<[^>]+>", ""));
		}
		Matcher costMatcher = costPattern.matcher(content);
		if (costMatcher.find()) {
			log.debug(costMatcher.group(1));
			data.setCost(costMatcher.group(1));
		}
		
		Matcher descriptionMatcher = descriptionPattern.matcher(content);
		if (descriptionMatcher.find()) {
			log.debug(descriptionMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		return data;
	}
}
