package com.pascalstieber.mrlocksmith.quotation.data;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
public class SpringRestDataConfig extends RepositoryRestMvcConfiguration{

    @Override
    protected void configureRepositoryRestConfiguration(RepositoryRestConfiguration config){
	config.exposeIdsFor(Quotation.class);
	config.exposeIdsFor(Item.class);
    }
    
}
