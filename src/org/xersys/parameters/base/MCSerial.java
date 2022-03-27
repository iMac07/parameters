package org.xersys.parameters.base;

import org.xersys.commander.iface.LRecordMas;
import com.mysql.jdbc.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import org.json.simple.JSONObject;
import org.xersys.commander.contants.EditMode;
import org.xersys.commander.iface.XNautilus;
import org.xersys.commander.iface.XRecord;
import org.xersys.commander.util.MiscUtil;
import org.xersys.commander.util.SQLUtil;
import org.xersys.parameters.search.ParamSearchF;

public class MCSerial implements XRecord{    
    private final String MASTER_TABLE = "Inv_Serial";
    
    private final XNautilus p_oNautilus;
    private final boolean p_bWithParent;
    private final String p_sBranchCd;
    
    private LRecordMas p_oListener;    
    private String p_sMessagex;
    private int p_nEditMode;
    
    private CachedRowSet p_oSerial;
    
    private ParamSearchF p_oSearchSerial;
    
    public MCSerial(XNautilus foNautilus, String fsBranchCd, boolean fbWithParent){
        p_oNautilus = foNautilus;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;
        
        p_oSearchSerial = new ParamSearchF(p_oNautilus, ParamSearchF.SearchType.searchSerial);
        
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
            p_oSerial = factory.createCachedRowSet();
            p_oSerial.populate(loRS);
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
                                
                p_oSerial.first();
                p_oSerial.updateObject("sSerialID", MiscUtil.getNextCode(MASTER_TABLE, "sSerialID", true, loConn, p_sBranchCd));
                p_oSerial.updateRow();
                
                if (!p_bWithParent) MiscUtil.close(loConn);
                
                lsSQL = MiscUtil.rowset2SQL(p_oSerial, MASTER_TABLE, "");
            } else { //old record
                lsSQL = MiscUtil.rowset2SQL(p_oSerial, MASTER_TABLE, "", "sSerialID = " + SQLUtil.toSQL((String) getMaster("sSerialID")));
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
        if (p_nEditMode != EditMode.READY) return false;
        
        p_nEditMode = EditMode.UPDATE;
        return true;
    }

    @Override
    public boolean OpenRecord(String fsSerialID) {
        System.out.println(this.getClass().getSimpleName() + ".OpenRecord()");
        setMessage("");   
        
        if (p_oNautilus == null){
            p_sMessagex = "Application driver is not set.";
            return false;
        }
        
        try {
            if (p_nEditMode != EditMode.UNKNOWN){
                if (p_oSerial != null){
                    p_oSerial.first();

                    if (p_oSerial.getString("sSerialID").equals(fsSerialID)){
                        p_nEditMode  = EditMode.READY;
                        return true;
                    }
                }
            }
            
            String lsSQL;
            ResultSet loRS;
            
            RowSetFactory factory = RowSetProvider.newFactory();
            
            //open master record
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "sSerialID = " + SQLUtil.toSQL(fsSerialID));
            loRS = p_oNautilus.executeQuery(lsSQL);
            p_oSerial = factory.createCachedRowSet();
            p_oSerial.populate(loRS);
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
            p_oSerial.first();
            return p_oSerial.getObject(fnIndex);
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
            p_oSerial.first();
            p_oSerial.updateObject(fnIndex, foValue);
            p_oSerial.updateRow();

            if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oSerial.getObject(fnIndex));
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage("SQLException on " + lsProcName + ". Please inform your System Admin.");
        }
    }

    @Override
    public void setMaster(String fsIndex, Object foValue){
        String lsProcName = this.getClass().getSimpleName() + ".setMaster(String fsIndex, Object foValue)";
        
        try {
            setMaster(MiscUtil.getColumnIndex(p_oSerial, fsIndex), foValue);
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage("SQLException on " + lsProcName + ". Please inform your System Admin.");
        }
    }

    @Override
    public Object getMaster(String fsFieldNm){
        try {
            return getMaster(MiscUtil.getColumnIndex(p_oSerial, fsFieldNm));
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
                    "  sSerialID" +
                    ", sBranchCd" +
                    ", sSerial01" +
                    ", sSerial02" +
                    ", nUnitPrce" +
                    ", sStockIDx" +
                    ", cLocation" +
                    ", cSoldStat" +
                    ", cUnitType" +
                    ", sCompnyID" +
                    ", sWarranty" +
                    ", dModified" +
                " FROM " + MASTER_TABLE;
    }
    
    private void initMaster() throws SQLException{
        p_oSerial.last();
        p_oSerial.moveToInsertRow();
        
        MiscUtil.initRowSet(p_oSerial);
        p_oSerial.updateObject("cLocation", "3");
        p_oSerial.updateObject("cSoldStat", "1");
        p_oSerial.updateObject("cUnitType", "1");
        
        p_oSerial.insertRow();
        p_oSerial.moveToCurrentRow();
    }
    
    private boolean isEntryOK(){
        try {
            //assign values to master record
            p_oSerial.first();
            
            if (String.valueOf(getMaster("sSerial01")).isEmpty()){
                setMessage("Engine no. must not be empty.");
                return false;
            }
            
            if (String.valueOf(getMaster("sSerial02")).isEmpty()){
                setMessage("Frame no. must not be empty.");
                return false;
            }
            
            p_oSerial.updateObject("sBranchCd", (String) p_oNautilus.getBranchConfig("sBranchCd"));
            p_oSerial.updateObject("dModified", p_oNautilus.getServerDate());
            p_oSerial.updateRow();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage(e.getMessage());
            return false;
        }
    }
    
    public JSONObject searchSerial(String fsKey, Object foValue, boolean fbExact){
        p_oSearchSerial.setKey(fsKey);
        p_oSearchSerial.setValue(foValue);
        p_oSearchSerial.setExact(fbExact);
        
        return p_oSearchSerial.Search();
    }
    
    public ParamSearchF getSearchSerial(){
        return p_oSearchSerial;
    }
}
