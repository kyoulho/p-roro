package io.playce.roro.db.asmt.sybase.mapper;

import io.playce.roro.common.util.ThreadLocalUtils;
import io.playce.roro.db.asmt.sybase.config.SqlSessionConfig;
import io.playce.roro.db.asmt.sybase.dto.*;
import io.playce.roro.db.asmt.util.CustomDelegatingDatabase;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.playce.roro.common.util.ThreadLocalUtils.DB_SCAN_ERROR;

@Slf4j
@Repository
public class SybaseMapper {

    public Instance getInstance(DataSource dataSource) {
        Instance instance;

        log.debug("Sybase Assessment - getInstances() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            instance = sqlSession.selectOne("database.assessment.sybase.selectInstance");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select an instance information error.");
            return new Instance();
        }

        return instance;
    }

    public List<Server> getServers(DataSource dataSource) {
        List<Server> servers;

        log.debug("Sybase Assessment - getServers() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            servers = sqlSession.selectList("database.assessment.sybase.selectServer");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select server list error.");
            return new ArrayList<>();
        }

        return servers;
    }

    public List<Memory> getMemories(DataSource dataSource) {
        List<Memory> memories = new ArrayList<>();

        log.debug("Sybase Assessment - getMemories() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            List<Map<String, String>> memoryMap = sqlSession.selectList("database.assessment.sybase.selectMemory");

            for (Map<String, String> tempMap : memoryMap) {
                Memory memory = new Memory();
                memory.setParameterName(tempMap.get("Parameter Name").trim());
                memory.setDefaultValue(tempMap.get("Default").trim());
                memory.setMemoryUsed(tempMap.get("Memory Used").trim());
                memory.setConfigValue(tempMap.get("Config Value").trim());
                memory.setRunValue(tempMap.get("Run Value").trim());
                memories.add(memory);
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select memory list error.");
            return new ArrayList<>();
        }

        return memories;
    }

    public List<Device> getDevices(DataSource dataSource) {
        List<Device> devices;

        log.debug("Sybase Assessment - getDevices() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            devices = sqlSession.selectList("database.assessment.sybase.selectDevice");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select device list error.");
            return new ArrayList<>();
        }

        return devices;
    }

    public List<Segment> getSegment(DataSource dataSource) {
        List<Segment> segments;

        log.debug("Sybase Assessment - getSegment() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            segments = sqlSession.selectList("database.assessment.sybase.selectSegment");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select segment list error.");
            return new ArrayList<>();
        }

        return segments;
    }

    public List<User> getUsers(DataSource dataSource) {
        List<User> users;

        log.debug("Sybase Assessment - getUsers() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            users = sqlSession.selectList("database.assessment.sybase.selectUser");

            for (User tempUser : users) {
                List<String> userRoles = sqlSession.selectList("database.assessment.sybase.selectUserRole", tempUser.getSuid());
                tempUser.setRoles(String.join(",", userRoles));
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select user list error.");
            return new ArrayList<>();
        }

        return users;
    }

    public List<Job> getJobs(DataSource dataSource) {
        List<Job> jobs;

        log.debug("Sybase Assessment - getJobs() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            jobs = sqlSession.selectList("database.assessment.sybase.selectJob");
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select job list error.");
            return new ArrayList<>();
        }

        return jobs;
    }

    public List<Database> getDatabases(DataSource dataSource, String databaseName, String allScanYn) {
        List<Database> databases;

        log.debug("Sybase Assessment - getDatabases() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            if (allScanYn.equals("Y")) {
                databases = sqlSession.selectList("database.assessment.sybase.selectAllDatabase");
            } else {
                databases = sqlSession.selectList("database.assessment.sybase.selectDatabase", databaseName);
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

        log.debug("Sybase Assessment - getObjectSummary() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            objectSummaryTemps = sqlSession.selectList("database.assessment.sybase.selectObjectSummary");

            for (ObjectSummaryTemp objectSummaryTemp : objectSummaryTemps) {
                if (objectSummaryTemp.getType().equals("C")) {
                    objectSummary.setComputedColumn(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("D")) {
                    objectSummary.setDefault(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("DD")) {
                    objectSummary.setDecryptDefault(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("EK")) {
                    objectSummary.setEncryptionKey(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("F")) {
                    objectSummary.setSqlJFunction(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("L")) {
                    objectSummary.setLog(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("N")) {
                    objectSummary.setPartitionCondition(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("P")) {
                    objectSummary.setTransactSqlOrSqlJProcedure(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("PP")) {
                    objectSummary.setThePredicateOfPrivilege(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("PR")) {
                    objectSummary.setPrepareObjects(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("R")) {
                    objectSummary.setRule(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("RI")) {
                    objectSummary.setReferentialConstraint(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("RS")) {
                    objectSummary.setPrecomputedResultSet(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("S")) {
                    objectSummary.setSystemTable(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("SF")) {
                    objectSummary.setScalarOrUserDefinedFunctions(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("TR")) {
                    objectSummary.setTrigger(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("U")) {
                    objectSummary.setUserTable(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("V")) {
                    objectSummary.setView(objectSummaryTemp.getObjectCount());
                } else if (objectSummaryTemp.getType().equals("XP")) {
                    objectSummary.setExtendedStoredProcedure(objectSummaryTemp.getObjectCount());
                }

            }

        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select an object summary error.");
            return new ObjectSummary();
        }

        return objectSummary;
    }

    public List<Table> getTables(DataSource dataSource, String databaseName) {
        List<Table> tables = new ArrayList<>();

        log.debug("Sybase Assessment - getTables() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            tables.addAll(sqlSession.selectList("database.assessment.sybase.selectTable"));
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select table list error.");
            return new ArrayList<>();
        }

        return tables;
    }

    public List<View> getViews(DataSource dataSource, String databaseName) {
        List<View> views = new ArrayList<>();

        log.debug("Sybase Assessment - getViews() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            List<View> tempViews = sqlSession.selectList("database.assessment.sybase.selectView", databaseName);

            for (View tempView : tempViews) {
                List<String> text = sqlSession.selectList("database.assessment.sybase.selectScript", tempView.getId());
                tempView.setDdlScript(String.join(" ", text));
            }

            views.addAll(tempViews);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select view list error.");
            return new ArrayList<>();
        }

        return views;
    }

    public List<Index> getIndexes(DataSource dataSource, List<Table> tables) {
        List<Index> indexes = new ArrayList<>();

        log.debug("Sybase Assessment - getIndexes() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(dataSource).openSession()) {
            for (Table tempTable : tables) {
                List<Index> tempIndexes = sqlSession.selectList("database.assessment.sybase.selectIndex", tempTable);

                if (tempIndexes != null) {
                    tempIndexes.forEach(index -> {
                        index.setDatabaseName(tempTable.getDatabaseName());
                        index.setTableName(tempTable.getTableName());
                    });
                    indexes.addAll(tempIndexes);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select index list error.");
            return new ArrayList<>();
        }

        return indexes;
    }

    public List<Procedure> getProcedures(DataSource dataSource, String databaseName) {
        List<Procedure> procedures = new ArrayList<>();

        log.debug("Sybase Assessment - getProcedures() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            List<Procedure> tempProcedures = sqlSession.selectList("database.assessment.sybase.selectProcedure");

            for (Procedure tempProcedure : tempProcedures) {
                List<String> text = sqlSession.selectList("database.assessment.sybase.selectScript", tempProcedure.getId());
                tempProcedure.setDdlScript(String.join(" ", text));
            }

            procedures.addAll(tempProcedures);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select procedure list error.");
            return new ArrayList<>();
        }

        return procedures;
    }

    public List<Function> getFunctions(DataSource dataSource, String databaseName) {
        List<Function> functions = new ArrayList<>();

        log.debug("Sybase Assessment - getFunctions() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            List<Function> tempFunctions = sqlSession.selectList("database.assessment.sybase.selectFunction");

            for (Function tempFunction : tempFunctions) {
                List<String> text = sqlSession.selectList("database.assessment.sybase.selectScript", tempFunction.getId());
                tempFunction.setDdlScript(String.join(" ", text));
            }

            functions.addAll(tempFunctions);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select function list error.");
            return new ArrayList<>();
        }

        return functions;
    }

    public List<Trigger> getTriggers(DataSource dataSource, String databaseName) {
        List<Trigger> triggers = new ArrayList<>();

        log.debug("Sybase Assessment - getTriggers() invoked.");

        try (SqlSession sqlSession = SqlSessionConfig.getSqlSessionFactory(new CustomDelegatingDatabase(databaseName, dataSource)).openSession()) {
            List<Trigger> tempTriggers = sqlSession.selectList("database.assessment.sybase.selectTrigger");

            for (Trigger tempTrigger : tempTriggers) {
                List<String> text = sqlSession.selectList("database.assessment.sybase.selectScript", tempTrigger.getId());
                tempTrigger.setDdlScript(String.join(" ", text));
            }

            triggers.addAll(tempTriggers);
        } catch (Exception e) {
            log.error(e.getMessage());
            ThreadLocalUtils.add(DB_SCAN_ERROR, "Select trigger list error.");
            return new ArrayList<>();
        }

        return triggers;
    }

}
