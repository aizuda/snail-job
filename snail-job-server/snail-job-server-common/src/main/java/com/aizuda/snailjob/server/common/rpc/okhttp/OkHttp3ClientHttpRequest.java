package com.aizuda.snailjob.server.common.rpc.okhttp;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.StreamingHttpOutputMessage.Body;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

class OkHttp3ClientHttpRequest extends AbstractClientHttpRequest {

	private final OkHttpClient client;

	private final URI uri;

	private final HttpMethod method;
	@Nullable
	private Body body;

	@Nullable
	private FastByteArrayOutputStream bodyStream;

	public OkHttp3ClientHttpRequest(OkHttpClient client, URI uri, HttpMethod method) {
		this.client = client;
		this.uri = uri;
		this.method = method;
	}


	@Override
	public HttpMethod getMethod() {
		return this.method;
	}

	@Override
	public URI getURI() {
		return this.uri;
	}

	@Override
	protected OutputStream getBodyInternal(final HttpHeaders headers) throws IOException {
		Assert.state(this.body == null, "Invoke either getBody or setBody; not both");

		if (this.bodyStream == null) {
			this.bodyStream = new FastByteArrayOutputStream(1024);
		}
		return this.bodyStream;
	}

	@NotNull
    @Override
	protected ClientHttpResponse executeInternal(final HttpHeaders headers) throws IOException {

		if (this.body == null && this.bodyStream != null) {
			this.body = outputStream -> this.bodyStream.writeTo(outputStream);
		}

		RequestBody requestBody;
		if (body != null) {
			requestBody = new BodyRequestBody(headers, body);
		}
		else if (okhttp3.internal.http.HttpMethod.requiresRequestBody(getMethod().name())) {
			String header = headers.getFirst(HttpHeaders.CONTENT_TYPE);
			MediaType contentType = (header != null) ? MediaType.parse(header) : null;
			requestBody = RequestBody.create(contentType, new byte[0]);
		}
		else {
			requestBody = null;
		}
		Request.Builder builder = new Request.Builder()
			.url(this.uri.toURL());
		builder.method(this.method.name(), requestBody);
		headers.forEach((headerName, headerValues) -> {
			for (String headerValue : headerValues) {
				builder.addHeader(headerName, headerValue);
			}
		});
		Request request = builder.build();
		return new OkHttp3ClientHttpResponse(this.client.newCall(request).execute());
	}

	private static class BodyRequestBody extends RequestBody {

		private final HttpHeaders headers;

		private final Body body;


		public BodyRequestBody(HttpHeaders headers, Body body) {
			this.headers = headers;
			this.body = body;
		}

		@Override
		public long contentLength() {
			return this.headers.getContentLength();
		}

		@Nullable
		@Override
		public MediaType contentType() {
			String contentType = this.headers.getFirst(HttpHeaders.CONTENT_TYPE);
			if (StringUtils.hasText(contentType)) {
				return MediaType.parse(contentType);
			}
			else {
				return null;
			}
		}

		@Override
		public void writeTo(BufferedSink sink) throws IOException {
			this.body.writeTo(sink.outputStream());
		}

		@Override
		public boolean isOneShot() {
			return !this.body.repeatable();
		}
	}


}
