package com.fetch.data.client;

import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.Page;
import com.fetch.data.tools.HttpUtils;

/**
 * 安徽省产权交易所
 * @author 
 *
 */
public abstract class AaeeClient extends AbstractClient{
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	final Semaphore semp = new Semaphore(10);
	
	public final static String domain = "http://www.aaee.com.cn/aaee/web/";
	public static int pageSize = 15;	   
	public static int currentPage = 1;
	public final String charset = "gb2312";
	
	// 提取总页数的正则表达式
	public static final Pattern pageCountPattern = Pattern.compile("<b>15</b>条/页   共<b>(.*?)</b>页", 
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    @Override
    public Page getPage(String url, Pattern pageCountPattern, int pageSize, String charset) throws Exception{
		try {
			HttpUtils httpUtils = new HttpUtils(url, charset);
			String result = httpUtils.execute();
			if (result==null) {
				return null;
			}
			Matcher pageCountMatcher = pageCountPattern.matcher(result);
            while (pageCountMatcher.find()) {
                String pageCount = pageCountMatcher.group(1);
                Page page = new Page();
                page.setPageSize(pageSize);
                page.setPageCount(Integer.parseInt(pageCount));
                return page;
            }
		} catch (Exception e) {
			log.error(String.format("[AbstractClient.getPage] url=%s; 获取page对象报错：%s", url, e.getMessage()), e);
			throw e;
		}
		return null;
	}
	
}
