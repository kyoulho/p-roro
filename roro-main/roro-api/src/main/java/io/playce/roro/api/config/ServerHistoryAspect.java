/*
 * Copyright 2023 The playce-roro Project.
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
 * Dong-Heon Han    Apr 06, 2023		First Draft.
 */

package io.playce.roro.api.config;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.exception.RoRoApiException;
import io.playce.roro.api.domain.common.aop.SubscriptionManager;
import io.playce.roro.api.domain.inventory.service.ServerService;
import io.playce.roro.common.dto.history.SubscriptionCount;
import io.playce.roro.common.dto.inventory.server.ServerRequest;
import io.playce.roro.common.dto.subscription.Subscription;
import io.playce.roro.history.enums.ACTION;
import io.playce.roro.history.event.InitSubscriptionCountEvent;
import io.playce.roro.history.event.ServerEvent;
import io.playce.roro.history.event.SubscriptionCountEvent;
import io.playce.roro.jpa.entity.InventoryMaster;
import io.playce.roro.jpa.entity.ServerMaster;
import io.playce.roro.jpa.repository.InventoryMasterRepository;
import io.playce.roro.jpa.repository.ServerMasterRepository;
import io.playce.roro.mybatis.domain.inventory.server.ServerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Aspect
@Slf4j
public class ServerHistoryAspect {
    private final ApplicationEventPublisher publisher;
    private final InventoryMasterRepository inventoryMasterRepository;
    private final ServerMasterRepository serverMasterRepository;
    private final ServerMapper serverMapper;

    @AfterReturning(pointcut = "execution(* io.playce.roro.jpa.repository.ServerMasterRepository.save(..))", returning = "res")
    public void create(ServerMaster res) throws Throwable {
        Long serverId = res.getServerInventoryId();
        InventoryMaster inventoryMaster = inventoryMasterRepository.findById(serverId).orElse(null);
        if(inventoryMaster == null) return;

        ServerEvent event = ServerEvent.builder()
                .eventTime(Instant.now())
                .action(ACTION.C)
                .projectId(inventoryMaster.getProjectId())
                .inventoryId(serverId)
                .ip(res.getRepresentativeIpAddress())
                .port(res.getConnectionPort())
                .build();
        publisher.publishEvent(event);
        log.trace("==> create serverId: {}", serverId);
    }

    @AfterReturning(pointcut =
            "execution(* io.playce.roro.api.domain.inventory.controller.ServerController.createServer(..)) || " +
            "execution(* io.playce.roro.api.domain.inventory.controller.InventoryController.uploadInventory(..))",
            returning = "res"
    )
    public void count(JoinPoint point, ResponseEntity<?> res) {
        if(res.getStatusCode() != HttpStatus.OK) return;

        Object[] args = point.getArgs();
        Long projectId = (Long) args[0];

        Instant now = Instant.now();
        log.trace("==> count - projectId: {}", projectId);
        publishServerCountEvent(now);
    }

    @AfterReturning(pointcut = "execution(* io.playce.roro.api.domain.inventory.controller.ServerController.modifyServer(..))", returning = "res")
    public void update(JoinPoint point, ResponseEntity<?> res) {
        if(res.getStatusCode() != HttpStatus.OK) return;

        Object[] args = point.getArgs();
        Long projectId = (Long) args[0];
        Long serverId = (Long) args[1];
        ServerRequest req = (ServerRequest) args[2];

        Instant now = Instant.now();
        ServerEvent event = createEvent(now, ACTION.U, projectId, serverId, req.getRepresentativeIpAddress(), req.getConnectionPort());
        publisher.publishEvent(event);
        log.trace("==> update - serverId: {}", serverId);
        publishServerCountEvent(now);
    }

    @AfterReturning(pointcut = "execution(* io.playce.roro.api.domain.inventory.controller.ServerController.deleteServer(..))", returning = "res")
    public void delete(JoinPoint point, ResponseEntity<?> res) {
        if(res.getStatusCode() != HttpStatus.NO_CONTENT) return;

        Object[] args = point.getArgs();
        Long projectId = (Long) args[0];
        Long serverId = (Long) args[1];
        ServerMaster svr = serverMasterRepository.findById(serverId).orElse(null);
        if(svr == null) return;

        Instant now = Instant.now();
        ServerEvent event = createEvent(now, ACTION.D, projectId, serverId, svr.getRepresentativeIpAddress(), svr.getConnectionPort());
        publisher.publishEvent(event);
        log.trace("==> delete - serverId: {}", serverId);
        publishServerCountEvent(now);
    }

    @EventListener({ ContextRefreshedEvent.class, InitSubscriptionCountEvent.class})
    public void totalCount() {
        Instant now = Instant.now();
        publishServerCountEvent(now);
        log.trace("==> calc subscription: {}", now);
    }

    private void publishServerCountEvent(Instant now) {
        List<SubscriptionCount> list = serverMapper.selectServerCountPerProjectId();
        SubscriptionCountEvent countEvent = SubscriptionCountEvent.builder()
                .eventTime(now)
                .list(list)
                .build();
        publisher.publishEvent(countEvent);
    }

    private ServerEvent createEvent(Instant now, ACTION action, Long projectId, Long serverId, String ip, int port) {
        return ServerEvent.builder()
                .eventTime(now)
                .action(action)
                .projectId(projectId)
                .inventoryId(serverId)
                .ip(ip)
                .port(port)
                .build();
    }
}