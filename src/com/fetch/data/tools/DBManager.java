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

public class DBManager {
	protected static final Logger log = LoggerFactory.getLogger(DBManager.class);
	
	public static final String url = "jdbc:mysql://127.0.0.1:3306/fetch_data?characterEncoding=utf-8";
	public static final String name = "com.mysql.jdbc.Driver";
	public static final String user = "root";
	public static final String password = "123456";

	private static Map<String, String> pageUrlMap = new HashMap<String, String>();
	
	public static Map<String, String> getPageUrlMap() {
		return pageUrlMap;
	}

	private static Connection conn = null;
	public static PreparedStatement pst = null;

	private DBManager() {}
	public static Connection getInstance() throws ClassNotFoundException, SQLException{
		if (conn == null) {
			Class.forName(name);//指定连接类型
			conn = DriverManager.getConnection(url, user, password);//获取连接
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
			pst.setString(20, fetchData.getPageUrl());
			
			pst.executeUpdate();
		} catch (Exception e) {
			log.error(String.format("[DBManager.prepareSql] 执行sql语句：%s,报错：%s", sql, e.getMessage()), e);
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
