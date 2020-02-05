
package com.coretronic.ccpclient.CCPUtils.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Software {

    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Title")
    @Expose
    private String title;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("Uri")
    @Expose
    private String uri;
    @SerializedName("Checksum")
    @Expose
    private String checksum;
    @SerializedName("PackageSize")
    @Expose
    private Double packageSize;
    @SerializedName("Version")
    @Expose
    private String version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Double getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(Double packageSize) {
        this.packageSize = packageSize;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
