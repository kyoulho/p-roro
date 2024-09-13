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
 * Dong-Heon Han    Apr 05, 2023		First Draft.
 */

package io.playce.roro.history.service;

import io.playce.roro.common.dto.history.SubscriptionCount;
import io.playce.roro.common.dto.subscription.Subscription;
import io.playce.roro.history.event.InitSubscriptionCountEvent;
import io.playce.roro.history.event.ServerEvent;
import io.playce.roro.history.event.SubscriptionCountEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerEventHandlerService {
    private final ApplicationEventPublisher publisher;
    private final JdbcTemplate sqliteJdbcTemplate;

    @PostConstruct
    public void init() {
        sqliteJdbcTemplate.execute("create table if not exists server_history(event_time text, action text, project_id integer, inventory_id integer, ip text, port integer)");
        sqliteJdbcTemplate.execute("create table if not exists subscription_count(project_id integer primary key, count integer)");
        sqliteJdbcTemplate.execute("create table if not exists subscription_count_history(event_time text, project_id integer, count integer)");
    }

    @EventListener
    public synchronized void history(ServerEvent event) {
        log.trace("server-event project: {}, ip: {}, port: {}", event.getProjectId(), event.getIp(), event.getPort());
        try {
            sqliteJdbcTemplate.execute("insert into server_history values (?, ?, ?, ?, ?, ?)", (PreparedStatement p) -> {
                int i = 0;
                p.setString(++i, event.getEventTime().toString());
                p.setString(++i, event.getAction().getDescription());
                p.setLong(++i, event.getProjectId());
                p.setLong(++i, event.getInventoryId());
                p.setString(++i, event.getIp());
                p.setLong(++i, event.getPort());
                return p.executeUpdate();
            });
        } catch (Exception e) {
            log.error(e.getMessage());
            init();
            publisher.publishEvent(InitSubscriptionCountEvent.builder().build());
        }
    }

    @EventListener
    public synchronized void count(SubscriptionCountEvent event) {
        log.trace("subscription-count-event date: {}", event.getEventTime());
        try {
            sqliteJdbcTemplate.execute("delete from subscription_count");
            List<SubscriptionCount> list = event.getList();
            list.forEach(l -> {
                sqliteJdbcTemplate.execute("insert into subscription_count values (?, ?)", (PreparedStatement p) -> {
                    int i = 0;
                    p.setLong(++i, l.getProjectId());
                    p.setLong(++i, l.getCount());
                    return p.executeUpdate();
                });
                sqliteJdbcTemplate.execute("insert into subscription_count_history values (?, ?, ?)", (PreparedStatement p) -> {
                    int i = 0;
                    p.setString(++i, event.getEventTime().toString());
                    p.setLong(++i, l.getProjectId());
                    p.setLong(++i, l.getCount());
                    return p.executeUpdate();
                });
            });
        } catch (Exception e) {
            log.error(e.getMessage());
            init();
            publisher.publishEvent(InitSubscriptionCountEvent.builder().build());
        }
    }

    private synchronized long getSubscriptionCount() {
        try {
            Long count = sqliteJdbcTemplate.queryForObject("select sum(count) from subscription_count", Long.class);
            if(count == null) return -1;
            return count.longValue();
        } catch (Exception e) {
            log.error(e.getMessage());
            init();
            publisher.publishEvent(InitSubscriptionCountEvent.builder().build());
        }
        return -1;
    }

    public void setSubscriptionUsedCount(Subscription subscription, List<SubscriptionCount> subscriptionCounts) {
        long mariadbSubscriptionCount = subscriptionCounts.stream().map(s -> s.getCount()).reduce(0L, Long::sum);
        long sqliteCount = getSubscriptionCount();
        long count = Math.max(mariadbSubscriptionCount, sqliteCount);
        log.trace("mariadb: {}, sqlite: {}", mariadbSubscriptionCount, count);
        subscription.setUsedCount((int) count);
    }
}