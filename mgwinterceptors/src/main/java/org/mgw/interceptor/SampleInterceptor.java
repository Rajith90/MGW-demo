package org.mgw.interceptor;

import org.apache.axiom.om.OMElement;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.jvm.values.api.BXML;
import org.json.JSONObject;
import org.wso2.micro.gateway.interceptor.Caller;
import org.wso2.micro.gateway.interceptor.Entity;
import org.wso2.micro.gateway.interceptor.Interceptor;
import org.wso2.micro.gateway.interceptor.InterceptorException;
import org.wso2.micro.gateway.interceptor.Request;
import org.wso2.micro.gateway.interceptor.Response;
import org.wso2.micro.gateway.interceptor.Utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLStreamException;

/**
 * Sample interceptor implementation.
 * sample curl command: curl -X POST "https://localhost:9095/petstore/v1/pet/1?test=play&max=2" -H "accept: application/xml" -H "Authorization:Bearer $TOKEN" -k -d "{'abc':'hello'}" -H "content-type:application/json" -H "test:sample"
 */
public class SampleInterceptor implements Interceptor {
    public static String value = "json";

    public boolean interceptRequest(Caller caller, Request request) {

        addHeader(request);
        Utils.addDataToContextAttributes("new-uuid", "3456-789");
        System.out.println(caller.getLocalAddress());
        System.out.println(caller.getRemoteAddress());
        String contentType = request.getHeader("content-type");

        getHeader(request);
        setHeader(request);
        getAllHeaders(request);
        getPath(request);
        getVerb(request);
        getVersion(request);
        getPathInfo(request);
        getUserAgent(request);
        value = getQueryParam(request);
        getQueryParamValues(request);
        getQueryparams(request);
        if ("application/json".equals(contentType))
            getJsonPayload(request);
        else if ("multipart/mixed".equals(contentType))
            getBodyParts(request);
        else if ("text/xml".equals(contentType))
            getXmlPayload(request);
        else if ("text/plain".equals(contentType))
            getTextPayload(request);
        else {
            getByteChannel(request);
        }
        return true;
    }

    public boolean interceptResponse(Caller caller, Response response) {

        System.out.println(Utils.getInvocationContextAttributes().get("new-uuid"));
        // The returned result will depend on the query parameter "expect".
        if ("json".equals(value))
            setJsonPayload(response);
        else if ("xml".equals(value))
            setXmlPayload(response);
        else if ("text".equals(value))
            setTextPayload(response);
        else if ("byte".equals(value))
            setBytePayload(response);
        else
            setBodyParts(response);
        return true;
    }

    public void getAllHeaders(Request request) {
        String[] headers = request.getHeaderNames();
        for (String heeader : headers) {
            System.out.println(heeader);
        }
    }

    public void getHeader(Request request) {
        System.out.println(request.getHeader("Content-type"));
    }

    public void setHeader(Request request) {
        request.setHeader("test", "value1");
    }

    public void getPath(Request request) {
        System.out.println(request.getRequestPath());
    }

    public void getVerb(Request request) {
        System.out.println(request.getRequestHttpMethod());
    }

    public void getVersion(Request request) {
        System.out.println(request.getRequestHttpVersion());
    }

    public void getPathInfo(Request request) {
        System.out.println(request.getPathInfo());
    }

    public void getUserAgent(Request request) {
        System.out.println(request.getUserAgent());
    }

    public void getJsonPayload(Request request) {
        try {
            System.out.println(request.getJsonPayload());
        } catch (InterceptorException e) {
            e.printStackTrace();
        }
    }

    public void getByteChannel(Request request) {
        ByteChannel io = null;
        InputStream in = null;
        try {
            io = request.getByteChannel();
            in = Channels.newInputStream(io);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            // StandardCharsets.UTF_8.name() > JDK 7
            System.out.println(result.toString("UTF-8"));
        } catch (InterceptorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDataFromByteChannel(ByteChannel io) {
        try {
            InputStream in = Channels.newInputStream(io);
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(
                    new InputStreamReader(in, Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
                return textBuilder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void getQueryparams(Request request) {
        System.out.println(request.getQueryParams());
    }

    public String getQueryParam(Request request) {
        String query = request.getQueryParamValue("expect");
        System.out.println(query);
        return query;
    }

    public void getQueryParamValues(Request request) {
        String[] params = request.getQueryParamValues("max");
        for (String param : params) {
            System.out.println(param);
        }
    }

    public void getXmlPayload(Request request) {
        try {
            System.out.println(request.getXmlPayload().toString());
        } catch (InterceptorException e) {
            e.printStackTrace();
        }
    }

    public void getTextPayload(Request request) {
        try {
            System.out.println(request.getTextPayload());
        } catch (InterceptorException e) {
            e.printStackTrace();
        }
    }

    public void setJsonPayload(Request request) {
        JSONObject jo = new JSONObject();
        jo.put("name", "jon doe");
        jo.put("age", "22");
        jo.put("city", "chicago");
        request.setJsonPayload(jo);
    }

    public void setJsonPayload(Response response) {
        JSONObject jo = new JSONObject();
        jo.put("name", "jon doe");
        jo.put("age", "22");
        jo.put("city", "chicago");
        response.setJsonPayload(jo);
    }

    public void setXmlPayload(Response response) {
        BXML bxml = null;
        try {
            OMElement omElement = XMLFactory.stringToOM("<abc>test</abc>");
            bxml = BValueCreator.createXMLItem(omElement);
            response.setXmlPayload(bxml);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

    }

    public void setTextPayload(Response response) {
        response.setTextPayload("hello world from MGW");
    }

    public void setBytePayload(Response response) {
        response.setBinaryPayload("hello world from MGW".getBytes());
    }

    public void setBodyParts(Response response) {
        Entity en1 = new Entity();
        en1.setJson(new JSONObject().put("hello", "world"));
        Entity en2 = new Entity();
        en2.setJson(new JSONObject().put("multipart", "data"));
        Entity[] entities = new Entity[] { en1, en2 };
        response.setBodyParts(entities, null);

    }

    public void addHeader(Request request) {
        request.addHeader("foo", "bar");
    }

    public void respondFromRequest(Caller caller, Request request) {
        Response response = new Response();
        JSONObject jo = new JSONObject();
        jo.put("name", "jon doe");
        jo.put("age", "22");
        jo.put("city", "chicago");
        response.setResponseCode(201);
        response.setJsonPayload(jo);
        caller.respond(response);

    }

    public void getBodyParts(Request request) {
        try {
            Entity[] entities = request.getBodyParts();
            if (entities != null) {
                for (Entity entity : entities) {
                    System.out.println(getDataFromByteChannel(entity.getByteChannel()));
                }
            }
        } catch (InterceptorException e) {
            e.printStackTrace();
        }
    }
}
