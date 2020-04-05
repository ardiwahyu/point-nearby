package com.gitlab.pointnearby.utils

import io.sentry.Sentry
import io.sentry.event.BreadcrumbBuilder

class SentryErrorReport() {

    init {
        val sentryDsn = "https://edc2ef6b1c86420d8fb098e54656dbaa@sentry.io/5188538"
        Sentry.init(sentryDsn)
    }

    fun reportAction(message: String?) {
        Sentry.getContext().recordBreadcrumb(BreadcrumbBuilder().setMessage(message).build())
    }

    fun reportException(exception: Exception?) {
        Sentry.capture(exception)
    }

    fun reportException(message: String?) {
        Sentry.capture(message)
    }
}