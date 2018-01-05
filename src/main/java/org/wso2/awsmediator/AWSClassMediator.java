package org.wso2.awsmediator;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.synapse.commons.json.JsonUtil;
import org.json.JSONObject;
import org.json.XML;

import javax.xml.namespace.QName;
import java.nio.charset.Charset;

public class AWSClassMediator  extends AbstractMediator{
    //private static final Log log = LogFactory.getLog(AWSClassMediator.class);

    private String region = "";
    private String functionName = "";
    private String accessKey = "";
    private String secretKey = "";
    private int maxConnections = 1100;

    public AWSClassMediator () {}

    public boolean mediate (MessageContext mc) {
        JSONObject jsonBody = XML.toJSONObject(mc.getEnvelope().getBody().toString()).getJSONObject("soapenv:Body").getJSONObject("jsonObject");

        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        ClientConfiguration clientConfiguration = new ClientConfiguration();

        clientConfiguration.setMaxConnections(maxConnections);
        AWSLambdaClient awsLambdaClient = new AWSLambdaClient(awsCredentials, clientConfiguration);

        awsLambdaClient.setRegion(Region.getRegion(Regions.fromName(region)));
        InvokeRequest invokeRequest = new InvokeRequest();

        invokeRequest.setFunctionName(functionName);
        invokeRequest.setInvocationType(InvocationType.RequestResponse);

        invokeRequest.setPayload(jsonBody.toString());

        InvokeResult invokeResult = awsLambdaClient.invoke(invokeRequest);
        String s = new String(invokeResult.getPayload().array(), Charset.forName("UTF-8"));

        jsonBody.put("response", s.replaceAll("^\"|\"$", ""));
        String updatedPayload = jsonBody.toString();

        JsonUtil.newJsonPayload(((Axis2MessageContext) mc).getAxis2MessageContext(), updatedPayload, true, true);

        return true;
    }

    public String getType() {
        return null;
    }

    public void setTraceState(int traceState) {
        traceState = 0;
    }

    public int getTraceState() {
        return 0;
    }

    public void setRegion(String pRegion) {
        region = pRegion;
    }

    public void setFunctionName(String pFunctionName) {
        functionName = pFunctionName;
    }

    public void setAccessKey(String pAccessKey) {
        accessKey = pAccessKey;
    }

    public void setSecretKey(String pSecretKey) {
        secretKey = pSecretKey;
    }

    public void setMaxConnections(String pMaxConnections) {
        maxConnections = Integer.parseInt(pMaxConnections);
    }

    public String getRegion () {
        return region;
    }

    public String getFunctionName () {
        return functionName;
    }

    public String getAccessKey () {
        return accessKey;
    }

    public String getSecretKey () {
        return secretKey;
    }

    public int getMaxConnections() {
        return maxConnections;
    }
}
