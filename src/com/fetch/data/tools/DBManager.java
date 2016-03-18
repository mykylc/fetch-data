package com.fetch.data.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.FetchData;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class DBManager {
	protected static final Logger log = LoggerFactory.getLogger(DBManager.class);
	
	public static final String url = "jdbc:mysql://127.0.0.1:3306/fetch_data?characterEncoding=utf-8";
	public static final String name = "com.mysql.jdbc.Driver";
	public static final String user = "root";
	public static final String password = "123456";
	
	public static final String driverClassKey = "jdbc.driverClass";
	public static final String jdbcUrlKey = "jdbc.jdbcUrl";
	public static final String usernameKey = "jdbc.username";
	public static final String passwordKey = "jdbc.password";
	

	private static Map<String, String> pageUrlMap = new HashMap<String, String>();
	
	public static Map<String, String> getPageUrlMap() {
		return pageUrlMap;
	}

	private static Connection conn = null;
	public static PreparedStatement pst = null;

	private DBManager() {}
	public static Connection getInstance() throws ClassNotFoundException, SQLException{
		if (conn == null) {
			Class.forName(PropertiesUtils.getProperty(driverClassKey, name));//指定连接类型
			String dbUrl = PropertiesUtils.getProperty(jdbcUrlKey, url);
			String userName = PropertiesUtils.getProperty(usernameKey, user);
			String pwd = PropertiesUtils.getProperty(passwordKey, password);
			conn = DriverManager.getConnection(dbUrl, userName, pwd);//获取连接
	    }
		return conn;
	}
	
	public static synchronized void prepareSql(String sql, FetchData fetchData)throws Exception{
		try {
			Connection conn = DBManager.getInstance();
			pst = conn.prepareStatement(sql);
			pst.setString(1, fetchData.getName());
			pst.setString(2, fetchData.getCountry());
			pst.setString(3, fetchData.getState());
			pst.setString(4, fetchData.getCity());
			pst.setString(5, fetchData.getLocation());
			pst.setString(6, fetchData.getPrice());
			pst.setString(7, fetchData.getIndustry());
			pst.setString(8, fetchData.getDescription());
			pst.setString(9, fetchData.getProjectType());
			pst.setString(10, fetchData.getCompanyType());
			pst.setString(11, fetchData.getSharesPercentage());
			pst.setString(12, fetchData.getHighlight());
			
			pst.setString(13, fetchData.getAssetType());
			pst.setString(14, fetchData.getValuation());
			pst.setString(15, fetchData.getPaybackRate());
			pst.setString(16, fetchData.getAssetOwner());
			pst.setString(17, fetchData.getReasonForSale());
			pst.setString(18, fetchData.getListingDate());
			pst.setString(19, fetchData.getEndDate());
			
			pst.setString(20, fetchData.getApprovalDate());
			pst.setString(21, fetchData.getStatus());
			pst.setString(22, fetchData.getAgency());
			pst.setString(23, fetchData.getSourceOfFunding());
			pst.setString(24, fetchData.getTotalCost());
			pst.setString(25, fetchData.getCost());
			pst.setString(26, fetchData.getInvestmentMode());
			pst.setString(27, fetchData.getProjectAdvantage());
			pst.setString(28, fetchData.getValidityPeriod());
			pst.setString(29, fetchData.getProjectProperties());
			pst.setString(30, fetchData.getProjectCapitals());
			pst.setString(31, fetchData.getExpectedAnnualRevenue());
			pst.setString(32, fetchData.getExpectedPaybackPeriod());
			pst.setString(33, fetchData.getDescOfEnvironmentProtection());
			pst.setString(34, fetchData.getDescOfInvestorConditions());
			
			pst.setString(35, fetchData.getListingTime());
			pst.setString(36, fetchData.getExpectedIndustry());
			pst.setString(37, fetchData.getExpectedCapital());
			pst.setString(38, fetchData.getExpectedIndustryCaracteristics());
			
			pst.setString(39, fetchData.getExpectedLocation());
			pst.setString(40, fetchData.getExpectedFinancialRatios());
			pst.setString(41, fetchData.getBusinessDescription());
			pst.setString(42, fetchData.getFinancialRatios());
			pst.setString(43, fetchData.getCompanyProperites());
			pst.setString(44, fetchData.getFinancingMode());
			
			pst.setString(45, fetchData.getPatent());
			pst.setString(46, fetchData.getExpectedInvestmentArea());
			pst.setString(47, fetchData.getProjectEnvironment());
			
			pst.setString(48, fetchData.getTurnover());
			pst.setString(49, fetchData.getNetProfit());
			pst.setString(50, fetchData.getGrossProfit());
			pst.setString(51, fetchData.getEBITDA());
			pst.setString(52, fetchData.getAsset());
			pst.setString(53, fetchData.getPotential());
			pst.setString(54, fetchData.getAdvantage());
			pst.setString(55, fetchData.getFinancialInformation());
			
			pst.setString(56, fetchData.getPageUrl());
			pst.setString(57, HashUtils.getHash(fetchData.getPageUrl()));
			pst.executeUpdate();
			log.info(String.format("insert data,web site = %s", fetchData.getPageUrl()));
		} catch (MySQLIntegrityConstraintViolationException ve) {
			log.error(String.format("[DBManager.prepareSql] this url already exist, url=%s", fetchData.getPageUrl()));
		} catch (Exception e) {
			log.error(String.format("[DBManager.prepareSql] 执行sql语句：%s,url=%s,报错：%s", sql, fetchData.getPageUrl(), e.getMessage()), e);
			throw e;
		}
	}
	
	public static void loadPageUrl() throws Exception{
		Connection conn = DBManager.getInstance();
		Statement stmt = conn.createStatement();  
		ResultSet rs = stmt.executeQuery("select page_url from fetch_data");
		while(rs.next()){
			String pageUrl = rs.getString("page_url");
			pageUrlMap.put(pageUrl, pageUrl);
		}
	}
	
	public static void close() throws Exception {
		try {
			if (conn != null) {
				conn.close();
			}
			if (pst != null) {
				pst.close();
			}
		} catch (Exception e) {
			log.error(String.format("[DBManager.close] 关闭conn、pst连接报错：%s", e.getMessage()), e);
			throw e;
		}
	}
	
}
