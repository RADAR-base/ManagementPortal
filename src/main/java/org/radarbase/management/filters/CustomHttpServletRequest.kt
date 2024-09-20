package org.radarbase.management.filters

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

/**
 * Create a new instance with the given request and additional parameters.
 *
 * @param request the request
 * @param additionalParams the additional parameters
 */
class CustomHttpServletRequest(
    private val request: HttpServletRequest,
    private val additionalParams: Map<String, Array<String>>,
) : HttpServletRequestWrapper(request) {
    override fun getParameterMap(): Map<String, Array<String>> {
        val map = request.parameterMap
        val param: MutableMap<String, Array<String>> = HashMap()
        param.putAll(map)
        param.putAll(additionalParams)
        return param
    }
}
