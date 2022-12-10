package org.xersys.parameters.base;

import org.xersys.commander.iface.LRecordMas;
import com.mysql.jdbc.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.xersys.commander.contants.EditMode;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.iface.XRecord;
import org.xersys.commander.util.MiscUtil;
import org.xersys.commander.util.SQLUtil;
import org.xersys.parameters.search.ParamSearchF;

public class Model implements XRecord{    
    private final String MASTER_TABLE = "Model";
    
    private final XNautilus p_oNautilus;
    private final boolean p_bWithParent;
    private final String p_sBranchCd;
    
    private LRecordMas p_oListener;    
    private String p_sMessagex;
    private int p_nEditMode;
    
    private CachedRowSet p_oModel;
    
    private ParamSearchF p_oInvType;
    private ParamSearchF p_oSearchBrand;
        private ParamSearchF p_oSearchModel;
    
    public Model(XNautilus foNautilus, String fsBranchCd, boolean fbWithParent){
        p_oNautilus = foNautilus;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;
        
        p_oInvType = new ParamSearchF(p_oNautilus, ParamSearchF.SearchType.searchInvType);
        p_oSearchModel = new ParamSearchF(p_oNautilus, ParamSearchF.SearchType.searchModel);
        p_oSearchBrand = new ParamSearchF(p_oNautilus, ParamSearchF.SearchType.searchBrand);
        
        p_nEditMode = EditMode.UNKNOWN;
    }
    
    @Override
    public int getEditMode() {
        return p_nEditMode;
    }
    
    @Override
    public boolean NewRecord() {
        System.out.println(this.getClass().getSimpleName() + ".NewRecord()");
        
        if (p_oNautilus == null){
            p_sMessagex = "Application driver is not set.";
            return false;
        }
        
        try {
            String lsSQL;
            ResultSet loRS;
            
            RowSetFactory factory = RowSetProvider.newFactory();
            
            //create empty master record
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "0=1");
            loRS = p_oNautilus.executeQuery(lsSQL);
            p_oModel = factory.createCachedRowSet();
            p_oModel.populate(loRS);
            MiscUtil.close(loRS);
            initMaster();           
        } catch (SQLException ex) {
            setMessage(ex.getMessage());
            return false;
        }
        
