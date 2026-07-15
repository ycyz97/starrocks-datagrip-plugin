CREATE MATERIALIZED VIEW mv_ads_trade_sale_by_order_ri
(
    biz_date COMMENT 'business date',
    order_id COMMENT 'order id',
    sale_time COMMENT 'sale time',
    member_card_no COMMENT 'member card number'
)
COMMENT 'order materialized view'
PARTITION BY date_trunc('day', biz_date)
DISTRIBUTED BY HASH(order_id) BUCKETS 12
REFRESH ASYNC
PROPERTIES (
    "replicated_storage" = "true"
)
AS
SELECT
    biz_date,
    order_id,
    sale_time,
    member_card_no
FROM dws.dws_trade_sale_by_order_ri;

CREATE MATERIALIZED VIEW mv_scheduled_orders
COMMENT 'scheduled orders'
PARTITION BY (biz_date, region, date_trunc('day', sale_time))
DISTRIBUTED BY HASH(order_id, region) BUCKETS 12
ORDER BY (biz_date, order_id)
REFRESH DEFERRED SCHEDULE START('2026-07-01 10:00:00') EVERY (INTERVAL 1 DAY)
PROPERTIES (
    "partition_refresh_number" = "3",
    "query_rewrite_consistency" = "force_mv"
)
AS
SELECT biz_date, region, sale_time, order_id
FROM dws.dws_trade_sale_by_order_ri;

CREATE MATERIALIZED VIEW mv_incremental_orders
PARTITION BY biz_date
REFRESH DEFERRED MANUAL
PROPERTIES ("refresh_mode" = "INCREMENTAL")
AS SELECT biz_date, order_id
FROM dws.dws_trade_sale_by_order_ri;
