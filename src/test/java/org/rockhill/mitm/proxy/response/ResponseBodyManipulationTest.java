package org.rockhill.mitm.proxy.response;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.rockhill.mitm.proxy.ProxyServer;
import org.rockhill.mitm.proxy.ResponseInterceptor;
import org.rockhill.mitm.proxy.help.AnsweringServerBase;
import org.rockhill.mitm.proxy.help.TestUtils;
import org.rockhill.mitm.proxy.http.MitmJavaProxyHttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * This test checks if the response body can be accessed and altered by the response interceptors.
 * Tests:
 * - No additional header - body untouched
 * - If header "A" added - reduce body size to 5 chars
 * - If header "B" added - duplicate the body text
 * - If header "C" added - replace it with a json message (changes content-type too)
 * - If header "D" added - tries to get the response as byte[], meanwhile the content is not altered
 * - If header "E" added - tries to get the response as byte[], meanwhile the content is altered (replaced with a json message)
 */
public class ResponseBodyManipulationTest extends AnsweringServerBase {
    protected static final String GET_REQUEST = "/anyUrl";
    private static final String REQ_JSON_BODY = "{ \"json\": \"simple text\" }";
    private HttpGet request;

    @Override
    protected void setUp() throws Exception {
        TestResponseInterceptor testResponseInterceptor = new TestResponseInterceptor();
        getProxyServer().addResponseInterceptor(testResponseInterceptor);
        ProxyServer.setResponseVolatile(true); //this is a must !!!
        //this is a must have body available via getBodyString + need string type of Content-Type header
        getProxyServer().setCaptureContent(true);
        //this is a must have body available via getBodyString in case of non-text contents - Base64 encoding is used + need Content-Type header
        getProxyServer().setCaptureBinaryContent(true);
        //note that if response has no Content-Type header then response body is available via getBodyBytes() method.
        request = new HttpGet(GET_REQUEST);
    }

    @Override
    protected void tearDown() {
    }

    @Override
    protected byte[] evaluateServerRequestResponse(HttpServletRequest request, HttpServletResponse response, String bodyString) {
        //nothing to do here
        return null;
    }

    @Test
    public void bodyInterceptedAndAccessibleButResponseIsNotVolatile() throws Exception {
        ProxyServer.setResponseVolatile(false); //interceptor shall not influence the response !
        request.addHeader("A", "A"); //this orders interceptor to alter the response
        CloseableHttpClient httpClient = TestUtils.buildHttpClient(true, getProxyPort());
        HttpResponse response = httpClient.execute(httpHost, request); //request is here
        String body = EntityUtils.toString(response.getEntity());
        httpClient.close();
        assertEquals("HTTP Response Status code is:" + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());
        assertNull(getLastException());
        //check that answer is not changed
        assertEquals(SERVER_BACKEND, body);
    }

    @Test
    public void noRequestBodyChange() throws Exception {
        CloseableHttpClient httpClient = TestUtils.buildHttpClient(true, getProxyPort());
        HttpResponse response = httpClient.execute(httpHost, request); //request is here
        String body = EntityUtils.toString(response.getEntity());
        httpClient.close();
        assertEquals("HTTP Response Status code is:" + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());
        assertNull(getLastException());
        //check that answer is not changed
        assertEquals(SERVER_BACKEND, body);
    }

    @Test
    public void noRequestBodyChangeSecure() throws Exception {
        CloseableHttpClient httpClient = TestUtils.buildHttpClient(true, getProxyPort());
        HttpResponse response = httpClient.execute(secureHost, request); //request is here
        String body = EntityUtils.toString(response.getEntity());
        httpClient.close();
        assertEquals("HTTPS Response Status code is:" + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());
        assertNull(getLastException());
        //check that answer is not changed
        assertEquals(SERVER_BACKEND, body);
    }

    @Test
    public void reduceTo5Chars() throws Exception {
        request.addHeader("A", "A");
        CloseableHttpClient httpClient = TestUtils.buildHttpClient(true, getProxyPort());
        HttpResponse response = httpClient.execute(httpHost, request); //request is here
        String body = EntityUtils.toString(response.getEntity());
        httpClient.close();
        assertEquals("HTTP Response Status code is:" + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());
        assertNull(getLastException());
        //check that answer is reduced to 5 chars length
        assertEquals(SERVER_BACKEND.substring(0, 5), body);
    }

    @Test
    public void reduceTo5CharsSecure() throws Exception {
        request.addHeader("A", "A");
        CloseableHttpClient httpClient = TestUtils.buildHttpClient(true, getProxyPort());
        HttpResponse response = httpClient.execute(secureHost, request); //request is here
        String body = EntityUtils.toString(response.getEntity());
        httpClient.close();
        assertEquals("HTTPS Response Status code is:" + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());
        assertNull(getLastException());
        //check that answer is reduced to 5 chars length
        assertEquals(SERVER_BACKEND.substring(0, 5), body);
    }

    @Test
    public void doubleBodySize() throws Exception {
        request.addHeader("B", "B");
        CloseableHttpClient httpClient = TestUtils.buildHttpClient(true, getProxyPort());
        HttpResponse response = httpClient.execute(httpHost, request); //request is here
        String body = EntityUtils.toString(response.getEntity());
        httpClient.close();
        assertEquals("HTTP Response Status code is:" + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());
        assertNull(getLastException());
        //check that answer is doubled
        assertEquals(SERVER_BACKEND + SERVER_BACKEND, body);
    }

    @Test
    public void doubleBodySizeSecure() throws Exception {
        request.addHeader("B", "B");
        CloseableHttpClient httpClient = TestUtils.buildHttpClient(true, getProxyPort());
        HttpResponse response = httpClient.execute(secureHost, request); //request is here
        String body = EntityUtils.toString(response.getEntity());
        httpClient.close();
        assertEquals("HTTPS Response Status code is:" + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());
        assertNull(getLastException());
        //check that answer is doubled
        assertEquals(SERVER_BACKEND + SERVER_BACKEND, body);
    }

