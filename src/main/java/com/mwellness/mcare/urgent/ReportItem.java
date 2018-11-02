package com.mwellness.mcare.urgent;

/**
 * Created by dev01 on 12/28/16.
 */
public class ReportItem {
    public String sessionId = "";
    public long dateCreatedLong = -1;
    public String pdfBase64 = "data:application/pdf;base64,";
    public String reportType = "";
    public String signedBy = "";
    public long signedAt = -1;
    public String patientName = "";
}
