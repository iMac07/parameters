package org.xersys.parameters.search;

import org.xersys.commander.iface.XSearch;
import org.json.simple.JSONObject;
import org.xersys.commander.iface.XNautilus;

public class ParameterSearchEngine implements XSearch{
    private final int DEFAULT_LIMIT = 50;
    
    private XNautilus _nautilus;
    
    private String _key;
    private String _filter;
    private int _max;
    private boolean _exact;
    
    private ParameterSearchFactory _instance;
    
    public enum Type{
        searchCountry,
        searchRegion,
        searchProvince,
        searchTownCity
    }
    
    public ParameterSearchEngine(XNautilus foValue){
        _nautilus = foValue;
        
        _key = "";
        _filter = "";
        _max = DEFAULT_LIMIT;
        _exact = false;
    }
    
    @Override
    public void setKey(String fsValue) {
        _key = fsValue;
    }

    @Override
    public void setFilter(String fsValue) {
        _filter = fsValue;
    }

    @Override
    public void sethMax(int fnValue) {
        _max = fnValue;
    }

    @Override
    public void setExact(boolean fbValue) {
        _exact = fbValue;
    }

    public JSONObject Search(Enum foType, Object foValue) {
        _instance = new ParameterSearchFactory(_nautilus, _key, _filter, _max, _exact);
        
        JSONObject loJSON = null;
        String lsColName;
        
        if (foType == Type.searchCountry){
            lsColName = "sCntryNme»sNational»sCntryCde";
            loJSON = _instance.searchParameter(foType, (String) foValue, lsColName);
            if ("success".equals((String) loJSON.get("result"))) {
                loJSON.put("headers", "Country»Nationality»ID");
                loJSON.put("colname", lsColName);
            }
        } else if (foType == Type.searchRegion){
            lsColName = "sRegionID»sRegionNm";
            loJSON = _instance.searchParameter(foType, (String) foValue, lsColName);
            if ("success".equals((String) loJSON.get("result"))) {
                loJSON.put("headers", "ID»Region");
                loJSON.put("colname", lsColName);
            }
        } else if (foType == Type.searchProvince){
            lsColName = "sProvIDxx»sProvName»sRegionNm";
            loJSON = _instance.searchParameter(foType, (String) foValue, lsColName);
            if ("success".equals((String) loJSON.get("result"))) {
                loJSON.put("headers", "ID»Province»Region");
                loJSON.put("colname", lsColName);
            }
        } else if (foType == Type.searchTownCity){
            lsColName = "sTownName»sProvName»sRegionNm»sZippCode»sTownIDxx";
            loJSON = _instance.searchParameter(foType, (String) foValue, lsColName);
            if ("success".equals((String) loJSON.get("result"))) {
                loJSON.put("headers", "Town/City»Province»Region»Postal Code»ID");
                loJSON.put("colname", lsColName);
            }
        }
        
        return loJSON;
    }
}
