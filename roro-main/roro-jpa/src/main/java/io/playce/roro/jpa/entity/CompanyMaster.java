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
@Table(name = "company_master")
public class CompanyMaster {

   @Id
   private String companyCode;

   @Column
   private String countryCode;

   @Column
   private String companyStatusCode;

   @Column
   private String companyNameKorean;

   @Column
   private String companyNameEnglish;

   @Column
   private String companyRepresentativeName;

   @Column
   private String companyPhone;

   @Column
   private String companyFax;

   @Column
   private String companyZipCode;

   @Column
   private String companyAddress;

   @Column
   private String companyAddressDetail;

   @Column
   private String companyUrl;

   @Column
   private String companyRemarks;

   @Column
   private String companyCorporationNumber;

   @Column
   private String companyBusinessNumber;

   @Column
   private Integer companyEmployeeCount;

   @Column
   private String companyIndustryTypeCode;

   @Column
   private String companyTestYn;

   @Column
   private Long registUserId;

   @Column
   private Date registDatetime;

   @Column
   private Long modifyUserId;

   @Column
   private Date modifyDatetime;

}
//end of CompanyMaster.java