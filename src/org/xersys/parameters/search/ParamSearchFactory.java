package org.xersys.parameters.search;

import java.sql.ResultSet;
import org.json.simple.JSONObject;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.util.MiscUtil;
import org.xersys.commander.util.SQLUtil;

public class ParamSearchFactory {
    private XNautilus _nautilus;
    
    private String _key;
    private String _filter;
    private int _max;
    private boolean _exact;
    
    public enum Type{
        searchCountry,
        searchRegion,
        searchProvince,
        searchTownCity,
        searchBanks,
        searchBrand,
        searchModel,
        searchCatalogCategory,
        searchModelSeries
    }
    
    public ParamSearchFactory(XNautilus foNautilus, String fsKey, String fsFilter, int fnMax, boolean fbExact){
        _nautilus = foNautilus;
        _key = fsKey;
        _filter = fsFilter;
        _max = fnMax;
        _exact = fbExact;
    }
    
    public JSONObject searchParameter(Enum foType, String fsValue, String fsFields){
        JSONObject loJSON = new JSONObject();
        
        if (_nautilus == null){
            loJSON.put("result", "error");
            loJSON.put("message", "Application driver is not set.");
            return loJSON;
        }
        
        String lsSQL = "";
        
        if (foType == Type.searchCountry){
            lsSQL = getSQ_Country();
        } else if (foType == Type.searchRegion){
            lsSQL = getSQ_Region();
        } else if (foType == Type.searchProvince){
            lsSQL = getSQ_Province();
        } else if (foType == Type.searchTownCity){
            lsSQL = getSQ_TownCity();
        } else if (foType == Type.searchBrand){
            lsSQL = getSQ_Brand();
        } else if (foType == Type.searchModel){
            lsSQL = getSQ_Model();
        } else if (foType == Type.searchModelSeries){
            lsSQL = getSQ_Model_Series();
        } else if (foType == Type.searchCatalogCategory){
            lsSQL = getSQ_Catalog_Category();
        } else if (foType == Type.searchBanks){
            lsSQL = getSQ_Banks();
        }
        
        if (lsSQL.isEmpty()){
            loJSON.put("result", "error");
            loJSON.put("message", "Search query was not set.");
            return loJSON;
        }
        
        //are we searching with an exact value
        if (_exact)
            lsSQL = MiscUtil.addCondition(lsSQL, _key + " = " + SQLUtil.toSQL(fsValue));
        else
            lsSQL = MiscUtil.addCondition(lsSQL, _key + " LIKE " + SQLUtil.toSQL(fsValue + "%"));
        
        //add filter on query
        if (!_filter.isEmpty()) lsSQL = MiscUtil.addCondition(lsSQL, _filter);
        
        //add order by and limit on query
        lsSQL = lsSQL + " ORDER BY " + _key + " LIMIT " + _max;
        
        ResultSet loRS = _nautilus.executeQuery(lsSQL);
        
        if (MiscUtil.RecordCount(loRS) <= 0){
            loJSON.put("result", "error");
            loJSON.put("message", "No record found.");
            return loJSON;
        }
        
        loJSON.put("result", "success");
        loJSON.put("payload", MiscUtil.RS2JSON(loRS, fsFields));
        
        //close resultset
        MiscUtil.close(loRS);
        return loJSON;
    }
    
    private String getSQ_Country(){
        return "SELECT" + 
                    "  sCntryCde" +
                    ", sCntryNme" +
                    ", IFNULL(sNational, '') sNational" +
                    ", cRecdStat" +
                " FROM Country" +
                " WHERE cRecdStat = '1'";
    }
    
    private String getSQ_Province(){
        return "SELECT" +
                    "  a.sProvIDxx" +
                    ", a.sProvName" +
                    ", a.sRegionID" +
                    ", a.cRecdStat" +
                    ", b.sRegionNm" +
                    ", b.sRegionID" +
                " FROM Province a" +
                    ", Region b" +
                " WHERE a.cRecdStat = '1'";
    }
    
    private String getSQ_Region(){
        return "SELECT" +
                    "  sRegionID" +
                    ", sRegionNm" +
                    ", cRecdStat" +
                " FROM Region" +
                " WHERE cRecdStat = '1'";
    }
    
    private String getSQ_TownCity(){
        return "SELECT" + 
                    "  a.sTownIDxx" +
                    ", a.sTownName" +
                    ", a.sZippCode" +
                    ", a.sProvIDxx" +
                    ", a.sProvCode" +
                    ", a.sMuncplCd" +
                    ", a.cHasRoute" +
                    ", a.cBlackLst" +
                    ", a.cRecdStat" +
                    ", b.sProvName" +
                    ", c.sRegionNm" +
                    ", b.sProvIDxx" +
                    ", b.sRegionID" +
                " FROM TownCity a" +
                    ", Province b" +
                    ", Region c" +
                " WHERE a.sProvIDxx = b.sProvIDxx" +
                    " AND b.sRegionID = c.sRegionID" +
                    " AND a.cRecdStat = '1'";
    }
    
    private String getSQ_Banks(){
        return "SELECT" +
                    "  sBankCode" +
                    ", sBankName" +
                " FROM Banks";
    }
    
    private String getSQ_Brand(){
        return "SELECT" +
                    "  sBrandCde" +
                    ", sInvTypCd" +
                    ", sDescript" +
                " FROM Brand";
    }
    
    private String getSQ_Model(){
        return "SELECT" +
                    "  sModelCde" +
                    ", sInvTypCd" +
                    ", sBriefDsc" +
                    ", sModelNme" +
                    ", sDescript" +
                    ", sBrandCde" +
                    ", sCategrCd" +
                    ", cEndOfLfe" +
                " FROM Model";
    }
    
    private String getSQ_Catalog_Category(){
        return "SELECT" +
                    "  sCategrCd" +
                    ", sDescript" +
                " FROM Catalog_Category";
    }
    
    private String getSQ_Model_Series(){
        return "SELECT" +
                    "  sSeriesID" +
                    ", sDescript" +
                    ", sModelCde" +
                " FROM Catalog_Category";
    }
}
