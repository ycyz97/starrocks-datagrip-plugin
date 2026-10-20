INSERT OVERWRITE dws.dws_trade_sale_by_order_ri
SELECT
    biz_date,
    store_id,
    order_id,
    sale_time,
    member_card_no
FROM tmp_trade_sale_by_order_ri;

INSERT INTO dws.dws_trade_sale_by_order_ri
TEMPORARY PARTITION (tp_trade_sale)
WITH LABEL trade_sale_batch
BY NAME
PROPERTIES ("timeout" = "600")
SELECT
    biz_date,
    store_id,
    order_id,
    sale_time,
    member_card_no
FROM tmp_trade_sale_by_order_ri;

INSERT INTO FILES(
    "path" = "s3://bucket/export/",
    "format" = "parquet"
)
SELECT order_id, sale_time
FROM dws.dws_trade_sale_by_order_ri;

UPDATE dws.dws_trade_sale_by_order_ri
SET member_card_no = source.member_card_no
FROM tmp_member_card source
WHERE dws.dws_trade_sale_by_order_ri.order_id = source.order_id;

DELETE FROM dws.dws_trade_sale_by_order_ri
WHERE biz_date < '2024-01-01';

DELETE FROM dws.dws_trade_sale_by_order_ri
PARTITION p202610
USING tmp_trade_sale_by_order_ri
WHERE dws.dws_trade_sale_by_order_ri.order_id = tmp_trade_sale_by_order_ri.order_id;

WITH expired_orders AS (
    SELECT order_id
    FROM tmp_trade_sale_by_order_ri
    WHERE biz_date < '2024-01-01'
)
DELETE FROM dws.dws_trade_sale_by_order_ri
USING expired_orders
WHERE dws.dws_trade_sale_by_order_ri.order_id = expired_orders.order_id;

MERGE INTO dws.dws_trade_sale_by_order_ri target
USING tmp_trade_sale_by_order_ri source
ON target.order_id = source.order_id
WHEN MATCHED THEN UPDATE SET
    target.member_card_no = source.member_card_no
WHEN NOT MATCHED THEN INSERT
(
    biz_date,
    store_id,
    order_id,
    sale_time,
    member_card_no
)
VALUES
(
    source.biz_date,
    source.store_id,
    source.order_id,
    source.sale_time,
    source.member_card_no
);