    @Test
    public void replaceWithJson() throws Exception {
        request.addHeader("C", "C");
        CloseableHttpClient httpClient = TestUtils.buildHttpClient(true, getProxyPort());
        HttpResponse response = httpClient.execute(httpHost, request); //request is here
        String body = EntityUtils.toString(response.getEntity());
        httpClient.close();
        assertEquals("HTTP Response Status code is:" + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());
        assertNull(getLastException());
        //check that answer is a json string
        assertEquals(REQ_JSON_BODY, body);
        assertEquals("application/json", response.getEntity().getContentType().getValue());
    }

    @Test
    public void replaceWithJsonSecure() throws Exception {
        request.addHeader("C", "C");
        CloseableHttpClient httpClient = TestUtils.buildHttpClient(true, getProxyPort());
        HttpResponse response = httpClient.execute(secureHost, request); //request is here
        String body = EntityUtils.toString(response.getEntity());
        assertEquals("application/json", response.getEntity().getContentType().getValue());
        httpClient.close();
        assertEquals("HTTPS Response Status code is:" + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());
        assertNull(getLastException());
        //check that answer is a json string
        assertEquals(REQ_JSON_BODY, body);
    }

    @Test
    public void getResponseAsByteResponseNotAltered() throws Exception {
        request.addHeader("D", "D");
        CloseableHttpClient httpClient = TestUtils.buildHttpClient(true, getProxyPort());
        HttpResponse response = httpClient.execute(httpHost, request); //request is here
        String body = EntityUtils.toString(response.getEntity());
        httpClient.close();
        assertEquals("HTTP Response Status code is:" + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());
        assertNull(getLastException());
        //check that answer is not changed
        assertEquals(SERVER_BACKEND, body);
    }

    @Test
    public void getResponseAsByteResponseNotAlteredSecure() throws Exception {
        request.addHeader("D", "D");
        CloseableHttpClient httpClient = TestUtils.buildHttpClient(true, getProxyPort());
        HttpResponse response = httpClient.execute(secureHost, request); //request is here
        String body = EntityUtils.toString(response.getEntity());
        httpClient.close();
        assertEquals("HTTP Response Status code is:" + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());
        assertNull(getLastException());
        //check that answer is not changed
        assertEquals(SERVER_BACKEND, body);
    }

    @Test
    public void getResponseAsByteResponseIsAltered() throws Exception {
        request.addHeader("E", "E");
        CloseableHttpClient httpClient = TestUtils.buildHttpClient(true, getProxyPort());
        HttpResponse response = httpClient.execute(httpHost, request); //request is here
        String body = EntityUtils.toString(response.getEntity());
        httpClient.close();
        assertEquals("HTTP Response Status code is:" + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());
        assertNull(getLastException());
        //check that answer is a json string
        assertEquals(REQ_JSON_BODY, body);
        assertEquals("application/json", response.getEntity().getContentType().getValue());
    }

    @Test
    public void getResponseAsByteResponseIsAlteredSecure() throws Exception {
        request.addHeader("E", "E");
        CloseableHttpClient httpClient = TestUtils.buildHttpClient(true, getProxyPort());
        HttpResponse response = httpClient.execute(secureHost, request); //request is here
        String body = EntityUtils.toString(response.getEntity());
        httpClient.close();
        assertEquals("HTTP Response Status code is:" + response.getStatusLine().getStatusCode(), 200, response.getStatusLine().getStatusCode());
        assertNull(getLastException());
        //check that answer is a json string
        assertEquals(REQ_JSON_BODY, body);
        assertEquals("application/json", response.getEntity().getContentType().getValue());
    }

    class TestResponseInterceptor implements ResponseInterceptor {

        @Override
        public void process(MitmJavaProxyHttpResponse response) {
            String body = response.getBodyString();// getBody works only in case the response content type is a kind of text

            byte[] newBody = null;
            Header[] requestHeaders = response.getRequestHeaders();

            assertEquals("Cannot find the expected body", SERVER_BACKEND, body);

            //alter body - if 'A' header - to 5 char long
            if (response.findHeader(requestHeaders, "A") != null) {
                newBody = body.substring(0, 5).getBytes(StandardCharsets.UTF_8);
            }

            //alter body - if 'B' header - double the body
            if (response.findHeader(requestHeaders, "B") != null) {
                newBody = (body + body).getBytes(StandardCharsets.UTF_8);
            }

            //alter body - if 'C' header - use json request
            if (response.findHeader(requestHeaders, "C") != null) {
                newBody = REQ_JSON_BODY.getBytes(StandardCharsets.UTF_8);
                response.setContentType("application/json");
            }

            //don't alter body - if 'D' header - bug get body as byte[]
            if (response.findHeader(requestHeaders, "D") != null) {
                byte[] oldBody = response.getBodyBytes();
                String bodyString = new String(oldBody);
                assertEquals("Cannot find the expected body", SERVER_BACKEND, bodyString);
            }

            //alter body - if 'E' header - use json request + get raw body too
            if (response.findHeader(requestHeaders, "E") != null) {

                byte[] oldBody = response.getBodyBytes();
                String bodyString = new String(oldBody);
                assertEquals("Cannot find the expected body", SERVER_BACKEND, bodyString);
                newBody = REQ_JSON_BODY.getBytes(StandardCharsets.UTF_8);
                response.setContentType("application/json");
            }

            try {
                response.setBody(newBody);
            } catch (IOException e) {
                setLastException(e);
            }
        }
    }
}