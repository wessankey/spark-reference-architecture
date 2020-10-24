CREATE EXTERNAL SCHEMA delta_bronze
FROM data catalog 
DATABASE 'delta' 
iam_role '<IAM_ROLE>'
CREATE EXTERNAL DATABASE IF NOT EXISTS;


CREATE EXTERNAL TABLE delta_bronze.link_clicked_v1 
(
	topic       VARCHAR(50),
	timestamp   TIMESTAMP,
	eventId     VARCHAR(36),
	event       STRUCT<
                    common:STRUCT<
                        sessionId:VARCHAR(36), 
                        url:VARCHAR(250), 
                        browser:VARCHAR(20), 
                        userAgent:VARCHAR(500), 
                        ip:VARCHAR(20)>, 
                link_url:VARCHAR(250)>
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS
INPUTFORMAT 'org.apache.hadoop.hive.ql.io.SymlinkTextInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION 's3://<BUCKET>/<PREFIX>/link_clicked.v1/_symlink_format_manifest';


CREATE EXTERNAL TABLE delta_bronze.page_viewed_v1 
(
	topic       VARCHAR(50),
	timestamp   TIMESTAMP,
	eventId     VARCHAR(36),
	event       STRUCT<
                    common:STRUCT<
                        sessionId:VARCHAR(36), 
                        url:VARCHAR(250), 
                        browser:VARCHAR(20), 
                        userAgent:VARCHAR(500), 
                        ip:VARCHAR(20)>>
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS
INPUTFORMAT 'org.apache.hadoop.hive.ql.io.SymlinkTextInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION 's3://<BUCKET>/<PREFIX>/page_viewed.v1/_symlink_format_manifest';