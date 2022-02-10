package controller.form;

public class ModifyExpiryTimeForm {
    private String shareInfoId;
    private String expiryTime;

    public String getShareInfoId() {
        return shareInfoId;
    }

    public void setShareInfoId( String shareInfoId) {
        this.shareInfoId = shareInfoId;
    }

    public String getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;
    }
}
