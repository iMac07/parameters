package org.xersys.parameters.search;

import java.sql.ResultSet;
import java.util.ArrayList;
import org.json.simple.JSONObject;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.iface.iSearch;
import org.xersys.commander.util.MiscUtil;
import org.xersys.commander.util.SQLUtil;

public class ParamSearchF implements iSearch{
    private final int DEFAULT_MAX_RESULT = 25;
    
    private XNautilus _app = null;  
    private String _message = "";
    private boolean _initialized = false;
    
    ArrayList<String> _filter;
    ArrayList<Object> _filter_value;
    
    ArrayList<String> _filter_list;
    ArrayList<String> _filter_description;
    
    ArrayList<String> _fields;
    ArrayList<String> _fields_descript;
    
    SearchType _search_type;
    String _search_key;
    Object _search_value;
    boolean _search_exact;
    int _search_result_max_row;
    
    public ParamSearchF(XNautilus foApp, Object foValue){
        _app = foApp;
        _message = "";
        
        _search_type = (SearchType) foValue;
        
        if (_app != null && _search_type != null) {   
            _search_key = "";
            _search_value = null;
            _search_exact = false;
            _search_result_max_row = DEFAULT_MAX_RESULT;
            
            _filter = new ArrayList<>();
            _filter_value = new ArrayList<>();
            
            initFilterList();

            _initialized = true;
        }
    }

    /**
     * setKey(String fsValue)
     * \n
     * Set the field to use in searching
     * 
     * @param fsValue
     */
    @Override
    public void setKey(String fsValue) {
        _search_key = fsValue;
    }

    /**
     * setValue(Object foValue)
     * \n
     * Set the field value to use in searching
     * 
     * @param foValue
     */
    @Override
    public void setValue(Object foValue) {
        _search_value = foValue;
    }

    /**
     * setExact(boolean fbValue)
     * \n
     * Inform the object how the filter will be used on searching.
     * 
     * @param fbValue
     */
    @Override
    public void setExact(boolean fbValue) {
        _search_exact = fbValue;
    }
    
    /**
     * setMaxResult(int fnValue)
     * \n
     * Set the maximum row of results in searching
     * 
     * @param fnValue
     */
    @Override
    public void setMaxResult(int fnValue) {
        _search_result_max_row = fnValue;
    }
    
    /**
     * getValue()
     * \n
     * Get the search key value
     * 
     * @return 
     */
    @Override
    public Object getValue(){
        return _search_value;
    }
    
    /**
     * getMaxResult()
     * \n
     * Set the maximum row of results in searching
     * @return 
     */
    @Override
    public int getMaxResult() {
        return _search_result_max_row;
    }

    /**
     * getFilterListDescription(int fnRow)
     * \n
     * Get the description of filter fields.
     * 
     * @return ArrayList
     */
    @Override
    public ArrayList<String> getFilterListDescription() {
        if (!_initialized) {
            _message = "Object was not initialized.";
            return null;
        }
        
        return _filter_description;
    }
    
    /**
     * getColumns()
     * \n
     * Get fields to use in displaying results.
     * 
     * @return ArrayList
     */
    @Override
    public ArrayList<String> getColumns() {
        if (!_initialized) {
            _message = "Object was not initialized.";
            return null;
        }
        
        return _fields;
    }
    
    /**
     * getColumnNames()
     * \n
     * Get column names to use in displaying results.
     * 
     * @return ArrayList
     */
    @Override
    public ArrayList<String> getColumnNames() {
        if (!_initialized) {
            _message = "Object was not initialized.";
            return null;
        }
        
        return _fields_descript;
    }   

    /**
     * getFilter()()
     * \n
     * Get the list of fields and value the user set for filtering
     * 
     * @return ArrayList
     */
    @Override
    public ArrayList getFilter() {
        if (!_initialized) {
            _message = "Object was not initialized.";
            return null;
        }
        
        return _filter;
    }

