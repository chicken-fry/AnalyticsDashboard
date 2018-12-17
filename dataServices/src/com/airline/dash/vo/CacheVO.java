package com.emirates.dash.vo;

import java.util.Hashtable;
import java.util.List;




public class CacheVO {

	//needs to be arraylist since hashtables do not have order.
	private List<MapEventVO> posActivity;
	//*IMP*NOTE* below needs to be converted to map if requirement arises that a user decides to order by different columns in the UI, 
	//for now assuming top10Pos in terms of PSP seats sold
	private List<Top10PosVO> top10POSAll;
	private List<Top10PosVO> top10POSWeb;
	private List<Top10PosVO> top10POSMob;
	private List<Top10PosVO> top10POSMand;
	private List<Top10PosVO> top10POSMiph;
	
	private List<ChannelVO> channelSplit;
	private OverallStatsVO overallStats;
	private List<MonthlyVO> monthlySplit;
	private List<TopHaulVO> topHaultypes;
	private List<TopBrandsVO> topFareBrands;
	private List<TopSeatCharVO> topSeatCharacteristics;
	private List<SkywardMembers> skywardMembers;
	private int delay;

	
	


	public List<SkywardMembers> getSkywardMembers() {
		return skywardMembers;
	}
	public void setSkywardMembers(List<SkywardMembers> skywardMembers) {
		this.skywardMembers = skywardMembers;
	}
	public List<MonthlyVO> getMonthlySplit() {
		return monthlySplit;
	}
	public void setMonthlySplit(List<MonthlyVO> monthlySplit) {
		this.monthlySplit = monthlySplit;
	}
	public List<TopHaulVO> getTopHaultypes() {
		return topHaultypes;
	}
	public void setTopHaultypes(List<TopHaulVO> topHaultypes) {
		this.topHaultypes = topHaultypes;
	}
	public List<TopBrandsVO> getTopFareBrands() {
		return topFareBrands;
	}
	public void setTopFareBrands(List<TopBrandsVO> topFareBrands) {
		this.topFareBrands = topFareBrands;
	}
	public List<TopSeatCharVO> getTopSeatCharacteristics() {
		return topSeatCharacteristics;
	}
	public void setTopSeatCharacteristics(List<TopSeatCharVO> topSeatCharacteristics) {
		this.topSeatCharacteristics = topSeatCharacteristics;
	}
	public OverallStatsVO getOverallStats() {
		return overallStats;
	}
	public void setOverallStats(OverallStatsVO overallStats) {
		this.overallStats = overallStats;
	}

	public int getDelay() {
		return delay;
	}
	public void setDelay(int delay) {
		this.delay = delay;
	}

	public List<MapEventVO> getPosActivity() {
		return posActivity;
	}
	public void setPosActivity(List<MapEventVO> posActivity) {
		this.posActivity = posActivity;
	}
	public List<ChannelVO> getChannelSplit() {
		return channelSplit;
	}
	public void setChannelSplit(List<ChannelVO> channelSplit) {
		this.channelSplit = channelSplit;
	}
	public List<Top10PosVO> getTop10POSAll() {
		return top10POSAll;
	}
	public void setTop10POSAll(List<Top10PosVO> top10posAll) {
		top10POSAll = top10posAll;
	}
	public List<Top10PosVO> getTop10POSWeb() {
		return top10POSWeb;
	}
	public void setTop10POSWeb(List<Top10PosVO> top10posWeb) {
		top10POSWeb = top10posWeb;
	}
	public List<Top10PosVO> getTop10POSMob() {
		return top10POSMob;
	}
	public void setTop10POSMob(List<Top10PosVO> top10posMob) {
		top10POSMob = top10posMob;
	}
	public List<Top10PosVO> getTop10POSMand() {
		return top10POSMand;
	}
	public void setTop10POSMand(List<Top10PosVO> top10posMand) {
		top10POSMand = top10posMand;
	}
	public List<Top10PosVO> getTop10POSMiph() {
		return top10POSMiph;
	}
	public void setTop10POSMiph(List<Top10PosVO> top10posMiph) {
		top10POSMiph = top10posMiph;
	}

	
	
	
	
	

	
}
