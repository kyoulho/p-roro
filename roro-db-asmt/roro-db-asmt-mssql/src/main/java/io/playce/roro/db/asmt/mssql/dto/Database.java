package io.playce.roro.db.asmt.mssql.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class Database {

    private String name;
    private int databaseId;
    private String sourceDatabaseId;
    private String ownerSid;
    private Integer compatibilityLevel;
    private Date createDate;
    private String collationName;
    private String userAccessDesc;
    private Boolean isReadOnly;
    private Boolean isAutoCloseOn;
    private Boolean isAutoShrinkOn;
    private String stateDesc;
    private Boolean isInStandby;
    private Boolean isCleanlyShutdown;
    private Boolean isSupplementalLoggingEnabled;
    private String snapshotIsolationStateDesc;
    private Boolean isReadCommittedSnapshotOn;
    private String recoveryModelDesc;
    private String pageVerifyOptionDesc;
    private Boolean isAutoCreateStatsOn;
    private Boolean isAutoUpdateStatsOn;
    private Boolean isAutoUpdateStatsAsyncOn;
    private Boolean isAnsiNullDefaultOn;
    private Boolean isAnsiNullsOn;
    private Boolean isAnsiPaddingOn;
    private Boolean isAnsiWarningsOn;
    private Boolean isArithabortOn;
    private Boolean isConcatNullYieldsNullOn;
    private Boolean isNumericRoundabortOn;
    private Boolean isQuotedIdentifierOn;
    private Boolean isRecursiveTriggersOn;
    private Boolean isCursorCloseOnCommitOn;
    private Boolean isLocalCursorDefault;
    private Boolean isFulltextEnabled;
    private Boolean isTrustworthyOn;
    private Boolean isDbChainingOn;
    private Boolean isParameterizationForced;
    private Boolean isMasterKeyEncryptedByServer;
    private Boolean isPublished;
    private Boolean isSubscribed;
    private Boolean isMergePublished;
    private Boolean isDistributor;
    private Boolean isSyncWithBackup;
    private Boolean isBrokerEnabled;
    private String logReuseWaitDesc;
    private Boolean isDateCorrelationOn;

    private List<ObjectSummary> objectSummaries;
    private List<Table> tables;
    private List<View> views;
    private List<Index> indexes;
    private List<Procedure> procedures;
    private List<Function> functions;
    private List<Queue> queues;
    private List<Trigger> triggers;
    private List<Sequence> sequences;
    private List<Synonym> synonyms;

}