        p_nEditMode = EditMode.ADDNEW;
        return true;
    }

    @Override
    public boolean SaveRecord() {
        System.out.println(this.getClass().getSimpleName() + ".SaveRecord()");
        
        setMessage("");
        
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            System.err.println("Transaction is not on update mode.");
            return false;
        }
        
        String lsSQL = "";
        
        if (!isEntryOK()) return false;

        try {
            if (!p_bWithParent) p_oNautilus.beginTrans();
        
            if (p_nEditMode == EditMode.ADDNEW){
                Connection loConn = getConnection();                
                
                if (!p_bWithParent) MiscUtil.close(loConn);
                
                lsSQL = MiscUtil.rowset2SQL(p_oModel, MASTER_TABLE, "xBrandNme");
            } else { //old record
                lsSQL = MiscUtil.rowset2SQL(p_oModel, MASTER_TABLE, "xBrandNme", "sModelCde = " + SQLUtil.toSQL((String) getMaster("sModelCde")));
            }
            
            if (lsSQL.equals("")){
                if (!p_bWithParent) p_oNautilus.rollbackTrans();
                
                setMessage("No record to update");
                return false;
            }
            
            if(p_oNautilus.executeUpdate(lsSQL, MASTER_TABLE, p_sBranchCd, "") <= 0){
                if(!p_oNautilus.getMessage().isEmpty())
                    setMessage(p_oNautilus.getMessage());
                else
                    setMessage("No record updated");
                
                if (!p_bWithParent) p_oNautilus.rollbackTrans();
                
                p_nEditMode = EditMode.UNKNOWN;
                return false;
            } 

            if (!p_bWithParent) p_oNautilus.commitTrans();
        } catch (SQLException ex) {
            if (!p_bWithParent) p_oNautilus.rollbackTrans();
            
            ex.printStackTrace();
            setMessage(ex.getMessage());
            return false;
        }
        
        p_nEditMode = EditMode.UNKNOWN;
        return true;
    }

    @Override
    public boolean UpdateRecord() {        
        if (p_nEditMode != EditMode.READY) {
            setMessage("No transaction was loaded.");
            return false;
        }
        
        p_nEditMode = EditMode.UPDATE;
        return true;
    }

    @Override
    public boolean OpenRecord(String fsModelCde) {
        System.out.println(this.getClass().getSimpleName() + ".OpenRecord()");
        setMessage("");   
        
        if (p_oNautilus == null){
            p_sMessagex = "Application driver is not set.";
            return false;
        }
        
        try {
            if (p_nEditMode != EditMode.UNKNOWN){
                if (p_oModel != null){
                    p_oModel.first();

                    if (p_oModel.getString("sModelCde").equals(fsModelCde)){
                        p_nEditMode  = EditMode.READY;
                        return true;
                    }
                }
            }
            
            String lsSQL;
            ResultSet loRS;
            
            RowSetFactory factory = RowSetProvider.newFactory();
            
            //open master record
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "sModelCde = " + SQLUtil.toSQL(fsModelCde));
            loRS = p_oNautilus.executeQuery(lsSQL);
            p_oModel = factory.createCachedRowSet();
            p_oModel.populate(loRS);
            MiscUtil.close(loRS);
            
            p_nEditMode = EditMode.READY;
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            setMessage(ex.getMessage());
        }
        
        p_nEditMode  = EditMode.UNKNOWN;
        return false;
    }

    @Override
    public boolean DeleteRecord(String fsTransNox) {
        return false;
    }

    @Override
    public boolean DeactivateRecord(String fsTransNox) {
        return false;
    }

    @Override
    public boolean ActivateRecord(String fsTransNox) {
        return false;
    }
    
    @Override
    public String getMessage() {
        return p_sMessagex;
    }

    @Override
    public void setListener(Object foListener) {
        p_oListener = (LRecordMas) foListener;
    }
    
    @Override
    public Object getMaster(int fnIndex) {
        try {
            p_oModel.first();
            return p_oModel.getObject(fnIndex);
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public void setMaster(int fnIndex, Object foValue) {
        String lsProcName = this.getClass().getSimpleName() + ".setMaster(int fnIndex, Object foValue)";
        
        if (p_nEditMode != EditMode.ADDNEW &&
            p_nEditMode != EditMode.UPDATE){
            System.err.println("Transaction is not on update mode.");
            return;
        }
        
        try {
            switch (fnIndex){
                case 6:
                    getBrand((String) foValue);
                    break;
                default:
                    p_oModel.first();
                    p_oModel.updateObject(fnIndex, foValue);
                    p_oModel.updateRow();
            }
            

            if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oModel.getObject(fnIndex));
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage("SQLException on " + lsProcName + ". Please inform your System Admin.");
        }
    }

    @Override
    public void setMaster(String fsIndex, Object foValue){
        String lsProcName = this.getClass().getSimpleName() + ".setMaster(String fsIndex, Object foValue)";
        
        try {
            setMaster(MiscUtil.getColumnIndex(p_oModel, fsIndex), foValue);
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage("SQLException on " + lsProcName + ". Please inform your System Admin.");
        }
    }

    @Override
    public Object getMaster(String fsFieldNm){
        try {
            return getMaster(MiscUtil.getColumnIndex(p_oModel, fsFieldNm));
        } catch (SQLException e) {
            return null;
        }
    }
    
    private Connection getConnection(){         
        Connection foConn;
        
        if (p_bWithParent){
            foConn = (Connection) p_oNautilus.getConnection().getConnection();
            
            if (foConn == null) foConn = (Connection) p_oNautilus.doConnect();
        } else 
            foConn = (Connection) p_oNautilus.doConnect();
        
        return foConn;
    }
    
    private void setMessage(String fsValue){
        p_sMessagex = fsValue;
    }
    
    private String getSQ_Master(){
        return "SELECT" +
                    "  a.sModelCde" +	
                    ", a.sInvTypCd" +	
                    ", a.sBriefDsc" +	
                    ", a.sModelNme" +	
                    ", a.sDescript" +	
                    ", a.sBrandCde" +	
                    ", a.sCategrCd" +
                    ", a.cEndOfLfe" +
                    ", a.cRecdStat" +
                    ", a.dModified" +
                    ", IFNULL(b.sDescript, '') xBrandNme" +
                " FROM " + MASTER_TABLE + " a" +
                    " LEFT JOIN Brand b ON a.sBrandCde = b.sBrandCde";
    }
    
    private void initMaster() throws SQLException{
        p_oModel.last();
        p_oModel.moveToInsertRow();
        
        MiscUtil.initRowSet(p_oModel);
        
        p_oModel.updateObject("cEndOfLfe", "0");
        p_oModel.updateObject("cRecdStat", "1");
        
        p_oModel.insertRow();
        p_oModel.moveToCurrentRow();
    }
    
    private boolean isEntryOK(){
        try {
            if (String.valueOf(getMaster("sModelCde")).isEmpty()){
                setMessage("Model code must not be empty.");
                return false;
            }       
            
            if (String.valueOf(getMaster("sModelCde")).length() > 15){
                setMessage("Model code must be more than 15 characters.");
                return false;
            }  
                    
            if (String.valueOf(getMaster("sBriefDsc")).isEmpty()){
                setMessage("Brief description must not be empty.");
                return false;
            }
            
            if (String.valueOf(getMaster("sBriefDsc")).length() > 32){
                setMessage("Brief desc. must be more than 32 characters.");
                return false;
            }  
            
            if (String.valueOf(getMaster("sDescript")).isEmpty()){
                setMessage("Description must not be empty.");
                return false;
            }
            
            if (String.valueOf(getMaster("sBriefDsc")).length() > 64){
                setMessage("Description must be more than 64 characters.");
                return false;
            }  
            
            if (String.valueOf(getMaster("sModelNme")).isEmpty()){
                setMessage("Model name must not be empty.");
                return false;
            }
            
            if (String.valueOf(getMaster("sModelNme")).length() > 64){
                setMessage("Name must be more than 64 characters.");
                return false;
            } 
            
            if (String.valueOf(getMaster("sBrandCde")).isEmpty()){
                setMessage("Brand must not be empty.");
                return false;
            }
            
            //assign values to master record
            p_oModel.first();
            p_oModel.updateObject("dModified", p_oNautilus.getServerDate());
            p_oModel.updateRow();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage(e.getMessage());
            return false;
        }
    }
    
    public JSONObject searchBrand(String fsKey, Object foValue, boolean fbExact){
        p_oSearchBrand.setKey(fsKey);
        p_oSearchBrand.setValue(foValue);
        p_oSearchBrand.setExact(fbExact);
        
        return p_oSearchBrand.Search();
    }
    
    public ParamSearchF getSearchBrand(){
        return p_oSearchBrand;
    }
    
    public JSONObject searchModel(String fsKey, Object foValue, boolean fbExact){
        p_oSearchModel.setKey(fsKey);
        p_oSearchModel.setValue(foValue);
        p_oSearchModel.setExact(fbExact);
        
        return p_oSearchModel.Search();
    }
    
    public ParamSearchF getSearchModel(){
        return p_oSearchModel;
    }
    
    private void getBrand(String foValue){
        String lsProcName = this.getClass().getSimpleName() + ".getBrand()";
        
        JSONObject loJSON = searchBrand("a.sBrandCde", foValue, true);
        if ("success".equals((String) loJSON.get("result"))){
            try {
                JSONParser loParser = new JSONParser();

                p_oModel.first();
                try {
                    JSONArray loArray = (JSONArray) loParser.parse((String) loJSON.get("payload"));

                    switch (loArray.size()){
                        case 0:
                            p_oModel.updateObject("sInvTypCd", "");
                            p_oModel.updateObject("sBrandCde", "");
                            p_oModel.updateObject("xBrandNme", "");
                            p_oModel.updateRow();
                            break;
                        default:
                            loJSON = (JSONObject) loArray.get(0);
                            p_oModel.updateObject("sInvTypCd", (String) loJSON.get("sInvTypCd"));
                            p_oModel.updateObject("sBrandCde", (String) loJSON.get("sBrandCde"));
                            p_oModel.updateObject("xBrandNme", (String) loJSON.get("sDescript"));
                            p_oModel.updateRow();
                    }
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    p_oListener.MasterRetreive("sBrandCde", "");
                    p_oListener.MasterRetreive("xBrandNme", "");
                    p_oModel.updateRow();
                }

                p_oListener.MasterRetreive("sBrandCde", (String) getMaster("xBrandNme"));
            } catch (SQLException ex) {
                ex.printStackTrace();
                setMessage("SQLException on " + lsProcName + ". Please inform your System Admin.");
            }
        }
    }
}
