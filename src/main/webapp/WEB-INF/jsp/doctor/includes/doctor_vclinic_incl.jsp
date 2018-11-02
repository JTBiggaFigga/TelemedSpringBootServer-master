<style>
    #mw-main-content {
        min-height:400px;
        width:100%;
    }
    #patientQueueTable {
        width: 820px;
        margin: 0px auto;
    }
    #patientQueueTable td, #patientQueueTable th {
        text-align: center;
    }

    .table-wrapper-2 {
        margin: 0px auto;
        display: block;
        max-height: 400px;
        overflow-y: auto;
        -ms-overflow-style: -ms-autohiding-scrollbar;
    }

    .opentok_iframe {
        width:570px;
        height:550px;
        border-style:none;
    }
    
    #evisit_table {
        width:100%;
        height:820px;
    }
    td {
        border-color:solid;
        border-color:red;
        border-width:2px;
    }
    #evisit_video_td {
        width: 600px;
    }

    #evisit_rightside_div {

        height: 818px;
        width: 1000px;
        overflow:auto;
        float: left;

        padding: 5px;
    }

    .old-data-bg {
        background-color:#ddd;
    }

    #evisit_rightside_div .card {
        height: 210px;
    }

    #evisit_rightside_div .card .btn-sm {
        position: absolute;
        bottom: 15px;
        right: 15px;
        width: 100px;
    }

    #trends_highcharts_div {
        width:100%;
        height:50%;
        text-align:center;
        box-shadow: 0px 0px 10px 2px #ccc;
        border-radius: 5px;
        padding: 10px;
        background-color:white;
    }

    #patient_location_td {
        box-shadow: 0px 0px 10px 2px #ccc;
        border-radius: 5px;
        padding: 10px;
        background-color:white;
    }


</style>

<script>
<%@ include file="../js/doctor_vclinic.js" %>
</script>

<div class="jumbotron" style="background-color:#f5f5f5;">
    <nav class="navbar navbar-expand-lg navbar-light grey lighten-3"
            style="box-shadow:none">
        <ul class="navbar-nav">
            <li class="nav-item">
                <a class="navbar-brand" href="#"><b>Virtual Clinic</b></a>
            </li>
            <li class="nav-item" id="vclinic-btn-open">
                <a class="nav-link" onclick="openClinic()">OPEN</a>
            </li>
            <li class="nav-item" id="vclinic-btn-closed">
                <a class="nav-link" onclick="closeClinic()">CLOSED</a>
            </li>
        </ul>
    </nav>

    <div id="mw-main-content">



    </div>

</div>





<!-- MODALS -->

<!-- Clinic Status Update Modal -->
<div class="modal fade" id="clinicStateModal" tabindex="-1"
     role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                Virtual Clinic
            </div>
            <div class="modal-body" id="clinicStateModalBody">

            </div>
        </div>
    </div>
</div>



<!-- Patient Current Location Modal -->
<div class="modal fade" id="patientLocationModal" tabindex="-1"
     role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                Patient Location
            </div>
            <div class="modal-body" id="clinicStateModalBody">
                <center>
                <img id="modal_patient_map_img" src="" width="300" />
                <br />
                <span id="modal_patient_address_span" style="line-height:50px;vertical-align:middle;"></span>
                <center>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-primary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>


<!-- Patient Photo Modal -->
<div class="modal" id="patientPhotoModal" tabindex="-1"
     role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog  modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="btn btn-outline-danger" data-dismiss="modal">Close</button>
                &nbsp;
                <b><big>Patient Photo</big></b>
            </div>
            <div class="modal-body" id="clinicStateModalBody">

                <center>
                <img id="modal_patient_photo_img" src="" width="750" />
                <br />
                <span id="modal_patient_photo_timestamp" style="line-height:50px;vertical-align:middle;"></span>
                <center>
            </div>

        </div>
    </div>
</div>



<!-- ALERTS -->

<!-- TEMPLATES -->

<!-- patient queue template -->
<template id="patientQueueTemplate">
    
    <div class="table-wrapper-2">
        <h4>Waiting Room</h4>
        <button class="btn-sm" onclick="seeNextPatient()">See Next Patient</button>
        <table class="table table-sm" id="patientQueueTable">
            <thead>
                <tr>
                    <th></th>
                    <th></th>
                </tr>
            </thead>
            <tbody>

            </tbody>
        </table>
    </div>
    
</template>


<!-- EVISIT TEMPLATE -->

