/*
 * Copyright 2021 The Playce-RoRo Project.
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
 * SangCheon Park   Oct 29, 2021		First Draft.
 */
package io.playce.roro.jpa.entity;

import io.playce.roro.common.dto.inventory.application.LastInventoryApplication;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "application_master")
@ToString
@SqlResultSetMapping(
        name = "LastInventoryApplicationMapping",
        classes = @ConstructorResult(
                targetClass = LastInventoryApplication.class,
                columns = {
                        @ColumnResult(name = "inventory_id", type = Long.class),
                        @ColumnResult(name = "delete_yn", type = String.class),
                        @ColumnResult(name = "automatic_regist_protection_yn", type = String.class),
                })
)
@NamedNativeQuery(
        name = "LastInventoryApplicationQuery",
        query = " select im.inventory_id\n" +
                "      , im.delete_yn\n" +
                "      , am.automatic_regist_protection_yn\n" +
                "   from application_master am\n" +
                "   join inventory_master im\n" +
                "     on im.inventory_id = am.application_inventory_id\n" +
                "  where im.server_inventory_id = :serverInventoryId\n" +
                "    and am.deploy_path = :deployPath\n" +
                "  order by im.inventory_id desc\n" +
                "  limit 1",
        resultSetMapping = "LastInventoryApplicationMapping")
public class ApplicationMaster {

    @Id
    private Long applicationInventoryId;

    @Column
    private String deployPath;

    @Column
    private String sourceLocationUri;

    @Column
    private String uploadSourceFileName;

    @Column
    private String uploadSourceFilePath;

    @Column
    private Long applicationSize;

    @Column
    private String analysisLibList;

    @Column
    private String analysisStringList;

    @Column
    private String automaticRegistProtectionYn = "N";

    @Column
    private String dedicatedAuthenticationYn = "N";

    @Column
    private String javaVersion;

    @Column
    private String javaVendor;

}
//end of ApplicationMaster.java