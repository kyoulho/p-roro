package io.playce.roro.db.asmt.sybase.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ObjectSummary {

    private int computedColumn;
    private int Default;
    private int decryptDefault;
    private int encryptionKey;
    private int SqlJFunction;
    private int log;
    private int partitionCondition;
    private int transactSqlOrSqlJProcedure;
    private int thePredicateOfPrivilege;
    private int prepareObjects;
    private int rule;
    private int referentialConstraint;
    private int precomputedResultSet;
    private int systemTable;
    private int scalarOrUserDefinedFunctions;
    private int trigger;
    private int userTable;
    private int view;
    private int extendedStoredProcedure;

}