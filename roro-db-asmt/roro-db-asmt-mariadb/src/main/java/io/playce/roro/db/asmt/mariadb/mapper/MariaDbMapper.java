package io.playce.roro.db.asmt.mariadb.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.db.asmt.mariadb.config.SqlSessionConfig;
import io.playce.roro.db.asmt.mariadb.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.SQLSyntaxErrorException;
import java.util.*;

import static io.playce.roro.common.util.ThreadLocalUtils.DB_SCAN_ERROR;

@Slf4j
@Repository
public class MariaDbMapper {

    public Map<String, String> getVariables(DataSource dataSource) {
        Map<String, String> variableMap = new HashMap<>();

        log.debug("MariaDB Assessment - getVariables() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            List<Variable> variables = sqlSession.selectList("database.assessment.mariadb.selectVariable");

            for (Variable variable : variables) {
                variableMap.put(variable.getVariableName(), variable.getValue());
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select variables error.");
            return new HashMap<>();
        }

        return variableMap;
    }

    public Date getStartUpTime(DataSource dataSource) {
        log.debug("MariaDB Assessment - getStartUpTime() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            return sqlSession.selectOne("database.assessment.mariadb.selectStartUpTime");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select startup time error.");
            return null;
        }
    }

    public List<Database> getDatabases(DataSource dataSource, String databaseName, String allScanYn) {
        List<Database> databases;

        log.debug("MariaDB Assessment - getDatabases() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            if (allScanYn.equals("Y")) {
                databases = sqlSession.selectList("database.assessment.mariadb.selectAllDatabase");
            } else {
                databases = sqlSession.selectList("database.assessment.mariadb.selectDatabase", databaseName);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select database list error.");
            return new ArrayList<>();
        }

        return databases;
    }

    public List<TableDataUsage> getTableDataUsage(DataSource dataSource, String databaseName) {
        List<TableDataUsage> tableDataUsages;

        log.debug("MariaDB Assessment - getTableDataUsage() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            tableDataUsages = sqlSession.selectList("database.assessment.mariadb.selectTableDataUsage", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select table data usage list error.");
            return new ArrayList<>();
        }

        return tableDataUsages;
    }

    public List<IndexUsage> getIndexUsages(DataSource dataSource, String databaseName, float version) {
        List<IndexUsage> indexUsages;

        log.debug("MariaDB Assessment - getIndexUsages() invoked.");

        final float indexUsageSupportVersion = 5.6f;

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            if (version >= indexUsageSupportVersion) {
                indexUsages = sqlSession.selectList("database.assessment.mariadb.selectIndexUsage", databaseName);
            } else {
                indexUsages = new ArrayList<>();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select index usage list error.");
            return new ArrayList<>();
        }

        return indexUsages;
    }

    public List<Table> getTable(DataSource dataSource, String databaseName) {
        List<Table> tables;

        log.debug("MariaDB Assessment - getTable() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            tables = sqlSession.selectList("database.assessment.mariadb.selectTable", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select table list error.");
            return new ArrayList<>();
        }

        return tables;
    }

    public String getTableScript(DataSource dataSource, String tableName) {

        log.debug("MariaDB Assessment - getTableScript() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            Map<String, String> tableMap = sqlSession.selectOne("database.assessment.mariadb.selectTableScript", tableName);
            return tableMap.get("Create Table");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select table DDL script error.");
            return "";
        }
    }

    public List<View> getViews(DataSource dataSource, String databaseName) {
        List<View> views;

        log.debug("MariaDB Assessment - getViews() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            views = sqlSession.selectList("database.assessment.mariadb.selectView", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select view list error.");
            return new ArrayList<>();
        }

        return views;
    }

    public List<Index> getIndex(DataSource dataSource, String databaseName) {
        List<Index> indexes;

        log.debug("MariaDB Assessment - getIndex() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            indexes = sqlSession.selectList("database.assessment.mariadb.selectIndex", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select index list error.");
            return new ArrayList<>();
        }

        return indexes;
    }

    public List<Procedure> getProcedure(DataSource dataSource, String databaseName) {
        List<Procedure> procedures;

        log.debug("MariaDB Assessment - getProcedure() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            procedures = sqlSession.selectList("database.assessment.mariadb.selectProcedure", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select procedure list error.");
            return new ArrayList<>();
        }

        return procedures;
    }

    public List<Function> getFunction(DataSource dataSource, String databaseName) {
        List<Function> functions;

        log.debug("MariaDB Assessment - getFunction() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            functions = sqlSession.selectList("database.assessment.mariadb.selectFunction", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select function list error.");
            return new ArrayList<>();
        }

        return functions;
    }

    public List<Trigger> getTrigger(DataSource dataSource, String databaseName) {
        List<Trigger> triggers;

        log.debug("MariaDB Assessment - getTrigger() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            triggers = sqlSession.selectList("database.assessment.mariadb.selectTrigger", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select trigger list error.");
            return new ArrayList<>();
        }

        return triggers;
    }

    public List<Event> getEvent(DataSource dataSource, String databaseName) {
        List<Event> events;

        log.debug("MariaDB Assessment - getEvent() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            events = sqlSession.selectList("database.assessment.mariadb.selectEvent", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select event list error.");
            return new ArrayList<>();
        }

        return events;
    }

    @SuppressWarnings("unchecked")
    public List<User> getUser(DataSource dataSource, float version) {
        List<User> users = new ArrayList<>();

        log.debug("MariaDB Assessment - getUser() invoked.");

        final float globalPrivSupportVersion = 10.4f;

        if (version >= globalPrivSupportVersion) {
            try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
                users = sqlSession.selectList("database.assessment.mariadb.selectGlobalPriv");

                for (User user : users) {
                    Map<String, String> privMap = new ObjectMapper().readValue(user.getPriv(), HashMap.class);
                    if (privMap.containsKey("plugin")) {
                        user.setAuthType(privMap.get("plugin"));
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                ThreadLocalUtils.add(DB_SCAN_ERROR, "Select user list error.");
            }
        } else {
            try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
                users = sqlSession.selectList("database.assessment.mariadb.selectUser");
            } catch (Exception e) {
                log.error(e.getMessage());
                ThreadLocalUtils.add(DB_SCAN_ERROR, "Select user list error.");
            }
        }

        return users;
    }

    public List<DbLink> getDbLink(DataSource dataSource) {
        List<DbLink> dbLinks = new ArrayList<>();

        log.debug("MariaDB Assessment - getDbLink() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            dbLinks = sqlSession.selectList("database.assessment.mariadb.selectDblink");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select DB Link list error.");
        }

        return dbLinks;
    }

    public Long getDbSizeMb(List<Database> databases) {
        long dbSizeMb = 0L;

        for (Database database : databases) {
            dbSizeMb += (long) database.getDatabaseSizeMb();
        }

        return dbSizeMb;
    }
}