<template id="eVisitTemplate">
    <br />
    <table id="evisit_table">
        <tr>
            <td align="right" valign="top" id="evisit_video_td">

                <h4>
                    E-Visit with 
                    <span id='evisit_pat_name'></span>
                    <img alt='' id='evisit_pat_img' src='/images/pleasewait.gif' width='50' height='50' style='border-radius:50%' />
                    <button class='btn-outline-primary btn-rounded waves-effect btn-sm' onclick='endEVisit()'>END EVISIT</button>
                </h4>
                <div style='text-align:center; display:block;' id='drchrono_patient_info_div'></div>
                <iframe  allow="geolocation; microphone; camera" id='opentok_iframe' class='opentok_iframe'  scrolling='no' src='/images/pleasewait.gif'></iframe>

            </td>


            <td valign="top" id="evisit_rightside_td">

                <div id="evisit_rightside_div" style="border: 0px solid #8f8;">


                    <table id="patient_location_td" style="border: 0px solid #f88;width:100%;height:10%" cellpadding="10"><tr>

                        <td id="curr_patient_map_td" height="100%" style="border: 0px solid #88f;vertical-align:middle;" valign="middle">
                            <img id="patient_map_img" src="/images/pleasewait.gif" width="50" height="50" />
                            &nbsp;
                            <span id="patient_address_span" style="line-height:50px;vertical-align:middle;"></span>
                        </td>
                        <td id="curr_patient_weather_td" height="100%" style="border: 0px solid #88f;" align="right">
                            Weather:&nbsp;
                            <img id="patient_weather_img" src="/images/pleasewait.gif" width="50" />
                            <span id="patient_weather_span" style="line-height:50px;vertical-align:middle;"></span>
                        </td>

                    </tr>
                    </table>


                    <br />

                    <table style="border: 0px solid #f88;width:100%;"><tr>

                        <td width="21%">
                            <!-- Sugar -->
                            <div class="card vitalcard sugarcard">
                                <div class="card-body" style="text-align:center">
                                    <h5 class="card-title"><b>Blood Sugar <i class="fas fa-tint"></i></b></h5>
                                    <big>
                                        <span id="sugar_latest_value">---</span> mg/dl
                                        <br /><br />
                                        <br />
                                    </big>
                                    <div id="sugar_latest_timestamp"></div>
                                    <br />
                                    <a href="javascript: showLast30DaysSugarGraph()" class="btn-outline-primary btn-sm">Trends</a>
                                </div>
                            </div>
                        </td>

                        <td width="21%">
                            <!-- SPO2/HR -->
                            <div class="card vitalcard spo2card">
                                <div class="card-body" style="text-align:center">
                                    <h5 class="card-title"><b>Blood Oxygen  <i class="fas fa-hand-point-right"></i></b></h5>
                                    <big>
                                        <span id="spo2_latest_value">---</span> %
                                        <br />
                                        <span id="hr_latest_value">---</span> bpm
                                        <br /><br />
                                    </big>

                                    <div id="spo2_latest_timestamp"></div>
                                    <br />
                                    <a href="javascript: showLast30DaysSpo2Graph()" class="btn-outline-primary btn-sm">Trends</a>
                                </div>
                            </div>
                        </td>


                        <td width="21%">
                            <!-- Blood Pressure -->
                            <div class="card vitalcard bpcard">
                                <div class="card-body" style="text-align:center">
                                    <h5 class="card-title"><b>Blood Pressure <i class="fas fa-chart-line"></i></b></h5>
                                    <big>
                                        <span id="bp_latest_value">---</span> mmHg
                                        <br />
                                        <br />
                                        <br />
                                    </big>
                                    <div id="bp_latest_timestamp"></div>
                                    <br />
                                    <a href="javascript: showLast30DaysBpGraph()" class="btn-outline-primary btn-sm">Trends</a>
                                </div>
                            </div>
                        </td>


                        <td width="21%">
                            <!-- Body Weight -->
                            <div class="card vitalcard weightcard">
                                <div class="card-body" style="text-align:center">
                                    <h5 class="card-title"><b>Body Weight <i class="fab fa-cloudscale"></i></b></h5>
                                    <big>
                                        <span id="wt_latest_value">---</span> lbs
                                        <br />
                                        (<span id="wt_kgs_latest_value"><img alt='' src='/images/pleasewait.gif' width='50' /></span> kg )
                                        <br /><br />
                                    </big>
                                    <div id="wt_latest_timestamp"></div>
                                    <br />
                                    <a href="javascript: showLast30DaysWtGraph()" class="btn-outline-primary btn-sm">Trends</a>
                                </div>
                            </div>
                        </td>


                        <tr>
                            <td colspan="10">
                                <br />
                                <a href="javascript: showEcgStripList()" class="btn-primary btn-lg">ECG List</a>
                                &nbsp;&nbsp;&nbsp;
                                <a href="javascript: showPhotoList()" class="btn-primary btn-lg">Photo List</a>
                            </td>
                        </tr>

                    </tr>

                    </table>



                    <br />
                    <div>
                        <div id="trends_highcharts_div">
                            <i>Trends: Click on any of the 'Trends' buttons above.</i>
                        </div>
                    </div>

                </div>
            </td>
        </tr>
    </table>
    
</template>


<script>
    getAndShowCurrentVClinicState();
</script>