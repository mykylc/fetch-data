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
 * 中国大买手 
 * @author 
 *
 */
public class ChinaMergerClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(10);
	
	private final static String domain = "http://www.chinamerger.com";
	private final static String webSite = "/website/loginAction!seemoresale.action?saleBusinese.industry=&cpage=";
	private static int pageSize = 5;
	private static int currentPage = 1;
	private final String charset = "UTF-8";
	
	// 提取总条数的正则表达式
    private static final Pattern totalCountPattern = Pattern.compile("<span class=\"add_font_o2\">(.*?)</span>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile("<span class=\"add_big_blue_list \" style=\" display: inline-block; margin-left: 12px;\"><a href=\"(.*?)\" class=\"add_big_blue2\"", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile("<div class=\"s_content\">\\s+(.*?)\\s+</div>");
    
    //提取listing_date值
    private static final Pattern listingDatePattern = Pattern.compile("<span>发布时间：</span>\\s+<font>(.*?)</font>");
    
    //提取end_date值
    private static final Pattern endDatePattern = Pattern.compile("<span>到期日：</span>\\s+<font>(.*?)</font>");
    
    //提取industry
    private static final Pattern industryPattern = Pattern.compile("<strong>行业：</strong>\\s+<span>(.*?)</span>");
    
    //提取country
    private static final Pattern countryPattern = Pattern.compile("<strong>区域：</strong>\\s+<span>(.*?)</span>");
    
    //提取shares_percentage
    private static final Pattern sharesPercentagePattern = Pattern.compile("<strong>标的类型：</strong>\\s+<span>\\s+(.*?)\\s+</span>");
    
    //提取shares_percentage 的%
    private static final Pattern sharesPercentagePartPattern = Pattern.compile("(.*?)\\s+<font style=\"margin-left:5px;\">(.*?)</font>");
    
    //提取project_type
    private static final Pattern projectTypePattern = Pattern.compile("<strong>交易方式：</strong>\\s+<span>\\s+(.*?)\\s+</span>");
    
    //提取price
    private static final Pattern pricePattern = Pattern.compile("<strong>价格：</strong>\\s+<span>\\s+(.*?)\\s+</span>");
    
    //提取high_light
    private static final Pattern highLightPattern = Pattern.compile("<div class=\"entia_a\">项目亮点</div>\\s+<div class=\"article\">(.*?)</div>");
    
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile("<div class=\"entia_a\">项目描述</div>\\s+<div class=\"article\">(.*?)</div>");
    
    //提取asset_owner
    private static final Pattern assetOwnerPattern = Pattern.compile("<div class=\"entia_a\">项目所有者描述</div>\\s+<div class=\"article\">(.*?)</div>");
    
    //提取reason_for_sale
    private static final Pattern reasonForSalePattern = Pattern.compile("<div class=\"entia_a\">出售原因</div>\\s+<div class=\"article\">(.*?)</div>");
    
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
			log.error("获取ChainMerger网站报错："+e.getMessage(), e);
			throw e;
		}
	}
	
	@Override
	public FetchData parseObject(String content, String pageUrl) throws Exception {
		FetchData data = new FetchData();
		data.setPageUrl(pageUrl);
		log.debug(content);
		Matcher nameMatcher = namePattern.matcher(content);
        if (nameMatcher.find()) {
        	data.setName(nameMatcher.group(1));
        }
		Matcher listingDateMatcher = listingDatePattern.matcher(content);
        if (listingDateMatcher.find()) {
        	data.setListingDate(listingDateMatcher.group(1));
        }
        Matcher endDateMatcher = endDatePattern.matcher(content);
        if (endDateMatcher.find()) {
			data.setEndDate(endDateMatcher.group(1));
		}
        Matcher industryMatcher = industryPattern.matcher(content);
        if (industryMatcher.find()) {
			data.setIndustry(industryMatcher.group(1));
		}
        Matcher countryMatcher = countryPattern.matcher(content);
        if (countryMatcher.find()) {
			data.setCountry(countryMatcher.group(1));
		}
        Matcher sharesPercentageMatcher = sharesPercentagePattern.matcher(content);
        if (sharesPercentageMatcher.find()) {
			String shartsPercenttage = sharesPercentageMatcher.group(1);
			Matcher sharesPercentagePartMatcher = sharesPercentagePartPattern.matcher(shartsPercenttage);
			if (sharesPercentagePartMatcher.find()) {
				data.setSharesPercentage(sharesPercentagePartMatcher.group(1)+" "+sharesPercentagePartMatcher.group(2));
			} else {
				data.setSharesPercentage(sharesPercentageMatcher.group(1));
			}
		}
        Matcher projectTypeMatcher = projectTypePattern.matcher(content);
        if (projectTypeMatcher.find()) {
			data.setProjectType(projectTypeMatcher.group(1));
		}
        Matcher priceMatcher = pricePattern.matcher(content);
        if (priceMatcher.find()) {
			data.setPrice(priceMatcher.group(1).replaceAll("\\s+", ""));
		}
        Matcher highLightMatcher = highLightPattern.matcher(content);
        if (highLightMatcher.find()) {
			data.setHighlight(highLightMatcher.group(1).replaceAll("<br>", "").replaceAll("<[^>]+>", ""));
		}
        
        Matcher descriptionMatcher = descriptionPattern.matcher(content);
        if (descriptionMatcher.find()) {
        	String description = descriptionMatcher.group(1).replaceAll("<br>", "").replaceAll("<[^>]+>", "");
        	description = description.replaceAll("[^\\u0000-\\uFFFF]", "");//过滤掉4个字节的UTF-8
			log.debug(description);
			data.setDescription(description);
		}
        Matcher assetOwnerMatcher = assetOwnerPattern.matcher(content);
        if (assetOwnerMatcher.find()) {
        	//log.debug("1=="+assetOwnerMatcher.group(1).replaceAll("<[^>]+>", ""));
			data.setAssetOwner(assetOwnerMatcher.group(1).replaceAll("<[^>]+>", ""));
		}
        Matcher reasonForSaleMatcher = reasonForSalePattern.matcher(content);
        if (reasonForSaleMatcher.find()) {
        	//log.debug(reasonForSaleMatcher.group(1));
			data.setReasonForSale(reasonForSaleMatcher.group(1));
		}
		return data;
	}
}
