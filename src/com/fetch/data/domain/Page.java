/*
 * Copyright (c) 2016 www.fetch.data.com All rights reserved.
 */
package com.fetch.data.domain;

/**
 * 分页对象
 * @author 
 * @since 2016-01-15
 */
public class Page {
	private int pageSize = 10;
	private int totalCount;
	private int pageCount;
	private int currentPage;
	private int startPage; // 开始页面
	private int endPage; // 结束页面
	private int displayPageCount = 5;

	public Page() {
		// 默认构造器
	}

	public Page(int currentPage) {
		this.currentPage = currentPage;
	}

	public Page(int currentPage, int pageSize) {
		this.currentPage = currentPage;
		this.pageSize = pageSize;
	}

	/**
	 * 获取开始索引
	 * @return
	 */
	public int getStartIndex() {
		return (getCurrentPage() - 1) * this.pageSize;
	}

	/**
	 * 获取结束索引
	 * @return
	 */
	public int getEndIndex() {
		return getCurrentPage() * this.pageSize;
	}

	/**
	 * 是否第一页
	 * @return
	 */
	public boolean isFirstPage() {
		return getCurrentPage() <= 1;
	}

	/**
	 * 是否末页
	 * @return
	 */
	public boolean isLastPage() {
		return getCurrentPage() >= getPageCount();
	}

	/**
	 * 获取下一页页码
	 * @return
	 */
	public int getNextPage() {
		if (isLastPage()) {
			return getCurrentPage();
		} 
		return getCurrentPage() + 1;
	}

	/**
	 * 获取上一页页码
	 * @return
	 */
	public int getPreviousPage() {
		if (isFirstPage()) {
			return 1;
		}
		return getCurrentPage() - 1;
	}

	/**
	 * 获取当前页页码
	 * @return
	 */
	public int getCurrentPage() {
		if (currentPage == 0) {
			currentPage = 1;
		}
		return currentPage;
	}

	/**
	 * 取得总页数
	 * @return
	 */
	public int getPageCount() {
		return pageCount;
	}
	
	public void setPageCount() {
		if (totalCount % pageSize == 0) {
			this.pageCount = totalCount / pageSize;
		} else {
			this.pageCount = totalCount / pageSize + 1;
		}
		this.countPages();
	}
	
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	/**
	 * 取总记录数.
	 * @return
	 */
	public int getTotalCount() {
		return this.totalCount;
	}

	/**
	 * 设置当前页
	 * @param currentPage
	 */
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * 获取每页数据容量.
	 * @return
	 */
	public int getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	/**
	 * 该页是否有下一页.
	 * @return
	 */
	public boolean hasNextPage() {
		return getCurrentPage() < getPageCount();
	}

	/**
	 * 该页是否有上一页.
	 * @return
	 */
	public boolean hasPreviousPage() {
		return getCurrentPage() > 1;
	}

	/**
	 * 设置总记录条数
	 * @param totalCount
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	
	public void countPages() {
		if (currentPage - displayPageCount / 2 < 1)	{
			startPage = 1;
			endPage = displayPageCount > pageCount ? pageCount : displayPageCount;
		} else if (currentPage + displayPageCount / 2 > pageCount) {
			int n = pageCount - displayPageCount + 1;
			startPage = n > 0 ? n : 1;
			endPage = pageCount;
		} else {
			startPage = currentPage - displayPageCount / 2;
			endPage = startPage + displayPageCount - 1;
		}
	}
	public int getStartPage() {
		return startPage;
	}

	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}

	public int getEndPage() {
		return endPage;
	}

	public void setEndPage(int endPage) {
		this.endPage = endPage;
	}
    
}
