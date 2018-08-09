package com.emirates.dash.vo;

import java.util.List;

import com.emirates.dash.utilities.Mode;
import com.emirates.dash.utilities.Overlay;

//identified by mode-overlay
public class CacheVOContainer {
	
	//DB load
	private Long lastLoadedTime;
	private Integer lastLoadedId;
	private Overlay overlay;
	private Mode mode;
	private List<CacheVO> cacheImages;
	private boolean readByUI;




	public Integer getLastLoadedId() {
		return lastLoadedId;
	}

	public void setLastLoadedId(Integer lastLoadedId) {
		this.lastLoadedId = lastLoadedId;
	}

	public Long getLastLoadedTime() {
		return lastLoadedTime;
	}

	public void setLastLoadedTime(Long lastLoadedTime) {
		this.lastLoadedTime = lastLoadedTime;
	}

	public Overlay getOverlay() {
		return overlay;
	}

	public void setOverlay(Overlay overlay) {
		this.overlay = overlay;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public List<CacheVO> getCacheImages() {
		return cacheImages;
	}

	public void setCacheImages(List<CacheVO> cacheImages) {
		this.cacheImages = cacheImages;
	}

	public boolean isReadByUI() {
		return readByUI;
	}

	public void setReadByUI(boolean readByUI) {
		this.readByUI = readByUI;
	}


}
