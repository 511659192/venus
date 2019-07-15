package com.sankuai.meituan.banma.thrift.mall.admin.dao;

import com.sankuai.meituan.banma.mall.common.domain.BmEquipmentActivityRecord;
import com.sankuai.meituan.banma.mall.common.domain.BmEquipmentActivityRecordExample;
import java.util.List;

public interface BmEquipmentActivityRecordMapper {
    int countByExample(BmEquipmentActivityRecordExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(BmEquipmentActivityRecord record);

    int insertSelective(BmEquipmentActivityRecord record);

    List<BmEquipmentActivityRecord> selectByExample(BmEquipmentActivityRecordExample example);

    BmEquipmentActivityRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(BmEquipmentActivityRecord record);

    int updateByPrimaryKey(BmEquipmentActivityRecord record);
}