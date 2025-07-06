package com.aizuda.snailjob.server.common.rpc.okhttp;

import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;

class OkHttp3ClientHttpResponse implements ClientHttpResponse {

    private final Response response;

    @Nullable
    private volatile HttpHeaders headers;


    public OkHttp3ClientHttpResponse(Response response) {
        Assert.notNull(response, "Response must not be null");
        this.response = response;
    }


    @Override
    public HttpStatusCode getStatusCode() throws IOException {
        return HttpStatusCode.valueOf(this.response.code());
    }

    @Override
    public String getStatusText() {
        return this.response.message();
    }

    @Override
    public InputStream getBody() throws IOException {
        ResponseBody body = this.response.body();
        return (body != null ? body.byteStream() : InputStream.nullInputStream());
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = this.headers;
        if (headers == null) {
            headers = new HttpHeaders();
            for (String headerName : this.response.headers().names()) {
                for (String headerValue : this.response.headers(headerName)) {
                    headers.add(headerName, headerValue);
                }
            }
            this.headers = headers;
        }
        return headers;
    }

    @Override
    public void close() {
        ResponseBody body = this.response.body();
        if (body != null) {
            body.close();
        }
    }

}
