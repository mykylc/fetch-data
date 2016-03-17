package com.fetch.data.main;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.client.AdbClient;
import com.fetch.data.client.AfdbClient;
import com.fetch.data.client.EibClient;
import com.fetch.data.client.FdiSinfoClient;
import com.fetch.data.client.FdibbSinfoClient;
import com.fetch.data.client.FirstNetClient;
import com.fetch.data.client.IicClient;
import com.fetch.data.client.InvestClient;
import com.fetch.data.client.RztongClient;
import com.fetch.data.client.ShftzClient;
import com.fetch.data.client.TpdealsBFSClient;
import com.fetch.data.client.TpdealsBSClient;
import com.fetch.data.client.TpdealsOOClient;
import com.fetch.data.client.TurnerbutlerAgriculturalClient;
import com.fetch.data.client.TurnerbutlerFranchiseClient;
import com.fetch.data.client.TurnerbutlerITClient;
import com.fetch.data.client.TurnerbutlerManufacturingClient;
import com.fetch.data.client.TurnerbutlerRetailClient;
import com.fetch.data.client.TurnerbutlerServicesClient;
import com.fetch.data.client.TurnerbutlerTransportClient;
import com.fetch.data.tools.DBManager;
import com.fetch.data.tools.FetchDataMainThreadPool;
import com.fetch.data.tools.FetchDataThreadPool;

public class Bootstrap2 {

	protected final static Logger log = LoggerFactory.getLogger(Bootstrap2.class);
	final static Semaphore semp = new Semaphore(20);
	public static void main(String[] args) throws Exception {
		start();
		System.exit(0);
	}
	
	public static void start() throws Exception{
		DBManager.loadPageUrl();
		
		startAfdb();
		startAdb();
		startEib();
		startIic();
		startFdi();
		startInvest();
		
		startRztong();
		
		startShftz();
		startFirstNet();
		startTurnerbutler1();
		startTurnerbutler2();
		startTurnerbutler3();
		startTurnerbutler4();
		startTurnerbutler5();
		startTurnerbutler6();
		startTurnerbutler7();
		
		startTpdeals();
		while (true) {
			if (semp.availablePermits()==20) {
				break;
			}
		}
		log.info("All web site fetch data end!");
		DBManager.close();
		FetchDataThreadPool.exec.shutdown();
		FetchDataMainThreadPool.exec.shutdown();
	}
	
	
	
