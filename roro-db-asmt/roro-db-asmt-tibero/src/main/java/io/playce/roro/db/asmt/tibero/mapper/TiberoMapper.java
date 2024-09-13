package io.playce.roro.db.asmt.tibero.mapper;

import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.db.asmt.tibero.config.SqlSessionConfig;
import io.playce.roro.db.asmt.tibero.dto.Package;
import io.playce.roro.db.asmt.tibero.dto.*;
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
public class TiberoMapper {

    public Instance getInstance(DataSource dataSource) {
        Instance instance;

        log.debug("Tibero Assessment - getInstance() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            instance = sqlSession.selectOne("database.assessment.tibero.selectInstance");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select an instance information error.");
            return new Instance();
        }

        log.debug("Tibero Assessment - DatabaseSize() invoked.");
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            instance.setDbSizeMb(sqlSession.selectOne("database.assessment.tibero.selectDbSize"));
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select dbSize error.");
            return instance;
        }

        return instance;
    }

    public List<Sga> getSga(DataSource dataSource) {
        List<Sga> sga;

        log.debug("Tibero Assessment - getSga() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            sga = sqlSession.selectList("database.assessment.tibero.selectSga");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select SGA list error.");
            return new ArrayList<>();
        }

        return sga;
    }


    public List<DataFile> getDataFiles(DataSource dataSource) {
        List<DataFile> dataFiles;

        log.debug("Tibero Assessment - getDataFiles() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            dataFiles = sqlSession.selectList("database.assessment.tibero.selectDataFile");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select data file list error.");
            return new ArrayList<>();
        }

        return dataFiles;
    }

    public List<ControlFile> getControlFiles(DataSource dataSource) {
        List<ControlFile> controlFiles;

        log.debug("Tibero Assessment - getControlFiles() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            controlFiles = sqlSession.selectList("database.assessment.tibero.selectControlFile");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select control file list error.");
            return new ArrayList<>();
        }

        return controlFiles;
    }

    public List<LogFile> getLogFiles(DataSource dataSource) {
        List<LogFile> logFiles;

        log.debug("Tibero Assessment - getLogFiles() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            logFiles = sqlSession.selectList("database.assessment.tibero.selectLogFile");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select log file list error.");
            return new ArrayList<>();
        }

        return logFiles;
    }

    public List<TableSpace> getTableSpaces(DataSource dataSource) {
        List<TableSpace> tableSpaces;

        log.debug("Tibero Assessment - getTableSpaces() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            tableSpaces = sqlSession.selectList("database.assessment.tibero.selectTableSpace");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select table space list error.");
            return new ArrayList<>();
        }

        return tableSpaces;
    }

    public List<Parameter> getParameters(DataSource dataSource) {
        List<Parameter> parameters;

        log.debug("Tibero Assessment - getParameters() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            parameters = sqlSession.selectList("database.assessment.tibero.selectParameter");
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

        log.debug("Tibero Assessment - getSegment() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            segmentTemps = sqlSession.selectList("database.assessment.tibero.selectSegment");

            for (SegmentTemp segmentTemp : segmentTemps) {
                if (segmentTemp.getName().equals("INDEX")) {
                    segment.setIndex(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("LOB")) {
                    segment.setLob(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("TABLE")) {
                    segment.setTable(segmentTemp.getValue());
                } else if (segmentTemp.getName().equals("UNDO")) {
                    segment.setUndo(segmentTemp.getValue());
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

        log.debug("Tibero Assessment - getUsers() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            users = sqlSession.selectList("database.assessment.tibero.selectUser");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select user list error.");
            return new ArrayList<>();
        }

        return users;
    }

    public List<PublicSynonym> getPublicSynonyms(DataSource dataSource) {
        List<PublicSynonym> publicSynonyms;

        log.debug("Tibero Assessment - getPublicSynonyms() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            publicSynonyms = sqlSession.selectList("database.assessment.tibero.selectPublicSynonym");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select public synonym list error.");
            return new ArrayList<>();
        }

        return publicSynonyms;
    }

    public List<DbLink> getDbLinks(DataSource dataSource) {
        List<DbLink> dbLinks;

        log.debug("Tibero Assessment - getDbLinks() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            dbLinks = sqlSession.selectList("database.assessment.tibero.selectDbLink");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select DB Link list error.");
            return new ArrayList<>();
        }

        return dbLinks;
    }

    public List<Database> getDatabases(DataSource dataSource, String databaseName, String allScanYn) {
        List<Database> databases;

        log.debug("Tibero Assessment - getDatabases() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            if (allScanYn.equals("Y")) {
                databases = sqlSession.selectList("database.assessment.tibero.selectAllDatabase");
            } else {
                databases = sqlSession.selectList("database.assessment.tibero.selectDatabase", databaseName);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select database list error.");
            return new ArrayList<>();
        }

        return databases;
    }

    public ObjectSummary getObjectSummary(DataSource dataSource, String databaseName) {
        ObjectSummary objectSummary = new ObjectSummary();
        List<ObjectSummaryTemp> objectSummaryTemps;

        log.debug("Tibero Assessment - getObjectSummary() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            objectSummaryTemps = sqlSession.selectList("database.assessment.tibero.selectObjectSummary");

            for (ObjectSummaryTemp objectSummaryTemp : objectSummaryTemps) {
                if (objectSummaryTemp.getObjectType().equals("DATABASE LINK")) {
                    objectSummary.setDatabaseLink(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("DIRECTORY")) {
                    objectSummary.setDirectory(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("FUNCTION")) {
                    objectSummary.setFunction(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("INDEX")) {
                    objectSummary.setIndex(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("JAVA")) {
                    objectSummary.setJava(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("LOB")) {
                    objectSummary.setLob(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("PACKAGE")) {
                    objectSummary.setPackages(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("PACKAGE BODY")) {
                    objectSummary.setPackageBody(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("PROCEDURE")) {
                    objectSummary.setProcedure(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("SEQUENCE")) {
                    objectSummary.setSequence(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("SQL TRANSLATION PROFILE")) {
                    objectSummary.setSqlTranslationProfile(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("SYNONYM")) {
                    objectSummary.setSynonym(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("TABLE")) {
                    objectSummary.setTable(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("TRIGGER")) {
                    objectSummary.setTrigger(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("TYPE")) {
                    objectSummary.setType(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("TYPE BODY")) {
                    objectSummary.setTypeBody(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getObjectType().equals("VIEW")) {
                    objectSummary.setView(objectSummaryTemp.getObjectCount());
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select object summary list error.");
            return new ObjectSummary();
        }

        return objectSummary;
    }

    public List<Table> getTables(DataSource dataSource, String databaseName) {
        List<Table> tables;

        log.debug("Tibero Assessment - getTables() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            tables = sqlSession.selectList("database.assessment.tibero.selectTable");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select table list error.");
            return new ArrayList<>();
        }

        return tables;
    }

    public List<View> getViews(DataSource dataSource, String databaseName) {
        List<View> views;

        log.debug("Tibero Assessment - getViews() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            views = sqlSession.selectList("database.assessment.tibero.selectView");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select view list error.");
            return new ArrayList<>();
        }

        return views;
    }

    public List<MaterializedView> getMaterializedViews(DataSource dataSource, String databaseName) {
        List<MaterializedView> materializedViews;

        log.debug("Tibero Assessment - getMaterializedViews() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            materializedViews = sqlSession.selectList("database.assessment.tibero.selectMaterializedView");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select materialized view list error.");
            return new ArrayList<>();
        }

        return materializedViews;
    }

    public List<Index> getIndexes(DataSource dataSource, String databaseName) {
        List<Index> indexes;

        log.debug("Tibero Assessment - getIndexes() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            indexes = sqlSession.selectList("database.assessment.tibero.selectIndex");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select index list error.");
            return new ArrayList<>();
        }

        return indexes;
    }

    public List<Procedure> getProcedures(DataSource dataSource, String databaseName) {
        List<Procedure> procedures;

        log.debug("Tibero Assessment - getProcedures() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            procedures = sqlSession.selectList("database.assessment.tibero.selectProcedure");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select procedure list error.");
            return new ArrayList<>();
        }

        return procedures;
    }

    public List<Package> getPackages(DataSource dataSource, String databaseName) {
        List<Package> packages;

        log.debug("Tibero Assessment - getPackages() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            packages = sqlSession.selectList("database.assessment.tibero.selectPackage");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select package list error.");
            return new ArrayList<>();
        }

        return packages;
    }

    public List<PackageBody> getPackageBodies(DataSource dataSource, String databaseName) {
        List<PackageBody> packageBodies;

        log.debug("Tibero Assessment - getPackageBodies() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            packageBodies = sqlSession.selectList("database.assessment.tibero.selectPackageBody");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select package body list error.");
            return new ArrayList<>();
        }

        return packageBodies;
    }

    public List<Function> getFunctions(DataSource dataSource, String databaseName) {
        List<Function> functions;

        log.debug("Tibero Assessment - getFunctions() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            functions = sqlSession.selectList("database.assessment.tibero.selectFunction");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select function list error.");
            return new ArrayList<>();
        }

        return functions;
    }

    public List<Queue> getQueues(DataSource dataSource, String databaseName) {
        List<Queue> queues;

        log.debug("Tibero Assessment - getQueues() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            queues = sqlSession.selectList("database.assessment.tibero.selectQueue");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select queue list error.");
            return new ArrayList<>();
        }

        return queues;
    }

    public List<Trigger> getTriggers(DataSource dataSource, String databaseName) {
        List<Trigger> triggers;

        log.debug("Tibero Assessment - getTriggers() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            triggers = sqlSession.selectList("database.assessment.tibero.selectTrigger");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select trigger list error.");
            return new ArrayList<>();
        }

        return triggers;
    }

    public List<Type> getTypes(DataSource dataSource, String databaseName) {
        List<Type> types;

        log.debug("Tibero Assessment - getTypes() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            types = sqlSession.selectList("database.assessment.tibero.selectType");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select type list error.");
            return new ArrayList<>();
        }

        return types;
    }

    public List<Sequence> getSequences(DataSource dataSource, String databaseName) {
        List<Sequence> sequences;

        log.debug("Tibero Assessment - getSequences() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            sequences = sqlSession.selectList("database.assessment.tibero.selectSequence");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select sequence list error.");
            return new ArrayList<>();
        }

        return sequences;
    }

    public List<Synonym> getSynonyms(DataSource dataSource, String databaseName) {
        List<Synonym> synonyms;

        log.debug("Tibero Assessment - getSynonyms() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            synonyms = sqlSession.selectList("database.assessment.tibero.selectSynonym");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select synonym list error.");
            return new ArrayList<>();
        }

        return synonyms;
    }

    public List<Job> getJobs(DataSource dataSource, String databaseName) {
        List<Job> jobs;

        log.debug("Tibero Assessment - getJobs() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            jobs = sqlSession.selectList("database.assessment.tibero.selectJob");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select job list error.");
            return new ArrayList<>();
        }

        return jobs;
    }

}
