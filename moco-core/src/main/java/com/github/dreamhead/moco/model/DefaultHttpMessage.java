package com.github.dreamhead.moco.model;

import com.github.dreamhead.moco.HttpMessage;
import com.github.dreamhead.moco.HttpProtocolVersion;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import static com.github.dreamhead.moco.model.MessageContent.content;
import static com.github.dreamhead.moco.util.Maps.listValueToArray;
import static com.github.dreamhead.moco.util.Maps.simpleValueToArray;
import static com.google.common.collect.ImmutableMap.copyOf;

public abstract class DefaultHttpMessage implements HttpMessage {
    private final HttpProtocolVersion version;
    private final MessageContent content;
    private final ImmutableMap<String, String[]> headers;

    protected DefaultHttpMessage(final HttpProtocolVersion version,
                                 final MessageContent content,
                                 final ImmutableMap<String, String[]> headers) {
        this.version = version;
        this.content = content;
        this.headers = headers;
    }

    @Override
    public HttpProtocolVersion getVersion() {
        return this.version;
    }

    @Override
    public ImmutableMap<String, String[]> getHeaders() {
        return this.headers;
    }

    @Override
    public String getHeader(final String name) {
        if (!this.headers.containsKey(name)) {
            return null;
        }

        String[] values = this.headers.get(name);
        return values[0];
    }

    @Override
    public MessageContent getContent() {
        return this.content;
    }

    protected static abstract class Builder<T extends Builder> {
        private final Class<T> clazz;
        protected HttpProtocolVersion version;
        protected MessageContent content;
        protected ImmutableMap<String, String[]> headers;

        public Builder() {
            this.clazz = getRealClass();
        }

        @SuppressWarnings("unchecked")
        private Class<T> getRealClass() {
            return (Class<T>) (((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        }

        public T withVersion(final HttpProtocolVersion version) {
            this.version = version;
            return clazz.cast(this);
        }

        public T withTextContent(final String content) {
            this.content = content(content);
            return clazz.cast(this);
        }

        public T withContent(final MessageContent content) {
            this.content = content;
            return clazz.cast(this);
        }

        public T forHeaders(final Map<String, String> headers) {
            if (headers != null) {
                this.headers = simpleValueToArray(headers);
            }

            return clazz.cast(this);
        }

        public T withHeaders(final Map<String, ?> headers) {
            if (headers != null && !headers.isEmpty()) {
                this.headers = asHeaders(headers);
            }

            return clazz.cast(this);
        }

        @SuppressWarnings("unchecked")
        private ImmutableMap<String, String[]> asHeaders(final Map<String, ?> headers) {
            Object value = Iterables.getFirst(headers.entrySet(), null).getValue();
            if (value instanceof String) {
                return simpleValueToArray((Map<String, String>)headers);
            }

            if (value instanceof String[]) {
                return copyOf((Map<String, String[]>)headers);
            }

            if (value instanceof List) {
                return listValueToArray((Map<String, List<String>>) headers);
            }

            throw new IllegalArgumentException("Unknown header value type [" + value.getClass() + "]");
        }
    }
}
