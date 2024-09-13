package io.playce.roro.db.asmt.mssql.mapper;

import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.db.asmt.mssql.config.SqlSessionConfig;
import io.playce.roro.db.asmt.mssql.dto.*;
import io.playce.roro.db.asmt.util.CustomDelegatingDatabase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static io.playce.roro.common.util.ThreadLocalUtils.DB_SCAN_ERROR;

@Slf4j
@Repository
public class MsSqlMapper {

    public Instance getInstances(DataSource dataSource) {
        log.debug("MSSQL Assessment - selectInstanceVersion() invoked.");

        int sqlServerMajorVersion = 0;
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            // https://sqlserverbuilds.blogspot.com RTM Version Check
            sqlServerMajorVersion = sqlSession.selectOne("database.assessment.mssql.selectInstanceVersion");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select an rtm version information error.");
        }

        Instance instance;

        log.debug("MSSQL Assessment - getInstances() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            // SQL SERVER 2012 이상일 경우.
            if(sqlServerMajorVersion > 10) {
                instance = sqlSession.selectOne("database.assessment.mssql.selectInstanceWithWinInfo");
            } else {
                instance = sqlSession.selectOne("database.assessment.mssql.selectInstance");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select an instance information error.");
            return new Instance();
        }

        return instance;
    }

    public List<Memory> getMemories(DataSource dataSource) {
        List<Memory> memories;

        log.debug("MSSQL Assessment - getMemories() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            memories = sqlSession.selectList("database.assessment.mssql.selectMemory");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select memory list error.");
            return new ArrayList<>();
        }

        return memories;
    }

    public List<DataFile> getDataFiles(DataSource dataSource) {
        List<DataFile> dataFiles;

        log.debug("MSSQL Assessment - getDataFiles() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            dataFiles = sqlSession.selectList("database.assessment.mssql.selectDataFile");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select data file list error.");
            return new ArrayList<>();
        }

        return dataFiles;
    }

    public List<User> getUsers(DataSource dataSource) {
        List<User> users;

        log.debug("MSSQL Assessment - getUsers() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            users = sqlSession.selectList("database.assessment.mssql.selectUser");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select user list error.");
            return new ArrayList<>();
        }

        return users;
    }

    public List<DbLink> getDbLinks(DataSource dataSource) {
        List<DbLink> dbLinks;

        log.debug("MSSQL Assessment - getDbLinks() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            dbLinks = sqlSession.selectList("database.assessment.mssql.selectDbLink");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select DB Link list error.");
            return new ArrayList<>();
        }

        return dbLinks;
    }

    public List<Database> getDatabases(DataSource dataSource, String databaseName, String allScanYn) {
        List<Database> databases;

        log.debug("MSSQL Assessment - getDatabases() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            if (allScanYn.equals("Y")) {
                databases = sqlSession.selectList("database.assessment.mssql.selectAllDatabase");
            } else {
                databases = sqlSession.selectList("database.assessment.mssql.selectDatabase", databaseName);
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

        log.debug("MSSQL Assessment - getObjectSummary() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            objectSummaries = sqlSession.selectList("database.assessment.mssql.selectObjectSummary");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select object summary list error.");
            return new ArrayList<>();
        }

        return objectSummaries;
    }

    public List<Table> getTables(DataSource dataSource, String databaseName) {
        List<Table> tables;

        log.debug("MSSQL Assessment - getTables() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            tables = sqlSession.selectList("database.assessment.mssql.selectTable");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select table list error.");
            return new ArrayList<>();
        }

        return tables;
    }

    public List<View> getViews(DataSource dataSource, String databaseName) {
        List<View> views;

        log.debug("MSSQL Assessment - getViews() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            views = sqlSession.selectList("database.assessment.mssql.selectView");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select view list error.");
            return new ArrayList<>();
        }

        return views;
    }

    public List<Index> getIndexes(DataSource dataSource, String databaseName) {
        List<Index> indexes;

        log.debug("MSSQL Assessment - getIndexes() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            indexes = sqlSession.selectList("database.assessment.mssql.selectIndex");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select index list error.");
            return new ArrayList<>();
        }

        return indexes;
    }

    public List<Procedure> getProcedures(DataSource dataSource, String databaseName) {
        List<Procedure> procedures;

        log.debug("MSSQL Assessment - getProcedures() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            procedures = sqlSession.selectList("database.assessment.mssql.selectProcedure");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select procedure list error.");
            return new ArrayList<>();
        }

        return procedures;
    }

    public List<Function> getFunctions(DataSource dataSource, String databaseName) {
        List<Function> functions;

        log.debug("MSSQL Assessment - getProcedures() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            functions = sqlSession.selectList("database.assessment.mssql.selectFunction");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select function list error.");
            return new ArrayList<>();
        }

        return functions;
    }

    public List<Queue> getQueues(DataSource dataSource, String databaseName) {
        List<Queue> queues;

        log.debug("MSSQL Assessment - getProcedures() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            queues = sqlSession.selectList("database.assessment.mssql.selectQueue");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select queue list error.");
            return new ArrayList<>();
        }

        return queues;
    }

    public List<Trigger> getTriggers(DataSource dataSource, String databaseName) {
        List<Trigger> triggers;

        log.debug("MSSQL Assessment - getProcedures() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            triggers = sqlSession.selectList("database.assessment.mssql.selectTrigger");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select trigger list error.");
            return new ArrayList<>();
        }

        return triggers;
    }

    public List<Sequence> getSequences(DataSource dataSource, String databaseName, String productVersion) {
        List<Sequence> sequences = new ArrayList<>();

        log.debug("MSSQL Assessment - getProcedures() invoked.");

        final int prefixSqlServer2012ProductVersion = 11;

        if (StringUtils.isNotEmpty(productVersion) && productVersion.contains(".")) {
            final int prefixProductVersion = Integer.parseInt(productVersion.substring(0, productVersion.indexOf(".")));

            if (prefixProductVersion >= prefixSqlServer2012ProductVersion) {

                try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
                    sequences = sqlSession.selectList("database.assessment.mssql.selectSequence");
                } catch (Exception e) {
                    log.error(e.getMessage());
                    ThreadLocalUtils.add(DB_SCAN_ERROR, "Select sequence list error.");
                    return new ArrayList<>();
                }
            }
        }

        return sequences;
    }

    public List<Synonym> getSynonyms(DataSource dataSource, String databaseName) {
        List<Synonym> synonyms;

        log.debug("MSSQL Assessment - getProcedures() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            synonyms = sqlSession.selectList("database.assessment.mssql.selectSynonym");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select synonym list error.");
            return new ArrayList<>();
        }

        return synonyms;
    }

    public Long getDbSizeMb(DataSource dataSource, List<Database> databases) {
        Long dbSizeMb = 0L;
        List<String> databaseNames = new ArrayList<>();

        for(Database database : databases) {
            databaseNames.add(database.getName());
        }

        log.debug("MSSQL Assessment - getDatabaseSize() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            dbSizeMb = sqlSession.selectOne("database.assessment.mssql.selectDbSize", databaseNames);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select dbSize error.");
            return dbSizeMb;
        }

        return dbSizeMb;
    }
}
