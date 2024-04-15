/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aizuda.snail.job.server.common.rpc.okhttp;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URI;

public class OkHttp3ClientHttpRequestFactory implements ClientHttpRequestFactory, DisposableBean {

	private OkHttpClient client;

	public OkHttp3ClientHttpRequestFactory(OkHttpClient client) {
		Assert.notNull(client, "OkHttpClient must not be null");
		this.client = client;
	}

	@Override
	public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) {
		return new OkHttp3ClientHttpRequest(this.client, uri, httpMethod);
	}

	@Override
	public void destroy() throws IOException {
		// Clean up the client if we created it in the constructor
		Cache cache = this.client.cache();
		if (cache != null) {
			cache.close();
		}
		this.client.dispatcher().executorService().shutdown();
		this.client.connectionPool().evictAll();
	}

}
