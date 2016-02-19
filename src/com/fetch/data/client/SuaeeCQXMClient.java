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
 * 上海联合产权交易所/产股权项目
 * @author 
 *
 */
public class SuaeeCQXMClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(10);
	
	private final static String domain = "http://www.suaee.com/suaee/portal/project/";
	private final static String webSite = "projlist.jsp?ptype=cqxm&cp=";
	private static int pageSize = 45;
	private static int currentPage = 1;
	private final String charset = "GBK";
	
	// 提取总条数的正则表达式
    private static final Pattern totalCountPattern = Pattern.compile("<font color=\"#ff3333\">(.*?)</font>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile("<a title=\".*?\" target=\"_black\" href=\"(.*?)\" class=\"proj\">.*?</a>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<td width=\"200\" align=\"left\" bgcolor=\"#7ec0f5\" height=\"25\">&nbsp;&nbsp;<b>转让标的名称</b></td> <td width=\"800\" colspan=\"3\" align=\"left\" bgColor=#eff8fe>(.*?)&nbsp;&nbsp;</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取listing_date值
    private static final Pattern listingDatePattern = Pattern.compile(
    		"<td width=\"200\" align=\"left\" bgcolor=\"#7ec0f5\" height=\"25\">&nbsp;&nbsp;<b>挂牌起始日期</b></td> <td width=\"200\" align=\"left\" bgColor=#eff8fe>(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取end_date值
    private static final Pattern endDatePattern = Pattern.compile(
    		"<td width=\"200\" align=\"left\" bgcolor=\"#7ec0f5\" height=\"25\">&nbsp;&nbsp;<b>挂牌期满日期</b></td> <td width=\"200\" align=\"left\" bgColor=#eff8fe>(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取industry
    private static final Pattern industryPattern = Pattern.compile(
    		"<td width=\"200\" align=\"left\" bgcolor=\"#7ec0f5\" height=\"25\">&nbsp;&nbsp;<b>标的所属行业</b></td> <td width=\"200\" align=\"left\" bgColor=#eff8fe>(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取city
    private static final Pattern cityPattern = Pattern.compile("<td width=\"200\" align=\"left\" bgcolor=\"#7ec0f5\" height=\"25\">&nbsp;&nbsp;<b>标的所在地区</b></td> <td width=\"200\" align=\"left\" bgColor=#eff8fe>(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取company_type
    private static final Pattern companyTypePattern = Pattern.compile("<td width=\"200\" bgcolor=#cce6fb height=\"25\" align=\"left\">&nbsp;&nbsp;公司类型（经济性质）  </td>  <td width=\"600\">(.*?)\\s+</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取shares_percentage
    private static final Pattern sharesPercentagePattern = Pattern.compile("<td width=\"200\" height=\"25\" bgColor=#cce6fb align=\"left\" valign=\"middle\">&nbsp;&nbsp;拟转让产\\(股\\)权比例  </td>  <td width=\"600\" colspan=3  height=\"25\" align=\"left\">(.*?)\\s+</td>");
    
    //提取price
    private static final Pattern pricePattern = Pattern.compile("<td width=\"200\" align=\"left\" bgcolor=\"#7ec0f5\" height=\"25\">&nbsp;&nbsp;<b>挂牌价格</b></td> <td width=\"200\" align=\"left\" bgColor=#eff8fe>(.*?)</td>");
    
    
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
		data.setPageUrl(pageUrl);
		data.setCountry("中国");
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
        Matcher endDateMatcher = endDatePattern.matcher(content);
        if (endDateMatcher.find()) {
        	log.debug(endDateMatcher.group(1));
			data.setEndDate(endDateMatcher.group(1));
		}
        Matcher industryMatcher = industryPattern.matcher(content);
        if (industryMatcher.find()) {
        	log.debug(industryMatcher.group(1));
			data.setIndustry(industryMatcher.group(1));
		}
        Matcher cityMatcher = cityPattern.matcher(content);
        if (cityMatcher.find()) {
        	log.debug(cityMatcher.group(1));
			data.setCity(cityMatcher.group(1));
		}
        
        Matcher companyTypeMatcher = companyTypePattern.matcher(content);
        if (companyTypeMatcher.find()) {
        	log.debug(companyTypeMatcher.group(1));
			data.setCompanyType(companyTypeMatcher.group(1));
		}
        Matcher sharesPercentageMatcher = sharesPercentagePattern.matcher(content);
        if (sharesPercentageMatcher.find()) {
			log.debug(sharesPercentageMatcher.group(1));
			data.setSharesPercentage(sharesPercentageMatcher.group(1));
		}
        Matcher priceMatcher = pricePattern.matcher(content);
        if (priceMatcher.find()) {
        	log.debug(priceMatcher.group(1).replaceAll("\\s+", ""));
			data.setPrice(priceMatcher.group(1).replaceAll("\\s+", ""));
		}
		return data;
	}
}
