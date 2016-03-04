CREATE TABLE `fetch_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(256) DEFAULT NULL COMMENT '标的名称/公司名称',
  `country` varchar(128) DEFAULT NULL COMMENT '国家',
  `state` varchar(128) DEFAULT NULL COMMENT '省/州',
  `city` varchar(128) DEFAULT NULL COMMENT '市',
  `location` varchar(128) DEFAULT NULL COMMENT '区域/地',
  `price` varchar(128) DEFAULT NULL COMMENT '价格/挂牌价格/起始价',
  `industry` varchar(2000) DEFAULT NULL COMMENT '行业',
  `description` text,
  `project_type` varchar(256) DEFAULT NULL COMMENT '交易方式/转让方式/融资方式',
  `company_type` varchar(256) DEFAULT NULL COMMENT '企业类型',
  `shares_percentage` varchar(128) DEFAULT NULL COMMENT '股权比例',
  `highlight` varchar(2000) DEFAULT NULL COMMENT '项目亮点',
  `asset_type` varchar(256) DEFAULT NULL COMMENT '资产信息',
  `valuation` varchar(256) DEFAULT NULL COMMENT '估值/估值区',
  `payback_rate` varchar(128) DEFAULT NULL COMMENT '投资回报率',
  `asset_owner` varchar(512) DEFAULT NULL COMMENT '项目所有者/项目所有者描述',
  `reason_for_sale` varchar(2000) DEFAULT NULL COMMENT '出售原因',
  `listing_date` varchar(128) DEFAULT NULL COMMENT '挂牌起始日期',
  `end_date` varchar(128) DEFAULT NULL COMMENT '挂牌期满日期/有效期',
  `approval_date` varchar(128) DEFAULT NULL,
  `status` varchar(256) DEFAULT NULL,
  `agency` varchar(512) DEFAULT NULL,
  `source_of_funding` varchar(512) DEFAULT NULL,
  `total_cost` varchar(512) DEFAULT NULL,
  `cost` varchar(512) DEFAULT NULL,
  `investment_mode` varchar(512) DEFAULT NULL,
  `project_advantage` text,
  `validity_period` varchar(512) DEFAULT NULL,
  `project_properties` varchar(512) DEFAULT NULL,
  `project_capitals` varchar(512) DEFAULT NULL,
  `expected_annual_revenue` varchar(512) DEFAULT NULL,
  `expected_payback_period` varchar(512) DEFAULT NULL,
  `desc_of_environment_protection` varchar(3000) DEFAULT NULL,
  `desc_of_investor_conditions` varchar(3000) DEFAULT NULL,
  `page_url` varchar(512) DEFAULT NULL,
  `inserting_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20675 DEFAULT CHARSET=utf8;
