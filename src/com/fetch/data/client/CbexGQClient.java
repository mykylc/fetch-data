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
 * 北京产权交易所/股权
 * @author 
 *
 */
public class CbexGQClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(10);
	
	private final static String domain = "http://www.cbex.com.cn";
	private final static String webSite = "/article/xmpd/ps_2010_newsearch.shtml?flag=1&numPerPage=30&order=projectcode&direction=down&key=&proCode=&proAssetType=%E8%82%A1%E6%9D%83&proAddress=&proIndustry=&curPage=";
	private static int pageSize = 30;
	private static int currentPage = 1;
	private final String charset = "UTF-8";
	
	// 提取总条数的正则表达式
    private static final Pattern totalCountPattern = Pattern.compile("<font class=font13>共有记录<font color=red><b>(.*?)</b>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile("<td align=left><font class=font13><a href=\"(.*?)\" target=_blank>.*?</A></font></td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取asset_owner
    private static final Pattern assetOwnerPattern = Pattern.compile(
    		"<td class=\"xmtd1\">((转让方/融资方)|(融资/招商主体))</td>\\s+<td colspan=\"3\" class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<td class=\"xmtd1\">项目名称</td>\\s+<td colspan=\"3\" class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
  //提取name值
    private static final Pattern namePattern2 = Pattern.compile(
    		"<tr>\\s+<td class=\"xm_logo\">(.*?)<div style=\"float:right; margin-bottom:5px;\">",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取location值
    private static final Pattern locationPattern = Pattern.compile(
    		"<td class=\"xmtd1\">所在地区</td>\\s+<td class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取industry
    private static final Pattern industryPattern = Pattern.compile(
    		"<td class=\"xmtd1\">所属行业</td>\\s+<td class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取location industry
    private static final Pattern locationIndustryPattern = Pattern.compile(
    		"<td class=\"xmtd1\">标的所在地区</td>\\s+<td class=\"xmtd2\">(.*?)</td>\\s+<td class=\"xmtd1\">标的所属行业</td>\\s+<td class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取company_type
    private static final Pattern companyTypePattern = Pattern.compile(
    		"<td class=\"xmtd1\">法人类型   </td>\\s+<td class=\"xmtd2\">(.*?)</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL); 
    private static final Pattern companyTypePattern2 = Pattern.compile(
    		"<td class=\"xmtd1\">企业类型   </td>\\s+<td class=\"xmtd2\">(.*?)</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL); 
    //提取project_type
    private static final Pattern projectTypePattern = Pattern.compile(
    		"<td class=\"xmtd1\">项目类别</td>\\s+<td class=\"xmtd2\">(.*?)</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取shares_percentage
    private static final Pattern sharesPercentagePattern = Pattern.compile(
    		"<td class=\"xmtd1\">((转让/增资)|(融资/招商))比例</td>\\s+<td class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取price
    private static final Pattern pricePattern = Pattern.compile(
    		"<td class=\"xmtd1\">((转让/增资)|(融资/招商))金额<br/>（万元）</td>\\s+<td class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern pricePattern2 = Pattern.compile(
    		"<td width=\"11%\" class=\"xmtd1\">挂牌价格</td>\\s+<td width=\"40%\" class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取listing_date值
    //<td class="xmtd1">项目发布起止日期</td>          <td class="xmtd2" colspan="3">2016-01-11 至 2016-01-25&nbsp;</td>
    private static final Pattern listingDatePattern = Pattern.compile(
    		"<td class=\"xmtd1\">项目发布起止日期</td>          <td class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final Pattern listingDatePattern2 = Pattern.compile(
    		"<td class=\"xmtd1\">项目发布起止日期</td>          <td class=\"xmtd2\" colspan=\"3\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取listing_date end_date值
    private static final Pattern datePattern = Pattern.compile(
    		"<td class=\"xmtd1\">挂牌起始日期</td>\\s+<td class=\"xmtd2\">(.*?)</td>\\s+<td class=\"xmtd1\">挂牌期满日期</td>\\s+<td class=\"xmtd2\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取description值
    //TODO http://www.cbex.com.cn/pro/1453101149580307361661.shtml
    //http://www.cbex.com.cn/pro/1452248939307506690941.shtml
    //http://www.cbex.com.cn/pro/1443064774598785755284.shtml
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<td class=\"xmtd1\">项目概况</td>\\s+<td colspan=\"3\" class=\"xmtd2\" style=\"width:500px;\">(.*?)</td>"
    		+ "\\s+</tr>\\s+<tr>\\s+<td class=\"xmtd1\">融资/招商条件</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final Pattern descriptionPattern2 = Pattern.compile(
    		"<td class=\"xmtd1\">项目概况</td>\\s+<td colspan=\"3\" class=\"xmtd2\" style=\"width:500px;\">(.*?)</td>"
    		+ "\\s+</tr>\\s+<tr>\\s+<td class=\"xmtd1\">转让条件</td>",
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
			log.error("获取CbexGQClient网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
	@Override
	public FetchData parseObject(String content, String pageUrl) throws Exception {
		log.debug(content);
		FetchData data = handleData(content);
        data.setPageUrl(pageUrl);
		data.setCountry("中国");
		return data;
	}
	
	private FetchData handleData(String content){
		FetchData data = new FetchData();
		Matcher assetOwnerMatcher = assetOwnerPattern.matcher(content);
	    if (assetOwnerMatcher.find()) {
	      	log.debug(assetOwnerMatcher.group(4).replaceAll("&nbsp;", ""));
	     	data.setAssetOwner(assetOwnerMatcher.group(4).replaceAll("&nbsp;", ""));
	    }
		Matcher nameMatcher = namePattern.matcher(content);
		Matcher nameMatcher2 = namePattern2.matcher(content);
        if (nameMatcher.find()) {
        	log.debug(nameMatcher.group(1));
        	data.setName(nameMatcher.group(1));
        }else if (nameMatcher2.find()) {
        	log.debug(nameMatcher2.group(1).replaceAll("\\s+", ""));
        	data.setName(nameMatcher2.group(1).replaceAll("\\s+", ""));
        }
        Matcher locationMatcher = locationPattern.matcher(content);
        if (locationMatcher.find()) {
        	log.debug(locationMatcher.group(1).replaceAll("\\s+", ""));
        	data.setLocation(locationMatcher.group(1).replaceAll("\\s+", ""));
        }
        Matcher industryMatcher = industryPattern.matcher(content);
        if (industryMatcher.find()) {
        	log.debug(industryMatcher.group(1).replaceAll("\\s+", ""));
			data.setIndustry(industryMatcher.group(1).replaceAll("\\s+", ""));
		}
        
        Matcher locationIndustryMatcher = locationIndustryPattern.matcher(content);
        if (locationIndustryMatcher.find()) {
        	log.debug(locationIndustryMatcher.group(1).replaceAll("&nbsp;", "")+"="+locationIndustryMatcher.group(2).replaceAll("&nbsp;", ""));
			data.setLocation(locationIndustryMatcher.group(1).replaceAll("&nbsp;", ""));
			data.setIndustry(locationIndustryMatcher.group(2).replaceAll("&nbsp;", ""));
		}
        
        Matcher companyTypeMatcher = companyTypePattern.matcher(content);
        Matcher companyTypeMatcher2 = companyTypePattern2.matcher(content);
        if (companyTypeMatcher.find()) {
        	log.debug(companyTypeMatcher.group(1).trim());
			data.setCompanyType(companyTypeMatcher.group(1).trim());
		}else if (companyTypeMatcher2.find()) {
        	log.debug(companyTypeMatcher2.group(1).trim());
			data.setCompanyType(companyTypeMatcher2.group(1).trim());
		}
        
        Matcher projectTypeMatcher = projectTypePattern.matcher(content);
        if (projectTypeMatcher.find()) {
        	log.debug(projectTypeMatcher.group(1).trim());
			data.setProjectType(projectTypeMatcher.group(1).trim());
		}
        
        Matcher sharesPercentageMatcher = sharesPercentagePattern.matcher(content);
        if (sharesPercentageMatcher.find()) {
			log.debug(sharesPercentageMatcher.group(4).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
			data.setSharesPercentage(sharesPercentageMatcher.group(4).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
		}
        Matcher priceMatcher = pricePattern.matcher(content);
        Matcher priceMatcher2 = pricePattern2.matcher(content);
        if (priceMatcher.find()) {
        	log.debug(priceMatcher.group(4).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
			data.setPrice(priceMatcher.group(4).replaceAll("\\s+", "").replaceAll("&nbsp;", ""));
		}else if (priceMatcher2.find()) {
        	log.debug(priceMatcher2.group(1).replaceAll("\\s+", ""));
			data.setPrice(priceMatcher2.group(1).replaceAll("\\s+", ""));
		}
		Matcher listingDateMatcher = listingDatePattern.matcher(content);
		Matcher listingDateMatcher2 = listingDatePattern2.matcher(content);
        if (listingDateMatcher.find()) {
        	log.debug(listingDateMatcher.group(1).replaceAll("&nbsp;", ""));
        	data.setListingDate(listingDateMatcher.group(1).replaceAll("&nbsp;", ""));
        }else if (listingDateMatcher2.find()) {
        	log.debug(listingDateMatcher2.group(1).replaceAll("&nbsp;", ""));
        	data.setListingDate(listingDateMatcher2.group(1).replaceAll("&nbsp;", ""));
        }
        Matcher dateMatcher = datePattern.matcher(content);
        if (dateMatcher.find()) {
        	log.debug(dateMatcher.group(1).replaceAll("&nbsp;", "")+"=="+dateMatcher.group(2).replaceAll("&nbsp;", ""));
        	data.setListingDate(dateMatcher.group(1).replaceAll("&nbsp;", ""));
        	data.setEndDate(dateMatcher.group(2).replaceAll("&nbsp;", ""));
        }
        Matcher descriptionMatcher = descriptionPattern.matcher(content);
        Matcher descriptionMatcher2 = descriptionPattern2.matcher(content);
        if (descriptionMatcher.find()) {
        	log.debug(descriptionMatcher.group(1).replaceAll("<[^>]+>", "").replaceAll("&nbsp;", ""));
			data.setDescription(descriptionMatcher.group(1).replaceAll("<[^>]+>", "").replaceAll("&nbsp;", ""));
		} else if (descriptionMatcher2.find()) {
        	log.debug(descriptionMatcher2.group(1).replaceAll("<[^>]+>", "").replaceAll("&nbsp;", ""));
			data.setDescription(descriptionMatcher2.group(1).replaceAll("<[^>]+>", "").replaceAll("&nbsp;", ""));
		} 
        return data;
	}
}
