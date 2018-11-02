package com.mwellness.mcare.controllers;

import com.mwellness.mcare.AMainApp;
import com.mwellness.mcare.auth0.Auth0ApiAudiences;
import com.mwellness.mcare.auth0.JwtToken;
import com.mwellness.mcare.auth0.JwtTokenVerifier;
import com.mwellness.mcare.responses.StandardInvalidTokenResponse;
import com.mwellness.mcare.responses.StandardResponse;
import com.mwellness.mcare.responses.StandardSuccessResponse;
import com.mwellness.mcare.urgent.ReportItem;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Created by dev01 on 12/12/16.
 */

@RestController
public class ReportsController {

    private void log(String str) {
        AMainApp.log(ReportsController.class.getSimpleName() + ": " + str);
    }

    /**
     * Upload Urgent Report from Technician Gateway
     * @param request
     * @param httpSession
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/ecg/reports/urgent/upload", method = RequestMethod.POST)
    public @ResponseBody
    StandardResponse uploadUrgentReport(HttpServletRequest request, HttpSession httpSession) throws IOException {

        // verifyToken(request)
        // checkValidApiAccess(request)

        String sessionId = request.getParameter("sessionId");
        long dateCreated = System.currentTimeMillis();

        String pdfBase64Str = request.getParameter("pdfBase64Str");
        byte[] pdfRaw = Base64.getDecoder().decode(pdfBase64Str.getBytes(StandardCharsets.UTF_8));
        //String pdfBase64 = Base64.getEncoder().encodeToString(pdfRaw);

        log(pdfRaw.length + " bytes base64pdf raw");

        int processStage = 2;

        try {

            String sql = "insert into urgent_reports(session_id, date_created, process_stage, report_type, pdfBlob) values(?, ?, ?, ?, ?);";
            int r = AMainApp.getJdbcTemplate(AMainApp.DB_SOURCE_QCARE_ECG).update(sql, new Object[]{sessionId, dateCreated, processStage, "Urgent", pdfRaw});
            String response;
            if(r == 1) {
                response = "SUCCESS";
            }
            else {
                response = "FAILED";
            }

            return new StandardResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new StandardResponse(e);
        }
    }

    @CrossOrigin
    @RequestMapping(value = "/ecg/reports/apitest", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse testAccessApi(HttpServletRequest request, HttpServletResponse response, HttpSession httpSession) {

        String access_token = request.getParameter("access_token");
        if(access_token.isEmpty() || access_token == null) {
            log("Token is empty/null");
            return new StandardInvalidTokenResponse();
        }

        if(!JwtTokenVerifier.isApiAccessTokenValid(access_token, JwtToken.ALGO_HS256, Auth0ApiAudiences.AUTH0_AUD_ECG_UPLOAD_API, AMainApp.Auth0ClientId, AMainApp.Auth0ApiSigningSecret)) {
            return new StandardInvalidTokenResponse();
        }

        return new StandardSuccessResponse();
    }

    /**
     * Upload progressive report created from Technician Gateway
     * ToDo: Set CrossOrigin IP addresses, Validate & Verify Token
     * @param request
     * @param httpSession
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "/ecg/reports/progressive/upload", method = RequestMethod.POST)
    public @ResponseBody
    StandardResponse uploadProgressiveReport(HttpServletRequest request, HttpSession httpSession) throws IOException {


        // verifyToken(request);
        // checkValidApiAccess(request)

        String sessionId = request.getParameter("sessionId");
        long dateCreated = System.currentTimeMillis();

        String pdfBase64Str = request.getParameter("pdfBase64Str");
        byte[] pdfRaw = Base64.getDecoder().decode(pdfBase64Str.getBytes(StandardCharsets.UTF_8));
        //String pdfBase64 = Base64.getEncoder().encodeToString(pdfRaw);

        log(pdfRaw.length + " bytes base64pdf raw");

        int processStage = 2;

        try {

            String sql = "insert into urgent_reports(session_id, date_created, process_stage, report_type, pdfBlob) values(?, ?, ?, ?, ?);";
            int r = AMainApp.getJdbcTemplate(AMainApp.DB_SOURCE_QCARE_ECG).update(sql, new Object[]{sessionId, dateCreated, processStage, "Progress", pdfRaw});
            String response;
            if(r == 1) {
                response = "SUCCESS";
            }
            else {
                response = "FAILED";
            }

            return new StandardResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new StandardResponse(e);
        }
    }



    /**
     * Doctor Signs Report
     * @param request
     * @param httpSession
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/ecg/reports/sign", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse signReport(HttpServletRequest request, HttpSession httpSession) throws IOException {
        String sessionId = request.getParameter("sid");
        long dateCreated = Long.parseLong(request.getParameter("gendate"));

        String signedBy = request.getParameter("signedBy");

        try {

            String sql = "update urgent_reports set signedBy=?, signedAt=? where session_id=? and date_created=?;";
            int r = AMainApp.getJdbcTemplate(AMainApp.DB_SOURCE_QCARE_ECG).update(sql, new Object[]{signedBy, System.currentTimeMillis(), sessionId, dateCreated});
            String response;
            if(r == 1) {
                response = "SUCCESS";
            }
            else {
                response = "FAILED";
            }

            return new StandardResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new StandardResponse(e);
        }
    }


    /**
     * Gets Base64 PDF string for selection.
     * @param request
     * @param response
     * @param httpSession
     * @return
     * @throws SQLException
     */
    //@CrossOrigin
    @RequestMapping(value = "/ecg/reports/pdf", method = RequestMethod.GET)
    public @ResponseBody
    String getPdfBase64Report (HttpServletRequest request, HttpServletResponse response, HttpSession httpSession) throws SQLException {

        String sessionId = request.getParameter("sessionId");
        String dateCreated = request.getParameter("dateCreated");

        String reportSql = "select pdfBlob from urgent_reports where session_id=? and date_created=? order by date_created desc";
        log("Performing query: " + reportSql);
        List<Map<String,Object>> results = AMainApp.getJdbcTemplate(AMainApp.DB_SOURCE_QCARE_ECG).queryForList(reportSql, new Object[]{sessionId, dateCreated});

        String url = "data:application/pdf;base64,"  + Base64.getEncoder().encodeToString( (byte[]) results.get(0).get("pdfBlob") ); //(String) results.get(0).get("pdfBlob");

        //response.setHeader("Location", url);

        /*try {
            return new StandardResponse(results.get(0).get("pdfBlob"));
        } catch (Exception e) {
            e.printStackTrace();
            return new StandardResponse(e);
        }*/

        return url;

    }


