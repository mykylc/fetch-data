package com.fetch.data.client;

import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.FetchData;
import com.fetch.data.domain.Page;
import com.fetch.data.tools.FetchDataThreadPool;

/**
 * 北京产权交易所/债权
 * @author 
 *
 */
public class CbexZQClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(10);
	//http://www.cbex.com.cn/article/xmpd/ps_2010_newsearch.shtml?flag=1&key=&proCode=&proAssetType=%E5%80%BA%E6%9D%83&proAddress=&proIndustry=&imageField.x=50&imageField.y=5&curPage=1
	private final static String domain = "http://www.cbex.com.cn";
	private final static String webSite = "/article/xmpd/ps_2010_newsearch.shtml?flag=1&key=&proCode=&proAssetType=%E5%80%BA%E6%9D%83&proAddress=&proIndustry=&imageField.x=50&imageField.y=5&curPage=";
	private static int pageSize = 30;
	private static int currentPage = 1;
	private final String charset = "UTF-8";
	
	// 提取总条数的正则表达式
    private static final Pattern totalCountPattern = Pattern.compile("<font class=font13>共有记录<font color=red><b>(.*?)</b></font>项", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile(
    		"<font class=font13><a href=\"(.*?)\" target=_blank>.*?</A></font></td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<tr>\\s+<td class=\"xm_logo\">(.*?)<div style=\"float:right; margin-bottom:5px;\">",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取listing_date end_date值
    private static final Pattern datePattern = Pattern.compile(
    		"<td class=\"xmtd1\">挂牌起始日期</td>\\s+<td class=\"xmtd2\">(.*?)</td>\\s+<td class=\"xmtd1\">挂牌期满日期</td>\\s+<td class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取location industry
    private static final Pattern locationIndustryPattern = Pattern.compile(
    		"<td class=\"xmtd1\">标的所在地区</td>\\s+<td class=\"xmtd2\">(.*?)</td>\\s+<td class=\"xmtd1\">标的所属行业</td>\\s+<td class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
    //提取location
    private static final Pattern locationPattern = Pattern.compile(
    		"<td class=\"xmtd1\">所在地区</td>\\s+<td class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取industry
    private static final Pattern industryPattern = Pattern.compile(
    		"<td class=\"xmtd1\">所属行业</td>\\s+<td class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
    //提取listing_date
    private static final Pattern listingDatePattern = Pattern.compile(
    		"<td class=\"xmtd1\">项目发布起止日期</td>\\s+<td class=\"xmtd2\" colspan=\"3\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取price
    private static final Pattern pricePattern = Pattern.compile(
    		"<td width=\"11%\" class=\"xmtd1\">挂牌价格</td>\\s+<td width=\"40%\" class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final Pattern pricePattern2 = Pattern.compile(
    		" <td class=\"xmtd1\">融资/招商金额<br/>（万元）</td>\\s+<td class=\"xmtd2\">(.*?)</td>",
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
			log.error("获取SuaeeCQXM网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
	@Override
	public FetchData parseObject(String content, String pageUrl) throws Exception {
		FetchData data = new FetchData();
		data.setCountry("中国");
		data.setPageUrl(pageUrl);
		//log.debug(content);
		Matcher nameMatcher = namePattern.matcher(content);
        if (nameMatcher.find()) {
        	log.debug(nameMatcher.group(1).replaceAll("\\s+", ""));
        	data.setName(nameMatcher.group(1).replaceAll("\\s+", ""));
        }
        Matcher priceMatcher = pricePattern.matcher(content);
        Matcher priceMatcher2 = pricePattern2.matcher(content);
        if (priceMatcher.find()) {
        	log.debug(priceMatcher.group(1).replaceAll("\\s+", ""));
			data.setPrice(priceMatcher.group(1).replaceAll("\\s+", ""));
		}else if (priceMatcher2.find()) {
        	log.debug(priceMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
			data.setPrice(priceMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
		}
		Matcher dateMatcher = datePattern.matcher(content);
        if (dateMatcher.find()) {
        	log.debug(dateMatcher.group(1).replaceAll("&nbsp;", "")+"=="+dateMatcher.group(2).replaceAll("&nbsp;", ""));
        	data.setListingDate(dateMatcher.group(1).replaceAll("&nbsp;", ""));
        	data.setEndDate(dateMatcher.group(2).replaceAll("&nbsp;", ""));
        }
        Matcher locationIndustryMatcher = locationIndustryPattern.matcher(content);
        if (locationIndustryMatcher.find()) {
        	log.debug(locationIndustryMatcher.group(1).replaceAll("&nbsp;", "")+"="+locationIndustryMatcher.group(2).replaceAll("&nbsp;", ""));
			data.setLocation(locationIndustryMatcher.group(1).replaceAll("&nbsp;", ""));
			data.setIndustry(locationIndustryMatcher.group(2).replaceAll("&nbsp;", ""));
		}
        
        
        Matcher locationMatcher = locationPattern.matcher(content);
        if (locationMatcher.find()) {
        	log.debug(locationMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("\\s+", ""));
			data.setLocation(locationMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("\\s+", ""));
		}
        Matcher industryMatcher = industryPattern.matcher(content);
        if (industryMatcher.find()) {
        	log.debug(industryMatcher.group(1).replaceAll("\\s+", ""));
			data.setIndustry(industryMatcher.group(1).replaceAll("\\s+", ""));
		}
        Matcher listingDateMatcher = listingDatePattern.matcher(content);
        if (listingDateMatcher.find()) {
        	log.debug(listingDateMatcher.group(1).replaceAll("&nbsp;", ""));
			data.setListingDate(listingDateMatcher.group(1).replaceAll("&nbsp;", ""));
		}
        
		return data;
	}
	
}
