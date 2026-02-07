package org.tcshare.app.network.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResUpdateCheckBean {

    @SerializedName("Message")
    private String message;
    @SerializedName("Code")
    private String code;
    @SerializedName("Data")
    private List<DataBean> data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        private String version;
        private String versionName;
        private String downloadpath;
        private String remark;
        private String created_at;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public String getDownloadpath() {
            return downloadpath;
        }

        public void setDownloadpath(String downloadpath) {
            this.downloadpath = downloadpath;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }
    }
}
