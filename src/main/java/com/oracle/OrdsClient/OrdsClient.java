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

public class OrdsClient {
    private static String ORDS_PATH_CREATE_PDB = "_/db-api/stable/database/pdbs/";
    private static String ORDS_PATH_DROP_PDB = "_/db-api/stable/database/pdbs/##PDB_NAME##/?action=##ACTION##";
    private static final Logger log = LoggerFactory.getLogger(OrdsClient.class.getName());
    private static URI getUrl(String path) {
        return URI.create(Utilities.getEnv(Constants.Environment.ENV_ORDS_PROTOCOL)
                + "://" + Utilities.getEnv(Constants.Environment.ENV_ORDS_HOST)
                + ":" + Utilities.getEnv(Constants.Environment.ENV_ORDS_PORT)
                + "/ords/" + path);
        //return URI.create("https://hookb.in/Z2lyReDEWyC1MVqkbz3N");
    }

    public static void createPdb(KubernetesClient k8sClient, String namespace, PdbCreationRequestModel pdbDetail)
            throws IOException {
        Secret s = getOrdsCredentialSecret(k8sClient, namespace);
        final String ordsPassword = Utilities.decodeBase64(s.getData().get("password"));
        final String ordsUsername = Utilities.decodeBase64(s.getData().get("username"));

        ObjectMapper mapper = new ObjectMapper();
        String bodyRequest = mapper.writeValueAsString(pdbDetail);
        log.info("Returned username [" + ordsUsername + "]");
        log.info("Returned credentials [" + ordsPassword + "]");
        URI url = getUrl(ORDS_PATH_CREATE_PDB);
        log.info("Request JSON [" + bodyRequest + "]");
        log.info("Calling ORDS service at [" + url.toString() + "]");

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(bodyRequest));
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        request.setHeader("Authorization", "Basic " + Utilities.encodeBase64(ordsUsername + ":" + ordsPassword));
        CloseableHttpResponse response = client.execute(request);
        log.info(EntityUtils.toString(response.getEntity()));
        client.close();
    }

    public static void deletePdb(KubernetesClient k8sClient, String namespace, String pdbName)
            throws IOException {
        Secret s = getOrdsCredentialSecret(k8sClient, namespace);
        final String ordsPassword = Utilities.decodeBase64(s.getData().get("password"));
        final String ordsUsername = Utilities.decodeBase64(s.getData().get("username"));

        log.info("Returned username [" + ordsUsername + "]");
        log.info("Returned credentials [" + ordsPassword + "]");
        URI url = getUrl(ORDS_PATH_DROP_PDB.replaceAll("##PDB_NAME##", pdbName).replaceAll("##ACTION##", "INCLUDING"));
        log.info("Calling ORDS service at [" + url.toString() + "]");

        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete request = new HttpDelete(url);
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
