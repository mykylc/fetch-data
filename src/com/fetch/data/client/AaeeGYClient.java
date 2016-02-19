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
 * 安徽省产权交易所/国有股权
 * @author 
 *
 */
public class AaeeGYClient extends AaeeClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(10);
	
	public final static String webSite = "list_item.jsp?colId=1361175195028000&strWebSiteId=1338175111067003&parentColId=1359610511716000&intCurPage=";
	// 提取URL列表的正则表达式
	public static final Pattern urlListPattern = Pattern.compile(
    		"<div style=\"overflow:hidden;height:24px;line-height:22px\"><a href=\"(.*?)\" target=\"_blank\" >.*?</a></div>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
	//提取iframe url
    private static final Pattern iframeUrlPattern = Pattern.compile(
    		"<iframe frameborder=\"0\" src=\"(.*?)\" width=\"100%\" scrolling=\"auto\" style=\"height:3500px\" allowtransparency></iframe>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<TD class=\"xmtd1\" width=\"11%\">标的名称</TD>\\s*<TD class=\"xmtd2\" colSpan=\"3\">(.*?)<SPAN\\s*class=\"red\"></SPAN> </TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取price
    private static final Pattern pricePattern = Pattern.compile(
    		"<TD class=\"xmtd1\" width=\"11%\">挂牌价格\\(万元\\)</TD>                <TD class=\"xmtd2\" width=\"40%\">(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取listing_date end_date值
    private static final Pattern datePattern = Pattern.compile(
    		"<TD class=\"xmtd1\">挂牌起始日期</TD>\\s*<TD class=\"xmtd2\">(.*?)</TD>\\s*<TD class=\"xmtd1\">挂牌期满日期</TD>\\s*<TD class=\"xmtd2\">(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取location industry值
    private static final Pattern locationIndustryPattern = Pattern.compile(
    		"<TD class=\"xmtd1\">标的所在地区</TD>\\s*<TD class=\"xmtd2\">(.*?)</TD>\\s*<TD class=\"xmtd1\">标的所属行业</TD>\\s*<TD class=\"xmtd2\">(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage;
			final Page page = getPage(url, pageCountPattern, pageSize, charset);
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
			log.error("获取AaeeGYClient网站报错："+e.getMessage(), e);
			throw e;
		}
	}
    
    /**
	 * 获取文章内容
	 * @param url
	 * @return FetchData
	 * @throws Exception
	 */
    @Override
	public FetchData getContent(String url, String charset) throws Exception{
		try {
			if (url.indexOf("G315AH1000084")!=-1) {
				return null;
			}
			HttpUtils httpUtils = new HttpUtils(url, charset);
			String content = httpUtils.execute();
			if (content!=null) {
				return parseObject(content, url);
			}
			return null;
		} catch (Exception e) {
			log.error(String.format("[AbstractClient.getContent] url=%s; 获取文章内容报错：%s", url, e.getMessage()), e);
			throw e;
		}
	}
    
    @Override
	public FetchData parseObject(String cxt, String pageUrl) throws Exception {
		String content = getContentByIframe(cxt);
		if (content==null) {
			return null;
		}
		FetchData data = new FetchData();
		data.setPageUrl(pageUrl);
		data.setCountry("中国");
		//log.debug(content);
		Matcher nameMatcher = namePattern.matcher(content);
		if (nameMatcher.find()) {
			log.debug(nameMatcher.group(1).replaceAll("&nbsp;", ""));
			data.setName(nameMatcher.group(1).replaceAll("&nbsp;", ""));
		}
		Matcher priceMatcher = pricePattern.matcher(content);
		if (priceMatcher.find()) {
			log.debug(priceMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
			data.setPrice(priceMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
		}
		Matcher dateMatcher = datePattern.matcher(content);
		if (dateMatcher.find()) {
			log.debug(dateMatcher.group(1).replaceAll("&nbsp;", "")+"="+dateMatcher.group(2).replaceAll("&nbsp;", ""));
			data.setListingDate(dateMatcher.group(1).replaceAll("&nbsp;", ""));
			data.setEndDate(dateMatcher.group(2).replaceAll("&nbsp;", ""));
		}
		Matcher locationIndustryMatcher = locationIndustryPattern.matcher(content);
		if (locationIndustryMatcher.find()) {
			log.debug(locationIndustryMatcher.group(1).replaceAll("&nbsp;", "")+"="+locationIndustryMatcher.group(2).replaceAll("&nbsp;", ""));
			data.setLocation(locationIndustryMatcher.group(1).replaceAll("&nbsp;", ""));
			data.setIndustry(locationIndustryMatcher.group(2).replaceAll("&nbsp;", ""));
		}
		
		return data;
	}
	
	public String getContentByIframe(String cxt) throws Exception{
		try{
			Matcher iframeUrlMatcher = iframeUrlPattern.matcher(cxt);
			if (iframeUrlMatcher.find()) {
				HttpUtils httpUtils = new HttpUtils(iframeUrlMatcher.group(1), "utf-8");
				return httpUtils.execute();
			} 
		} catch (Exception e) {
			log.error(String.format("[AbstractClient.getContentByIframe] 获取iframe对象报错：%s", e.getMessage()), e);
			throw e;
		}
		return null;
	}
	
}
