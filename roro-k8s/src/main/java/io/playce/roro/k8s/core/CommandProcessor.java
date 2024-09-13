/*
 * Copyright 2023 The playce-roro-k8s-assessment Project.
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
 * Dong-Heon Han    Jul 13, 2023		First Draft.
 */

package io.playce.roro.k8s.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import io.playce.roro.common.code.Domain1003;
import io.playce.roro.common.dto.k8s.CommandProcessorRequest;
import io.playce.roro.common.util.K8SUtil;
import io.playce.roro.k8s.command.CommandFactory;
import io.playce.roro.k8s.command.enums.COMMAND_KEY;
import io.playce.roro.k8s.common.exception.CancelException;
import io.playce.roro.k8s.config.Command;
import io.playce.roro.k8s.config.K8sConfig;
import io.playce.roro.k8s.handler.CommandResultHandler;
import io.playce.roro.k8s.service.ClusterScanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandProcessor {
    private final K8sConfig k8sConfig;
    private final CommandFactory commandFactory;
    private final Map<String, CommandResultHandler> handlerMap;
    private final Map<String, CommandRunner> runnerMap;
    private final ClusterScanService clusterScanService;

    public void process(CommandProcessorRequest req) {
        log.debug("start process..");
        try {
            clusterScanService.setStatus(Domain1003.PROC, req);

            //set config to temp
            String configPath = K8SUtil.writeHomeKube(req.getConfig());
            if (configPath == null) {
                clusterScanService.setStatus(Domain1003.FAIL, req);
                return;
            }
            log.info("create temp config: {}", configPath);

            // config 설정목록 가져오기
            List<COMMAND_KEY> processCommands = k8sConfig.getProcessCommand();

            CommandRunner commandRunner = runnerMap.get(req.getRunnerName());
            log.info("use runner: {}", commandRunner);

            //설정된 실행 명령 수행.
            for (COMMAND_KEY processCommand: processCommands) {
                clusterScanService.checkCancel(req);

                List<Command> commands = commandFactory.getCommand(processCommand);
                if(commands == null) {
                    log.error("Check your settings");
                    continue;
                }

                log.debug("{}", processCommand);
                processCommand(req.getClusterScanId(), configPath, commandRunner, processCommand, commands);
            }

            //config 삭제
            log.debug("delete temp config: {}", configPath);
            K8SUtil.deleteFile(configPath);

            log.debug("done process..");
        } catch (CancelException e) {
            log.debug("cluster scan id {} is canceled", req.getClusterScanId());
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            clusterScanService.setStatus(Domain1003.FAIL, req);
        } finally {
            clusterScanService.setStatus(Domain1003.CMPL, req);
        }
    }

    private void processCommand(Long clusterScanId, String configPath, CommandRunner commandRunner, COMMAND_KEY processCommand, List<Command> commands) {
        for(Command command: commands) {
            // command 실행
            String result = commandRunner.run(command.getCommandString(), configPath);

            if(result == null) {
                log.error("Check your command");
            } else {
                CommandResultHandler commandResultHandler = handlerMap.get(command.getHandlerName());
                log.info("use result handler: {}, parser: {}", command.getHandlerName(), command.getParserName());

                // 스캔 원본 저장
                clusterScanService.saveOriginResult(clusterScanId, processCommand, result);

                // handler 실행.
                JsonNode parseResult = commandResultHandler.parse(result, command.getParserName());
                if(parseResult instanceof MissingNode) {
                    continue;
                }
                commandResultHandler.saveData(clusterScanId, parseResult);
                commandResultHandler.setRelation(parseResult);
                break;
            }
        }
    }

}