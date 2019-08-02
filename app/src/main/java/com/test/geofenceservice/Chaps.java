
package com.test.geofenceservice;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Chaps {

    @SerializedName("count")
    @Expose
    private Integer count;
    @SerializedName("company_logo")
    @Expose
    private Object companyLogo;
    @SerializedName("chaps")
    @Expose
    private List<Chap> chaps = null;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Object getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(Object companyLogo) {
        this.companyLogo = companyLogo;
    }

    public List<Chap> getChaps() {
        return chaps;
    }

    public void setChaps(List<Chap> chaps) {
        this.chaps = chaps;
    }

}
