package com.mwellness.mcare;

import com.google.gson.Gson;
import com.mwellness.mcare.auth0.Auth0MgmtToken;
import com.mwellness.mcare.ecg.analysis.AFibAnalyzer;
import com.mwellness.mcare.jdbcTemplates.MCareJdbcTemplate;
import com.mwellness.mcare.utils.ArrayScaler;
import com.mwellness.mcare.utils.GsonUtils;
import com.mwellness.mcare.utils.NativeUtils;
import com.mwellness.mcare.utils.ToByteArray;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

@SpringBootApplication
@ComponentScan(basePackages = {"com.mwellness.mcare", "com.auth0.web"})
@EnableAutoConfiguration
@PropertySources({
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:jdbc.properties"),
        @PropertySource("classpath:auth0.properties")
})
public class AMainApp  extends SpringBootServletInitializer {


    private static ApplicationContext xmlBeanContext;
    private static final Logger logger = LoggerFactory.getLogger(AMainApp.class);

    public static void log(String str) {
        logger.info(str);
    }

    public static final String Auth0Domain = "https://qubitmed.auth0.com/";
    public static final String Auth0ClientId = "DSXX4UCgvGf1t6WRWbJ0KBidx03IHRfe";
    //public static final String Auth0ClientSecret = "4K33i6E6_HKjIXe8yXpX4aXRtgu3LDIcKSUzdmh9C7WE-UE03O6a-kzJiGovZk3a";
    public static final String Auth0ApiSigningSecret = "WTWYVohAwqSZjFmTOlkpPAKC2pHO1nkv";


	/* // doesn't work ... complains for already loaded ...
	static {
	    System.load(AMainApp.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "libQCardio.so");
    }*/

