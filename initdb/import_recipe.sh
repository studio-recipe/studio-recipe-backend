#!/bin/bash
set -e

echo "Waiting for MySQL to be ready..."
until mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SELECT 1;" > /dev/null 2>&1; do
  sleep 1
done

# 테이블이 없으면 생성
mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" <<'EOF'
CREATE TABLE IF NOT EXISTS recipes (
    rcp_sno     BIGINT NOT NULL AUTO_INCREMENT,
    rcp_ttl     VARCHAR(255),
    ckg_nm      VARCHAR(255),
    inq_cnt     INT,
    rcmm_cnt    INT,
    ckg_mth_acto_nm  VARCHAR(255),
    ckg_mtrl_acto_nm VARCHAR(255),
    ckg_knd_acto_nm  VARCHAR(255),
    ckg_mtrl_cn TEXT,
    ckg_inbun_nm     VARCHAR(255),
    ckg_dodf_nm      VARCHAR(255),
    ckg_time_nm      VARCHAR(255),
    first_reg_dt     DATETIME(6),
    rcp_img_url      VARCHAR(255),
    PRIMARY KEY (rcp_sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
EOF

COUNT=$(mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" \
  -se "SELECT COUNT(*) FROM recipes;" 2>/dev/null || echo "0")

if [ "$COUNT" -eq "0" ]; then
  echo "Loading recipes from CSV..."
  mysql --local-infile=1 -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" <<'EOF'
LOAD DATA LOCAL INFILE '/csv/recipe_data_241226.csv'
INTO TABLE recipes
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(rcp_sno, rcp_ttl, ckg_nm, @dummy, @dummy, inq_cnt, rcmm_cnt, @dummy,
 ckg_mth_acto_nm, @dummy, ckg_mtrl_acto_nm, ckg_knd_acto_nm,
 @dummy, ckg_mtrl_cn, ckg_inbun_nm, ckg_dodf_nm, ckg_time_nm,
 first_reg_dt, rcp_img_url);
EOF
  echo "Recipes loaded successfully."
else
  echo "Recipes already exist ($COUNT rows). Skipping."
fi