package org.example.bookstore.config;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;

@Configuration
public class JooqConfig {

    @Bean
    public DataSourceConnectionProvider connectionProvider(DataSource dataSource) {
        return new DataSourceConnectionProvider(
                new TransactionAwareDataSourceProxy(dataSource));
    }

    @Bean
    public DefaultDSLContext dsl(org.jooq.Configuration config) {
        return new DefaultDSLContext(config);
    }

    @Bean
    public DefaultConfiguration jooqConfiguration(
            DataSourceConnectionProvider connectionProvider,
            DataSource dataSource) {

        DefaultConfiguration config = new DefaultConfiguration();
        config.setSQLDialect(SQLDialect.POSTGRES);
        config.setConnectionProvider(connectionProvider);
        config.setExecuteListenerProvider(
                new DefaultExecuteListenerProvider(new JooqExceptionTranslator(dataSource)));

        Settings settings = new Settings()
                .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED);
        config.setSettings(settings);

        return config;
    }

    record JooqExceptionTranslator(DataSource dataSource) implements ExecuteListener {

        @Override
            public void exception(ExecuteContext ctx) {
                SQLDialect dialect = ctx.configuration().dialect();
                SQLExceptionTranslator translator =
                        new SQLErrorCodeSQLExceptionTranslator(dialect.name());

                if (ctx.sqlException() != null) {
                    ctx.exception(translator.translate(
                            "jOOQ", ctx.sql(), ctx.sqlException()));
                }
            }
        }
}
