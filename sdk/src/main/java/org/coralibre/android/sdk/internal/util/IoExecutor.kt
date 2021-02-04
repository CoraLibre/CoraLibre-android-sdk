package org.coralibre.android.sdk.internal.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object IoExecutor : ExecutorService by Executors.newScheduledThreadPool(2)
