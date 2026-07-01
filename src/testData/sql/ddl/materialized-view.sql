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
