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
 * Jeongho Baek   11월 02, 2021		First Draft.
 */
package io.playce.roro.mybatis.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * <pre>
 * MyBatis 설정을 한다.
 * SqlSession, Transaction등 이외의 설정은 MybatisAutoConfiguration.class에서 자동으로 설정이 된다.
 * MapperScan을 통하여 Mapper Interface에 대한 implements 구현을 하지 않더라도 xml에 정의된 namespace와 id를 찾아서 Mapping을 한다.
 * </pre>
 *
 * @author Jeongho Baek
 * @version 2.0.0
 */
@Configuration
@MapperScan(basePackages = "io.playce.roro.mybatis.domain")
public class MyBatisConfig {

    private final DataSource dataSource;

    public MyBatisConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * MYBATIS_CONFIG : Mybatis Setting과 관련된 파일의 위치이다.
     * TYPE_ALIAS_PACKAGE : Type Alias를 등록하기 위해 검색할 Package명이다. (Dto을 이름으로만 접근 가능하게 해준다.)
     * MAPPER_LOCATION : Mapper가 있는 파일의 위치이다.
     */
    private static final String MYBATIS_CONFIG = "classpath:config/mybatis-config.xml";
    // MybatisX Plugin을 통한 Full Package 명을 입력할 수 있도록 한다.
    // private static final String TYPE_ALIAS_PACKAGE = "io.playce.roro.common.dto";
    private static final String MAPPER_LOCATION = "classpath*:**/mapper/**/*Mapper.xml";

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        factoryBean.setDataSource(dataSource);
        // factoryBean.setTypeAliasesPackage(TYPE_ALIAS_PACKAGE);
        factoryBean.setMapperLocations(resolver.getResources(MAPPER_LOCATION));
        factoryBean.setConfigLocation(resolver.getResource(MYBATIS_CONFIG));

        return factoryBean.getObject();
    }

}
//end of MybatisConfig.java