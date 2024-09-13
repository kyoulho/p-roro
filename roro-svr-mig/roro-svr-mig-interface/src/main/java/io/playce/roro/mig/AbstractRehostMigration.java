/*
 * Copyright 2022 The playce-roro-v3 Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * SangCheon Park   Mar 10, 2022		    First Draft.
 */
package io.playce.roro.mig;

import com.fasterxml.jackson.databind.JsonNode;
import io.playce.roro.common.dto.migration.MigrationProcessDto;
import io.playce.roro.common.dto.migration.enums.StatusType;
import io.playce.roro.common.exception.RoRoException;
import io.playce.roro.common.util.CommandUtil;
import io.playce.roro.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <pre>
 *
 * </pre>
 *
 * @author SangCheon Park
 * @version 3.0
 */
@Slf4j
public abstract class AbstractRehostMigration extends AbstractMigration {
    protected String bucketName;
    protected String folderName;

    public void init() {
        // nothing to do
    }

    /**
     * Pre migrate.
     *
     * @throws Exception the exception
     */
    protected void preMigrate() throws Exception {
        this.bucketName = MigrationManager.getBucketName();
        this.folderName = Long.toString(migration.getInventoryProcessId());

        updateStatus(StatusType.CREATE_RAW_FILES);
        createRawFiles();
        updateStatus(StatusType.CREATED_RAW_FILES);

        chmod();
    }

