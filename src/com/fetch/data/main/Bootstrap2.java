package com.fetch.data.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.client.AdbClient;
import com.fetch.data.client.AfdbClient;
import com.fetch.data.client.EibClient;
import com.fetch.data.client.FdiSinfoClient;
import com.fetch.data.client.FdibbSinfoClient;
import com.fetch.data.client.FirstNetClient;
import com.fetch.data.client.IicClient;
import com.fetch.data.client.InvestRongZiClient;
import com.fetch.data.client.InvestTouZiClient;
import com.fetch.data.client.RztongClient;
import com.fetch.data.client.ShftzClient;
import com.fetch.data.client.TpdealsBFSClient;
import com.fetch.data.client.TpdealsBSClient;
import com.fetch.data.client.TpdealsClient;
import com.fetch.data.client.TpdealsOOClient;
import com.fetch.data.client.TurnerbutlerAgriculturalClient;
import com.fetch.data.client.TurnerbutlerClient;
import com.fetch.data.client.TurnerbutlerFranchiseClient;
import com.fetch.data.client.TurnerbutlerITClient;
import com.fetch.data.client.TurnerbutlerManufacturingClient;
import com.fetch.data.client.TurnerbutlerRetailClient;
import com.fetch.data.client.TurnerbutlerServicesClient;
import com.fetch.data.client.TurnerbutlerTransportClient;
import com.fetch.data.tools.DBManager;
import com.fetch.data.tools.FetchDataThreadPool;

public class Bootstrap2 {

	protected final static Logger log = LoggerFactory.getLogger(Bootstrap2.class);
	
	public static void main(String[] args) throws Exception {
		start();
		System.exit(0);
	}
	
	public static void start() throws Exception{
		DBManager.loadPageUrl();
		log.info("http://www.FdibbSinfoClient.cn/ begin...");
		startFdi();
		DBManager.close();
		FetchDataThreadPool.exec.shutdown();
	}
	//afdb
	public static void startAfdb(){
		try {
			AfdbClient client = new AfdbClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【afdb】网站出错"+e.getMessage(), e);
		}
	}
	//adb
	public static void startAdb(){
		try {
			AdbClient client = new AdbClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【adb】网站出错"+e.getMessage(), e);
		}
	}
	//eib
	public static void startEib(){
		try {
			EibClient client = new EibClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【Eib】网站出错"+e.getMessage(), e);
		}
	}
	//Iic
	public static void startIic(){
		try {
			IicClient client = new IicClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【Iic】网站出错"+e.getMessage(), e);
		}
	}
	//Fdi
	public static void startFdi(){
		try {
//			FdiSinfoClient client = new FdiSinfoClient();
//			client.fetchData();
			FdibbSinfoClient client2 = new FdibbSinfoClient();
			client2.fetchData();
		} catch (Exception e) {
			log.error("处理【Fdi】网站出错"+e.getMessage(), e);
		}
	}
	//Invest
	public static void startInvest(){
		try {
			InvestRongZiClient client = new InvestRongZiClient();
			//client.fetchData();
			InvestTouZiClient client2 = new InvestTouZiClient();
			client2.fetchData();
		} catch (Exception e) {
			log.error("处理【Invest】网站出错"+e.getMessage(), e);
		}
	}
	//Rztong
	public static void startRztong(){
		try {
			RztongClient client = new RztongClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【Rztong】网站出错"+e.getMessage(), e);
		}
	}
	//ShftzClient
	public static void startShftz(){
		try {
			ShftzClient client = new ShftzClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【Shftz】网站出错"+e.getMessage(), e);
		}
	}
	//FirstNet
	public static void startFirstNet(){
		try {
			FirstNetClient client = new FirstNetClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【FirstNet】网站出错"+e.getMessage(), e);
		}
	}
	//Turnerbutler
	public static void startTurnerbutler(){
		try {
			TurnerbutlerClient client = new TurnerbutlerAgriculturalClient();
			client.fetchData();
			client = new TurnerbutlerFranchiseClient();
			client.fetchData();
			client = new TurnerbutlerITClient();
			client.fetchData();
			client = new TurnerbutlerManufacturingClient();
			client.fetchData();
			client = new TurnerbutlerRetailClient();
			client.fetchData();
			client = new TurnerbutlerServicesClient();
			client.fetchData();
			client = new TurnerbutlerTransportClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【Turnerbutler】网站出错"+e.getMessage(), e);
		}
	}
	//Tpdeals
	public static void startTpdeals(){
		try {
			TpdealsClient client = new TpdealsBFSClient();
			client.fetchData();
			client = new TpdealsBSClient();
			client.fetchData();
			client = new TpdealsOOClient();
			client.fetchData();
		} catch (Exception e) {
			log.error("处理【Tpdeals】网站出错"+e.getMessage(), e);
		}
	}
}
