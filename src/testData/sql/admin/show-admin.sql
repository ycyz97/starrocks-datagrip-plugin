SHOW DATABASES;
SHOW TABLES FROM dws;
SHOW CREATE TABLE dws.dws_trade_sale_by_order_ri;
SHOW MATERIALIZED VIEWS;
SHOW PARTITIONS FROM dws.dws_trade_sale_by_order_ri;
SHOW PROC '/frontends';

SET CATALOG default_catalog;
SHOW PROCESSLIST;
SHOW FULL PROCESSLIST;
SHOW FULL TABLES FROM default_catalog.dws LIKE 'dws%';
SHOW FULL COLUMNS FROM default_catalog.dws.dws_trade_sale_by_order_ri;
SHOW GLOBAL VARIABLES LIKE 'query%';
SHOW SESSION VARIABLES WHERE Variable_name = 'query_timeout';
KILL QUERY 123;
KILL CONNECTION 123;

ADMIN SHOW FRONTEND CONFIG;
ADMIN SHOW BACKEND CONFIG;

SET enable_profile = true;
UNSET VARIABLE enable_profile;
