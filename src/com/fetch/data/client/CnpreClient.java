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
 * 中国产权交易所
 * @author 
 *
 */
public class CnpreClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(10);
	
	private final static String domain = "http://www.cnpre.com/listing/";
	private final static String webSite = "?page=";
	private static int pageSize = 10;
	private static int currentPage = 1;
	private final String charset = "UTF-8";
	
	private Map<String, String> map = new HashMap<String, String>();
	
	// 提取总条数的正则表达式
    private static final Pattern totalCountPattern = Pattern.compile("<td width=\"53%\" height=\"36\" align=\"left\" valign=\"middle\" class=\"ya2\">数量 <span class=\"red\">(.*?)\\s*</span>个", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile("<a class=\"protitle\" href=\"(.*?)\">.*?[^\\<.*\\>]</a>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取location值
    private static final Pattern locationPattern = Pattern.compile(
    		"<td height=\"30\">所属地区：(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<span class=\"protitle\">(.*?)</span>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取price
    private static final Pattern pricePattern = Pattern.compile(
    		"起 始 价： <span class=\"pricetag\">(.*?)</span><span class=\"price\">(.*?)</span>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern pricePattern2 = Pattern.compile(
    		"当 前 价： <span class=\"pricetag\">(.*?)</span><span class=\"price\" id=\"nowprice_13\">(.*?)</span>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern pricePattern3 = Pattern.compile(
    		"一 口 价： <span class=\"pricetag\">(.*?)</span><span class=\"price\">(.*?)</span>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取project_type
    private static final Pattern projectTypePattern = Pattern.compile(
    		"<td height=\"(26|30)\">付款方式：\\s*(.*?)<a href=", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取listing_date end_date值
    private static final Pattern datePattern = Pattern.compile(
    		"<td height=\"(26|30)\">报价周期：\\s*(.*?)\\s*至\\s*(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取description值
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<dd class=\"dd3 showtxt\">(.*?)</dd>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage;
			final Page page = getPage(url, totalCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			log.debug(page.getTotalCount()+"");
			getUrlListByPage2(url, urlListPattern, domain, charset);
			while (page.hasNextPage()) {
				semp.acquire();
				final int nextPage = page.getNextPage();
				page.setCurrentPage(nextPage);
				FetchDataThreadPool.exec.execute(new Runnable() {
					@Override
					public void run() {
						try {
							String urlPage = domain + webSite + nextPage;
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
				if (semp.availablePermits()==10) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("获取CnpreClient网站报错："+e.getMessage(), e);
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
					log.debug(hrefUrl);
				}
            }
            List<String> locationList = new ArrayList<String>();
            Matcher locationmatcher = locationPattern.matcher(result);
            while (locationmatcher.find()) {
            	String location = locationmatcher.group(1);
				log.debug(location);
				locationList.add(location);
            }
            int urlSize = urlList.size();
            int locationSize = locationList.size();
			for (int i=0; i < urlSize; i++) {
            	if (urlSize==locationSize) {
            		map.put(urlList.get(i), locationList.get(i));
				} else if (urlSize > locationSize){
					if (i < locationSize) {
						map.put(urlList.get(i), locationList.get(i));
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
		data.setCountry("中国");
		data.setLocation(map.get(pageUrl));
		log.debug(content);
		Matcher nameMatcher = namePattern.matcher(content);
        if (nameMatcher.find()) {
        	log.debug(nameMatcher.group(1));
        	data.setName(nameMatcher.group(1));
        }
        Matcher priceMatcher = pricePattern.matcher(content);
        Matcher priceMatcher2 = pricePattern2.matcher(content);
        Matcher priceMatcher3 = pricePattern3.matcher(content);
        if (priceMatcher.find()) {
        	log.debug(priceMatcher.group(1).replaceAll("\\s+", "").replaceAll("&yen;", "￥") + priceMatcher.group(2).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
			data.setPrice(priceMatcher.group(1).replaceAll("\\s+", "").replaceAll("&yen;", "￥") + priceMatcher.group(2).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
		}else if (priceMatcher2.find()) {
        	log.debug(priceMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&yen;", "￥") + priceMatcher2.group(2).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
			data.setPrice(priceMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&yen;", "￥") + priceMatcher2.group(2).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
		}else if (priceMatcher3.find()) {
        	log.debug(priceMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&yen;", "￥") + priceMatcher3.group(2).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
			data.setPrice(priceMatcher3.group(1).replaceAll("\\s+", "").replaceAll("&yen;", "￥") + priceMatcher3.group(2).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
		}
        Matcher projectTypeMatcher = projectTypePattern.matcher(content);
        if (projectTypeMatcher.find()) {
        	log.debug(projectTypeMatcher.group(2).trim());
			data.setProjectType(projectTypeMatcher.group(2).trim());
		}
        Matcher dateMatcher = datePattern.matcher(content);
		if (dateMatcher.find()) {
			log.debug(dateMatcher.group(2).trim()+"="+dateMatcher.group(3).trim());
			data.setListingDate(dateMatcher.group(2).trim());
			data.setEndDate(dateMatcher.group(3).trim());
		}
		Matcher descriptionMatcher = descriptionPattern.matcher(content);
		if (descriptionMatcher.find()) {
      		log.debug(descriptionMatcher.group(1).replaceAll("<[^>]+>", "").replaceAll("&nbsp;", ""));
			data.setDescription(descriptionMatcher.group(1).replaceAll("<[^>]+>", "").replaceAll("&nbsp;", ""));
		}

		return data;
	}
}
