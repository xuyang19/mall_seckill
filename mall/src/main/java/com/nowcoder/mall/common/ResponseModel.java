package com.nowcoder.mall.common;

public class ResponseModel {

    // 定义业务状态常量
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAILURE = 1;
    // 业务状态
    private int status;

    // 业务数据
    private Object data;

    public ResponseModel() {
        this.status = STATUS_SUCCESS;
    }

    public ResponseModel(Object data) {
        this.status = STATUS_SUCCESS;
        this.data = data;
    }

    public ResponseModel(int status, Object data) {
        this.status = status;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseModel{" +
                "status=" + status +
                ", data=" + data +
                '}';
    }
}
