package com.coretronic.ccpclient.CCPUtils.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LatestOTAByDeviceGson {


    /**
     * product : {"gtin":"string","guid":"3fa85f64-5717-4562-b3fc-2c963f66afa6","name":"string","description":"string","hardwareId":0,"tenantId":0,"private":true,"creationTime":"2020-03-31T02:26:55.296Z","creator":"string","modificationTime":"2020-03-31T02:26:55.296Z","modifier":"string","deletionTime":"2020-03-31T02:26:55.296Z","deleter":"string","isDeleted":true,"id":0}
     * firmware : {"version":"string","title":"string","description":"string","name":"string","uri":"string","checksum":"string","packageSize":0,"fingerprint":"string"}
     * softwares : [{"version":"string","title":"string","description":"string","package":"string","packageFileName":"string","uri":"string","checksum":"string","packageSize":0}]
     */

    private ProductBean product;
    private FirmwareBean firmware;
    private List<SoftwaresBean> softwares;

    public ProductBean getProduct() {
        return product;
    }

    public void setProduct(ProductBean product) {
        this.product = product;
    }

    public FirmwareBean getFirmware() {
        return firmware;
    }

    public void setFirmware(FirmwareBean firmware) {
        this.firmware = firmware;
    }

    public List<SoftwaresBean> getSoftwares() {
        return softwares;
    }

    public void setSoftwares(List<SoftwaresBean> softwares) {
        this.softwares = softwares;
    }

    public static class ProductBean {
        /**
         * gtin : string
         * guid : 3fa85f64-5717-4562-b3fc-2c963f66afa6
         * name : string
         * description : string
         * hardwareId : 0
         * tenantId : 0
         * private : true
         * creationTime : 2020-03-31T02:26:55.296Z
         * creator : string
         * modificationTime : 2020-03-31T02:26:55.296Z
         * modifier : string
         * deletionTime : 2020-03-31T02:26:55.296Z
         * deleter : string
         * isDeleted : true
         * id : 0
         */

        private String gtin;
        private String guid;
        private String name;
        private String description;
        private int hardwareId;
        private int tenantId;
        @SerializedName("private")
        private boolean privateX;
        private String creationTime;
        private String creator;
        private String modificationTime;
        private String modifier;
        private String deletionTime;
        private String deleter;
        private boolean isDeleted;
        private int id;

        public String getGtin() {
            return gtin;
        }

        public void setGtin(String gtin) {
            this.gtin = gtin;
        }

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getHardwareId() {
            return hardwareId;
        }

        public void setHardwareId(int hardwareId) {
            this.hardwareId = hardwareId;
        }

        public int getTenantId() {
            return tenantId;
        }

        public void setTenantId(int tenantId) {
            this.tenantId = tenantId;
        }

        public boolean isPrivateX() {
            return privateX;
        }

        public void setPrivateX(boolean privateX) {
            this.privateX = privateX;
        }

        public String getCreationTime() {
            return creationTime;
        }

        public void setCreationTime(String creationTime) {
            this.creationTime = creationTime;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public String getModificationTime() {
            return modificationTime;
        }

        public void setModificationTime(String modificationTime) {
            this.modificationTime = modificationTime;
        }

        public String getModifier() {
            return modifier;
        }

        public void setModifier(String modifier) {
            this.modifier = modifier;
        }

        public String getDeletionTime() {
            return deletionTime;
        }

        public void setDeletionTime(String deletionTime) {
            this.deletionTime = deletionTime;
        }

        public String getDeleter() {
            return deleter;
        }

        public void setDeleter(String deleter) {
            this.deleter = deleter;
        }

        public boolean isIsDeleted() {
            return isDeleted;
        }

        public void setIsDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class FirmwareBean {
        /**
         * version : string
         * title : string
         * description : string
         * name : string
         * uri : string
         * checksum : string
         * packageSize : 0
         * fingerprint : string
         */

        private String version;
        private String title;
        private String description;
        private String name;
        private String uri;
        private String checksum;
        private int packageSize;
        private String fingerprint;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getChecksum() {
            return checksum;
        }

        public void setChecksum(String checksum) {
            this.checksum = checksum;
        }

        public int getPackageSize() {
            return packageSize;
        }

        public void setPackageSize(int packageSize) {
            this.packageSize = packageSize;
        }

        public String getFingerprint() {
            return fingerprint;
        }

        public void setFingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
        }
    }

    public static class SoftwaresBean {
        /**
         * version : string
         * title : string
         * description : string
         * package : string
         * packageFileName : string
         * uri : string
         * checksum : string
         * packageSize : 0
         */

        private String version;
        private String title;
        private String description;
        @SerializedName("package")
        private String packageName;
        private String packageFileName;
        private String uri;
        private String checksum;
        private int packageSize;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getPackageFileName() {
            return packageFileName;
        }

        public void setPackageFileName(String packageFileName) {
            this.packageFileName = packageFileName;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getChecksum() {
            return checksum;
        }

        public void setChecksum(String checksum) {
            this.checksum = checksum;
        }

        public int getPackageSize() {
            return packageSize;
        }

        public void setPackageSize(int packageSize) {
            this.packageSize = packageSize;
        }
    }
}
