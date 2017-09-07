package example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapter;

public class DetailedInboundHttp2ToHttpAdapter extends InboundHttp2ToHttpAdapter {
    static final int maxContentLength = 10 * 1024 * 1024;

    DetailedInboundHttp2ToHttpAdapter(Http2Connection connection) {
        super(connection, maxContentLength, false, false);
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endOfStream) throws Http2Exception {
        super.onHeadersRead(ctx, streamId, headers, padding, endOfStream);

        final FullHttpResponse fullHttpResponse = HttpConversionUtil.toHttpResponse(streamId, headers, ByteBufAllocator.DEFAULT, false);

        final DefaultHttpResponse httpResponse = new DefaultHttpResponse(fullHttpResponse.protocolVersion(), fullHttpResponse.status());
        ctx.fireChannelRead(httpResponse);

        final HttpHeaders httpHeaders = fullHttpResponse.headers();
        ctx.fireChannelRead(httpHeaders);

        ctx.flush();
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endOfStream) throws Http2Exception {
        super.onHeadersRead(ctx, streamId, headers, streamDependency, weight, exclusive, padding, endOfStream);

        final FullHttpResponse fullHttpResponse = HttpConversionUtil.toHttpResponse(streamId, headers, ByteBufAllocator.DEFAULT, false);

        final DefaultHttpResponse httpResponse = new DefaultHttpResponse(fullHttpResponse.protocolVersion(), fullHttpResponse.status());
        ctx.fireChannelRead(httpResponse);

        final HttpHeaders httpHeaders = fullHttpResponse.headers();
        ctx.fireChannelRead(httpHeaders);

        ctx.flush();
    }

    @Override
    public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
        final int bytesProcessed = super.onDataRead(ctx, streamId, data, padding, endOfStream);

        final DefaultHttpContent httpContent = endOfStream ? new DefaultLastHttpContent(data.retain()) : new DefaultHttpContent(data.retain());
        ctx.fireChannelRead(httpContent);
        ctx.flush();

        return bytesProcessed;
    }
}
