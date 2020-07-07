package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关系型数据源，`JdbcTemplate`所操作的数据源
 */
public class SqlDataSource extends DataSourceDialect {

    private JdbcTemplate jdbcTemplate;

    private SqlDataSource(){}

    public SqlDataSource(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SqlDataSource(JdbcTemplate jdbcTemplate, boolean storeApi) {
        this.storeApi = storeApi;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String listApiInfoScript() {
        return "select id,method,path,datasource,`type`,`service`,`group`,editor,`comment`,script,options,create_time,update_time from api_info where service = #{service}";
    }

    @Override
    String lastApiInfoHistoryScript() {
        return "select id,api_info_id,method,path,datasource,`type`,`service`,`group`,editor,`comment`,script,options,create_time from api_info_history where service = #{service} ?{apiInfoId,and api_info_id = #{apiInfoId}} order by id desc limit #{index},#{pageSize}";
    }

    @Override
    public String saveApiInfoHistoryScript() {
        return "insert into api_info_history(api_info_id,method,path,datasource,`type`,`service`,`group`,editor,`comment`,script,options,create_time) values(#{apiInfoId},#{method},#{path},#{datasource},#{type},#{service},#{group},#{editor},#{comment},#{script},#{options},#{createTime})";
    }

    @Override
    public String getApiInfoScript() {
        return "select id,method,path,datasource,`type`,`service`,`group`,editor,`comment`,script,options,create_time,update_time from api_info where method = #{method} and path = #{path}";
    }

    @Override
    public String saveApiInfoScript() {
        return "insert into api_info(method,path,datasource,`type`,`service`,`group`,editor,`comment`,script,options,create_time,update_time) values(#{method},#{path},#{datasource},#{type},#{service},#{group},#{editor},#{comment},#{script},#{options},#{createTime},#{updateTime})";
    }

    @Override
    public String updateApiInfoScript() {
        return "update api_info set method=#{method},path=#{path},datasource=#{datasource},`service`=#{service},`group`=#{group},editor=#{editor},`comment`=#{comment},script=#{script},options=#{options},update_time=#{updateTime} where id = #{id}";
    }

    @Override
    public String deleteApiInfoScript() {
        return "delete from api_info where id = #{id}";
    }

    @Override
    String saveApiExampleScript() {
        return "insert into api_example(api_info_id,method,url,request_header,request_body,response_header,response_body,status,time,options,editor,create_time) " +
                "values(#{apiInfoId},#{method},#{url},#{requestHeader},#{requestBody},#{responseHeader},#{responseBody},#{status},#{time},#{options},#{editor},#{createTime})";
    }

    @Override
    String lastApiExampleScript() {
        return "select id,api_info_id,method,url,request_header,request_body,response_header,response_body,status,time,options,editor,create_time from api_example where api_info_id = #{apiInfoId} order by id desc limit #{limit}";
    }

    @Override
    String deleteExampleScript() {
        return "delete from api_example where id in (#{ids})";
    }

    @Override
    public List<Map<String,Object>> find(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        List<Map<String,Object>> resultList = jdbcTemplate.queryForList(script.toString());
        return resultList.stream().map(this::toReplaceKeyLow).collect(Collectors.toList());
    }

    @Override
    Long update(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        return Long.valueOf(jdbcTemplate.update(script.toString()));
    }

    @Override
    Long remove(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        return Long.valueOf(jdbcTemplate.update(script.toString()));
    }

    @Override
    Object insert(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator preparedStatementCreator = con -> {
            PreparedStatement ps = con.prepareStatement(script.toString(), Statement.RETURN_GENERATED_KEYS);
            return ps;
        };
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        return keyHolder.getKey();
    }
}