    /**
     * addFilter(String fsField, Object foValue)
     * 
     * \n
     * Adds filter on searching
     * 
     * @param  fsField - field to filter
     * @param  foValue - field value
     * 
     * @return int - index of the field on the ArrayList
     * 
     * \n\t please see getFilterList() for available fields to use for filtering
     */
    @Override
    public int addFilter(String fsField, Object foValue) {
        if (!_initialized) {
            _message = "Object was not initialized.";
            return -1;
        }
        
        if (_filter.isEmpty()){
            _filter.add(fsField);
            _filter_value.add(foValue);
            return _filter.size()-1;
        }
        
        for (int lnCtr = 0; lnCtr <= _filter.size()-1; lnCtr++){
            if (_filter.get(lnCtr).toLowerCase().equals(fsField.toLowerCase())){
                _filter_value.set(lnCtr, foValue);
                return lnCtr;
            }
        }
            
        _filter.add(fsField);
        _filter_value.add(foValue);
        return _filter.size()-1;
    }
    
    /**
     * getFilterValue(String fsField)
     * \n
     * Get the value of a particular filter
     * 
     * @param fsField  - filter field to retrieve value
     * 
     * @return Object
     */
    @Override
    public Object getFilterValue(String fsField) {
        for (int lnCtr = 0; lnCtr <= _filter.size()-1; lnCtr++){
            if (_filter.get(lnCtr).toLowerCase().equals(fsField.toLowerCase())){
                return _filter_value.get(lnCtr);
            }
        }
        
        return null;
    }
    
    /**
     * removeFilter(String fsField)
     * \n
     * Removes filter on searching
     * 
     * @param  fsField - filter field to remove in the in the ArrayList
     * 
     * @return Boolean
     * 
     * \n\t please see getFilterList() for available fields to use for filtering
     */
    @Override
    public boolean removeFilter(String fsField) {
        if (!_initialized) {
            _message = "Object was not initialized.";
            return false;
        }
        
        if (!_filter.isEmpty()){        
            for (int lnCtr = 0; lnCtr <= _filter.size()-1; lnCtr++){
                if (_filter.get(lnCtr).toLowerCase().equals(fsField.toLowerCase())){
                    _filter.remove(lnCtr);
                    _filter_value.remove(lnCtr);
                    return true;
                }
            }
        }
        
        _message = "Filter variable was empty.";
        return false;
    }
    
    /**
     * removeFilter()
     * \n
     * Removes all filter on searching
     * 
     * @return Boolean
     */
    @Override
    public boolean removeFilter() {
        _filter.clear();
        _filter_value.clear();
        return true;
    }

    
    /**
     * getMessage()
     * \n
     * Get the warning/error message from this object.
     * 
     * @return String
     */
    @Override
    public String getMessage() {
        return _message;
    }
    
