package net.lightbody.bmp.proxy.http;

import org.apache.http.Header;
import org.junit.Test;
import website.magyar.mitm.proxy.ProxyServer;
import website.magyar.mitm.proxy.help.AbstractComplexProxyTool;
import website.magyar.mitm.proxy.help.ContentEncoding;
import website.magyar.mitm.proxy.help.DefaultRequestInterceptor;
import website.magyar.mitm.proxy.help.DefaultResponseInterceptor;
import website.magyar.mitm.proxy.help.ResponseInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ContentEncodingWithProxyTest extends AbstractComplexProxyTool {

    @Override
    protected void setUp() {
        String stubUrl = "http://127.0.0.1:" + getStubServerPort() + "/stub";
        LOGGER.info("STUB URL used: {}", stubUrl);
        DefaultRequestInterceptor defaultRequestInterceptor = new DefaultRequestInterceptor(getRequestCount(), NEED_STUB_RESPONSE, stubUrl);
        DefaultResponseInterceptor defaultResponseInterceptor = new DefaultResponseInterceptor(getResponseCount());
        getProxyServer().addRequestInterceptor(defaultRequestInterceptor);
        getProxyServer().addResponseInterceptor(defaultResponseInterceptor);
        getProxyServer().setCaptureBinaryContent(true);
        getProxyServer().setCaptureContent(true);
    }

    private void checkEncoding(Header contentEncodingHeader, ContentEncoding contentEncoding) {
        if (contentEncoding == ContentEncoding.NONE) {
            assertNull(contentEncodingHeader);
        } else {
            assertTrue("Response is not encoded with:" + contentEncoding.getValue(),
                    contentEncodingHeader != null && contentEncodingHeader.getValue().contains(contentEncoding.getValue()));
        }
    }

    @Test
    public void testSimpleGetRequest_NoEncoding() throws Exception {
        ContentEncoding contentEncoding = ContentEncoding.NONE;
        ResponseInfo proxiedResponse = httpGetWithApacheClient(getWebHost(), NO_NEED_STUB_RESPONSE, true, false, contentEncoding);
        assertEquals(200, proxiedResponse.getStatusCode());
        assertEquals(SERVER_BACKEND, proxiedResponse.getBody());
        assertEquals(1, getResponseCount().get());
        assertEquals(1, getRequestCount().get());
        checkEncoding(proxiedResponse.getContentEncoding(), contentEncoding);
    }

    @Test
    public void testSimpleGetRequest_GzipEncoding() throws Exception {
        ContentEncoding contentEncoding = ContentEncoding.GZIP;
        ResponseInfo proxiedResponse = httpGetWithApacheClient(getWebHost(), NO_NEED_STUB_RESPONSE, true, false, contentEncoding);
        assertEquals(200, proxiedResponse.getStatusCode());
        assertEquals(SERVER_BACKEND, proxiedResponse.getBody());
        assertEquals(1, getResponseCount().get());
        assertEquals(1, getRequestCount().get());
        //gzipped response is decoded by proxy - no checkEncoding(proxiedResponse.getContentEncoding(), contentEncoding);
    }

    @Test
    public void testSimpleGetRequest_Deflate() throws Exception {
        ContentEncoding contentEncoding = ContentEncoding.DEFLATE;
        ResponseInfo proxiedResponse = httpGetWithApacheClient(getWebHost(), NO_NEED_STUB_RESPONSE, true, false, contentEncoding);
        assertEquals(200, proxiedResponse.getStatusCode());
        assertEquals(SERVER_BACKEND, proxiedResponse.getBody());
        assertEquals(1, getResponseCount().get());
        assertEquals(1, getRequestCount().get());
        //deflated response is decoded by proxy - no checkEncoding(proxiedResponse.getContentEncoding(), contentEncoding);
    }

    @Test
    public void testSimpleGetRequest_Brotli() throws Exception {
        ContentEncoding contentEncoding = ContentEncoding.BROTLI;
        ResponseInfo proxiedResponse = httpGetWithApacheClient(getWebHost(), NO_NEED_STUB_RESPONSE, true, false, contentEncoding);
        assertEquals(200, proxiedResponse.getStatusCode());
        assertEquals(SERVER_BACKEND, proxiedResponse.getBody());
        assertEquals(1, getResponseCount().get());
        assertEquals(1, getRequestCount().get());
        checkEncoding(proxiedResponse.getContentEncoding(), contentEncoding);
    }

    @Test
    public void testSimpleGetRequest_Any() throws Exception {
        ContentEncoding contentEncoding = ContentEncoding.ANY;
        ResponseInfo proxiedResponse = httpGetWithApacheClient(getWebHost(), NO_NEED_STUB_RESPONSE, true, false, contentEncoding);
        assertEquals(200, proxiedResponse.getStatusCode());
        assertEquals(SERVER_BACKEND, proxiedResponse.getBody());
        assertEquals(1, getResponseCount().get());
        assertEquals(1, getRequestCount().get());
        //received content encoding is not checked
    }

}