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

public class Brand implements XRecord{    
    private final String MASTER_TABLE = "Brand";
    
    private final XNautilus p_oNautilus;
    private final boolean p_bWithParent;
    private final String p_sBranchCd;
    
    private LRecordMas p_oListener;    
    private String p_sMessagex;
    private int p_nEditMode;
    
    private CachedRowSet p_oBrand;
    
    private ParamSearchF p_oInvType;
    private ParamSearchF p_oSearchBrand;
    
    public Brand(XNautilus foNautilus, String fsBranchCd, boolean fbWithParent){
        p_oNautilus = foNautilus;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;
        
        p_oInvType = new ParamSearchF(p_oNautilus, ParamSearchF.SearchType.searchInvType);
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
            p_oBrand = factory.createCachedRowSet();
            p_oBrand.populate(loRS);
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
                
                lsSQL = MiscUtil.rowset2SQL(p_oBrand, MASTER_TABLE, "xInvTypNm");
            } else { //old record
                lsSQL = MiscUtil.rowset2SQL(p_oBrand, MASTER_TABLE, "xInvTypNm", 
                        "sBrandCde = " + SQLUtil.toSQL((String) getMaster("sBrandCde")) +
                        " AND sInvTypCd = " + SQLUtil.toSQL((String) getMaster("sInvTypCd")));
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
    public boolean OpenRecord(String fsBrandCde) {
        System.out.println(this.getClass().getSimpleName() + ".OpenRecord()");
        setMessage("");   
        
        if (p_oNautilus == null){
            p_sMessagex = "Application driver is not set.";
            return false;
        }
        
        try {
            if (p_nEditMode != EditMode.UNKNOWN){
                if (p_oBrand != null){
                    p_oBrand.first();

                    if (p_oBrand.getString("sBrandCde").equals(fsBrandCde)){
                        p_nEditMode  = EditMode.READY;
                        return true;
                    }
                }
            }
            
            String lsSQL;
            ResultSet loRS;
            
            RowSetFactory factory = RowSetProvider.newFactory();
            
            //open master record
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "sBrandCde = " + SQLUtil.toSQL(fsBrandCde));
            loRS = p_oNautilus.executeQuery(lsSQL);
            p_oBrand = factory.createCachedRowSet();
            p_oBrand.populate(loRS);
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
            p_oBrand.first();
            return p_oBrand.getObject(fnIndex);
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
                case 2:
                    getInvType((String) foValue);
                    break;
                default:
                    p_oBrand.first();
                    p_oBrand.updateObject(fnIndex, foValue);
                    p_oBrand.updateRow();
                    
                    if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oBrand.getObject(fnIndex));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage("SQLException on " + lsProcName + ". Please inform your System Admin.");
        }
    }

    @Override
    public void setMaster(String fsIndex, Object foValue){
        String lsProcName = this.getClass().getSimpleName() + ".setMaster(String fsIndex, Object foValue)";
        
        try {
            setMaster(MiscUtil.getColumnIndex(p_oBrand, fsIndex), foValue);
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage("SQLException on " + lsProcName + ". Please inform your System Admin.");
        }
    }

    @Override
    public Object getMaster(String fsFieldNm){
        try {
            return getMaster(MiscUtil.getColumnIndex(p_oBrand, fsFieldNm));
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
                    "  a.sBrandCde" +
                    ", a.sInvTypCd" +
                    ", a.sDescript" +
                    ", a.cRecdStat" +
                    ", a.dModified" +
                    ", IFNULL(b.sDescript, '') xInvTypNm" +
                " FROM " + MASTER_TABLE + " a" +
                    " LEFT JOIN Inv_Type b ON a.sInvTypCd = b.sInvTypCd";
    }
    
    private void initMaster() throws SQLException{
        p_oBrand.last();
        p_oBrand.moveToInsertRow();
        
        MiscUtil.initRowSet(p_oBrand);
        
        p_oBrand.updateObject("cRecdStat", "1");
        
        p_oBrand.insertRow();
        p_oBrand.moveToCurrentRow();
    }
    
    private boolean isEntryOK(){
        try {
            //assign values to master record
            p_oBrand.first();
            
            if (String.valueOf(getMaster("sInvTypCd")).isEmpty()){
                setMessage("Inventory type must not be empty.");
                return false;
            }
            
            if (String.valueOf(getMaster("sBrandCde")).isEmpty()){
                setMessage("Brand code must not be empty.");
                return false;
            }
            
            if (String.valueOf(getMaster("sBrandCde")).length() > 8){
                setMessage("Brand code must be more than 8 characters.");
                return false;
            }           
            
            if (String.valueOf(getMaster("sDescript")).isEmpty()){
                setMessage("Description must not be empty.");
                return false;
            }
            
            if (String.valueOf(getMaster("sBrandCde")).length() > 64){
                setMessage("Brand code must be more than 64 characters.");
                return false;
            }           
            
            p_oBrand.updateObject("dModified", p_oNautilus.getServerDate());
            p_oBrand.updateRow();

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
    
    public JSONObject searchInvType(String fsKey, Object foValue, boolean fbExact){
        p_oInvType.setKey(fsKey);
        p_oInvType.setValue(foValue);
        p_oInvType.setExact(fbExact);
        
        return p_oInvType.Search();
    }
    
    public ParamSearchF getSearchInvType(){
        return p_oInvType;
    }
    
    private void getInvType(String foValue){
        String lsProcName = this.getClass().getSimpleName() + ".getInvType()";
        
        JSONObject loJSON = searchInvType("sInvTypCd", foValue, true);
        if ("success".equals((String) loJSON.get("result"))){
            try {
                JSONParser loParser = new JSONParser();

                p_oBrand.first();
                try {
                    JSONArray loArray = (JSONArray) loParser.parse((String) loJSON.get("payload"));

                    switch (loArray.size()){
                        case 0:
                            p_oBrand.updateObject("sInvTypCd", "");
                            p_oBrand.updateObject("xInvTypNm", "");
                            p_oBrand.updateRow();
                            break;
                        default:
                            loJSON = (JSONObject) loArray.get(0);
                            p_oBrand.updateObject("sInvTypCd", (String) loJSON.get("sInvTypCd"));
                            p_oBrand.updateObject("xInvTypNm", (String) loJSON.get("sDescript"));
                            p_oBrand.updateRow();
                    }
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    p_oListener.MasterRetreive("sInvTypCd", "");
                    p_oListener.MasterRetreive("xInvTypNm", "");
                    p_oBrand.updateRow();
                }

                p_oListener.MasterRetreive("sInvTypCd", (String) getMaster("xInvTypNm"));
            } catch (SQLException ex) {
                ex.printStackTrace();
                setMessage("SQLException on " + lsProcName + ". Please inform your System Admin.");
            }
        }
    }
}
