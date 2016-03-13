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
    		"<strong>Business Profile.*?</span>(.*?)<h4>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL); 
    private static final Pattern descriptionPattern2 = Pattern.compile(
    		"<strong>Business Profile</strong> </span>(.*?)<h4 style=\"text-align: justify;\">", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern descriptionPattern3 = Pattern.compile(
    		"Introduction.*?</h4>(.*?)<h4", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取potential
    private static final Pattern potentialPattern = Pattern.compile(
    		"Key Considerations.*?</h4>(.*?)<h4", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);   
//    private static final Pattern potentialPattern2 = Pattern.compile(
//    		"<h4 style=\"text-align: justify;\"><span style=\"color: #125a96;\"> <strong>Key Considerations</strong></span></h4>(.*?)<h4 style=\"text-align: justify;\">", 
//    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    
    //提取advantage
    private static final Pattern advantagePattern = Pattern.compile(
    		"(Key strengths:|Key Strengths:).*?</p>(.*?)<h4", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern advantagePattern1 = Pattern.compile(
    		"(Key strengths:|Key Strengths:).*?</h4>(.*?)<h4", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
//    private static final Pattern advantagePattern1 = Pattern.compile(
//    		"<h4><span style=\"color: #125a96;\"><strong>Key Strengths:</strong><strong>&nbsp;</strong></span></h4>(.*?)<h4><span style=\"color: #125a96;\">", 
//    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);  
//    private static final Pattern advantagePattern2 = Pattern.compile(
//    		"<span style=\"color: #125a96;\"> <strong>Key strengths:</strong> </span></p>(.*?)<h4 style=\"text-align: justify;\">", 
//    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);  
//    private static final Pattern advantagePattern3 = Pattern.compile(
//    		"<span style=\"color: #125a96;\"><strong>Key Strengths:</strong> </span></p>(.*?)<h4 style=\"text-align: justify;\">", 
//    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL); 
//    private static final Pattern advantagePattern4 = Pattern.compile(
//    		"<h4 style=\"text-align: justify;\"><span style=\"color: #125a96;\"><strong>Key Strengths:</strong></span></h4>(.*?)<h4 style=\"text-align: justify;\">", 
//    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL); 
    private static final Pattern advantagePattern5 = Pattern.compile(
    		"(Key strengths|Key Strengths):.*?</p>(.*?)<p><span style=\"color: #125a96;\">\\s+<strong>Key (opportunities|Opportunities):</strong>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL); 
//    private static final Pattern advantagePattern6 = Pattern.compile(
//    		"<span style=\"color: #125a96;\">Key Strengths:</span></strong></h4>(.*?)<h4><span style=\"color: #125a96;\">", 
//    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL); 
//    private static final Pattern advantagePattern7 = Pattern.compile(
//    		"<h4 style=\"text-align: justify;\"><span style=\"color: #125a96;\"> <strong> Key Strengths:</strong></span></h4>(.*?)<h4 style=\"text-align: justify;\">", 
//    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
//    private static final Pattern advantagePattern8 = Pattern.compile(
//    		"<strong>Key Strengths:</strong></span></p>(.*?)<p><span style=\"color: #125a96;\"> <strong>Key Opportunities:</strong>", 
//    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
//    private static final Pattern advantagePattern9 = Pattern.compile(
//    		"<strong>Key Strengths:</strong></span></p>(.*?)<h4 style=\"text-align: justify;\"><span style=\"color: #125a96;\"><strong>Growth and Expansion</strong>", 
//    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
//    private static final Pattern advantagePattern10 = Pattern.compile(
//    		"<strong>Key Strengths:</strong></span></h4>(.*?)<h4 style=\"text-align: justify;\">", 
//    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
//    private static final Pattern advantagePattern11 = Pattern.compile(
//    		"<strong>Key strengths:</strong> </span></h4>(.*?)<h4 style=\"text-align: justify;\">", 
//    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取financialInformation
    private static final Pattern financialInformationPattern = Pattern.compile(
    		"(Financial profile|Financial Information):.*?</p>(.*?)<p>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern financialInformationPattern1 = Pattern.compile(
    		"(Financial profile|Financial Information):.*?</p>(.*?)<h4", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern financialInformationPattern2 = Pattern.compile(
    		"(Financial profile|Financial Information):.*?</h4>(.*?)<span", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern financialInformationPattern3 = Pattern.compile(
    		"(Financial profile|Financial Information):.*?</h4>(.*?)<h4", 
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
    
	/**
	 * 获取文章内容
	 * @param url
	 * @return FetchData
	 * @throws Exception
	 */
	@Override
	public FetchData getContent(String url, String charset) throws Exception{
		try {
			if (url.indexOf("http://www.turnerbutler.co.uk/memo.php?id=IK272")!=-1) {
				url = "http://www.turnerbutler.co.uk/memo.php?id=IK272";
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
	public FetchData parseObject(String content, String pageUrl) throws Exception {
		log.debug(content);
		FetchData data = new FetchData();
		data.setPageUrl(pageUrl);
		data.setCountry("UK");
		data.setIndustry(getIndustry());
		Matcher nameMatcher = namePattern.matcher(content);
		if (nameMatcher.find()) {
			log.debug(nameMatcher.group(1).replaceAll("<[^>]+>", ""));
			data.setName(nameMatcher.group(1).replaceAll("<[^>]+>", ""));
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
			log.debug(netProfitMatcher.group(1).replaceAll("\\s+", "").replaceAll("&pound;", "£"));
			data.setNetProfit(netProfitMatcher.group(1).replaceAll("\\s+", "").replaceAll("&pound;", "£"));
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
			log.debug(descriptionMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher descriptionMatcher2 = descriptionPattern2.matcher(content);
		if (descriptionMatcher2.find()) {
			log.debug(descriptionMatcher2.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setDescription(descriptionMatcher2.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		if (data.getDescription()==null || "".equals(data.getDescription()) || data.getDescription().length()==0) {
			Matcher descriptionMatcher3 = descriptionPattern3.matcher(content);
			if (descriptionMatcher3.find()) {
				log.debug(descriptionMatcher3.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
				data.setDescription(descriptionMatcher3.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			}
		}
		
		Matcher potentialMatcher = potentialPattern.matcher(content);
		if (potentialMatcher.find()) {
			log.debug(potentialMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "")
					.replaceAll("&rsquo;", "’").replaceAll("&bull;", "•").replaceAll("&pound;", "£"));
			data.setPotential(potentialMatcher.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "")
					.replaceAll("&rsquo;", "’").replaceAll("&bull;", "•").replaceAll("&pound;", "£"));
		}
//		Matcher potentialMatcher2 = potentialPattern2.matcher(content);
//		if (potentialMatcher2.find()) {
//			log.debug(potentialMatcher2.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "")
//					.replaceAll("&rsquo;", "’").replaceAll("&bull;", "•").replaceAll("&pound;", "£"));
//			data.setPotential(potentialMatcher2.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "")
//					.replaceAll("&rsquo;", "’").replaceAll("&bull;", "•").replaceAll("&pound;", "£"));
//		}
		
		
		Matcher advantageMatcher = advantagePattern.matcher(content);
		if (advantageMatcher.find()) {
			log.debug(advantageMatcher.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
			data.setAdvantage(advantageMatcher.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
		}
		
		Matcher advantageMatcher1 = advantagePattern1.matcher(content);
		if (advantageMatcher1.find()) {
			log.debug(advantageMatcher1.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
			data.setAdvantage(advantageMatcher1.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
		}
//		Matcher advantageMatcher2 = advantagePattern2.matcher(content);
//		if (advantageMatcher2.find()) {
//			log.debug(advantageMatcher2.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//			data.setAdvantage(advantageMatcher2.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//		}
//		Matcher advantageMatcher3 = advantagePattern3.matcher(content);
//		if (advantageMatcher3.find()) {
//			log.debug(advantageMatcher3.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//			data.setAdvantage(advantageMatcher3.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//		}
//		Matcher advantageMatcher4 = advantagePattern4.matcher(content);
//		if (advantageMatcher4.find()) {
//			log.debug(advantageMatcher4.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//			data.setAdvantage(advantageMatcher4.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//		}
		Matcher advantageMatcher5 = advantagePattern5.matcher(content);
		if (advantageMatcher5.find()) {
			log.debug(advantageMatcher5.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
			data.setAdvantage(advantageMatcher5.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
		}
//		Matcher advantageMatcher6 = advantagePattern6.matcher(content);
//		if (advantageMatcher6.find()) {
//			log.debug(advantageMatcher6.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//			data.setAdvantage(advantageMatcher6.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//		}
//		Matcher advantageMatcher7 = advantagePattern7.matcher(content);
//		if (advantageMatcher7.find()) {
//			log.debug(advantageMatcher7.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//			data.setAdvantage(advantageMatcher7.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//		}
//		Matcher advantageMatcher8 = advantagePattern8.matcher(content);
//		if (advantageMatcher8.find()) {
//			log.debug(advantageMatcher8.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//			data.setAdvantage(advantageMatcher8.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//		}
//		Matcher advantageMatcher9 = advantagePattern9.matcher(content);
//		if (advantageMatcher9.find()) {
//			log.debug(advantageMatcher9.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//			data.setAdvantage(advantageMatcher9.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//		}
//		Matcher advantageMatcher10 = advantagePattern10.matcher(content);
//		if (advantageMatcher10.find()) {
//			log.debug(advantageMatcher10.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//			data.setAdvantage(advantageMatcher10.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//		}
//		Matcher advantageMatcher11 = advantagePattern11.matcher(content);
//		if (advantageMatcher11.find()) {
//			log.debug(advantageMatcher11.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//			data.setAdvantage(advantageMatcher11.group(1).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&bull;", "•").replaceAll("&ndash;", "–"));
//		}
		Matcher financialInformationMatcher = financialInformationPattern.matcher(content);
		if (financialInformationMatcher.find()) {
			log.debug(financialInformationMatcher.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setFinancialInformation(financialInformationMatcher.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher financialInformationMatcher1 = financialInformationPattern1.matcher(content);
		if (financialInformationMatcher1.find()) {
			log.debug(financialInformationMatcher1.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setFinancialInformation(financialInformationMatcher1.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		Matcher financialInformationMatcher2 = financialInformationPattern2.matcher(content);
		if (financialInformationMatcher2.find()) {
			log.debug(financialInformationMatcher2.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&pound;", "£"));
			data.setFinancialInformation(financialInformationMatcher2.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", "").replaceAll("&pound;", "£"));
		}
		Matcher financialInformationMatcher3 = financialInformationPattern3.matcher(content);
		if (financialInformationMatcher3.find()) {
			log.debug(financialInformationMatcher3.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
			data.setFinancialInformation(financialInformationMatcher3.group(2).replaceAll("&nbsp;", "").replaceAll("<[^>]+>", ""));
		}
		
		return data;
	}
	
	public abstract String getIndustry();
}