    /**
     *
     * @param request
     * @param httpSession
     * @return
     * @throws SQLException
     */
    @RequestMapping(value = "/ecg/reports/urgent/all", method = RequestMethod.GET)
    public @ResponseBody
    StandardResponse listAllReports (HttpServletRequest request, HttpSession httpSession) throws SQLException {

        String reportSql = "select session_id, date_created, process_stage, report_type, signedBy, signedAt from urgent_reports order by date_created desc";
        log("Performing query: " + reportSql);
        List<Map<String,Object>> results = AMainApp.getJdbcTemplate(AMainApp.DB_SOURCE_QCARE_ECG).queryForList(reportSql);

        ArrayList<ReportItem> reportItemArr = new ArrayList<>();

        for(Map m: results) {

            String sessionId = (String) m.get("session_id");
            long reportCreated = (long) m.get("date_created");
            String reportType = (String) m.get("report_type");
            String signedBy = (String) (((m.get("signedBy")+"").equals("null"))?"-":m.get("signedBy"));
            long signedAt = (long) ((m.get("signedAt")+"").equals("null")?-1L:m.get("signedAt"));

            //String pdfBase64Str = Base64.getEncoder().encodeToString( (byte[]) m.get("pdfBlob") );


            ReportItem reportItem = new ReportItem();
            reportItem.sessionId = sessionId;
            reportItem.dateCreatedLong = reportCreated;
            reportItem.patientName = "";
            reportItem.reportType = reportType;
            reportItem.pdfBase64 = ""; //"data:application/pdf;base64," + pdfBase64Str;
            reportItem.signedAt = signedAt;
            reportItem.signedBy = signedBy;

            reportItemArr.add(reportItem);
        }


        try {
            return new StandardResponse(reportItemArr);
        } catch (Exception e) {
            e.printStackTrace();
            return new StandardResponse(e);
        }
    }




}
