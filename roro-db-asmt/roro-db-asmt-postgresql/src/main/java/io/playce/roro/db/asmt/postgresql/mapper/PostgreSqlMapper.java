package io.playce.roro.db.asmt.postgresql.mapper;

import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.db.asmt.postgresql.config.SqlSessionConfig;
import io.playce.roro.db.asmt.postgresql.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.playce.roro.common.util.ThreadLocalUtils.DB_SCAN_ERROR;

@Slf4j
@Repository
public class PostgreSqlMapper {

    public Instance getInstance(DataSource dataSource) {
        Instance instance = new Instance();
        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            List<Setting> settings = sqlSession.selectList("database.assessment.postgresql.selectSetting");

            for (Setting tempSetting : settings) {
                if (tempSetting.getName().equals("server_version")) {
                    instance.setVersion(tempSetting.getSetting());
                } else if (tempSetting.getName().equals("search_path")) {
                    instance.setSearchPath(tempSetting.getSetting());
                } else if (tempSetting.getName().equals("archive_command")) {
                    instance.setArchiveCommand(tempSetting.getSetting().replaceAll("\\(", "").replaceAll("\\)", ""));
                } else if (tempSetting.getName().equals("archive_mode")) {
                    instance.setArchiveMode(tempSetting.getSetting());
                } else if (tempSetting.getName().equals("archive_timeout")) {
                    instance.setArchiveTimeout(tempSetting.getSetting());
                }
            }

            instance.setStartupTime(sqlSession.selectOne("database.assessment.postgresql.selectStartupTime"));
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select instance list error.");
            return new Instance();
        }

        return instance;
    }

    public List<User> getUsers(DataSource dataSource) {
        List<User> users;

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            users = sqlSession.selectList("database.assessment.postgresql.selectUser");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select user list error.");
            return new ArrayList<>();
        }

        return users;

    }

    public List<Database> getDatabases(DataSource dataSource, String databaseName, String allScanYn) {
        List<Database> databases;

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            if (allScanYn.equals("Y")) {
                databases = sqlSession.selectList("database.assessment.postgresql.selectAllDatabase");
            } else {
                databases = sqlSession.selectList("database.assessment.postgresql.selectDatabase", databaseName);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select database list error.");
            return new ArrayList<>();
        }

        return databases;
    }

    public List<Table> getTables(DataSource dataSource) {
        List<Table> tables;

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            tables = sqlSession.selectList("database.assessment.postgresql.selectTable");

            // table ddl script query
            for (Table tempTable : tables) {
                Map<String, String> param = new HashMap<>();
                param.put("schemaName", tempTable.getSchemaName());
                param.put("tableName", tempTable.getTableName());

                tempTable.setDdlScript(sqlSession.selectOne("database.assessment.postgresql.selectTableDdlScript", param));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select table list error.");
            return new ArrayList<>();
        }


        return tables;
    }

    public List<View> getViews(DataSource dataSource) {
        List<View> views;

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            views = sqlSession.selectList("database.assessment.postgresql.selectView");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select view list error.");
            return new ArrayList<>();
        }

        return views;
    }

    public List<Index> getIndexes(DataSource dataSource) {
        List<Index> indexes;

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            indexes = sqlSession.selectList("database.assessment.postgresql.selectIndex");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select index list error.");
            return new ArrayList<>();
        }

        return indexes;
    }

    public List<Procedure> getProcedures(DataSource dataSource) {
        List<Procedure> procedures;

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            procedures = sqlSession.selectList("database.assessment.postgresql.selectProcedure");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select procedure list error.");
            return new ArrayList<>();
        }

        return procedures;
    }

    public List<Function> getFunctions(DataSource dataSource) {
        List<Function> functions;

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            functions = sqlSession.selectList("database.assessment.postgresql.selectFunction");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select function list error.");
            return new ArrayList<>();
        }

        return functions;
    }

    public List<Sequence> getSequences(DataSource dataSource) {
        List<Sequence> sequences;

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            sequences = sqlSession.selectList("database.assessment.postgresql.selectSequence");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select sequence list error.");
            return new ArrayList<>();
        }

        return sequences;
    }

    public List<Trigger> getTriggers(DataSource dataSource) {
        List<Trigger> triggers;

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            triggers = sqlSession.selectList("database.assessment.postgresql.selectTrigger");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select trigger list error.");
            return new ArrayList<>();
        }

        return triggers;
    }

}
