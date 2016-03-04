package com.fetch.data.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fetch.data.domain.FetchData;
import com.fetch.data.tools.DBManager;

public class FetchDataDao {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private static final String insert_sql = "INSERT into `fetch_data` (`name`, country, state, city, location, price, industry, description, project_type, company_type, "
			+ "shares_percentage, highlight, asset_type, valuation, payback_rate, asset_owner, reason_for_sale, listing_date, end_date, approval_date, "
			+ "status, agency, source_of_funding, total_cost, cost, page_url, inserting_time) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW());";
	
	public void insert(FetchData fetchData) throws Exception{
		try {
			DBManager.prepareSql(insert_sql, fetchData);
		} catch (Exception e) {
			log.error(String.format("[FetchDataDao.insert] 插入fetchData对象：%s,报错：%s", fetchData.toString(), e.getMessage()), e);
			throw e;
		}
	}
	
}
