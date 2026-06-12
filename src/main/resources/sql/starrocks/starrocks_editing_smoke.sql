-- StarRocks editing smoke SQL for dialect, formatting, completion, and highlighting checks.

CREATE TABLE IF NOT EXISTS ads_order_detail (
    order_id       BIGINT       COMMENT 'order id',
    user_id        BIGINT       COMMENT 'user id',
    biz_date       DATE         COMMENT 'business date',
    amount         DECIMAL(18, 2) SUM COMMENT 'order amount',
    attrs          JSON         COMMENT 'attributes',
    tags           ARRAY<STRING> COMMENT 'tags',
    bitmap_user_id BITMAP       BITMAP_UNION COMMENT 'bitmap user id'
)
DUPLICATE KEY(order_id)
PARTITION BY date_trunc('day', biz_date)
DISTRIBUTED BY HASH(order_id) BUCKETS 12
PROPERTIES (
    "replication_num" = "3",
    "datacache.enable" = "true",
    "storage_format" = "DEFAULT"
);

CREATE MATERIALIZED VIEW mv_ads_order_daily (
    biz_date        COMMENT 'business date',
    user_id         COMMENT 'user id',
    total_amount    COMMENT 'total amount',
    paid_order_cnt  COMMENT 'paid order count',
    bitmap_user_ids COMMENT 'bitmap users'
)
COMMENT 'daily order statistics'
PARTITION BY (date_trunc('day', biz_date))
DISTRIBUTED BY HASH(user_id) BUCKETS 8
REFRESH MANUAL
PROPERTIES (
    "replicated_storage" = "true",
    "partition_refresh_number" = "8",
    "replication_num" = "3",
    "session.enable_spill" = "true"
)
AS
SELECT
    biz_date,
    user_id,
    SUM(amount) AS total_amount,
    COUNT_IF(amount > 0) AS paid_order_cnt,
    BITMAP_UNION(TO_BITMAP(user_id)) AS bitmap_user_ids
FROM ads_order_detail
GROUP BY biz_date, user_id;

INSERT OVERWRITE ads_order_detail
SELECT
    order_id,
    user_id,
    TO_DATE(biz_date) AS biz_date,
    amount,
    PARSE_JSON(attrs) AS attrs,
    SPLIT(tags, ',') AS tags,
    TO_BITMAP(user_id) AS bitmap_user_id
FROM ods_order_detail
WHERE biz_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY);

UPDATE ads_order_detail
SET amount = b.amount,
    attrs = JSON_SET(ads_order_detail.attrs, '$.updated', 'true'),
    tags = ARRAY_DISTINCT(ARRAY_CONCAT(ads_order_detail.tags, b.tags))
FROM tmp_order_detail b
WHERE ads_order_detail.order_id = b.order_id;

DELETE FROM ads_order_detail
USING tmp_deleted_order d
WHERE ads_order_detail.order_id = d.order_id;

CREATE PIPE pipe_order_load
PROPERTIES (
    "AUTO_INGEST" = "TRUE"
)
AS
INSERT INTO ads_order_detail
SELECT *
FROM FILES(
    "path" = "s3://bucket/order/",
    "format" = "parquet"
);

SUBMIT TASK task_refresh_mv AS
REFRESH MATERIALIZED VIEW mv_ads_order_daily;

SELECT
    a.order_id,
    a.user_id,
    b.refund_amount
FROM ads_order_detail a
FULL JOIN dwd_refund_detail b
    ON a.order_id = b.order_id;

WITH paid_orders AS (
    SELECT order_id, user_id, amount
    FROM ads_order_detail
),
refund_orders AS (
    SELECT order_id, refund_amount
    FROM dwd_refund_detail
)
SELECT
    p.order_id,
    p.user_id,
    r.refund_amount
FROM paid_orders p
FULL OUTER JOIN refund_orders r
    ON p.order_id = r.order_id;

CREATE MATERIALIZED VIEW mv_order_refund_full_join
AS
SELECT
    a.order_id,
    a.user_id,
    b.refund_amount
FROM ads_order_detail a
FULL OUTER JOIN dwd_refund_detail b
    ON a.order_id = b.order_id;

SELECT
    t.order_id,
    tag.unnest AS tag
FROM ads_order_detail t,
UNNEST(t.tags) AS tag;

SELECT
    t.order_id,
    tag_value
FROM ads_order_detail t
CROSS JOIN LATERAL UNNEST(t.tags) AS tag(tag_value);

SELECT
    t.order_id,
    tag_value
FROM ads_order_detail t
LEFT JOIN UNNEST(t.tags) AS tag(tag_value)
    ON TRUE;
