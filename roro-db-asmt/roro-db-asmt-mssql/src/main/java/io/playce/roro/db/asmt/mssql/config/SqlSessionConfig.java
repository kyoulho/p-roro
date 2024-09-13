package io.playce.roro.db.asmt.mssql.config;

import lombok.SneakyThrows;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

public class SqlSessionConfig {

    @SneakyThrows
    public static SqlSessionFactory getSqlSessionFactory(final DataSource dataSource) {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();

        ClassPathResource configResource = new ClassPathResource("config/db-mybatis-config.xml");
        ClassPathResource mapperResource = new ClassPathResource("mapper/mssqlMapper.xml");;

        factoryBean.setDataSource(dataSource);
        factoryBean.setConfigLocation(configResource);
        factoryBean.setMapperLocations(mapperResource);

        return factoryBean.getObject();
    }

}