    /**
     * <pre>
     * raw image 파일을 생성한다.
     * </pre>
     *
     * @throws Exception the exception
     */
    protected void createRawFiles() throws Exception {
        try {
            String result = runScript("create");

            JsonNode node = JsonUtil.readTree(result);
            JsonNode subNode = node.get("files");
            Iterator<String> pathIter = subNode.fieldNames();
            while (pathIter.hasNext()) {
                String path = pathIter.next();
                for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
                    if (volume.getVolumePath().equals(path)) {
                        volume.setRawFileName(subNode.get(volume.getVolumePath()).textValue());
                        volume.setRawFileSize(new File(volume.getRawFileName()).length());
                    }
                }
            }

            runScript("rsync");
            result = runScript("config");

            if ("unknown os variant".equals(result)) {
                throw new RoRoException("unknown os variant");
            }

			/*
			if (server.getSummary().getOs().indexOf("5.") > -1) {
				runScript("config");
			} else if (server.getSummary().getOs().indexOf("6.") > -1) {
				runScript("config");
			} else if (server.getSummary().getOs().indexOf("7.") > -1) {
				runScript("config");
			} else {
				throw new RoRoException(server.getSummary().getOs() + " is an unsupported os version.");
			}
			*/

            // DO NOT Execute chmod()
            //chmod();
        } finally {
            runScript("detach");
        }
    }

    /**
     * <pre>
     * Worker 서버에 저장된 raw image 파일을 삭제한다.
     * </pre>
     *
     * @throws Exception the exception
     */
    protected void deleteRawFiles() throws Exception {
        try {
            DefaultExecutor executor = new DefaultExecutor();

            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findCommand("rm"),
                    "-rf",
                    workDir + "/raw_files");

            /*
            CommandLine cl = new CommandLine(CommandUtil.findCommand("sudo"))
                    .addArguments(CommandUtil.findCommand("rm"))
                    .addArguments("-rf")
                    .addArguments(workDir)
                    .addArguments("/raw_files");
            */

            log.debug("Command for remove raw file(s) : [{}]", cl);

            executor.execute(cl);
        } catch (Exception e) {
            log.error("Unhandled exception occurred while delete raw image directory.", e);
            throw e;
        }
    }

    /**
     * <pre>
     * 마이그레이션 디렉토리에 대한 퍼미션 일괄 변경 (detach 이전에 변경하면 부팅 안됨)
     * </pre>
     *
     * @return the string
     *
     * @throws Exception the exception
     */
    protected String chmod() throws Exception {
        String result = null;
        ByteArrayOutputStream baos = null;

        try {
            DefaultExecutor executor = new DefaultExecutor();
            baos = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    CommandUtil.findCommand("chmod"),
                    "-R",
                    "777",
                    workDir);

            /*
            CommandLine cl = new CommandLine(CommandUtil.findCommand("sudo"))
                    .addArguments(CommandUtil.findCommand("chmod"))
                    .addArguments("-R")
                    .addArguments("777")
                    .addArguments(workDir);
            */

            log.debug("chmod()'s CommandLine : {}", cl);

            int exitCode = executor.execute(cl);

            if (exitCode == 0) {
                result = baos.toString();
            } else {
                throw new Exception(baos.toString());
            }

            log.debug("chmod()'s result : {}", result);
        } catch (Exception e) {
            log.error("Shell execution error while change permissions. Error Log => [{}]", baos.toString());
            throw e;
        } finally {
            IOUtils.closeQuietly(baos);
        }

        return result;
    }

    /**
     * <pre>
     * python script를 수행하여 디스크 생성 및 mount
     * </pre>
     *
     * @param type the type
     *
     * @return the string
     *
     * @throws Exception the exception
     */
    protected String runScript(String type) throws Exception {
        String result = null;
        ByteArrayOutputStream baos = null;

        try {
            DefaultExecutor executor = new DefaultExecutor();
            baos = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
            executor.setStreamHandler(streamHandler);

            CommandLine cl = CommandUtil.getCommandLine(CommandUtil.findCommand("sudo"),
                    MigrationManager.getLinuxMigrationFile().getAbsolutePath(),
                    "-s " + targetHost.getIpAddress(),
                    "--work_dir " + workDir,
                    "--log_dir " + logDir,
                    "--kernel " + migration.getServerSummary().getOsKernel(),
                    "--os_family " + migration.getServerSummary().getOsFamily().toLowerCase(),
                    "--swapsize " + migration.getSwapSize());

            /*
            CommandLine cl = new CommandLine(CommandUtil.findCommand("sudo"))
                    //.addArguments(findCommand("python"))
                    .addArguments(MigrationManager.getLinuxMigrationFile().getAbsolutePath())
                    .addArguments("-s " + server.getIpAddress())
                    .addArguments("--work_dir " + workDir)
                    .addArguments("--log_dir " + logDir)
                    .addArguments("--kernel " + server.getServerSummary().getKernel())
                    .addArguments("--os_family " + server.getServerSummary().getFamily().toLowerCase())
                    .addArguments("--swapsize " + migration.getSwap());
            */

            StringBuilder sb = null;
            List<String> extraDisks = new ArrayList<String>();
            for (MigrationProcessDto.Volume volume : migration.getVolumes()) {
                if ("Y".equals(volume.getRootYn())) {
                    cl.addArguments("--rootsize " + volume.getVolumeSize());
                } else {
                    String path = volume.getVolumePath();

                    if (!path.startsWith("/")) {
                        path = "/" + path;
                    }

                    if (path.equals("/boot") || path.startsWith("/boot/")) {
                        //continue;
                        throw new RoRoException("/boot path is not allowed.");
                    }

                    if (sb == null) {
                        sb = new StringBuilder();
                    } else {
                        sb.append(",");
                    }

                    extraDisks.add(path);
                    sb.append(path).append(":").append(volume.getVolumeSize());
                }
            }

            if (sb != null) {
                cl.addArguments("--extra_disks " + sb.toString());
            }

            if (StringUtils.isNotEmpty(targetHost.getKeyFilePath())) {
                cl.addArguments("--port " + targetHost.getPort())
                        .addArguments("--user " + targetHost.getUsername())
                        .addArguments("--key " + targetHost.getKeyFilePath())
                        .addArguments("-t " + type);

            } else {
                cl.addArguments("--port " + targetHost.getPort())
                        .addArguments("--user " + targetHost.getUsername())
                        .addArguments("--password " + targetHost.getPassword())
                        .addArguments("-t " + type);
            }

            //*
            if (type.equals("config")) {
                if (migration.getServerSummary().getDiskInfos() != null) {
                    for (MigrationProcessDto.DiskInfo diskInfo : migration.getServerSummary().getDiskInfos()) {
                        if ("/".equals(diskInfo.getMountPath())) {
                            //cl.addArguments("--filesystem  " + disk.getType());
                            cl.addArguments("--filesystem  " + "ext4");  // ext4 is default value in python module
                            break;
                        }
                    }
                }
            }
			/*/
			for (Disk disk : server.getSummary().getDisks()) {
				if ("/".equals(disk.getMount())) {
					cl.addArguments("--filesystem  " + disk.getType());
					break;
				}
			}
			//*/

            sb = null;
            /*
            if (server.getServerSummary() != null && server.getServerSummary().getDiskInfo() != null) {
                for (String path : server.getServerSummary().getDiskInfo().keySet()) {
                    Map<String, String> diskMap = (Map<String, String>) server.getServerSummary().getDiskInfo().get(path);

                    if (diskMap.get("fstype").startsWith("nfs") || diskMap.get("fstype").startsWith("cifs") ||
                            diskMap.get("fstype").startsWith("efs") || diskMap.get("fstype").startsWith("hdfs") ||
                            diskMap.get("fstype").startsWith("gfs") || diskMap.get("fstype").startsWith("fuse") ||
                            diskMap.get("fstype").startsWith("ocfs") || diskMap.get("fstype").startsWith("owfs")) {
                        // extra_disk에 추가된 볼륨이 아닌 공유 스토리지 일 경우 raw 이미지를 생성하지 않는다.
                        if (!extraDisks.contains(path)) {
                            if (sb == null) {
                                sb = new StringBuilder();
                            } else {
                                sb.append(",");
                            }

                            sb.append(path);
                        }
                    }
                }
            }
            */

            // exclude directory
            if (migration.getExcludeDirectories() != null && migration.getExcludeDirectories().size() > 0) {
                for (String directory : migration.getExcludeDirectories()) {
                    String[] dirs = directory.split("/");

                    String dir = null;
                    for (String d : dirs) {
                        if (StringUtils.isNotEmpty(d)) {
                            if (dir == null) {
                                dir = "/";
                            }

                            if (!dir.endsWith("/")) {
                                dir += "/";
                            }

                            dir += d;
                        }
                    }

                    if (dir != null) {
                        if (sb == null) {
                            sb = new StringBuilder();
                        } else {
                            sb.append(",");
                        }

                        sb.append(dir);
                    }
                }
            }

            if (sb != null) {
                cl.addArguments("--exclude " + sb.toString());
            }

			/*
			if (type.equals("config") && scriptUrl != null) {
				scriptUrl = java.net.URLEncoder.encode(scriptUrl, "UTF-8");
				cl.addArguments("--script '" + scriptUrl + "'");

				log.debug("scriptUrl : [{}]", scriptUrl);
			}
			//*/

            log.debug("runScript()'s CommandLine : {}", cl);

            int exitCode = executor.execute(cl);

            if (exitCode == 0) {
                result = baos.toString();
            } else if (exitCode == 5) {
                throw new Exception(baos.toString() + " Please check rsync package installed or not.");
            } else {
                throw new Exception(baos.toString());
            }

            log.debug("runScript()'s result : {}", result);
        } catch (Exception e) {
            log.error("Python error while executing script. Error Log => [{}]", baos.toString());
            throw e;
        } finally {
            IOUtils.closeQuietly(baos);
        }

        return result;
    }

    protected abstract void upload() throws Exception;

    protected abstract void deleteFolder() throws Exception;

    protected abstract void statusCheck() throws Exception;

    protected abstract void attachVolumes() throws Exception;

    protected abstract void createImage() throws Exception;

    protected abstract void terminateInstance();

    protected abstract void createInstance() throws Exception;
}
//end of AbstractRehostMigration.java