    /**
     * Search()
     * \n
     * Execute search
     * 
     * @return JSONObject
     */
    @Override
    public JSONObject Search() {
        JSONObject loJSON = new JSONObject();
        
        if (!_initialized) {
            loJSON.put("result", "error");
            loJSON.put("message", "Object was not initialized.");
            return loJSON;
        }
        
        String lsSQL = "";
        
        //get the query for the particular search type
        if (null != _search_type)switch (_search_type) {
            case searchCountry:
                lsSQL = getSQ_Country(); break;
            case searchRegion:
                lsSQL = getSQ_Region(); break;
            case searchProvince:
                lsSQL = getSQ_Province(); break;
            case searchTownCity:
                lsSQL = getSQ_TownCity(); break;
            case searchBanks:
                lsSQL = getSQ_Banks(); break;
            case searchBrand:
                lsSQL = getSQ_Brand(); break;
            case searchModel:
                lsSQL = getSQ_Model(); break;
            case searchCatalogCategory:
                lsSQL = getSQ_Catalog_Category(); break;
            case searchModelSeries:
                lsSQL = getSQ_Model_Series(); break;
            case searchInvType:
                lsSQL = getSQ_Inv_Type(); break;
            case searchTerm:
                lsSQL = getSQ_Term(); break;
            case searchMCDealer:
                lsSQL = getSQ_MC_Dealers(); break;
            case searchLabor:
                lsSQL = getSQ_Labor(); break;
            case searchBarangay:
                lsSQL = getSQ_Barangay(); break;
            case searchBranch:
                lsSQL = getSQ_Branch(); break;
        }
        
        if (lsSQL.isEmpty()){
            loJSON.put("result", "error");
            loJSON.put("message", "Query was not set for this type.");
            return loJSON;
        }
        
        //add condition
        if (_search_exact)
            lsSQL = MiscUtil.addCondition(lsSQL, _search_key + " = " + SQLUtil.toSQL(_search_value));
        else
            lsSQL = MiscUtil.addCondition(lsSQL, _search_key + " LIKE " + SQLUtil.toSQL("%" + _search_value + "%"));
        
        //add filter on query
        if (!_filter.isEmpty()){
            for (int lnCtr = 0; lnCtr <= _filter.size()-1; lnCtr++){
                lsSQL = MiscUtil.addCondition(lsSQL, getFilterField(_filter.get(lnCtr)) + " LIKE " + SQLUtil.toSQL(_filter_value.get(lnCtr)));
            }
        }
        
        //add order by based on the search key
        lsSQL +=  " ORDER BY " + _search_key;
        
        //add the max row limit on query
        lsSQL +=  " LIMIT " + _search_result_max_row;
        
        try {
            ResultSet loRS = _app.executeQuery(lsSQL);
            //convert resultset to json array string
            lsSQL = MiscUtil.RS2JSON(loRS).toJSONString();
            //close the resultset
            MiscUtil.close(loRS);
            
            //assign the value to return
            loJSON.put("result", "success");
            loJSON.put("payload", lsSQL);
        } catch (Exception ex) {
            ex.printStackTrace();
            loJSON.put("result", "error");
            loJSON.put("result", "Exception detected.");
        }
        
        return loJSON;
    }
    
