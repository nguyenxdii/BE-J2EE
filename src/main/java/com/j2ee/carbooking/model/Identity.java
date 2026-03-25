package com.j2ee.carbooking.model;

import com.j2ee.carbooking.enums.VerifyStatus;

public class Identity {
    private String cccdFront;
    private String cccdBack;
    private String drivingLicense;
    private VerifyStatus verifyStatus = VerifyStatus.PENDING;
    private String rejectReason;

    public String getCccdFront() { return cccdFront; }
    public void setCccdFront(String cccdFront) { this.cccdFront = cccdFront; }
    public String getCccdBack() { return cccdBack; }
    public void setCccdBack(String cccdBack) { this.cccdBack = cccdBack; }
    public String getDrivingLicense() { return drivingLicense; }
    public void setDrivingLicense(String drivingLicense) { this.drivingLicense = drivingLicense; }
    public VerifyStatus getVerifyStatus() { return verifyStatus; }
    public void setVerifyStatus(VerifyStatus verifyStatus) { this.verifyStatus = verifyStatus; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}