package com.oracle;

import com.oracle.OrdsClient.OrdsClient;
import com.oracle.OrdsClient.PdbCreationRequestModel;
import io.fabric8.kubernetes.api.model.*;

import io.radanalytics.operator.common.AbstractOperator;
import io.radanalytics.operator.common.Operator;
import io.radanalytics.types.OracleCdbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.logging.Level;
import okhttp3.OkHttpClient;



@Operator(forKind = OracleCdbService.class, prefix = "com.oracle")
public class DbOperator extends AbstractOperator<OracleCdbService> {

    private static final Logger log = LoggerFactory.getLogger(AbstractOperator.class.getName());
    private static String DEFAULT_USERNAME="admin";

    public DbOperator() {
        java.util.logging.Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    }


    protected void onAdd(OracleCdbService srv) {
        log.info("new service is to be created: {}", srv);
        String resName = srv.getName();
        String pdbName = Utilities.sanitizePDBName(resName);

        String pdbUsername = DEFAULT_USERNAME;
        String pdbPassword = Utilities.randomPassword();

        PdbCreationRequestModel request = new PdbCreationRequestModel();
        request.setAdminPwd(pdbPassword);
        request.setAdminUser(pdbUsername);

        if(Utilities.isEnvSet(Constants.Environment.ENV_DB_FILENAME_CONVERSION_PATTERN)) {
            String conversionPattern = Utilities.getEnv(Constants.Environment.ENV_DB_FILENAME_CONVERSION_PATTERN);
            conversionPattern = conversionPattern.replaceAll("##PDBNAME##", pdbName);
            request.setFileNameConversions(conversionPattern);
        }

        request.setPdb_name(pdbName);
        request.setTempSize(srv.getTempStorage().toString());
        request.setTotalSize(srv.getStorage().toString());
        log.info("pdb: " + pdbName + " tempsize: " + srv.getTempStorage() + " totalsize: " + srv.getStorage());

        try {
            OrdsClient.createPdb(client, namespace, request);

            Secret secret = new SecretBuilder()
                    .withNewMetadata()
                    .withName(resName)
                    .endMetadata()
                    .addToData("jdbcUrl", Utilities.encodeBase64(getJdbcUrl( pdbName )))
                    .addToData("username", Utilities.encodeBase64(pdbUsername))
                    .addToData("passwd", Utilities.encodeBase64(pdbPassword))
                    .build();

            client.secrets().inNamespace(namespace).create(secret);

            log.info("new service has been created: {}", srv);

        } catch (IOException e) {
            log.error("error", e);
        }
    }

    protected void onDelete(OracleCdbService srv) {
        log.info("existing example has been deleted: {}", srv);


        try {
            OrdsClient.deletePdb(client, namespace, Utilities.sanitizePDBName(srv.getName()));

            client.services().inNamespace(namespace).withName(srv.getName()).delete();
            client.secrets().inNamespace(namespace).withName(srv.getName()).delete();

        } catch (IOException e) {
            log.error("error", e);
        }
    }

    protected void onModify(OracleCdbService srv) {
        log.info("existing example has been modified: {}", srv);
    }

//time curl -i --basic --user deseap1_ords:maerija_74  -X POST -d '' -H 'Content-Type: application/json' http://l0398022.idst.ibaintern.de:8082/ords/deseap1/REST_PROXY_PDBMNGT/pdbmngt_user/clone/GET_HOST_PORT
// {"~ret":"(ADDRESS=(PROTOCOL=TCP)(HOST=l9783022.sdst.sbaintern.de)(PORT=57011))"}	
    private static String getJdbcUrl(String serviceName) {
        /*return "jdbc:oracle:thin:@(DESCRIPTION = (TRANSPORT_CONNECT_TIMEOUT=3)(CONNECT_TIMEOUT=120)(RETRY_COUNT=20)(RETRY_DELAY=3)(FAILOVER=ON)(ADDRESS_LIST=(ADDRESS=(PROTOCOL=tcp)(HOST="
				+ Utilities.getEnv(Constants.Environment.ENV_CONNECTION_MANAGER_SERVICE_NAME) +
				"))(PORT="
				+ Utilities.getEnv(Constants.Environment.ENV_CONNECTION_MANAGER_SERVICE_PORT) +
				"))))(CONNECT_DATA=(SERVICE_NAME="
				+ serviceName
				+ ")))";*/
        
        /* return  "jdbc:oracle:thin:@" + Utilities.getEnv(Constants.Environment.ENV_CONNECTION_MANAGER_SERVICE_NAME)
                + ":" + Utilities.getEnv(Constants.Environment.ENV_CONNECTION_MANAGER_SERVICE_PORT) + "/" + serviceName;
        */
        return "TBD: call ORDS for dtabase host and port information.";

    }


}