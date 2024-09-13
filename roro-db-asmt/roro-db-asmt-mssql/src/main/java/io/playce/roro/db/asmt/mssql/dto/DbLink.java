package io.playce.roro.db.asmt.mssql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class DbLink {

    private Integer srvId;
    private String srvName;
    private String srvProduct;
    private String providerName;
    private String datasource;
    private String location;
    private String providerString;
    private Date schemaDate;
    private String catalog;
    private String remoteUserName;
    private String srvCollation;
    private Integer connectTimeout;
    private Integer queryTimeout;
    private Boolean isRemote;
    private Boolean rpc;
    private Boolean pub;
    private Boolean sub;
    private Boolean dist;
    private Boolean dpub;
    private Boolean rpcout;
    private Boolean dataAccess;
    private Boolean collationCompatible;
    private Boolean system;
    private Boolean useRemoteCollation;
    private Boolean lazySchemaValidation;
    private String collation;
    private Boolean nonsqlsub;

}