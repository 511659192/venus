#!/usr/bin/env bash
SOURCE_PATH=/Users/yangmeng/work/projects/banma_service_mall_admin_client/src/main/java/com/sankuai/meituan/banma/thrift
for file in `find $SOURCE_PATH -type f` ; do
    targetFile=`echo $file | sed 's:banma_service_mall_admin_client:banma_service_mall_admin_server:g'`
    diff $file $targetFile
done
