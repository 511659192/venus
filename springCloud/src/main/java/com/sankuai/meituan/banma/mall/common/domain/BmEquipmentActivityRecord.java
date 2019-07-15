package com.sankuai.meituan.banma.mall.common.domain;

public class BmEquipmentActivityRecord {
    private Integer id;

    private Integer activityId;

    private Byte operateType;

    private String operateDesc;

    private String operateDetail;

    private Integer ctime;

    private Integer utime;

    private Integer operateId;

    private String operateName;

    private Integer valid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public Byte getOperateType() {
        return operateType;
    }

    public void setOperateType(Byte operateType) {
        this.operateType = operateType;
    }

    public String getOperateDesc() {
        return operateDesc;
    }

    public void setOperateDesc(String operateDesc) {
        this.operateDesc = operateDesc == null ? null : operateDesc.trim();
    }

    public String getOperateDetail() {
        return operateDetail;
    }

    public void setOperateDetail(String operateDetail) {
        this.operateDetail = operateDetail == null ? null : operateDetail.trim();
    }

    public Integer getCtime() {
        return ctime;
    }

    public void setCtime(Integer ctime) {
        this.ctime = ctime;
    }

    public Integer getUtime() {
        return utime;
    }

    public void setUtime(Integer utime) {
        this.utime = utime;
    }

    public Integer getOperateId() {
        return operateId;
    }

    public void setOperateId(Integer operateId) {
        this.operateId = operateId;
    }

    public String getOperateName() {
        return operateName;
    }

    public void setOperateName(String operateName) {
        this.operateName = operateName == null ? null : operateName.trim();
    }

    public Integer getValid() {
        return valid;
    }

    public void setValid(Integer valid) {
        this.valid = valid;
    }
}