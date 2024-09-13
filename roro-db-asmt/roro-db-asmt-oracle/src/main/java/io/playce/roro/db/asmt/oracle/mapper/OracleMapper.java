package io.playce.roro.db.asmt.oracle.mapper;

import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.db.asmt.oracle.config.SqlSessionConfig;
import io.playce.roro.db.asmt.oracle.dto.Package;
import io.playce.roro.db.asmt.oracle.dto.*;
import io.playce.roro.db.asmt.util.CustomDelegatingDatabase;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static io.playce.roro.common.util.ThreadLocalUtils.DB_SCAN_ERROR;

@Slf4j
@Repository
public class OracleMapper {

    public Instance getInstance(DataSource dataSource) {
        Instance instance;

        log.debug("[17/27] Oracle Assessment - getInstance() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            instance = sqlSession.selectOne("database.assessment.oracle.selectInstance");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select an instance information error.");
            return new Instance();
        }

        log.debug("Oracle Assessment - DatabaseSize() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            instance.setDbSizeMb(sqlSession.selectOne("database.assessment.oracle.selectDbSize"));
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select dbSize error.");
            return instance;
        }

        return instance;
    }

    public Sga getSga(DataSource dataSource) {
        Sga sga = new Sga();
        List<SgaTemp> sgaTemps;

        log.debug("[18/27] Oracle Assessment - getSga() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            sgaTemps = sqlSession.selectList("database.assessment.oracle.selectSga");

            for (SgaTemp sgaTemp : sgaTemps) {
                if (sgaTemp.getName().equals("Fixed Size")) {
                    sga.setFixedSize(sgaTemp.getValue());
                } else if (sgaTemp.getName().equals("Variable Size")) {
                    sga.setVariableSize(sgaTemp.getValue());
                } else if (sgaTemp.getName().equals("Database Buffers")) {
                    sga.setDatabaseBuffers(sgaTemp.getValue());
                } else if (sgaTemp.getName().equals("Redo Buffers")) {
                    sga.setRedoBuffers(sgaTemp.getValue());
                } else if (sgaTemp.getName().equals("total_size_mb")) {
                    sga.setTotalSizeMb(sgaTemp.getValue());
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select a SGA error.");
            return new Sga();
        }

        return sga;
    }


    public List<DataFile> getDataFiles(DataSource dataSource) {
        List<DataFile> dataFiles;

        log.debug("[19/27] Oracle Assessment - getDataFiles() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            dataFiles = sqlSession.selectList("database.assessment.oracle.selectDataFile");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select data file list error.");
            return new ArrayList<>();
        }

        return dataFiles;
    }

    public List<ControlFile> getControlFiles(DataSource dataSource) {
        List<ControlFile> controlFiles;

        log.debug("[20/27] Oracle Assessment - getControlFiles() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            controlFiles = sqlSession.selectList("database.assessment.oracle.selectControlFile");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select control file list error.");
            return new ArrayList<>();
        }

        return controlFiles;
    }

    public List<LogFile> getLogFiles(DataSource dataSource) {
        List<LogFile> logFiles;

        log.debug("[21/27] Oracle Assessment - getLogFiles() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            logFiles = sqlSession.selectList("database.assessment.oracle.selectLogFile");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select log file list error.");
            return new ArrayList<>();
        }

        return logFiles;
    }

    public List<TableSpace> getTableSpaces(DataSource dataSource) {
        List<TableSpace> tableSpaces;

        log.debug("[22/27] Oracle Assessment - getTableSpaces() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            tableSpaces = sqlSession.selectList("database.assessment.oracle.selectTableSpace");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select table space list error.");
            return new ArrayList<>();
        }

        return tableSpaces;
    }

    public List<Parameter> getParameters(DataSource dataSource) {
        List<Parameter> parameters;

        log.debug("[23/27] Oracle Assessment - getParameters() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            parameters = sqlSession.selectList("database.assessment.oracle.selectParameter");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select parameter list error.");
            return new ArrayList<>();
        }

        return parameters;
    }

    public Segment getSegment(DataSource dataSource) {
        Segment segment = new Segment();
        List<SegmentTemp> segmentTemps;

        log.debug("[24/27] Oracle Assessment - getSegment() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            segmentTemps = sqlSession.selectList("database.assessment.oracle.selectSegment");

            for (SegmentTemp segmentTemp : segmentTemps) {
                if (segmentTemp.getName().equals("CLUSTER")) {
                    segment.setCluster(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("INDEX")) {
                    segment.setIndex(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("INDEX PARTITION")) {
                    segment.setIndexPartition(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("LOB PARTITION")) {
                    segment.setLobPartition(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("LOBINDEX")) {
                    segment.setLobIndex(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("LOBSEGMENT")) {
                    segment.setLobSegment(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("NESTED TABLE")) {
                    segment.setNestedTable(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("ROLLBACK")) {
                    segment.setRollback(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("TABLE")) {
                    segment.setTable(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("TABLE PARTITION")) {
                    segment.setTablePartition(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("TABLE SUBPARTITION")) {
                    segment.setTableSubPartition(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("TYPE2 UNDO")) {
                    segment.setType2Undo(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("total_size_mb")) {
                    segment.setTotalSizeMb(segmentTemp.getValue());
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select segment list error.");
            return new Segment();
        }

        return segment;
    }

    public List<User> getUsers(DataSource dataSource) {
        List<User> users;

        log.debug("[25/27] Oracle Assessment - getUsers() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            users = sqlSession.selectList("database.assessment.oracle.selectUser");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select user list error.");
            return new ArrayList<>();
        }

        return users;
    }

    public List<PublicSynonym> getPublicSynonyms(DataSource dataSource) {
        List<PublicSynonym> publicSynonyms;

        log.debug("[26/27] Oracle Assessment - getPublicSynonyms() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            publicSynonyms = sqlSession.selectList("database.assessment.oracle.selectPublicSynonym");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select public synonym list error.");
            return new ArrayList<>();
        }

        return publicSynonyms;
    }

    public List<DbLink> getDbLinks(DataSource dataSource) {
        List<DbLink> dbLinks;

        log.debug("[27/27] Oracle Assessment - getDbLinks() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            dbLinks = sqlSession.selectList("database.assessment.oracle.selectDbLink");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select DB Link list error.");
            return new ArrayList<>();
        }

        return dbLinks;
    }

    public List<Database> getDatabases(DataSource dataSource, String databaseName, String allScanYn) {
        List<Database> databases;

        log.debug("[1/27] Oracle Assessment - getDatabases() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            if (allScanYn.equals("Y")) {
                databases = sqlSession.selectList("database.assessment.oracle.selectAllDatabase");
            } else {
                databases = sqlSession.selectList("database.assessment.oracle.selectDatabase", databaseName);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select database list error.");
            return new ArrayList<>();
        }
        return databases;
    }

    public List<ObjectSummary> getObjectSummary(DataSource dataSource, String databaseName) {
        List<ObjectSummary> objectSummaries;

        log.debug("[2/27] Oracle Assessment - getObjectSummary() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            objectSummaries = sqlSession.selectList("database.assessment.oracle.selectObjectSummary");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select object summary list error.");
            return new ArrayList<>();
        }

        return objectSummaries;
    }

    public List<Table> getTables(DataSource dataSource, String databaseName) {
        List<Table> tables;

        log.debug("[3/27] Oracle Assessment - getTables() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            tables = sqlSession.selectList("database.assessment.oracle.selectTable");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select table list error.");
            return new ArrayList<>();
        }

        return tables;
    }

    public List<View> getViews(DataSource dataSource, String databaseName) {
        List<View> views;

        log.debug("[4/27] Oracle Assessment - getViews() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            views = sqlSession.selectList("database.assessment.oracle.selectView");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select view list error.");
            return new ArrayList<>();
        }

        return views;
    }

    public List<MaterializedView> getMaterializedViews(DataSource dataSource, String databaseName) {
        List<MaterializedView> materializedViews;

        log.debug("[5/27] Oracle Assessment - getMaterializedViews() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            materializedViews = sqlSession.selectList("database.assessment.oracle.selectMaterializedView");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select materialized view list error.");
            return new ArrayList<>();
        }

        return materializedViews;
    }

    public List<Index> getIndexes(DataSource dataSource, String databaseName) {
        List<Index> indexes;

        log.debug("[6/27] Oracle Assessment - getIndexes() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            indexes = sqlSession.selectList("database.assessment.oracle.selectIndex");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select index list error.");
            return new ArrayList<>();
        }

        return indexes;
    }

    public List<Procedure> getProcedures(DataSource dataSource, String databaseName) {
        List<Procedure> procedures;

        log.debug("[7/27] Oracle Assessment - getProcedures() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            procedures = sqlSession.selectList("database.assessment.oracle.selectProcedure");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select procedure list error.");
            return new ArrayList<>();
        }

        return procedures;
    }

    public List<Package> getPackages(DataSource dataSource, String databaseName) {
        List<Package> packages;

        log.debug("[8/27] Oracle Assessment - getPackages() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            packages = sqlSession.selectList("database.assessment.oracle.selectPackage");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select package list error.");
            return new ArrayList<>();
        }

        return packages;
    }

    public List<PackageBody> getPackageBodies(DataSource dataSource, String databaseName) {
        List<PackageBody> packageBodies;

        log.debug("[9/27] Oracle Assessment - getPackageBodies() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            packageBodies = sqlSession.selectList("database.assessment.oracle.selectPackageBody");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select package body list error.");
            return new ArrayList<>();
        }

        return packageBodies;
    }

    public List<Function> getFunctions(DataSource dataSource, String databaseName) {
        List<Function> functions;

        log.debug("[10/27] Oracle Assessment - getFunctions() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            functions = sqlSession.selectList("database.assessment.oracle.selectFunction");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select function list error.");
            return new ArrayList<>();
        }

        return functions;
    }

    public List<Queue> getQueues(DataSource dataSource, String databaseName) {
        List<Queue> queues;

        log.debug("[11/27] Oracle Assessment - getQueues() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            queues = sqlSession.selectList("database.assessment.oracle.selectQueue");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select queue list error.");
            return new ArrayList<>();
        }

        return queues;
    }

    public List<Trigger> getTriggers(DataSource dataSource, String databaseName) {
        List<Trigger> triggers;

        log.debug("[12/27] Oracle Assessment - getTriggers() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            triggers = sqlSession.selectList("database.assessment.oracle.selectTrigger");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select trigger list error.");
            return new ArrayList<>();
        }

        return triggers;
    }

    public List<Type> getTypes(DataSource dataSource, String databaseName) {
        List<Type> types;

        log.debug("[13/27] Oracle Assessment - getTypes() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            types = sqlSession.selectList("database.assessment.oracle.selectType");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select type list error.");
            return new ArrayList<>();
        }

        return types;
    }

    public List<Sequence> getSequences(DataSource dataSource, String databaseName) {
        List<Sequence> sequences;

        log.debug("[14/27] Oracle Assessment - getSequences() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            sequences = sqlSession.selectList("database.assessment.oracle.selectSequence");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select sequence list error.");
            return new ArrayList<>();
        }

        return sequences;
    }

    public List<Synonym> getSynonyms(DataSource dataSource, String databaseName) {
        List<Synonym> synonyms;

        log.debug("[15/27] Oracle Assessment - getSynonyms() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            synonyms = sqlSession.selectList("database.assessment.oracle.selectSynonym");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select synonym list error.");
            return new ArrayList<>();
        }

        return synonyms;
    }

    public List<Job> getJobs(DataSource dataSource, String databaseName) {
        List<Job> jobs;

        log.debug("[16/27] Oracle Assessment - getJobs() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            jobs = sqlSession.selectList("database.assessment.oracle.selectJob");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select job list error.");
            return new ArrayList<>();
        }

        return jobs;
    }
}