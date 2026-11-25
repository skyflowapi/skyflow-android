package Skyflow.collect.client

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer

/**
 * Wraps a [RequestBody] and reports bytes as they are written to the socket, so the upload path can
 * surface progress. [onBytesWritten] is called with each chunk's size on the network thread.
 */
internal class CountingRequestBody(
    private val delegate: RequestBody,
    private val onBytesWritten: (Long) -> Unit,
) : RequestBody() {

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun writeTo(sink: BufferedSink) {
        val counting = object : ForwardingSink(sink) {
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                onBytesWritten(byteCount)
            }
        }
        val buffered = counting.buffer()
        delegate.writeTo(buffered)
        buffered.flush()
    }
}
