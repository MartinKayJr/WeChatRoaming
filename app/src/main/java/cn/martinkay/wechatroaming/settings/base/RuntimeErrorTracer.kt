package cn.martinkay.wechatroaming.settings.base

interface RuntimeErrorTracer {
    val runtimeErrors: List<Throwable>
    fun traceError(e: Throwable)
}
