package org.xersys.parameters.search;

import org.json.simple.JSONObject;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.iface.XNeoSearch;

public class ParamSearchEngine implements XNeoSearch{
    private final int DEFAULT_LIMIT = 50;
    
    private XNautilus _nautilus;
    
    private Object _type;
    
    private String _value;
    private String _key;
    private String _filter;
    private int _max;
    private boolean _exact;
    
    public ParamSearchEngine(XNautilus foValue){
        _nautilus = foValue;
        
        _type = null;
        _value = "";
        _key = "";
        _filter = "";
        _max = DEFAULT_LIMIT;
        _exact = false;
    }
    
    @Override
    public void setSearchType(Object foValue){
        _type = foValue;
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
    public void setMax(int fnValue) {
        _max = fnValue;
    }

    @Override
    public void setExact(boolean fbValue) {
        _exact = fbValue;
    }

    @Override
    public JSONObject Search(Object foValue) {
        ParamSearchFactory _instance = new ParamSearchFactory(_nautilus, _key, _filter, _max, _exact);
        
        JSONObject loJSON = null;
        String lsColName;
        
        ParamSearchFactory.Type loType = (ParamSearchFactory.Type) _type;
        
        if (null != loType)switch (loType) {
            case searchCountry:
                lsColName = "sCntryNme»sNational»sCntryCde";
                loJSON = _instance.searchParameter(loType, (String) foValue, lsColName);
                if ("success".equals((String) loJSON.get("result"))) {
                    loJSON.put("headers", "Country»Nationality»ID");
                    loJSON.put("colname", lsColName);
                }   break;
            case searchRegion:
                lsColName = "sRegionID»sRegionNm";
                loJSON = _instance.searchParameter(loType, (String) foValue, lsColName);
                if ("success".equals((String) loJSON.get("result"))) {
                    loJSON.put("headers", "ID»Region");
                    loJSON.put("colname", lsColName);
                }   break;
            case searchProvince:
                lsColName = "sProvIDxx»sProvName»sRegionNm";
                loJSON = _instance.searchParameter(loType, (String) foValue, lsColName);
                if ("success".equals((String) loJSON.get("result"))) {
                    loJSON.put("headers", "ID»Province»Region");
                    loJSON.put("colname", lsColName);
                }   break;
            case searchTownCity:
                lsColName = "sTownName»sProvName»sRegionNm»sZippCode»sTownIDxx";
                loJSON = _instance.searchParameter(loType, (String) foValue, lsColName);
                if ("success".equals((String) loJSON.get("result"))) {
                    loJSON.put("headers", "Town/City»Province»Region»Postal Code»ID");
                    loJSON.put("colname", lsColName);
                }   break;
            case searchBanks:
                lsColName = "sBankCode»sBankName";
                loJSON = _instance.searchParameter(loType, (String) foValue, lsColName);
                if ("success".equals((String) loJSON.get("result"))) {
                    loJSON.put("headers", "Code»Name");
                    loJSON.put("colname", lsColName);
                }   break;
            default:
                break;
        }
        
        return loJSON;
    }
}
