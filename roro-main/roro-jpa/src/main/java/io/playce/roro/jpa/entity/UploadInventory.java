/*
 * Copyright 2022 The Playce-RoRo Project.
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
 * Author            Date                Description
 * ---------------  ----------------    ------------
 * Jaeeon Bae       1월 07, 2022            First Draft.
 */
package io.playce.roro.jpa.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Date;

/**
 * <pre>
 *
 * </pre>
 *
 * @author Jaeeon Bae
 * @version 3.0
 */
@Entity
@Getter
@Setter
@Table(name = "upload_inventory")
@Where(clause = "delete_yn='N'")
@ToString
public class UploadInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uploadInventoryId;

    @Column
    private Long projectId;

    @Column
    private String fileName;

    @Column
    private String filePath;

    @Column
    private String uploadStatusTypeCode;

    @Column
    private String uploadProcessResultTxt;

    @Column
    private int serviceCount;

    @Column
    private int serverCount;

    @Column
    private int middlewareCount;

    @Column
    private int applicationCount;

    @Column
    private int dbmsCount;

    @Column
    private String deleteYn;

    @Column
    private Long registUserId;

    @Column
    private Date registDatetime;

    @Column
    private Long modifyUserId;

    @Column
    private Date modifyDatetime;
}