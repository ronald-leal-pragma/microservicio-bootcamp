package com.pragma.bootcamp.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

/**
 * Configuración dual de base de datos: R2DBC (MySQL) + MongoDB reactivo.
 *
 * - R2dbcTransactionManager marcado como @Primary → @Transactional apunta siempre a MySQL.
 * - MappingMongoConverter con typeKey null → desactiva el campo "_class" como discriminador,
 *   permitiendo leer/actualizar documentos escritos por el microservicio-reporte sin conflicto
 *   de nombre de clase.
 */
@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public ReactiveTransactionManager r2dbcTransactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoCustomConversions conversions,
                                                       MongoMappingContext context) {
        MappingMongoConverter converter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context);
        converter.setCustomConversions(conversions);
        // null como typeKey desactiva el uso de "_class" como discriminador
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }
}
