-- 执行以下sql，创建 flyway_schema_history 表即可
-- redpacket_manage 为要使用flyway的非空数据库
create table if not exists flyway_schema_history
(
    installed_rank int not null
    primary key,
    version varchar(50) null,
    description varchar(200) not null,
    type varchar(20) not null,
    script varchar(1000) not null,
    checksum int null,
    installed_by varchar(100) not null,
    installed_on timestamp default CURRENT_TIMESTAMP not null,
    execution_time int not null,
    success tinyint(1) not null
    );