    public static final Gson gson = new Gson();

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(AMainApp.class);
    }

    public static String echo(String echo) {
        return echo;
    }

    public static void main(final String[] args) {

        //boolean x = true; if(x) return;


        new Thread(() -> {
            try {
                log("Auth0Mgmt Token: " + Auth0MgmtToken.getAuth0MgmtToken());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


        String rootPathOfClasses = "/" + AMainApp.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        log("Root path: " + rootPathOfClasses);
        //xmlBeanContext = new FileSystemXmlApplicationContext(rootPathOfClasses + "beans.xml");
        xmlBeanContext = new ClassPathXmlApplicationContext("beans.xml");


        SpringApplication app = new SpringApplication(AMainApp.class);
        app.setRegisterShutdownHook(false);
        ConfigurableApplicationContext appContext = app.run(args);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log("SHUTDOWN HOOK REACHED! ... closing app!");
            appContext.close();
        }));




        // always load library after running SpringApplication.run()
        try {
            loadAlgoLibrary();
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            return;
        }

        try {
            loadAFibLibrary();
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            return;
        }



        log("Current Database Name: " + ((DriverManagerDataSource) xmlBeanContext.getBean("qcareDataSource")).getUrl());


        /*new Thread(() -> {
            try {
                log("Auth0Mgmt Token: " + Auth0MgmtToken.getAuth0MgmtToken());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();*/



        /*
        String testDataFolder = "/home/dev01/Projects/qubitlabs/aretas/code/SleepPortal/src/main/java/com/qubitmed/qcare/ecg/analysis/testdata/";
        try {

            //testAlgorithm(testDataFolder,"ecg_test_data.txt");
            insertEcgStripTestData(testDataFolder,"ecg_test_data.txt");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        */



        //testAFib();



        /*
        new Thread(new Runnable() {

            @Override
            public void run() {


                //VitalsRestController.generateSpo2Data();

                try {
                    Thread.sleep(5000);
                    testStripAnalysis();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }).start();
        */


    }


    private static void testAFib() {

        log("Testing AFib output");

        //long[] beatPosArr = new long[] {100,433,666,868,1005,1271,1488,1721,1878,2105,2271,2333,2664,2797,2937,3128,3337,3465,3716,3995,4240,4531,4726,4863,5086,5250,5437,5653,5797,6033,6202,6437,6714,6946,7283,7592,7754,7886,8157,8316,8544,8878,9117,9296,9435,9675,9954,10171,10500,10833,11084,11345,11656,11861,12021,12280,12530,12758,12994,13201,13544,13780,14018,14293,14593,14789,14929,15246,15438,15632,15932,16062,16309,16598,16728,16817,16933,17279,17404,17656,17926,18232,18438,18646,18915,19120,19261,19397,19747,19885,20105,20299,20457,20586,20713,20901,21038,21162,21283,21412};
        long[] beatPosArr = new long[] {107,258,377,541,663,787,914,1050,1168,1287,1663,1802,2041,2167,2476,2624,2764,2887,3126,3284,3402,3703,3838,4076,4401,4701,4847,4991,5223,5376,5617,5919,6229,6362,6620,6854,7038,7263,7424,7743,7920,8185,8463,8681,8992,9145,9411,9649,9980,10252,10409,10645,10917,11131,11387,11532,11652,11805,12165,12298,12687,12847,12982,13124,13388,13522,13675,13934,14063,14192,14333,14480,14697,14895,15080,15319,15467,15697,15943,16164,16294,16396,16516,16794,17024,17270,17395,17527,17670,17872,18054,18185,18326,18432,18663,18792,19101,19239,19631,19789,20061,20214,20526,20839,21064,21306,21456,21796,21958,22087,22231,22388,22620,22852,23022,23159,23287,23431,23562,23680,23965,24113,24399,24508,24733,24865,25135,25251,25462,25639,25754,25985,26430,26583,26799,27128,27463,27590,27822,27958,28102,28329,28513,28840,29137,29452,29586,29702,29829,30052,30307,30583,30885,31333,31569,31754,32150,32535,32732,32870,33150,33328,33543,33827,34074,34213,34383,34634,34766,34894,35247,35395,35671,35928,36199,36488,36749,36982,37222,37455,37768,38036,38300,38504,38821,39014,39215,39366,39626,39899,40095,40311,40519,40684,40918,41192,41472,41658,41948,42150,42295,42405,42535,42757,43013};
        AFibAnalyzer.detectAFib(beatPosArr);
    }

    private static void testStripAnalysis() throws IOException {

        File f = new File("/home/qubit/Projects/SleepPortalServerSide/SleepPortal/src/main/java/com/qubitmed/qcare/ecg/analysis/testdata/afibexample.csv");
        Scanner scanner = new Scanner(f);
        ArrayList<Double> dAL = new ArrayList<>();
        int MAX_INPUT_SAMPLE_COUNT = 360 * 60;
        int MAX_FINAL_SAMPLE_COUNT = 250 * 60;
        int i = 0;
        while(scanner.hasNextLine()) {

            if(i > MAX_INPUT_SAMPLE_COUNT) break;

            dAL.add(Double.parseDouble(scanner.nextLine().split(",")[1]));

            if(!scanner.hasNextLine()) {
                if(i != MAX_INPUT_SAMPLE_COUNT - 1) {
                    scanner = new Scanner(f);
                }
            }

            i++;
        }

        short[] chShorts = new short[dAL.size()];

        i = 0;
        for(double d: dAL) {
            chShorts[i] = (short) (d * 10000);
            i++;
        }

        // DOWNSAMPLING TO 250
        chShorts = ArrayScaler.scaleArray(chShorts, MAX_FINAL_SAMPLE_COUNT);


        final String ch1B64 = Base64.getEncoder().encodeToString(ToByteArray.fromShortArray(chShorts));

        log("=====================================");
        log(GsonUtils.getInstance().toJson(chShorts));
        log(ch1B64);
        log("=====================================");

        final String patientId = "google-oauth2|101010739141713793949";
        final long insertedAtMs = System.currentTimeMillis();


        OkHttpClient httpClient = new OkHttpClient().newBuilder().build();
        String URL = "http://localhost:3099/vitals/ecgstrip";
        RequestBody requestPostBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("ecgstripB64", ch1B64)
                .addFormDataPart("timestampMs", insertedAtMs + "")
                .addFormDataPart("patientId", patientId)
                .build();

        Request request = new Request.Builder().url(URL)
                .method("POST", RequestBody.create(null, new byte[0]))
                .post(requestPostBody)
                .build();

        Response response = httpClient.newCall(request).execute();
        log("Response: " + response.body().string());
    }



    private static void loadAlgoLibrary() throws UnsatisfiedLinkError {

        // load algo library
        String rootPathOfClasses = AMainApp.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String path =  rootPathOfClasses + "libQCardio.so";

        log("loading ... " + path);
        try {
            System.load(path);
        } catch (UnsatisfiedLinkError e) {
            log("Library not found in ... " + path);
            try {
                log("Looking in jar");
                rootPathOfClasses = "/";
                path =  rootPathOfClasses + "libQCardio.so";
                NativeUtils.loadLibraryFromJar(path);
                log("... FOUND!!!");
            } catch (IOException e1) {
                log("... NOT FOUND IN JAR TOO");
                e1.printStackTrace();
            }
        }
        //System.loadLibrary("libQCardio");
        log("loaded library ... libQCardio.so");
    }


    private static void loadAFibLibrary() throws UnsatisfiedLinkError {

        // load algo library
        String rootPathOfClasses = AMainApp.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String path =  rootPathOfClasses + "libQAFib.so";

        log("loading ... " + path);
        try {
            System.load(path);
        } catch (UnsatisfiedLinkError e) {
            log("Library not found in ... " + path);
            try {
                log("Looking in jar");
                rootPathOfClasses = "/";
                path =  rootPathOfClasses + "libQAFib.so";
                NativeUtils.loadLibraryFromJar(path);
                log("... FOUND!!!");
            } catch (IOException e1) {
                log("... NOT FOUND IN JAR TOO");
                e1.printStackTrace();
            }
        }
        log("loaded library ... libQAFib.so");
    }



    public static final String DB_SOURCE_QCARE_ECG = "qcare";
    public static JdbcTemplate getJdbcTemplate(final String dbSource) throws IllegalArgumentException {

        switch (dbSource) {
            case DB_SOURCE_QCARE_ECG:
                return ((MCareJdbcTemplate) xmlBeanContext.getBean("qcareJdbcTemplate"));
            default:
                return ((MCareJdbcTemplate) xmlBeanContext.getBean("qcareJdbcTemplate"));
        }
    }



    public static DataSource getDataSource(final String dbSource) throws IllegalArgumentException {
        return getJdbcTemplate(dbSource).getDataSource();
    }


    public static DataSourceTransactionManager getTransactionManager(final String dbSource) {
        switch (dbSource) {
            case DB_SOURCE_QCARE_ECG:
                return ((DataSourceTransactionManager) xmlBeanContext.getBean("qcareTransactionManager"));
            default:
                return ((DataSourceTransactionManager) xmlBeanContext.getBean("qcareTransactionManager"));
        }
    }



    // Set maxPostSize of embedded tomcat server to 10 megabytes (default is 2 MB, not large enough to support file uploads > 1.5 MB)
    @Bean
    EmbeddedServletContainerCustomizer containerCustomizer() throws Exception {
        return (ConfigurableEmbeddedServletContainer container) -> {
            if (container instanceof TomcatEmbeddedServletContainerFactory) {
                TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container;
                tomcat.addConnectorCustomizers(
                        (connector) -> {
                            connector.setMaxPostSize(30000000); // 10 MB
                        }
                );
            }
        };
    }




}
