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

public class Province implements XRecord{    
    private final String MASTER_TABLE = "Brand";
    
    private final XNautilus p_oNautilus;
    private final boolean p_bWithParent;
    private final String p_sBranchCd;
    
    private LRecordMas p_oListener;    
    private String p_sMessagex;
    private int p_nEditMode;
    
    private CachedRowSet p_oProvince;
    
    private ParamSearchF p_oRegion;
    private ParamSearchF p_oSearchProvince;
    
    public Province(XNautilus foNautilus, String fsBranchCd, boolean fbWithParent){
        p_oNautilus = foNautilus;
        p_sBranchCd = fsBranchCd;
        p_bWithParent = fbWithParent;
        
        p_oRegion = new ParamSearchF(p_oNautilus, ParamSearchF.SearchType.searchRegion);
        p_oSearchProvince = new ParamSearchF(p_oNautilus, ParamSearchF.SearchType.searchProvince);
        
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
            p_oProvince = factory.createCachedRowSet();
            p_oProvince.populate(loRS);
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
                
                lsSQL = MiscUtil.rowset2SQL(p_oProvince, MASTER_TABLE, "xRegionNm");
            } else { //old record
                lsSQL = MiscUtil.rowset2SQL(p_oProvince, MASTER_TABLE, "xRegionNm", 
                        "sProvIDxx = " + SQLUtil.toSQL((String) getMaster("sProvIDxx")));
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
    public boolean OpenRecord(String fsProvIDxx) {
        System.out.println(this.getClass().getSimpleName() + ".OpenRecord()");
        setMessage("");   
        
        if (p_oNautilus == null){
            p_sMessagex = "Application driver is not set.";
            return false;
        }
        
        try {
            if (p_nEditMode != EditMode.UNKNOWN){
                if (p_oProvince != null){
                    p_oProvince.first();

                    if (p_oProvince.getString("sProvIDxx").equals(fsProvIDxx)){
                        p_nEditMode  = EditMode.READY;
                        return true;
                    }
                }
            }
            
            String lsSQL;
            ResultSet loRS;
            
            RowSetFactory factory = RowSetProvider.newFactory();
            
            //open master record
            lsSQL = MiscUtil.addCondition(getSQ_Master(), "sProvIDxx = " + SQLUtil.toSQL(fsProvIDxx));
            loRS = p_oNautilus.executeQuery(lsSQL);
            p_oProvince = factory.createCachedRowSet();
            p_oProvince.populate(loRS);
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
            p_oProvince.first();
            return p_oProvince.getObject(fnIndex);
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
                case 3:
                    getRegion((String) foValue);
                    break;
                default:
                    p_oProvince.first();
                    p_oProvince.updateObject(fnIndex, foValue);
                    p_oProvince.updateRow();
                    
                    if (p_oListener != null) p_oListener.MasterRetreive(fnIndex, p_oProvince.getObject(fnIndex));
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
            setMaster(MiscUtil.getColumnIndex(p_oProvince, fsIndex), foValue);
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage("SQLException on " + lsProcName + ". Please inform your System Admin.");
        }
    }

    @Override
    public Object getMaster(String fsFieldNm){
        try {
            return getMaster(MiscUtil.getColumnIndex(p_oProvince, fsFieldNm));
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
                    "  sProvIDxx" +
                    ", sProvName" +
                    ", sRegionID" +
                    ", cRecdStat" +
                    ", dModified" +
                    ", IFNULL(b.sRegionNm, '') xRegionNm" +
                " FROM " + MASTER_TABLE + " a" +
                    " LEFT JOIN Region b ON a.sRegionID = b.sRegionID";
    }
    
    private void initMaster() throws SQLException{
        p_oProvince.last();
        p_oProvince.moveToInsertRow();
        
        MiscUtil.initRowSet(p_oProvince);
        
        p_oProvince.updateObject("cRecdStat", "1");
        
        p_oProvince.insertRow();
        p_oProvince.moveToCurrentRow();
    }
    
    private boolean isEntryOK(){
        try {
            //assign values to master record
            p_oProvince.first();
            
            if (String.valueOf(getMaster("sRegionID")).isEmpty()){
                setMessage("Region must not be empty.");
                return false;
            }
            
            if (String.valueOf(getMaster("sProvName")).isEmpty()){
                setMessage("Description must not be empty.");
                return false;
            }
            
            p_oProvince.updateObject("dModified", p_oNautilus.getServerDate());
            p_oProvince.updateRow();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            setMessage(e.getMessage());
            return false;
        }
    }
    
    public JSONObject searchProvince(String fsKey, Object foValue, boolean fbExact){
        p_oSearchProvince.setKey(fsKey);
        p_oSearchProvince.setValue(foValue);
        p_oSearchProvince.setExact(fbExact);
        
        return p_oSearchProvince.Search();
    }
    
    public ParamSearchF getSearchProvince(){
        return p_oSearchProvince;
    }
    
    public JSONObject searchRegion(String fsKey, Object foValue, boolean fbExact){
        p_oRegion.setKey(fsKey);
        p_oRegion.setValue(foValue);
        p_oRegion.setExact(fbExact);
        
        return p_oRegion.Search();
    }
    
    public ParamSearchF getSearchRegion(){
        return p_oRegion;
    }
    
    private void getRegion(String foValue){
        String lsProcName = this.getClass().getSimpleName() + ".getInvType()";
        
        JSONObject loJSON = searchRegion("sRegionID", foValue, true);
        if ("success".equals((String) loJSON.get("result"))){
            try {
                JSONParser loParser = new JSONParser();

                p_oProvince.first();
                try {
                    JSONArray loArray = (JSONArray) loParser.parse((String) loJSON.get("payload"));

                    switch (loArray.size()){
                        case 0:
                            p_oProvince.updateObject("sRegionID", "");
                            p_oProvince.updateObject("xRegionNm", "");
                            p_oProvince.updateRow();
                            break;
                        default:
                            loJSON = (JSONObject) loArray.get(0);
                            p_oProvince.updateObject("sRegionID", (String) loJSON.get("sRegionID"));
                            p_oProvince.updateObject("xRegionNm", (String) loJSON.get("sRegionNm"));
                            p_oProvince.updateRow();
                    }
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    p_oListener.MasterRetreive("sRegionID", "");
                    p_oListener.MasterRetreive("xRegionNm", "");
                    p_oProvince.updateRow();
                }

                p_oListener.MasterRetreive("sRegionID", (String) getMaster("xRegionNm"));
            } catch (SQLException ex) {
                ex.printStackTrace();
                setMessage("SQLException on " + lsProcName + ". Please inform your System Admin.");
            }
        }
    }
}
