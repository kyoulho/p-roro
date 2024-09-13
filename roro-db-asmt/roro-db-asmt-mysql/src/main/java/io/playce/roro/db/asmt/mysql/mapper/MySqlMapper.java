package io.playce.roro.db.asmt.mysql.mapper;

import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.db.asmt.mysql.config.SqlSessionConfig;
import io.playce.roro.db.asmt.mysql.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

import static io.playce.roro.common.util.ThreadLocalUtils.DB_SCAN_ERROR;

@Slf4j
@Repository
public class MySqlMapper {

    public Map<String, String> getVariables(DataSource dataSource) {
        Map<String, String> variableMap = new HashMap<>();

        log.debug("MySQL Assessment - getVariables() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            List<Variable> variables = sqlSession.selectList("database.assessment.mysql.selectVariable");

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

        log.debug("MySQL Assessment - getStartUpTime() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            return sqlSession.selectOne("database.assessment.mysql.selectStartUpTime");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select startup time error.");
            return null;
        }
    }

    public Date getStartUpTime2(DataSource dataSource) {
        log.debug("MySQL Assessment - getStartUpTime() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            return sqlSession.selectOne("database.assessment.mysql.selectStartUpTime2");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select startup time error.");
            return null;
        }
    }


    public List<Database> getDatabases(DataSource dataSource, String databaseName, String allScanYn) {
        List<Database> databases;

        log.debug("MySQL Assessment - getDatabases() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            if (allScanYn.equals("Y")) {
                databases = sqlSession.selectList("database.assessment.mysql.selectAllDatabase");
            } else {
                databases = sqlSession.selectList("database.assessment.mysql.selectDatabase", databaseName);
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

        log.debug("MySQL Assessment - getTableDataUsage() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            tableDataUsages = sqlSession.selectList("database.assessment.mysql.selectTableDataUsage", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select table data usage list error.");
            return new ArrayList<>();
        }

        return tableDataUsages;
    }

    public List<IndexUsage> getIndexUsages(DataSource dataSource, String databaseName) {
        List<IndexUsage> indexUsages;

        log.debug("MySQL Assessment - getIndexUsages() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            indexUsages = sqlSession.selectList("database.assessment.mysql.selectIndexUsage", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select index usage list error.");
            return new ArrayList<>();
        }

        return indexUsages;
    }

    public List<Table> getTable(DataSource dataSource, String databaseName) {
        List<Table> tables;

        log.debug("MySQL Assessment - getTable() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            tables = sqlSession.selectList("database.assessment.mysql.selectTable", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select table list error.");
            return new ArrayList<>();
        }

        return tables;
    }

    public String getTableScript(DataSource dataSource, String tableName) {

        log.debug("MySQL Assessment - getTableScript() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            Map<String, String> tableMap = sqlSession.selectOne("database.assessment.mysql.selectTableScript", tableName);
            return tableMap.get("Create Table");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select table DDL script list error.");
            return "";
        }
    }

    public List<View> getViews(DataSource dataSource, String databaseName) {
        List<View> views;

        log.debug("MySQL Assessment - getViews() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            views = sqlSession.selectList("database.assessment.mysql.selectView", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select view list error.");
            return new ArrayList<>();
        }

        return views;
    }

    public List<Index> getIndex(DataSource dataSource, String databaseName) {
        List<Index> indexes;

        log.debug("MySQL Assessment - getIndex() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            indexes = sqlSession.selectList("database.assessment.mysql.selectIndex", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select index list error.");
            return new ArrayList<>();
        }

        return indexes;
    }

    public List<Procedure> getProcedure(DataSource dataSource, String databaseName) {
        List<Procedure> procedures;

        log.debug("MySQL Assessment - getProcedure() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            procedures = sqlSession.selectList("database.assessment.mysql.selectProcedure", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select procedure list error.");
            return new ArrayList<>();
        }

        return procedures;
    }

    public List<Function> getFunction(DataSource dataSource, String databaseName) {
        List<Function> functions;

        log.debug("MySQL Assessment - getFunction() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            functions = sqlSession.selectList("database.assessment.mysql.selectFunction", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select function list error.");
            return new ArrayList<>();
        }

        return functions;
    }

    public List<Trigger> getTrigger(DataSource dataSource, String databaseName) {
        List<Trigger> triggers;

        log.debug("MySQL Assessment - getTrigger() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            triggers = sqlSession.selectList("database.assessment.mysql.selectTrigger", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select trigger list error.");
            return new ArrayList<>();
        }

        return triggers;
    }

    public List<Event> getEvent(DataSource dataSource, String databaseName) {
        List<Event> events;

        log.debug("MySQL Assessment - getEvent() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            events = sqlSession.selectList("database.assessment.mysql.selectEvent", databaseName);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select event list error.");
            return new ArrayList<>();
        }

        return events;
    }

    public List<User> getUser(DataSource dataSource) {
        List<User> users;

        log.debug("MySQL Assessment - getUser() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            users = sqlSession.selectList("database.assessment.mysql.selectUser");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select user list error.");
            return new ArrayList<>();
        }

        return users;
    }

    public List<DbLink> getDbLink(DataSource dataSource) {
        List<DbLink> dbLinks;

        log.debug("MySQL Assessment - getDbLink() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            dbLinks = sqlSession.selectList("database.assessment.mysql.selectDblink");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select DB Link list error.");
            return new ArrayList<>();
        }

        return dbLinks;
    }

    public Long getDbSizeMb(List<Database> databases) {
        long dbSizeMb = 0L;

        for(Database database : databases) {
            dbSizeMb += (long) database.getDatabaseSizeMb();
        }

        return dbSizeMb;
    }

}
