package com.fetch.data.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fetch.data.domain.FetchData;
import com.fetch.data.domain.Page;
import com.fetch.data.tools.FetchDataThreadPool;
import com.fetch.data.tools.HttpUtils;

/**
 * FirstNet
 * @author 
 *
 */
public class FirstNetClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(20);
	
	private final static String domain = "http://www.first-net.cn";
	private final static String webSite = "/portal/project_showLatestInvestmentList?filter=1&latestInvestment.from=pt&pagination.pageSize=20&pagination.pageIndex=";
	private final static String prefix = "http://www.first-net.cn/portal/project_introduction?project.id=";
	private final static String prefix2 = "http://www.first-net.cn/portal/plan_queryPlanSummaryById?plan.id=";
	
	private static int pageSize = 20;
	private static int currentPage = 0;
	private final String charset = "utf-8";
	
	private Map<String, FetchData> map = new HashMap<String, FetchData>();
	
    //提取name值
    private static final Pattern namePattern = Pattern.compile(
    		"<div class=\"til\">(.*?)</div>",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //提取description
    private static final Pattern descriptionPattern = Pattern.compile(
    		"<dd class=\"text\">(.*?)<b>融资预算：</b>", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    @Override
	public void fetchData() throws Exception{
		try {
			String url = domain + webSite + currentPage;
			final Page page = getPage2(url, pageSize, charset);
			if (page == null) {
				return;
			}
			log.debug(page.getTotalCount()+"");
			getUrlListByPage2(url, charset);
			while (page.hasNextPage()) {
				semp.acquire();
				int next = page.getNextPage();
				final int nextPage = next - 1;
				page.setCurrentPage(next);
				FetchDataThreadPool.exec.execute(new Runnable() {
					@Override
					public void run() {
						try {
							String urlPage = domain + webSite + nextPage;
							getUrlListByPage2(urlPage, charset);
						} catch (Exception e) {
							log.error("启动线程报错："+e.getMessage(), e);
						} finally {
							semp.release();
						}
					}
				});
			}
			while (true) {
				if (semp.availablePermits()==20) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("获取FirstNet网站报错："+e.getMessage(), e);
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
	public Page getPage2(String url, int pageSize, String charset) throws Exception{
		try {
			HttpUtils httpUtils = new HttpUtils(url, charset);
			String result = httpUtils.execute();
			if (result==null) {
				return null;
			}
			log.debug(result);
			JSONObject jsonObject = JSONObject.parseObject(result);
			Page page = new Page();
			page.setTotalCount(Integer.parseInt(jsonObject.getString("total")));
			page.setPageSize(pageSize);
            page.setPageCount();
            return page;
		} catch (Exception e) {
			log.error(String.format("[FirstNetClient.getPage] url=%s; 获取page对象报错：%s", url, e.getMessage()), e);
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
	public void getUrlListByPage2(String url, String charset) throws Exception{
		try {
			List<String> urlList = new ArrayList<String>();
			HttpUtils httpUtils = new HttpUtils(url, charset);
			String result = httpUtils.execute();
			if (result==null) {
				return;
			}
			log.debug(result);
			JSONObject jsonObject = JSONObject.parseObject(result);
			JSONArray jsonArray = jsonObject.getJSONArray("result");
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject json = (JSONObject)jsonArray.get(i);
				String fromType = json.getString("fromType");
				String hrefUrl = prefix + json.getString("id");
				if ("2".equals(fromType)) {
					hrefUrl = prefix2 + json.getString("id");
				}
				urlList.add(hrefUrl);
				FetchData fetchData = new FetchData();
				fetchData.setIndustry(getTypeName(json.getString("projectType")));
				fetchData.setPrice(json.getString("amount")+"万");
				fetchData.setListingTime(json.getString("releaseDate"));
				fetchData.setStatus(getProjectPhase(json.getString("projectPhase")));
				fetchData.setPageUrl(hrefUrl);
				map.put(hrefUrl, fetchData);
			}
            handlerData(urlList, charset);
		} catch (Exception e) {
			log.error(String.format("[AbstractClient.getUrlListByPage] url=%s; 根据每页获取URL列表报错：%s", url, e.getMessage()), e);
			throw e;
		}
	}
    
	private String getTypeName(String projectType){
		if(projectType=="1"){
			return "软件及信息服务";
		}else if(projectType =="2"){
			return "集成电路";
		}else if(projectType =="3"){
			return "电子信息";
		}else if(projectType =="4"){
			return "高技术服务";
		}else if(projectType =="5"){
			return "先进制造";
		}else if(projectType =="6"){
			return "生物医疗及环保";
		}else if(projectType =="8"){
			return "新材料";
		}else{
			return "其它";
		}
	}
	
	private String getProjectPhase(String projectPhase){
		if(projectPhase=="1"){
			return "前期研发";
		}else if(projectPhase=="2"){
			return "试生产和市场推广";
		}else if(projectPhase=="3"){
			return "量产和稳定销售";
		}else{
			return "";
		}
	}
	@Override
	public FetchData parseObject(String content, String pageUrl) throws Exception {
		log.debug(content);
		FetchData data = map.get(pageUrl);
		Matcher nameMatcher = namePattern.matcher(content);
		if (nameMatcher.find()) {
			log.debug(nameMatcher.group(1).replaceAll("\\s+", ""));
			data.setName(nameMatcher.group(1).replaceAll("\\s+", ""));
		}
		Matcher descriptionMatcher = descriptionPattern.matcher(content);
		if (descriptionMatcher.find()) {
			String desc1 = descriptionMatcher.group(1).replaceAll("\\s+", "").replaceAll("<[^>]+>", "");
			log.debug(desc1);
			data.setDescription(desc1);
		}
		if (pageUrl.startsWith(prefix2)) {
			JSONObject jsonObject = JSONObject.parseObject(content);
			String result = jsonObject.getString("result");
			JSONObject parseObject = JSONObject.parseObject(result);
			data.setName(parseObject.getString("name"));
			data.setDescription(parseObject.getString("summary").replaceAll("\\s+", "").replaceAll("<[^>]+>", "").replaceAll("&nbsp;", ""));
		}
		return data;
	}
}
