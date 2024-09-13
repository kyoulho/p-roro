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

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "user_master")
public class UserMaster {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long userId;

   @Column
   private String userCompanyCode;

   @Column
   private String userNameKorean;

   @Column
   private String userNameEnglish;

   @Column
   private String userEmail;

   @Column
   private String userPhone;

   @Column
   private String userMobile;

   @Column
   private String userFax;

   @Column
   private String userJobGrade;

   @Column
   private String userDepartment;

   @Column
   private String userRemarks;

   @Column
   private Long registUserId;

   @Column
   private Date registDatetime;

   @Column
   private Long modifyUserId;

   @Column
   private Date modifyDatetime;

}
//end of UserMaster.java