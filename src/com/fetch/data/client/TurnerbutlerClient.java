package com.fetch.data.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fetch.data.domain.FetchData;
import com.fetch.data.domain.Page;
import com.fetch.data.tools.HttpUtils;

/**
 * Turnerbutler
 * @author 
 *
 */
public abstract class TurnerbutlerClient extends AbstractClient{
	
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<center><h2 style=\"font-size:30px;color: #125a96;  width:100%; padding-top:4px; padding-bottom:1px;margin-bottom:10px;\"><p style=\"text-align: left;\">(.*?)</p></h2></center>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取location
    private static final Pattern locationPattern = Pattern.compile(
    		"<p><strong>Location : </strong>(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Price
    private static final Pattern pricePattern = Pattern.compile(
    		"<p><strong>Asking Price :</strong>(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取Turnover
    private static final Pattern turnoverPattern = Pattern.compile(
    		"<p><strong>Turnover :</strong>(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Net Profit值
    private static final Pattern netProfitPattern = Pattern.compile(
    		"<p><strong>Net Profit : </strong>(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //提取Gross Profit值
    private static final Pattern grossProfitPattern = Pattern.compile(
    		"<p><strong>Gross Profit : </strong>(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取EBITDA值
    private static final Pattern EBITDAPattern = Pattern.compile(
    		"<p><strong>EBITDA :</strong>(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取Asset值
    private static final Pattern assetPattern = Pattern.compile(
    		"<p><strong>Net Asset :</strong>(.*?)</p>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<strong>Business Profile</strong> </span>(.*?)<h4><span style=\"color: #125a96;\">", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);   
    //提取potential
    private static final Pattern potentialPattern = Pattern.compile(
    		"<h4><span style=\"color: #125a96;\"><strong>Key Considerations</strong></span></h4>(.*?)<h4><span style=\"color: #125a96;\">", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);   
    
    //提取advantage
    private static final Pattern advantagePattern = Pattern.compile(
    		"<h4><span style=\"color: #125a96;\"><strong>Key Strengths:</strong><strong>&nbsp;</strong></span></h4>(.*?)<h4><span style=\"color: #125a96;\">", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);  
    //提取financialInformation
    private static final Pattern financialInformationPattern = Pattern.compile(
    		"<h4 style=\"text-align: justify;\"><span style=\"color: #125a96;\"><strong>Financial profile:</strong></span></h4>(.*?)<h4 style=\"text-align: justify;\">", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
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
                String pageCount = pageCountMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "");
                Page page = new Page();
                page.setPageSize(pageSize);
                page.setPageCount(Integer.parseInt(pageCount));
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
		log.info(content);
		FetchData data = new FetchData();
		data.setPageUrl(pageUrl);
		data.setCountry(getCountry());
		data.setIndustry(getIndustry());
		Matcher nameMatcher = namePattern.matcher(content);
		if (nameMatcher.find()) {
			log.debug(nameMatcher.group(1));
			data.setName(nameMatcher.group(1));
		}
		Matcher locationMatcher = locationPattern.matcher(content);
		if (locationMatcher.find()) {
			log.debug(locationMatcher.group(1).replaceAll("<[^>]+>", ""));
			data.setLocation(locationMatcher.group(1).replaceAll("<[^>]+>", ""));
		}
		Matcher priceMatcher = pricePattern.matcher(content);
		if (priceMatcher.find()) {
			log.debug(priceMatcher.group(1).replaceAll("&pound;", "£").replaceAll("\\s+", ""));
			data.setPrice(priceMatcher.group(1).replaceAll("&pound;", "£").replaceAll("\\s+", ""));
		}
		Matcher turnoverMatcher = turnoverPattern.matcher(content);
		if (turnoverMatcher.find()) {
			log.debug(turnoverMatcher.group(1).replaceAll("&pound;", "£").replaceAll("\\s+", ""));
			data.setTurnover(turnoverMatcher.group(1).replaceAll("&pound;", "£").replaceAll("\\s+", ""));
		}
		Matcher netProfitMatcher = netProfitPattern.matcher(content);
		if (netProfitMatcher.find()) {
			log.debug(netProfitMatcher.group(1).replaceAll("\\s+", ""));
			data.setNetProfit(netProfitMatcher.group(1).replaceAll("\\s+", ""));
		}
		Matcher grossProfitMatcher = grossProfitPattern.matcher(content);
		if (grossProfitMatcher.find()) {
			log.debug(grossProfitMatcher.group(1).replaceAll("&pound;", "£").replaceAll("\\s+", ""));
			data.setGrossProfit(grossProfitMatcher.group(1).replaceAll("&pound;", "£").replaceAll("\\s+", ""));
		}
		Matcher EBITDAMatcher = EBITDAPattern.matcher(content);
		if (EBITDAMatcher.find()) {
			log.debug(EBITDAMatcher.group(1).replaceAll("&pound;", "£").replaceAll("\\s+", ""));
			data.setEBITDA(EBITDAMatcher.group(1).replaceAll("&pound;", "£").replaceAll("\\s+", ""));
		}
		Matcher assetMatcher = assetPattern.matcher(content);
		if (assetMatcher.find()) {
			log.debug(assetMatcher.group(1).replaceAll("&pound;", "£").replaceAll("\\s+", ""));
			data.setAsset(assetMatcher.group(1).replaceAll("&pound;", "£").replaceAll("\\s+", ""));
		}
		
		Matcher descriptionMatcher = descriptionPattern.matcher(content);
		if (descriptionMatcher.find()) {
			log.debug(descriptionMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher potentialMatcher = potentialPattern.matcher(content);
		if (potentialMatcher.find()) {
			log.debug(potentialMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setPotential(potentialMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		Matcher advantageMatcher = advantagePattern.matcher(content);
		if (advantageMatcher.find()) {
			log.debug(advantageMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setAdvantage(advantageMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher financialInformationMatcher = financialInformationPattern.matcher(content);
		if (financialInformationMatcher.find()) {
			log.debug(financialInformationMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setFinancialInformation(financialInformationMatcher.group(1).replaceAll("\\s+", "").replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		return data;
	}
	
	public abstract String getCountry();
	public abstract String getIndustry();
}
