package com.fetch.data.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.client.AaeeFGYClient;
import com.fetch.data.client.AaeeGYClient;
import com.fetch.data.client.CbexGQClient;
import com.fetch.data.client.CbexZQClient;
import com.fetch.data.client.ChinaMergerClient;
import com.fetch.data.client.CnpreClient;
import com.fetch.data.client.CquaeGQClient;
import com.fetch.data.client.CquaeZSCQClient;
import com.fetch.data.client.CsuaeeGYCQClient;
import com.fetch.data.client.CsuaeeZXQYClient;
import com.fetch.data.client.SuaeeCQXMClient;
import com.fetch.data.client.SuaeeJSXMClient;
import com.fetch.data.client.SuaeeYXGPClient;
import com.fetch.data.client.TprtcGYGQClient;
import com.fetch.data.client.TprtcYQGQClient;
import com.fetch.data.tools.DBManager;
import com.fetch.data.tools.FetchDataThreadPool;

public class Bootstrap {

	protected final static Logger log = LoggerFactory.getLogger(Bootstrap.class);
	
	public static void main(String[] args) throws Exception {
		start();
		System.exit(0);
	}
	
	public static void start() throws Exception{
		DBManager.loadPageUrl();
		log.error("http://www.chinamerger.com begin...");
		startChinaMerger();
		log.error("http://www.suaee.com begin...");
		startSuaee();
		log.error("http://www.cbex.com.cn begin...");
		startCbex();
		log.error("http://www.cnpre.com begin...");
		startCnpre();
		log.error("http://xinxipingtai.tprtc.com begin...");
		startTprtc();
		log.error("http://www.cquae.com begin...");
		startCquae();
		log.error("http://www.aaee.com.cn begin...");
		startAaee();
		log.error("http://www.csuaee.com.cn begin...");
		startCsuaee();
		log.error("fetch data end...");
		DBManager.close();
		FetchDataThreadPool.exec.shutdown();
	}
	//中国大买手
	public static void startChinaMerger(){
		try {
			ChinaMergerClient client = new ChinaMergerClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【中国大买手】网站出错"+e.getMessage(), e);
		}
	}
	
	//上海联合产权交易所
	public static void startSuaee(){
		try {//产股权项目
			SuaeeCQXMClient client = new SuaeeCQXMClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【上海联合产权交易所/产股权项目】网站出错"+e.getMessage(), e);
		}
		try {//知识产权项目
			SuaeeJSXMClient client = new SuaeeJSXMClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【上海联合产权交易所/知识产权项目】网站出错"+e.getMessage(), e);
		}
		try {//意向项目
			SuaeeYXGPClient client = new SuaeeYXGPClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【上海联合产权交易所/意向项目】网站出错"+e.getMessage(), e);
		}
	}
	
	//北京产权交易所
	public static void startCbex(){
		try {//股权
			CbexGQClient client = new CbexGQClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【北京产权交易所/股权】网站出错"+e.getMessage(), e);
		}
		try {//债权
			CbexZQClient client = new CbexZQClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【北京产权交易所/债权】网站出错"+e.getMessage(), e);
		}
	}
	//中国产权交易所
	public static void startCnpre(){
		try {
			CnpreClient client = new CnpreClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【中国产权交易所】网站出错"+e.getMessage(), e);
		}
	}
	//天津产权交易中心
	public static void startTprtc(){
		try {//国有股权
			TprtcGYGQClient client = new TprtcGYGQClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【天津产权交易中心/国有股权】网站出错"+e.getMessage(), e);
		}
		try {//央企股权
			TprtcYQGQClient client = new TprtcYQGQClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【天津产权交易中心/央企股权】网站出错"+e.getMessage(), e);
		}
	}
	//重庆联合产权交易所
	public static void startCquae(){
		try {//股权
			CquaeGQClient client = new CquaeGQClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【重庆联合产权交易所/股权】网站出错"+e.getMessage(), e);
		}
		try {//知识产权
			CquaeZSCQClient client = new CquaeZSCQClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【重庆联合产权交易所/知识产权】网站出错"+e.getMessage(), e);
		}
	}
	
	//安徽省产权交易所
	public static void startAaee(){
		try {//国有股权
			AaeeGYClient client = new AaeeGYClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【安徽省产权交易所/国有股权】网站出错"+e.getMessage(), e);
		}
		try {//非国有股权
			AaeeFGYClient client = new AaeeFGYClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【安徽省产权交易所/非国有股权】网站出错"+e.getMessage(), e);
		}
	}
	//南方联合产权交易中心
	public static void startCsuaee(){
		try {//国有产权
			CsuaeeGYCQClient client = new CsuaeeGYCQClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【南方联合产权交易中心/国有产权】网站出错"+e.getMessage(), e);
		}
		try {//中小企业
			CsuaeeZXQYClient client = new CsuaeeZXQYClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【南方联合产权交易中心/中小企业】网站出错"+e.getMessage(), e);
		}
	}
}
