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
 * 上海联合产权交易所/知识产权项目
 * @author 
 *
 */
public class SuaeeJSXMClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(10);
	
	private final static String domain = "http://www.suaee.com/suaee/portal/project/";
	private final static String webSite = "projlist.jsp?ptype=jsxm&cp=";
	private static int pageSize = 45;
	private static int currentPage = 1;
	private final String charset = "GBK";
	
	// 提取总条数的正则表达式
    private static final Pattern totalCountPattern = Pattern.compile("当前第 <font color=\"#ff3333\">1/(.*?)</font> 页 </td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile("<td align=\"left\"><a .*? target=\"_black\" href=\"(.*?)\" class=\"proj\">.*?</a></td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<span style=\"font-family:宋体;font-size:14px;\">项目名称</span></b><span style=\"font-family:宋体;font-size:9pt;\"></span>\\s+</p>\\s+</td>\\s+<td style=\"background:\\#fafafa;\" width=\"721\">\\s+<p style=\"text-align:left;text-indent:0\\.05pt;\" class=\"MsoNormal\" align=\"left\">\\s+<span style=\"font-family:宋体;color:black;font-size:9pt;\"><span style=\"font-size:14px;\">(.*?)</span><span></span></span>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern namePattern2 = Pattern.compile(
    		"<td>\\s+<span style=\"font-size:14px;\">&nbsp;</span><b><span style=\"font-family:宋体;font-size:14px;\">项目名称</span></b>\\s+</td>\\s+<td>(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern namePattern3 = Pattern.compile(
    		//"<td style=\"background:\\#cae8ea;\" width=\"180\">\\s+<p style=\"text-align:left;margin-left:12\\.3pt;\" class=\"MsoNormal\" align=\"left\">\\s+<b><span style=\"font-family:宋体;color:black;font-size:14px;\">项目名称</span></b><span style=\"font-family:宋体;color:black;font-size:12pt;\"></span>\\s+</p>\\s+</td>\\s+<td style=\"background:\\#fafafa;\" width=\"721\">(.*?)</td>",
    		"<td.*>.*?项目名称.*?</td>\\s+<td style=\"background:\\#fafafa;\" width=\"721\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final Pattern namePattern4 = Pattern.compile(
    		"<td.*>.*?项目名称.*?</td>\\s+<td width=\"800\" align=\"left\" bgcolor=\"\\#FAFAFA\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取location
    private static final Pattern locationPattern = Pattern.compile(
    		"<span style=\"font-size:14px;\">项目所在国家</span><span style=\"font-size:14px;\">/</span><span style=\"font-size:14px;\">地区</span></span></b><span style=\"font-family:宋体;font-size:9pt;\"></span>\\s+</p>\\s+</td>\\s+<td style=\"background:\\#fafafa;\" width=\"721\">\\s+<p style=\"text-align:left;text-indent:0\\.05pt;\" class=\"MsoNormal\" align=\"left\">\\s+<span style=\"font-family:宋体;color:black;font-size:9pt;\">(.*?)<span></span>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final Pattern locationPattern2 = Pattern.compile(
    		"<td>\\s+<span style=\"font-size:14px;\">&nbsp;</span><b><span style=\"font-family:宋体;font-size:9pt;\"><span style=\"font-size:14px;\">项目所在国家</span><span style=\"font-size:14px;\">/</span><span style=\"font-size:14px;\">地区</span></span></b>\\s+</td>\\s+<td>(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern locationPattern3 = Pattern.compile(
    		//"<td style=\"background:\\#cae8ea;\" width=\"180\">\\s+<p style=\"text-align:left;margin-left:12\\.3pt;\" class=\"MsoNormal\" align=\"left\">\\s+<b><span style=\"font-family:宋体;color:black;font-size:12pt;\"><span style=\"font-size:14px;\">项目所在国家</span><span style=\"font-size:14px;\">/</span><span style=\"font-size:14px;\">地区</span></span></b><span style=\"font-family:宋体;color:black;font-size:12pt;\"></span>\\s+</p>\\s+</td>\\s+<td style=\"background:\\#fafafa;\" width=\"721\">(.*?)</td>",
    		"<td.*>.*?项目所在国家.*?</td>\\s+<td style=\"background:\\#fafafa;\" width=\"721\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern locationPattern4 = Pattern.compile(
    		"<td.*>.*?项目所在地区.*?</td>\\s+<td width=\"800\" bgcolor=\"\\#FAFAFA\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<b><span style=\"font-family:宋体;font-size:14px;\">项目简介</span></b><span style=\"font-family:宋体;font-size:9pt;\"></span>\\s+</p>\\s+</td>\\s+<td style=\"background:\\#fafafa;\" width=\"721\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final Pattern descriptionPattern2 = Pattern.compile(
    		"<td>\\s+<span style=\"font-size:14px;\">&nbsp;</span><b><span style=\"font-family:宋体;font-size:14px;\">项目简介</span></b>\\s+</td>\\s+<td>(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern descriptionPattern3 = Pattern.compile(
    		"<td.*>.*?项目简介.*?</td>\\s+<td style=\"background:\\#fafafa;\" width=\"721\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern descriptionPattern4 = Pattern.compile(
    		"<td.*>&nbsp;&nbsp;<b>项目简介</b></td>\\s+<td width=\"800\" align=\"left\" bgcolor=\"\\#FAFAFA\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取project_type
    private static final Pattern projectTypePattern = Pattern.compile(
    		"<td style=\"background:#cae8ea;\" valign=\"top\" width=\"180\">\\s+<p style=\"text-align:left;margin-left:12\\.3pt;\" class=\"MsoNormal\" align=\"left\">\\s+<b><span style=\"font-family:宋体;font-size:9pt;\"><span style=\"font-size:14px;\">交易方式</span><span></span></span></b>\\s+</p>\\s+</td>\\s+<td style=\"background:\\#fafafa;\" valign=\"top\" width=\"721\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final Pattern projectTypePattern2 = Pattern.compile(
    		"<td>\\s+<span style=\"font-size:14px;\">&nbsp;</span><b><span style=\"font-family:宋体;font-size:14px;\">交易方式</span></b>\\s+</td>\\s+<td>(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern projectTypePattern3 = Pattern.compile(
    		"<td.*>.*?交易方式.*?</td>\\s+<td style=\"background:\\#fafafa;\" valign=\"top\" width=\"721\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取price
    private static final Pattern pricePattern = Pattern.compile(
    		"<td style=\"background:\\#cae8ea;\" valign=\"top\" width=\"180\">\\s+<p style=\"margin-left:12\\.3pt;\" class=\"MsoNormal\" align=\"left\">\\s+<b><span style=\"font-family:宋体;font-size:9pt;\"><span style=\"font-size:14px;\">项目挂牌报价</span><span></span></span></b>\\s+</p>\\s+</td>\\s+<td style=\"background:\\#fafafa;\" valign=\"top\" width=\"721\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern pricePattern2 = Pattern.compile(
    		"<td>\\s+<span style=\"font-size:14px;\">&nbsp;</span><b><span style=\"font-family:宋体;font-size:14px;\">项目挂牌报价</span></b>\\s+</td>\\s+<td>(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern pricePattern3 = Pattern.compile(
    		"<td.*>.*?项目挂牌报价.*?</td>\\s+<td style=\"background:\\#fafafa;\" valign=\"top\" width=\"721\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取listing_date end_date值
    private static final Pattern datePattern = Pattern.compile(
    		"<td style=\"background:\\#cae8ea;\" width=\"180\">\\s+<p style=\"text-align:left;margin-left:12\\.3pt;\" class=\"MsoNormal\" align=\"left\">\\s+<b><span style=\"font-family:宋体;font-size:14px;\">信息发布期限</span></b>\\s+</p>\\s+</td>\\s+<td style=\"background:\\#fafafa;\" width=\"721\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern datePattern2 = Pattern.compile(
    		"<td>\\s+<span style=\"font-size:14px;\">&nbsp;</span><b><span style=\"font-family:宋体;font-size:14px;\">委托期限</span></b>\\s+</td>\\s+<td>(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern datePattern3 = Pattern.compile(
    		"<td.*>.*?委托期限.*?</td>\\s+<td style=\"background:\\#fafafa;\" width=\"721\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern datePattern4 = Pattern.compile(
    		"<td.*>&nbsp;&nbsp;<b>委托期限</b></td>\\s+<td width=\"800\"  align=\"left\" bgcolor=\"\\#FAFAFA\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage;
			final Page page = getPage(url, totalCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			getUrlListByPage(url, urlListPattern, domain, charset);
			while (page.hasNextPage()) {
				semp.acquire();
				final int nextPage = page.getNextPage();
				page.setCurrentPage(nextPage);
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
				if (semp.availablePermits()==10) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("获取SuaeeJSXMClient网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
    /**
	 * 获取page对象
	 * @param url
	 * @param totalCountPattern
	 * @param pageSize
	 * @return Page
	 * @throws Exception
	 */
	public Page getPage(String url, Pattern totalCountPattern, int pageSize, String charset) throws Exception{
		try {
			HttpUtils httpUtils = new HttpUtils(url, charset);
			String result = httpUtils.execute();
			if (result==null) {
				return null;
			}
			Matcher pageCountMatcher = totalCountPattern.matcher(result);
            while (pageCountMatcher.find()) {
                String pageCount = pageCountMatcher.group(1);
                Page page = new Page();
                page.setPageSize(pageSize);
                page.setPageCount(Integer.parseInt(pageCount));
                return page;
            }
		} catch (Exception e) {
			log.error(String.format("[SuaeeJSXMClient.getPage] url=%s; 获取page对象报错：%s", url, e.getMessage()), e);
			throw e;
		}
		return null;
	} 
    
	@Override
	public FetchData parseObject(String content, String pageUrl) throws Exception {
		FetchData data = new FetchData();
		data.setPageUrl(pageUrl);
		data.setCountry("中国");
		log.debug(content);
		Matcher nameMatcher = namePattern.matcher(content);
		Matcher nameMatcher2 = namePattern2.matcher(content);
		Matcher nameMatcher3 = namePattern3.matcher(content);
		Matcher nameMatcher4 = namePattern4.matcher(content);
        if (nameMatcher.find()) {
        	log.debug(nameMatcher.group(1));
        	data.setName(nameMatcher.group(1));
        } else if(nameMatcher2.find()){
        	log.debug(nameMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
        	data.setName(nameMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
        } else if(nameMatcher3.find()){
        	log.debug(nameMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
        	data.setName(nameMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
        }	else if(nameMatcher4.find()){
        	log.debug(nameMatcher4.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
        	data.setName(nameMatcher4.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
        }
        
        Matcher locationMatcher = locationPattern.matcher(content);
        Matcher locationMatcher2 = locationPattern2.matcher(content);
        Matcher locationMatcher3 = locationPattern3.matcher(content);
        Matcher locationMatcher4 = locationPattern4.matcher(content);
        if (locationMatcher.find()) {
        	log.debug(locationMatcher.group(1).replaceAll("<[^>]+>", ""));
			data.setLocation(locationMatcher.group(1).replaceAll("<[^>]+>", ""));
		} else if (locationMatcher2.find()){
			log.debug(locationMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setLocation(locationMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		} else if (locationMatcher3.find()){
			log.debug(locationMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setLocation(locationMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		} else if (locationMatcher4.find()){
			log.debug(locationMatcher4.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setLocation(locationMatcher4.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
        
        Matcher descriptionMatcher = descriptionPattern.matcher(content);
        Matcher descriptionMatcher2 = descriptionPattern2.matcher(content);
        Matcher descriptionMatcher3 = descriptionPattern3.matcher(content);
        Matcher descriptionMatcher4 = descriptionPattern4.matcher(content);
        if (descriptionMatcher.find()) {
        	log.debug(descriptionMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		} else if (descriptionMatcher2.find()) {
        	log.debug(descriptionMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		} else if (descriptionMatcher3.find()) {
        	log.debug(descriptionMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		} else if (descriptionMatcher4.find()) {
        	log.debug(descriptionMatcher4.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher4.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
        Matcher projectTypeMatcher = projectTypePattern.matcher(content);
        Matcher projectTypeMatcher2 = projectTypePattern2.matcher(content);
        Matcher projectTypeMatcher3 = projectTypePattern3.matcher(content);
        if (projectTypeMatcher.find()) {
        	log.debug(projectTypeMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setProjectType(projectTypeMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		} else if (projectTypeMatcher2.find()) {
        	log.debug(projectTypeMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setProjectType(projectTypeMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		} else if (projectTypeMatcher3.find()) {
        	log.debug(projectTypeMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setProjectType(projectTypeMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
        Matcher priceMatcher = pricePattern.matcher(content);
        Matcher priceMatcher2 = pricePattern2.matcher(content);
        Matcher priceMatcher3 = pricePattern3.matcher(content);
        if (priceMatcher.find()) {
        	log.debug(priceMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setPrice(priceMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		} else if (priceMatcher2.find()) {
        	log.debug(priceMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setPrice(priceMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		} else if (priceMatcher3.find()) {
        	log.debug(priceMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setPrice(priceMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
        
		Matcher dateMatcher = datePattern.matcher(content);
		Matcher dateMatcher2 = datePattern2.matcher(content);
		Matcher dateMatcher3 = datePattern3.matcher(content);
		Matcher dateMatcher4 = datePattern4.matcher(content);
        if (dateMatcher.find()) {
        	String date = dateMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "");
			log.debug(date.split("至")[0]+"=="+date.split("至")[1]);
        	data.setListingDate(date.split("至")[0]);
        	data.setEndDate(date.split("至")[1]);
        } else if (dateMatcher2.find()) {
        	String date = dateMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "");
			log.debug(date.split("至")[0]+"=="+date.split("至")[1]);
        	data.setListingDate(date.split("至")[0]);
        	data.setEndDate(date.split("至")[1]);
        } else if (dateMatcher3.find()) {
        	String date = dateMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "");
			log.debug(date.split("至")[0]+"=="+date.split("至")[1]);
        	data.setListingDate(date.split("至")[0]);
        	data.setEndDate(date.split("至")[1]);
        } else if (dateMatcher4.find()) {
        	String date = dateMatcher4.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "");
			log.debug(date.split("至")[0]+"=="+date.split("至")[1]);
        	data.setListingDate(date.split("至")[0]);
        	data.setEndDate(date.split("至")[1]);
        }
       
		return data;
	}
	
}
