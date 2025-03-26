package dev.krishna.rickmorty.utils

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class CurlLoggingInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val curlCommand = buildCurlCommand(request)

        android.util.Log.d("API_CURL", curlCommand)
        return chain.proceed(request)
    }

    private fun buildCurlCommand(request: okhttp3.Request): String {
        val curl = StringBuilder().apply {
            append("curl -X ")
            append(request.method)

            request.headers.forEach { header ->
                append(" -H \"${header.first}: ${header.second}\"")
            }

            request.body?.let { body ->
                val buffer = Buffer()
                body.writeTo(buffer)
                append(" -d '${buffer.readUtf8()}'")
            }

            append(" \"${request.url}\"")
        }
        return curl.toString()
    }
}