    private void initFilterList(){
        _filter_list = new ArrayList<>();
        _filter_description = new ArrayList<>();
        _fields = new ArrayList<>();
        _fields_descript = new ArrayList<>();
        
        if (null != _search_type)switch (_search_type) {
            case searchCountry:
                _fields.add("sCntryCde"); _fields_descript.add("ID");
                _fields.add("sCntryNme"); _fields_descript.add("Country");
                _fields.add("sNational"); _fields_descript.add("Nationality");
                
                _filter_list.add("sCntryNme"); _filter_description.add("Country");
                _filter_list.add("IFNULL(sNational, '')"); _filter_description.add("Nationality");
                break;
            case searchRegion:
                _fields.add("sRegionID"); _fields_descript.add("ID");
                _fields.add("sRegionNm"); _fields_descript.add("Region");
                
                _filter_list.add("sRegionNm"); _filter_description.add("Region");
                break;
            case searchProvince:
                _fields.add("sProvIDxx"); _fields_descript.add("ID");
                _fields.add("sProvName"); _fields_descript.add("Province");
                _fields.add("sRegionNm"); _fields_descript.add("Region");
                
                _filter_list.add("a.sProvName"); _filter_description.add("Province Name");
                _filter_list.add("a.sRegionID"); _filter_description.add("Region ID");
                break;
            case searchTownCity:
                _fields.add("sTownIDxx"); _fields_descript.add("ID");
                _fields.add("sTownName"); _fields_descript.add("Town ");
                _fields.add("sZippCode"); _fields_descript.add("Postal Code");
                _fields.add("sProvName"); _fields_descript.add("Province");
                _fields.add("sRegionNm"); _fields_descript.add("Region");
                
                _filter_list.add("a.sTownName"); _filter_description.add("Town");
                _filter_list.add("a.sProvIDxx"); _filter_description.add("Province ID");
                _filter_list.add("b.sRegionID"); _filter_description.add("Region ID");
                break;
            case searchBanks:
                _fields.add("sBankCode"); _fields_descript.add("Code");
                _fields.add("sBankName"); _fields_descript.add("Bank");
                
                _filter_list.add("sBankName"); _filter_description.add("Bank");
                break;
            case searchBrand:
                _fields.add("sBrandCde"); _fields_descript.add("Code");
                _fields.add("sDescript"); _fields_descript.add("Brand");
                _fields.add("xInvTypNm"); _fields_descript.add("Inv. Type");
                
                _filter_list.add("a.sDescript"); _filter_description.add("Brand");
                _filter_list.add("a.sInvTypCd"); _filter_description.add("Inv. Type Code");
                break;
            case searchModel:
                _fields.add("sModelCde"); _fields_descript.add("Code");
                _fields.add("sModelNme"); _fields_descript.add("Model");
                _fields.add("sDescript"); _fields_descript.add("Description");
                _fields.add("xBrandNme"); _fields_descript.add("Brand");
                _fields.add("xInvTypNm"); _fields_descript.add("Inv. Type");
                
                _filter_list.add("a.sModelNme"); _filter_description.add("Model");
                _filter_list.add("a.sDescript"); _filter_description.add("Description");
                _filter_list.add("a.sBrandCde"); _filter_description.add("Brand Code");
                _filter_list.add("a.sInvTypCd"); _filter_description.add("Inv. Type Code");
                break;  
            case searchCatalogCategory:
                _fields.add("sCategrCd"); _fields_descript.add("Code");
                _fields.add("sDescript"); _fields_descript.add("Category");
                
                _filter_list.add("sDescript"); _filter_description.add("Category");
                break;
            case searchModelSeries:
                _fields.add("sSeriesID"); _fields_descript.add("ID");
                _fields.add("sDescript"); _fields_descript.add("Decription");
                _fields.add("xModelNme"); _fields_descript.add("Model Name");
                
                _filter_list.add("a.sDescript"); _filter_description.add("Decription");
                _filter_list.add("a.sModelCde"); _filter_description.add("Model Code");
                break;
            case searchInvType:
                _fields.add("sInvTypCd"); _fields_descript.add("Code");
                _fields.add("sDescript"); _fields_descript.add("Inv. Type");
                
                _filter_list.add("sDescript"); _filter_description.add("Inv. Type");
                break;
            case searchTerm:
                _fields.add("sTermCode"); _fields_descript.add("Code");
                _fields.add("sDescript"); _fields_descript.add("Term Name");
                
                _filter_list.add("sDescript"); _filter_description.add("Term Name");
                break;
            case searchMCDealer:
                _fields.add("sDealerCd"); _fields_descript.add("Code");
                _fields.add("sDescript"); _fields_descript.add("Dealer Name");
                
                _filter_list.add("sDescript"); _filter_description.add("Dealer Name");
                break;
            case searchLabor:
                _fields.add("sLaborCde"); _fields_descript.add("Code");
                _fields.add("sDescript"); _fields_descript.add("Description");
                _fields.add("nPriceLv1"); _fields_descript.add("Price 1");
                _fields.add("nPriceLv2"); _fields_descript.add("Price 2");
                _fields.add("nPriceLv3"); _fields_descript.add("Price 3");
                break;
            case searchBarangay:
                _fields.add("sBrgyIDxx"); _fields_descript.add("ID");
                _fields.add("sBrgyName"); _fields_descript.add("Baranagay");
                _fields.add("xTownName"); _fields_descript.add("Town");
                _fields.add("xProvName"); _fields_descript.add("Province");
                
                _filter_list.add("b.sTownName"); _filter_description.add("Town");
                _filter_list.add("c.sProvname"); _filter_description.add("Province");
            case searchBranch:
                _fields.add("sBranchCd"); _fields_descript.add("ID");
                _fields.add("sCompnyNm"); _fields_descript.add("Branch Name");
            default:
                break;
        }
    }
    
    private String getFilterField(String fsValue){
        String lsField = "";
        
        for(int lnCtr = 0; lnCtr <= _filter_description.size()-1; lnCtr++){
            if (_filter_description.get(lnCtr).toLowerCase().equals(fsValue.toLowerCase())){
                lsField = _filter_list.get(lnCtr);
                break;
            }
        }
        
        return lsField;
    }
    
