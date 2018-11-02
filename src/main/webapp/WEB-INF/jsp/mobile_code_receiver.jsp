<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script>

    var code = "${code}";

    try {
        SleepAuth0Java.processAuthCode(code);
    }
    catch(e) {
    }


</script>