package com.plezha.achi.shared.data.network

import com.plezha.achi.shared.data.network.infrastructure.HttpResponse
import io.ktor.client.request.forms.FormPart
import io.ktor.client.request.forms.InputProvider
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.io.Buffer

/**
 * Creates a FormPart for multipart file uploads.
 */
fun createFormPart(
    imageBytes: ByteArray,
    imageFileName: String,
): FormPart<InputProvider> =
    FormPart(
        key = "image",
        value = InputProvider { 
            Buffer().apply { write(imageBytes) }
        },
        headers = Headers.build {
            append(HttpHeaders.ContentDisposition, "filename=$imageFileName")
        }
    )

/**
 * Checks if the HTTP response was successful, throws an exception otherwise.
 */
fun <T : Any> HttpResponse<T>.check() {
    if (!success) {
        throw Exception("Network error $response")
    }
}
