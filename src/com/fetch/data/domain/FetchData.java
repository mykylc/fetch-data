package com.fetch.data.domain;

public class FetchData {

	/**
	 * 标的名称/公司名称
	 */
	private String name;
	
	/**
	 * 国家
	 */
	private String country;
	
	/**
	 * 省/州
	 */
	private String state;
	
	/**
	 * 市
	 */
	private String city;
	
	/**
	 * 区域/地
	 */
	private String location;
	
	/**
	 * 价格/挂牌价格/起始价
	 */
	private String price;
	
	/**
	 * 行业
	 */
	private String industry;
	
	private String description;
	
	/**
	 * 交易方式/转让方式/融资方式
	 */
	private String projectType;
	
	/**
	 * 企业类型
	 */
	private String companyType;
	
	/**
	 * 股权比例
	 */
	private String sharesPercentage;
	
	/**
	 * 项目亮点
	 */
	private String highlight;
	
	/**
	 * 资产信息
	 */
	private String assetType;
	
	/**
	 * 估值/估值区
	 */
	private String valuation;
	
	/**
	 * 投资回报率
	 */
	private String paybackRate;
	
	/**
	 * 项目所有者/项目所有者描述
	 */
	private String assetOwner;
	
	/**
	 * 出售原因
	 */
	private String reasonForSale;
	
	/**
	 * 挂牌起始日期
	 */
	private String listingDate;
	
	/**
	 * 挂牌期满日期/有效期
	 */
	private String endDate;
	
	private String pageUrl;

	public String getName() {
		if (name == null) {
			return "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		if (country == null) {
			return "";
		}
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getState() {
		if (state == null) {
			return "";
		}
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCity() {
		if (city == null) {
			return "";
		}
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getLocation() {
		if (location == null) {
			return "";
		}
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPrice() {
		if (price == null) {
			return "";
		}
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getIndustry() {
		if (industry == null) {
			return "";
		}
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getDescription() {
		if (description == null) {
			return "";
		}
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProjectType() {
		if (projectType == null) {
			return "";
		}
		return projectType;
	}

	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}

	public String getCompanyType() {
		if (companyType == null) {
			return "";
		}
		return companyType;
	}

	public void setCompanyType(String companyType) {
		this.companyType = companyType;
	}

	public String getSharesPercentage() {
		if (sharesPercentage == null) {
			return "";
		}
		return sharesPercentage;
	}

	public void setSharesPercentage(String sharesPercentage) {
		this.sharesPercentage = sharesPercentage;
	}

	public String getHighlight() {
		if (highlight == null) {
			return "";
		}
		return highlight;
	}

	public void setHighlight(String highlight) {
		this.highlight = highlight;
	}

	public String getAssetType() {
		if (assetType == null) {
			return "";
		}
		return assetType;
	}

	public void setAssetType(String assetType) {
		this.assetType = assetType;
	}

	public String getValuation() {
		if (valuation == null) {
			return "";
		}
		return valuation;
	}

	public void setValuation(String valuation) {
		this.valuation = valuation;
	}

	public String getPaybackRate() {
		if (paybackRate == null) {
			return "";
		}
		return paybackRate;
	}

	public void setPaybackRate(String paybackRate) {
		this.paybackRate = paybackRate;
	}

	public String getAssetOwner() {
		if (assetOwner == null) {
			return "";
		}
		return assetOwner;
	}

	public void setAssetOwner(String assetOwner) {
		this.assetOwner = assetOwner;
	}

	public String getReasonForSale() {
		if (reasonForSale == null) {
			return "";
		}
		return reasonForSale;
	}

	public void setReasonForSale(String reasonForSale) {
		this.reasonForSale = reasonForSale;
	}

	public String getListingDate() {
		if (listingDate == null) {
			return "";
		}
		return listingDate;
	}

	public void setListingDate(String listingDate) {
		this.listingDate = listingDate;
	}

	public String getEndDate() {
		if (endDate == null) {
			return "";
		}
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getPageUrl() {
		if (pageUrl == null) {
			return "";
		}
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	
	@Override
	public String toString() {
		return "name="+getName()+",country="+getCountry()+",state="+getState()+",city="+getCity()+",location="+getLocation()
			+",price="+getPrice()+",industry="+getIndustry()+",description="+getDescription()+",project_type="+getProjectType()
			+",company_type="+getCompanyType()+",shares_percentage="+getSharesPercentage()+",highlight="+getHighlight()
			+",asset_type="+getAssetType()+",valuation="+getValuation()+",payback_rate="+getPaybackRate()+",asset_owner="+getAssetOwner()
			+",reason_for_sale="+getReasonForSale()+",listing_date="+getListingDate()+",end_date="+getEndDate()+",page_url="+getPageUrl();
	}
}
