
<style>
    @media (min-width: 1200px) {
        .container{
            max-width: 1200px;
        }
    }

    .ui-dialog {
        box-shadow: 0px 0px 30px 5px #aaa;
    }
    .reportListTable td {
        padding:10px;
        width:900px;
        text-align:center;
    }
    .reportListTable th {
        text-align: center;
    }
    #reportList{
        height: 600px;
        overflow:auto;
    }
</style>



<script>
<%@ include file="../js/doctor_reports.js" %>
</script>


<h4>Patient Reports</h4>

<div class="jumbotron">
    <div  id="reportList">
    </div>
</div>

<div id="pdfDialog" title="Urgent Report"></div>