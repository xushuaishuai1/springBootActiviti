package com.xtm.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 * 自定义json返回
 * @author xtm
 *
 */
@Configuration
public class JacksonConfig {


    static {
        //开启autotype功能
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
//        ParserConfig.getGlobalInstance().addAccept("com.xxx.xxx.bo");
//        ParserConfig.getGlobalInstance().addAccept("com.xxx.xxx.redis");
    }
    @Bean
	public ObjectMapper getObjectMapper() {
	   ObjectMapper map = new ObjectMapper();
	   map.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	   return map;
   }
}
