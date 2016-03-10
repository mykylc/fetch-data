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
 * rztong
 * @author 
 *
 */
public class RztongClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(50);
	
	private final static String domain = "http://www.rztong.com.cn";
	private final static String webSite = "/xm/index_";
	private final static String suffix = ".htm";
	private static int pageSize = 20;
	private static int currentPage = 1;
	private final String charset = "gb2312";
	
	// 提取总条数的正则表达式
    private static final Pattern totalCountPattern = Pattern.compile(
    		"<td colspan=3>&nbsp;共有 <font color=\"#FF0000\">(.*?)</font> 个项目　</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // 提取URL列表的正则表达式
    private static final Pattern urlListPattern = Pattern.compile(
    		"<td colspan=2>\\s+<a href=\"(.*?)\" target=\"_blank\".*?title=\".*?\">.*?</a>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<td height=\"39\" colspan=\"2\" align=\"center\"><span class=\"style1\">(.*?)</span></td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern namePattern2 = Pattern.compile(
    		"<DIV align=center class=xmtitle><b>(.*?)</b></DIV>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final Pattern projectTypePattern = Pattern.compile(
    		"<TD  width=33%>.*?项目类别:(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
    //提取listingTime
    private static final Pattern listingTimePattern = Pattern.compile(
    		"<td colspan=\"3\" align=\"right\"><strong>发布日期:</strong>(.*?)</td></tr>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern listingTimePattern2 = Pattern.compile(
    		"<TD  width=33%>.*?发布日期：(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
    //提取industry
    private static final Pattern industryPattern = Pattern.compile(
    		"<td width=\".*?\" align=\"right\"><strong>所属行业：</strong></td>\\s+<td width=\".*?\" align=\"left\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern industryPattern2 = Pattern.compile(
    		"<TD  width=33%>.*?所属行业:(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
    //提取status
    private static final Pattern statusPattern = Pattern.compile(
    		"<td width=\"9%\" align=\"right\"><strong>现处阶段：</strong></td>\\s+<td width=\"20%\" align=\"left\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern statusPattern2 = Pattern.compile(
    		"<td height=\"6\" align=\"right\"><strong>现处阶段：</strong></td>\\s+<td align=\"left\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取patent
    private static final Pattern patentPattern = Pattern.compile(
    		"<td height=\"14\" align=\"right\"><strong>获专利情况：</strong></td>\\s+<td align=\"left\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern patentPattern2 = Pattern.compile(
    		"<td width=\"11%\" align=\"right\"><strong>获专利情况：</strong></td>\\s+<td width=\"22%\" align=\"left\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern patentPattern3 = Pattern.compile(
    		"<TD width=33%>.*?获专利情况:(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
    //提取location值
    private static final Pattern locationPattern = Pattern.compile(
    		"<td align=\"right\"><strong>项目所在地：</strong></td>\\s+<td align=\"left\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern locationPattern2 = Pattern.compile(
    		"<td width=\"79\"  align=\"right\"><strong>项目所在地：</strong></td>\\s+<td  width=\"140\" align=\"left\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern locationPattern3 = Pattern.compile(
    		"<td align=right>项目所在地：</td>\\s+<td>(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern locationPattern4 = Pattern.compile(
    		"<TD width=33%>.*?项目所在地：(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
    private static final Pattern statusPattern3 = Pattern.compile(
    		"<TD width=34%>.*?现处阶段：(.*?) </TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取price值
    private static final Pattern pricePattern = Pattern.compile(
    		"<td align=\"right\"><strong>融资额：</strong></td>\\s+<td align=\"left\">(.*?)</td>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern pricePattern2 = Pattern.compile(
    		"<TD  width=33%>.*?融资额:(.*?)</TD>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取financingMode
    private static final Pattern financingModePattern = Pattern.compile(
    		"<td height=\"14\" align=\"right\"><strong>融资方式：</strong></td>\\s+<td align=\"left\">(.*?)</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取expected investment area
    private static final Pattern expectedInvestmentAreaPattern = Pattern.compile(
    		"<td   align=\"right\"><strong>招商地区：</strong></td>\\s+<td   colspan=5 align=left>(.*?)</td>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<h4>(.*?)</h4>(.*?)<div.*?>(.*?)</div>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private static final Pattern descriptionPattern2 = Pattern.compile(
    		"<TD vAlign=top  height=300   style=\"word-break:break-all;font-size:14px;\">(.*?)</TD></TR>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage + suffix;
			final Page page = getPage(url, totalCountPattern, pageSize, charset);
			if (page == null) {
				return;
			}
			log.debug(page.getTotalCount()+"");
			getUrlListByPage2(url, urlListPattern, domain, charset);
			while (page.hasNextPage()) {
				semp.acquire();
				int next = page.getNextPage();
				final int nextPage = next - 1;
				page.setCurrentPage(next);
				FetchDataThreadPool.exec.execute(new Runnable() {
					@Override
					public void run() {
						try {
							String urlPage = domain + webSite + nextPage + suffix;
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
				if (semp.availablePermits()==50) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("获取rztong网站报错："+e.getMessage(), e);
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
			List<String> urlList = new ArrayList<String>();
			HttpUtils httpUtils = new HttpUtils(url, charset);
			String result = httpUtils.execute();
			if (result==null) {
				return;
			}
			log.debug(result);
			Matcher urlListmatcher = urlListPattern.matcher(result);
            while (urlListmatcher.find()) {
            	String hrefUrl = urlListmatcher.group(1);
            	if (hrefUrl.startsWith("http://")) {
            		urlList.add(hrefUrl);
            		log.debug(hrefUrl);
				} else {
					urlList.add(domain+hrefUrl.replace("..", ""));
					log.debug(domain+hrefUrl.replace("..", ""));
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
		log.debug(content);
		FetchData data = new FetchData();
		if (pageUrl.equals("http://www.rztong.com.cn/xm/xm48803.htm")) {
			return null;
		}
		data.setPageUrl(pageUrl);
		data.setCountry("中国");
		Matcher nameMatcher = namePattern.matcher(content);
		if (nameMatcher.find()) {
			log.debug(nameMatcher.group(1).replaceAll("\\s+", ""));
			data.setName(nameMatcher.group(1).replaceAll("\\s+", ""));
		}
		Matcher nameMatcher2 = namePattern2.matcher(content);
		if (nameMatcher2.find()) {
			log.debug(nameMatcher2.group(1).replaceAll("\\s+", ""));
			data.setName(nameMatcher2.group(1).replaceAll("\\s+", ""));
		}
		Matcher projectTypeMatcher = projectTypePattern.matcher(content);
		if (projectTypeMatcher.find()) {
			log.debug(projectTypeMatcher.group(1).replaceAll("\\s+", ""));
			data.setProjectType(projectTypeMatcher.group(1).replaceAll("\\s+", ""));
		}
		
		
		Matcher listingTimeMatcher = listingTimePattern.matcher(content);
		if (listingTimeMatcher.find()) {
			log.debug(listingTimeMatcher.group(1).replaceAll("\\s+", ""));
			data.setListingTime(listingTimeMatcher.group(1).replaceAll("\\s+", ""));
		}
		Matcher listingTimeMatcher2 = listingTimePattern2.matcher(content);
		if (listingTimeMatcher2.find()) {
			log.debug(listingTimeMatcher2.group(1).replaceAll("\\s+", ""));
			data.setListingTime(listingTimeMatcher2.group(1).replaceAll("\\s+", ""));
		}
		Matcher industryMatcher = industryPattern.matcher(content);
		if (industryMatcher.find()) {
			log.debug(industryMatcher.group(1).replaceAll("\\s+", ""));
			data.setIndustry(industryMatcher.group(1).replaceAll("\\s+", ""));
		}
		Matcher industryMatcher2 = industryPattern2.matcher(content);
		if (industryMatcher2.find()) {
			log.debug(industryMatcher2.group(1).replaceAll("\\s+", ""));
			data.setIndustry(industryMatcher2.group(1).replaceAll("\\s+", ""));
		}
		Matcher statusMatcher = statusPattern.matcher(content);
		if (statusMatcher.find()) {
			log.debug(statusMatcher.group(1).replaceAll("\\s+", ""));
			data.setStatus(statusMatcher.group(1).replaceAll("\\s+", ""));
		}
		Matcher statusMatcher2 = statusPattern2.matcher(content);
		if (statusMatcher2.find()) {
			log.debug(statusMatcher2.group(1).replaceAll("\\s+", ""));
			data.setStatus(statusMatcher2.group(1).replaceAll("\\s+", ""));
		}
		Matcher statusMatcher3 = statusPattern3.matcher(content);
		if (statusMatcher3.find()) {
			log.debug(statusMatcher3.group(1).replaceAll("\\s+", ""));
			data.setStatus(statusMatcher3.group(1).replaceAll("\\s+", ""));
		}
		Matcher patentMatcher = patentPattern.matcher(content);
		if (patentMatcher.find()) {
			log.debug(patentMatcher.group(1).replaceAll("\\s+", ""));
			data.setPatent(patentMatcher.group(1).replaceAll("\\s+", ""));
		}
		Matcher patentMatcher2 = patentPattern2.matcher(content);
		if (patentMatcher2.find()) {
			log.debug(patentMatcher2.group(1).replaceAll("\\s+", ""));
			data.setPatent(patentMatcher2.group(1).replaceAll("\\s+", ""));
		}
		Matcher patentMatcher3 = patentPattern3.matcher(content);
		if (patentMatcher3.find()) {
			log.debug(patentMatcher3.group(1).replaceAll("\\s+", ""));
			data.setPatent(patentMatcher3.group(1).replaceAll("\\s+", ""));
		}
		Matcher locationMatcher = locationPattern.matcher(content);
		if (locationMatcher.find()) {
			log.debug(locationMatcher.group(1).replaceAll("\\s+", ""));
			data.setLocation(locationMatcher.group(1).replaceAll("\\s+", ""));
		}
		Matcher locationMatcher2 = locationPattern2.matcher(content);
		if (locationMatcher2.find()) {
			log.debug(locationMatcher2.group(1).replaceAll("\\s+", ""));
			data.setLocation(locationMatcher2.group(1).replaceAll("\\s+", ""));
		}
		Matcher locationMatcher3 = locationPattern3.matcher(content);
		if (locationMatcher3.find()) {
			log.debug(locationMatcher3.group(1).replaceAll("\\s+", ""));
			data.setLocation(locationMatcher3.group(1).replaceAll("\\s+", ""));
		}
		Matcher locationMatcher4 = locationPattern4.matcher(content);
		if (locationMatcher4.find()) {
			log.debug(locationMatcher4.group(1).replaceAll("\\s+", ""));
			data.setLocation(locationMatcher4.group(1).replaceAll("\\s+", ""));
		}
		Matcher priceMatcher = pricePattern.matcher(content);
		if (priceMatcher.find()) {
			log.debug(priceMatcher.group(1).replaceAll("\\s+", ""));
			data.setPrice(priceMatcher.group(1).replaceAll("\\s+", ""));
		}
		Matcher priceMatcher2 = pricePattern2.matcher(content);
		if (priceMatcher2.find()) {
			log.debug(priceMatcher2.group(1).replaceAll("\\s+", ""));
			data.setPrice(priceMatcher2.group(1).replaceAll("\\s+", ""));
		}
		Matcher financingModeMatcher = financingModePattern.matcher(content);
		if (financingModeMatcher.find()) {
			log.debug(financingModeMatcher.group(1).replaceAll("\\s+", ""));
			data.setFinancingMode(financingModeMatcher.group(1).replaceAll("\\s+", ""));
		}
		
		Matcher expectedInvestmentAreaMatcher = expectedInvestmentAreaPattern.matcher(content);
		if (expectedInvestmentAreaMatcher.find()) {
			log.debug(expectedInvestmentAreaMatcher.group(1).replaceAll("\\s+", ""));
			data.setExpectedInvestmentArea(expectedInvestmentAreaMatcher.group(1).replaceAll("\\s+", ""));
		}
		Matcher descriptionMatcher = descriptionPattern.matcher(content);
		if (descriptionMatcher.find()) {
			String desc1 = descriptionMatcher.group(1).replaceAll("\\s+", "");
			String desc2 = descriptionMatcher.group(2).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "");
			String desc3 = descriptionMatcher.group(3).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "");
			String desc = desc1+desc2+desc3;
			//log.info(desc);
			desc = desc.replaceAll("[^\\u0000-\\uFFFF]", "");//过滤掉4个字节的UTF-8
			data.setDescription(desc);
		}
		
		Matcher descriptionMatcher2 = descriptionPattern2.matcher(content);
		if (descriptionMatcher2.find()) {
			log.debug(descriptionMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher2.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		return data;
	}
}
