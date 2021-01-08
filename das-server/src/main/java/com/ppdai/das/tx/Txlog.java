package com.ppdai.das.tx;

import java.sql.JDBCType;
import java.math.BigInteger;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import com.ppdai.das.client.ColumnDefinition;
import com.ppdai.das.client.TableDefinition;

/**
 * create by das-console
 * 请勿修改此文件
 */

@Table
public final class Txlog {

    public static final TxlogDefinition TXLOG = new TxlogDefinition();

    public static class TxlogDefinition extends TableDefinition {
        public final ColumnDefinition id;
        public final ColumnDefinition xid;
        public final ColumnDefinition nodeid;
        public final ColumnDefinition ip;
        public final ColumnDefinition type;
        public final ColumnDefinition status;
        public final ColumnDefinition applicationid;
        public final ColumnDefinition inserttime;

        public TxlogDefinition as(String alias) {
            return _as(alias);
        }
        public TxlogDefinition inShard(String shardId) {
            return _inShard(shardId);
        }

        public TxlogDefinition shardBy(String shardValue) {
            return _shardBy(shardValue);
        }

        public TxlogDefinition() {
            super("txlog");
            id = column("id", JDBCType.BIGINT);
            xid = column("XID", JDBCType.VARCHAR);
            nodeid = column("nodeID", JDBCType.VARCHAR);
            ip = column("ip", JDBCType.VARCHAR);
            type = column("type", JDBCType.VARCHAR);
            status = column("status", JDBCType.VARCHAR);
            applicationid = column("applicationID", JDBCType.VARCHAR);
            inserttime = column("inserttime", JDBCType.TIMESTAMP);
            setColumnDefinitions(
                    id, xid, nodeid, ip, type, status, applicationid, inserttime
            );
        }
    }


    /** PK **/
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;

    @Column(name = "XID")
    private String xid;

    @Column(name = "nodeID")
    private String nodeid;

    @Column(name = "ip")
    private String ip;

    @Column(name = "type")
    private String type;

    @Column(name = "status")
    private String status;

    @Column(name = "applicationID")
    private String applicationid;

    @Column(name = "inserttime")
    private Timestamp inserttime;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger aId) {
        this.id = aId;
    }

    public String getXID() {
        return xid;
    }

    public void setXID(String aXID) {
        this.xid = aXID;
    }

    public String getNodeID() {
        return nodeid;
    }

    public void setNodeID(String aNodeID) {
        this.nodeid = aNodeID;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String aIp) {
        this.ip = aIp;
    }

    public String getType() {
        return type;
    }

    public void setType(String aType) {
        this.type = aType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String aStatus) {
        this.status = aStatus;
    }

    public String getApplicationID() {
        return applicationid;
    }

    public void setApplicationID(String aApplicationID) {
        this.applicationid = aApplicationID;
    }

    public Timestamp getInserttime() {
        return inserttime;
    }

    public void setInserttime(Timestamp aInserttime) {
        this.inserttime = aInserttime;
    }

}