	//afdb
	public static void startAfdb(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						AfdbClient client = new AfdbClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startAfdb线程报错："+e.getMessage(), e);
					} finally {
						log.info("afdb web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【afdb】网站出错"+e.getMessage(), e);
		}
	}
	//adb
	public static void startAdb(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						AdbClient client = new AdbClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startAdb线程报错："+e.getMessage(), e);
					} finally {
						log.info("adb web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【adb】网站出错"+e.getMessage(), e);
		}
	}
	//eib
	public static void startEib(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						EibClient client = new EibClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startEib线程报错："+e.getMessage(), e);
					} finally {
						log.info("eib web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Eib】网站出错"+e.getMessage(), e);
		}
	}
	//Iic
	public static void startIic(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						IicClient client = new IicClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startIic线程报错："+e.getMessage(), e);
					} finally {
						log.info("Iic web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Iic】网站出错"+e.getMessage(), e);
		}
	}
	//Fdi
	public static void startFdi(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						FdiSinfoClient client = new FdiSinfoClient();
						client.fetchData();
						FdibbSinfoClient client2 = new FdibbSinfoClient();
						client2.fetchData();
					} catch (Exception e) {
						log.error("启动startFdi线程报错："+e.getMessage(), e);
					} finally {
						log.info("Fdi web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Fdi】网站出错"+e.getMessage(), e);
		}
	}
	//Invest
	public static void startInvest(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						InvestClient client = new InvestClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startInvest线程报错："+e.getMessage(), e);
					} finally {
						log.info("Invest web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Invest】网站出错"+e.getMessage(), e);
		}
	}
	//Rztong
	public static void startRztong(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RztongClient client = new RztongClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startRztong线程报错："+e.getMessage(), e);
					} finally {
						log.info("Rztong web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Rztong】网站出错"+e.getMessage(), e);
		}
	}
	//ShftzClient
	public static void startShftz(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						ShftzClient client = new ShftzClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startShftz线程报错："+e.getMessage(), e);
					} finally {
						log.info("Shftz web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Shftz】网站出错"+e.getMessage(), e);
		}
	}
	//FirstNet
	public static void startFirstNet(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						FirstNetClient client = new FirstNetClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startFirstNet线程报错："+e.getMessage(), e);
					} finally {
						log.info("FirstNet web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【FirstNet】网站出错"+e.getMessage(), e);
		}
	}
	//Turnerbutler
	public static void startTurnerbutler1(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						TurnerbutlerAgriculturalClient client = new TurnerbutlerAgriculturalClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startTurnerbutler1线程报错："+e.getMessage(), e);
					} finally {
						log.info("Turnerbutler1 web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Turnerbutler】网站出错"+e.getMessage(), e);
		}
	}
	public static void startTurnerbutler2(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						TurnerbutlerFranchiseClient client = new TurnerbutlerFranchiseClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startTurnerbutler2线程报错："+e.getMessage(), e);
					} finally {
						log.info("Turnerbutler2 web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Turnerbutler】网站出错"+e.getMessage(), e);
		}
	}
	public static void startTurnerbutler3(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						TurnerbutlerITClient client  = new TurnerbutlerITClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startTurnerbutler3线程报错："+e.getMessage(), e);
					} finally {
						log.info("Turnerbutler3 web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Turnerbutler】网站出错"+e.getMessage(), e);
		}
	}
	public static void startTurnerbutler4(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						TurnerbutlerManufacturingClient client  = new TurnerbutlerManufacturingClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startTurnerbutler4线程报错："+e.getMessage(), e);
					} finally {
						log.info("Turnerbutler4 web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Turnerbutler】网站出错"+e.getMessage(), e);
		}
	}
	public static void startTurnerbutler5(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						TurnerbutlerRetailClient client  = new TurnerbutlerRetailClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startTurnerbutler5线程报错："+e.getMessage(), e);
					} finally {
						log.info("Turnerbutler5 web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Turnerbutler】网站出错"+e.getMessage(), e);
		}
	}
	
	public static void startTurnerbutler6(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						TurnerbutlerServicesClient client  = new TurnerbutlerServicesClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startTurnerbutler6线程报错："+e.getMessage(), e);
					} finally {
						log.info("Turnerbutler6 web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Turnerbutler】网站出错"+e.getMessage(), e);
		}
	}
	public static void startTurnerbutler7(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						TurnerbutlerTransportClient client  = new TurnerbutlerTransportClient();
						client.fetchData();
					} catch (Exception e) {
						log.error("启动startTurnerbutler7线程报错："+e.getMessage(), e);
					} finally {
						log.info("Turnerbutler7 web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Turnerbutler】网站出错"+e.getMessage(), e);
		}
	}
	
	//Tpdeals
	public static void startTpdeals(){
		try {
			semp.acquire();
			FetchDataMainThreadPool.exec.execute(new Runnable() {
				@Override
				public void run() {
					try {
						TpdealsBFSClient client = new TpdealsBFSClient();
						client.fetchData();
						TpdealsBSClient client2 = new TpdealsBSClient();
						client2.fetchData();
						TpdealsOOClient client3 = new TpdealsOOClient();
						client3.fetchData();
					} catch (Exception e) {
						log.error("启动startTpdeals线程报错："+e.getMessage(), e);
					} finally {
						log.info("Tpdeals web site fetch data end!");
						semp.release();
					}
				}
			});
		} catch (Exception e) {
			log.error("处理【Tpdeals】网站出错"+e.getMessage(), e);
		}
	}
}