    private String getSQ_Country(){
        return "SELECT" + 
                    "  sCntryCde" +
                    ", sCntryNme" +
                    ", IFNULL(sNational, '') sNational" +
                " FROM Country";
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
                    ", Region b";
    }
    
    private String getSQ_Region(){
        return "SELECT" +
                    "  sRegionID" +
                    ", sRegionNm" +
                    ", cRecdStat" +
                " FROM Region";
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
                    "  a.sBrandCde" +
                    ", a.sInvTypCd" +
                    ", a.sDescript" +
                    ", b.sDescript xInvTypNm" +
                " FROM Brand a" +
                    ", Inv_Type b" +
                " WHERE a.sInvTypCd = b.sInvTypCd";
    }
    
    private String getSQ_Model(){
        return "SELECT" +
                    "  a.sModelCde" +
                    ", a.sInvTypCd" +
                    ", a.sBriefDsc" +
                    ", a.sModelNme" +
                    ", a.sDescript" +
                    ", a.sBrandCde" +
                    ", a.sCategrCd" +
                    ", a.cEndOfLfe" +
                    ", b.sDescript xBrandNme" +
                    ", c.sDescript xInvTypNm" +
                " FROM Model a" +
                    ", Brand b" +
                    ", Inv_Type c" +
                " WHERE a.sBrandCde = b.sBrandCde" +
                    " AND a.sInvTypCd = c.sInvTypCd";
    }
    
    private String getSQ_Catalog_Category(){
        return "SELECT" +
                    "  sCategrCd" +
                    ", sDescript" +
                " FROM Catalog_Category";
    }
    
    private String getSQ_Model_Series(){
        return "SELECT" +
                    "  a.sSeriesID" +
                    ", a.sDescript" +
                    ", a.sModelCde" +
                    ", b.sDescript xModelNme" +
                " FROM Model_Series a" +
                    ", Model b" +
                " WHERE a.sModelCde = b.sModelCde";
    }
    
    private String getSQ_Inv_Type(){
        return "SELECT" +
                    "  sInvTypCd" +
                    ", sDescript" +
                " FROM Inv_Type";
    }
    
    private String getSQ_Term(){
        return "SELECT" +
                    "  sTermCode" +
                    ", sDescript" +
                    ", cCoverage" +
                    ", nTermValx" +
                    ", cRecdStat" +
                " FROM Term";
    }
    
    private String getSQ_MC_Dealers(){
        return "SELECT" +
                    "  sDealerCd" +
                    ", sDescript" +
                    ", cRecdStat" +
                " FROM MC_Dealers";
    }
    
    private String getSQ_Labor(){
        return "SELECT" +
                    "  sLaborCde" +
                    ", sDescript" +
                    ", sBriefDsc" +
                    ", nPriceLv1" +
                    ", nPriceLv2" +
                    ", nPriceLv3" +
                    ", cInHousex" +
                    ", cLaborTyp" +
                    ", cRecdStat" +
                " FROM Labor";
    }
    
    private String getSQ_Barangay(){
        return "SELECT" +
                    "  a.sBrgyIDxx" +
                    ", a.sBrgyName" +
                    ", a.sTownIDxx" +
                    ", a.cHasRoute" +
                    ", a.cBlackLst" +
                    ", a.cRecdStat" +
                    ", b.sTownName xTownName" +
                    ", c.sProvName xProvName" +
                " FROM Barangay a" +
                    ", TownCity b" +
                    ", Province c" +
                " WHERE a.sTownIDxx = b.sTownIDxx" +
                    " AND b.sProvIDxx = c.sProvIDxx";
    }
    
    private String getSQ_Branch(){
        return "SELECT" +
                    "  sBranchCd" +
                    ", sCompnyNm" +
                " FROM xxxSysClient"; 
    }
    
    //let outside objects can call this variable without initializing the class.
    public static enum SearchType{
        searchCountry,
        searchRegion,
        searchProvince,
        searchTownCity,
        searchBarangay,
        searchBanks,
        searchBrand,
        searchModel,
        searchCatalogCategory,
        searchModelSeries,
        searchInvType,
        searchTerm,
        searchMCDealer,
        searchLabor,
        searchBranch
    }
}