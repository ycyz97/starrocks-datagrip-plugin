LOAD LABEL db1.label1
(
    DATA INFILE("s3://bucket/path/file.csv")
    INTO TABLE fact_order
    COLUMNS TERMINATED BY ","
    FORMAT AS "csv"
)
WITH BROKER
PROPERTIES (
    "timeout" = "3600"
);

CREATE ROUTINE LOAD db1.routine_load_job ON fact_order
COLUMNS TERMINATED BY ","
PROPERTIES (
    "desired_concurrent_number" = "3"
)
FROM KAFKA (
    "kafka_broker_list" = "localhost:9092",
    "kafka_topic" = "orders"
);

LOAD LABEL db1.transformed_label
(
    DATA INFILE("s3://bucket/path/*.csv")
    NEGATIVE
    INTO TABLE fact_order
    PARTITION (p202610)
    TEMPORARY PARTITION (tp202610)
    COLUMNS TERMINATED BY ","
    ROWS TERMINATED BY "\n"
    FORMAT AS "csv"
    (raw_order_id, raw_amount, order_id = CAST(raw_order_id AS BIGINT), amount = CAST(raw_amount AS DECIMAL(18, 2)))
    COLUMNS FROM PATH AS (path_date)
    SET (biz_date = str2date(path_date, '%Y-%m-%d'))
    WHERE raw_order_id IS NOT NULL
)
WITH BROKER
PROPERTIES ("timeout" = "3600");

CREATE ROUTINE LOAD db1.transformed_routine_load ON fact_order
COLUMNS TERMINATED BY ","
ROWS TERMINATED BY "\n"
COLUMNS (raw_order_id, order_id = CAST(raw_order_id AS BIGINT))
PARTITION (p202610)
WHERE raw_order_id IS NOT NULL
DELETE ON op = 'D'
PROPERTIES (
    "desired_concurrent_number" = "3"
)
FROM KAFKA (
    "kafka_broker_list" = "localhost:9092",
    "kafka_topic" = "orders"
);

PAUSE ROUTINE LOAD db1.transformed_routine_load;
RESUME ROUTINE LOAD db1.transformed_routine_load;
STOP ROUTINE LOAD db1.transformed_routine_load;
SHOW ROUTINE LOAD FROM db1 LIKE "transformed%";
SHOW ROUTINE LOAD TASK FROM db1;

SUBMIT TASK refresh_mv_task AS
REFRESH MATERIALIZED VIEW mv_ads_trade_sale_by_order_ri;

SUBMIT TASK insert_trade_sale_task AS
INSERT OVERWRITE dws.dws_trade_sale_by_order_ri
SELECT * FROM tmp_trade_sale_by_order_ri;

SUBMIT TASK scheduled_trade_sale_task
SCHEDULE START('2026-07-01 10:00:00') EVERY(INTERVAL 1 DAY)
PROPERTIES ("session.insert_timeout" = "10000")
AS
CREATE TABLE dws.dws_trade_sale_by_order_async AS
SELECT * FROM dws.dws_trade_sale_by_order_ri;
