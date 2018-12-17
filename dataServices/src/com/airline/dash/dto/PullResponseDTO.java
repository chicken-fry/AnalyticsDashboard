package com.emirates.dash.dto;

import java.util.List;

import com.emirates.dash.vo.CacheVOContainer;

public class PullResponseDTO {

	private List<CacheVOContainer> cacheVOContainers;
    	
	public List<CacheVOContainer> getCacheVOContainers() {
		return cacheVOContainers;
	}

	public void setCacheVOContainers(List<CacheVOContainer> cacheVOContainers) {
		this.cacheVOContainers = cacheVOContainers;
	}


	
	
}
