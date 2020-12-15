package com.oracle.OrdsClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.Constants;
import com.oracle.Utilities;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import java.util.ArrayList;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import java.util.ArrayList;
import java.util.List;

public class OrdsClient {
//    private static String ORDS_PATH_CREATE_PDB = "_/db-api/stable/database/pdbs/";  http://l0398022.idst.ibaintern.de:8082/ords/deseap1/REST_PROXY_PDBMNGT/pdbmngt_user/clone/DROP_SERVICE
//    private static String ORDS_PATH_DROP_PDB = "_/db-api/stable/database/pdbs/##PDB_NAME##/?action=##ACTION##";
    private static String ORDS_PATH_CREATE_PDB = "http://l0398022.idst.ibaintern.de:8082/ords/deseap1/REST_PROXY_PDBMNGT/pdbmngt_user/clone/CREATE_SERVICE";
    private static String ORDS_PATH_DROP_PDB = "http://l0398022.idst.ibaintern.de:8082/ords/deseap1/REST_PROXY_PDBMNGT/pdbmngt_user/clone/DROP_SERVICE";
    private static final Logger log = LoggerFactory.getLogger(OrdsClient.class.getName());
    private static URI getUrl(String path) {
        return URI.create(Utilities.getEnv(Constants.Environment.ENV_ORDS_PROTOCOL)
                + "://" + Utilities.getEnv(Constants.Environment.ENV_ORDS_HOST)
                + ":" + Utilities.getEnv(Constants.Environment.ENV_ORDS_PORT)
                + "/ords/" + path);
        //return URI.create("https://hookb.in/Z2lyReDEWyC1MVqkbz3N");
    }

// time curl -i --basic --user deseap1_ords:maerija_74  -X POST -d '{"p_admpwd":"oracle","p_ziel":"oracledboperator"}' -H 'Content-Type: application/json' http://l0398022.idst.ibaintern.de:8082/ords/deseap1/REST_PROXY_PDBMNGT/pdbmngt_user/clone/CREATE_SERVICE
    public static void createPdb(KubernetesClient k8sClient, String namespace, PdbCreationRequestModel pdbDetail)
            throws IOException {
        Secret s = getOrdsCredentialSecret(k8sClient, namespace);
        final String ordsPassword = Utilities.decodeBase64(s.getData().get("password"));
        final String ordsUsername = Utilities.decodeBase64(s.getData().get("username"));

        ObjectMapper mapper = new ObjectMapper();
//        String bodyRequest = mapper.writeValueAsString(pdbDetail);
        StringEntity bodyRequest;
		
		// Add your data
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("p_admpwd", pdbDetail.getAdminPwd()));
		nameValuePairs.add(new BasicNameValuePair("p_ziel", pdbDetail.getPdb_name()));
		bodyRequest=new UrlEncodedFormEntity(nameValuePairs,"utf-8");		
		
        log.info("Returned username [" + ordsUsername + "]");
        log.info("Returned credentials [" + ordsPassword + "]");
        URI url = URI.create(ORDS_PATH_CREATE_PDB);
        log.info("Request JSON [" + EntityUtils.toString(bodyRequest,"utf-8") + "]");
        log.info("Calling ORDS service at [" + url.toString() + "]");

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost(url);
        request.setEntity(bodyRequest);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("Authorization", "Basic " + Utilities.encodeBase64(ordsUsername + ":" + ordsPassword));
        CloseableHttpResponse response = client.execute(request);
        log.info(EntityUtils.toString(response.getEntity()));
        client.close();
    }

// time curl -i --basic --user deseap1_ords:maerija_74  -X POST -d '{"p_service":"oracledboperator"}' -H 'Content-Type: application/json' http://l0398022.idst.ibaintern.de:8082/ords/deseap1/REST_PROXY_PDBMNGT/pdbmngt_user/clone/DROP_SERVICE
    public static void deletePdb(KubernetesClient k8sClient, String namespace, String pdbName)
            throws IOException {
        Secret s = getOrdsCredentialSecret(k8sClient, namespace);
        final String ordsPassword = Utilities.decodeBase64(s.getData().get("password"));
        final String ordsUsername = Utilities.decodeBase64(s.getData().get("username"));

        StringEntity bodyRequest;
		
		// Add your data
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("p_service", pdbName));
		bodyRequest=new UrlEncodedFormEntity(nameValuePairs);		

        log.info("Returned username [" + ordsUsername + "]");
        log.info("Returned credentials [" + ordsPassword + "]");
//        URI url = URI.create(ORDS_PATH_DROP_PDB.replaceAll("##PDB_NAME##", pdbName).replaceAll("##ACTION##", "INCLUDING"));
        URI url = URI.create(ORDS_PATH_DROP_PDB);
        log.info("Request JSON [" + EntityUtils.toString(bodyRequest,"utf-8") + "]");
        log.info("Calling ORDS service at [" + url.toString() + "]");

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost(url);
        request.setEntity(bodyRequest);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("Authorization", "Basic " + Utilities.encodeBase64(ordsUsername + ":" + ordsPassword));
        CloseableHttpResponse response = client.execute(request);
        log.info(EntityUtils.toString(response.getEntity()));
        client.close();        
    }

    private static Secret getOrdsCredentialSecret(KubernetesClient client, String namespace) {
        String secretName = Utilities.getEnv(Constants.Environment.ENV_ORDS_CREDENTIAL_SECRET_NAME);
        Secret s = client.secrets().inNamespace(namespace).withName(secretName).get();
        if (s == null ) {
            Utilities.errorAndExit("Unable to find secret [" + Utilities.getEnv(Constants.Environment.ENV_ORDS_CREDENTIAL_SECRET_NAME) + "] in namespace [" + namespace + "]");
        }
        return s;
    }

}
