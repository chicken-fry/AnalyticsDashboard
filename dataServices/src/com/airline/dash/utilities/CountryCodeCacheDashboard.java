package com.emirates.dash.utilities;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;

import com.emirates.dash.exceptions.DashMiddlewareException;
import com.emirates.ibe.middleware.delivery.entities.CountryEntity;
import com.emirates.ibe.middleware.util.IBEMWConstants;
import com.emirates.ibe.middleware.util.cache.CodeCache;
import com.emirates.ibe.middleware.util.cache.ObjectNotFoundException;


public class CountryCodeCacheDashboard extends CodeCache{


		public static final String CODE_COLUMN_NAME = "COUNTRY_CODE";
		private final Map<String, String> countryCodes = new HashMap<String, String>(
				CACHE_INIT_SIZE);
		
	 
		//15.4.0 CC Surcharge changes - By Karthi 
		//Ancillary Stretch AS I2
		private static final String COUNTRY_ENTITY_QRY = "SELECT Country_Name, IsCCSurcharge, IsPieceBasedOrg,Zone, Brand_Perc_REV, Brand_Perc_Redeem, MinThresholdSurcharge " +
				"FROM COUNTRY WITH(NOLOCK) WHERE Country_Code=?";
		private final Map<String, CountryEntity> countryEntityCache = new HashMap<String, CountryEntity>(
				CACHE_INIT_SIZE);
		
		private static final CountryCodeCacheDashboard COUNTRY_CODE_CACHE = new CountryCodeCacheDashboard();

		private CountryCodeCacheDashboard() {
		}

		public static final CountryCodeCacheDashboard getInstance() {
			return COUNTRY_CODE_CACHE;
		}

		public String retrieveCountryCode(String airportCode)
				throws ObjectNotFoundException, SQLException {
			return getCode(airportCode, CODE_COLUMN_NAME, EK_DEST_TABLE_NAME,
					AIR_CODE_COLUMN_NAME, DESTINATION_RETRIEVE_TYPE_COUNTRY_CODE,
					countryCodes);
		}
		
		//for Dashboard
		public void loadCountryEntityCache(String countryCode){
			java.sql.Connection con = DBConnectionPool.getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				ps = con.prepareStatement(COUNTRY_ENTITY_QRY);
				ps.setString(1, countryCode);
				rs = ps.executeQuery();
				CountryEntity countryEntity = null;
				while (rs.next()) {
					countryEntity =  new CountryEntity();
					countryEntity.setCountryName(rs.getString("Country_Name"));
					//Ancillary Stretch AS I2
					countryEntity.setZone(rs.getString("Zone"));
					//16.5.0 release changes by s709981 -starts
					try {
						countryEntity.setBestValPercentRevenue(Double
								.parseDouble(rs.getString("Brand_Perc_REV")));
						countryEntity.setBestValPercentRedemption(Double
								.parseDouble(rs.getString("Brand_Perc_Redeem")));
					} catch (Exception nfe) {
						DashLoggerUtil.log(Level.ERROR, nfe);
					}
					//16.5.0 release changes by s709981 -ends
					countryEntity.setCCSurcharge(IBEMWConstants.FLAG_YES.equals(rs.getString("IsCCSurcharge"))? true : false);
					countryEntity.setPieceBasedOrg(IBEMWConstants.FLAG_YES.equals(rs.getString("IsPieceBasedOrg"))? true : false);
					//Added on 11/10/2016 for minimum threshold amount to apply cc surcharge
					countryEntity.setMinThresholdCCSurcharge(rs.getString("MinThresholdSurcharge"));
					countryEntityCache.put(countryCode, countryEntity);
				}
			} catch (SQLException objSqlException) {
				throw new DashMiddlewareException(objSqlException.getMessage());
			} catch (Exception objGenException) {
				throw new DashMiddlewareException(objGenException.getMessage());
			} finally {
				DBConnectionPool.releaseConnection(rs, ps, con);
			}
		}
		
		//15.4.0 CC Surcharge changes - By Karthi
		public CountryEntity getCountryEntity(String countryCode){
			try {
				DashLoggerUtil.log(Level.DEBUG, "CountryEntity cache loading key: "
						+ countryCode);
				if (countryEntityCache.get(countryCode) == null) {
					loadCountryEntityCache(countryCode);
				}
			} catch (Exception e) {
				DashLoggerUtil.log(Level.ERROR, e);
			}
			
			return countryEntityCache.get(countryCode);
		}
		
		@Override
		public boolean clearCode(String key) {
			boolean success = true;
			DashLoggerUtil.log(Level.DEBUG, "Inside " + 
					this.getClass().getSimpleName() + " clearCode");
			try {
				DashLoggerUtil.log(Level.DEBUG, "Country code cache cleared : " + 
						key);
				if (key ==null) {
					countryCodes.clear();
					//15.4.0 CC Surcharge changes - By Karthi
					countryEntityCache.clear();
				}else {
					countryCodes.remove(key);
					//15.4.0 CC Surcharge changes - By Karthi
					countryEntityCache.remove(key);
				}
				
			} catch (Exception e) {
				DashLoggerUtil.log(Level.ERROR, e);
				success = false;
			}
			return success;
		}
	
}
