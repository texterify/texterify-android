package com.texterify.android.ota

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.SystemClock
import android.util.Log
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.util.Locale

/**
 * Texterify adds support for OTA (Over-The-Air) translation with Texterify.
 *
 * You must call [Texterify.init] to initialize the component before you can use `[Texterify.wrapContext]
 * to add OTA support to your UI.
 *
 * For components not yet supported you can use [Texterify.Config.addTranscriberFactory] to add your own transcribers.
 *
 * @see Texterify.config
 * @see Transcriber
 */
public object Texterify {

    private lateinit var downloader: TexterifyDownloader
    private lateinit var locale: Locale
    private var isInitialized = false

    private lateinit var transcriptionManager: TranscriptionManager
    private val repository: TexterifyRepository = TexterifyRepository()

    public fun init(config: Config) {
        val start = SystemClock.elapsedRealtime()
        check(!isInitialized) { "Already initialized. Only call this once!" }
        isInitialized = true

        val downloadUrl = config.downloadUrl
        requireNotNull(downloadUrl) { "Config not set up correctly, could not find project." }

        downloader = TexterifyDownloader(config.context, downloadUrl)

        locale = config.context.resources.configuration.locale
        downloader.loadCached(locale, onLoaded = this::updateLocalization)
        downloader.download(locale, onLoaded = this::updateLocalization)

        val factories = readLibraryFactories(config)

        transcriptionManager = TranscriptionManager(
            TexterifyResources(repository, config.context.resources),
            config.transcribers + factories
        )

        val done = SystemClock.elapsedRealtime()
        val viewPump = ViewPump.builder()
            .addInterceptor(TexterifyUpdateTranslationsInterceptor(transcriptionManager))
            .build()
        ViewPump.init(viewPump)
        val done2 = SystemClock.elapsedRealtime()
        Log.d("TxtfyInit", "Init took ${done - start}ms, total ${done2 - start}ms")
    }

    /**
     * Refresh the cached localization data for the active locale. This will force a request
     */
    public fun refreshTranslations(onComplete: ((success: Boolean) -> Unit)? = null) {
        downloader.download(
            locale = locale,
            forceRefresh = true,
            onLoaded = this::updateLocalization,
            onComplete = onComplete,
        )
    }

    private fun readLibraryFactories(config: Config): List<Transcriber.Factory> {
        val meta = config.context.packageManager
            .getApplicationInfo(config.context.packageName, PackageManager.GET_META_DATA).metaData

        return meta.keySet()
            .filter { key -> meta.getInt(key, -1) == R.id.texterify_transcriber }
            .map { clazzName ->
                Log.d("TxtfyConfig", "Found $clazzName")
                Class.forName(clazzName).newInstance() as Transcriber.Factory
            }
    }

    private fun updateLocalization(translations: I18NData) {
        repository.updateTranslations(translations)
    }

    /**
     * Create a configuration to initialize Texterify.
     */
    public fun config(context: Context): Config = Config(context)

    public class Config internal constructor(public val context: Context) {

        private val _transcribers: MutableList<Transcriber.Factory> = mutableListOf()
        public val transcribers: List<Transcriber.Factory>
            get() = _transcribers

        /**
         * The combined url where we can download the latest translation data, or `null` if the
         * configuration is incomplete.
         */
        internal val downloadUrl: String?
            get() {
                return if (!host.isEmpty() && !projectId.isEmpty() && !exportConfigId.isEmpty()) {
                    "$host/api/v1/projects/$projectId/export_configs/$exportConfigId/release"
                } else {
                    null
                }
            }

        /**
         * The host url where Texterify is running. This must follow the form of `{{host}}/api/v1/...`.
         */
        public var host: String = ""
            private set

        /**
         * The project id of the Texterify project to export localization data from.
         */
        public var projectId: String = ""
            private set

        /**
         * The export configuration id of the Texterify project which to export. This should be the
         * same id that is used to generate the strings for the app.
         */
        public var exportConfigId: String = ""
            private set

        /**
         * The host url where Texterify is running. This must follow the form of `{{host}}/api/v1/...`.
         */
        public fun host(host: String): Config {
            this.host = host
            return this
        }

        /**
         * The project id of the Texterify project to export localization data from.
         */
        public fun projectId(id: String): Config {
            this.projectId = id
            return this
        }

        /**
         * The export configuration id of the Texterify project which to export. This should be the
         * same id that is used to generate the strings for the app.
         */
        public fun exportConfigId(id: String): Config {
            this.exportConfigId = id
            return this
        }

        /**
         * Add a transcriber factory to add OTA support for more views.
         * @see Transcriber
         * @see Transcriber.Factory
         */
        public fun addTranscriberFactory(factory: Transcriber.Factory): Config {
            _transcribers += factory
            return this
        }
    }

    /**
     * Wraps the context to add OTA support with Texterify. You must call [Texterify.init] before calling this method.
     */
    public fun wrapContext(baseContext: Context): Context {
        check(isInitialized) { "Not initialized. Call `Texterify.init(context)` from your Application." }
        return TexterifyContextWrapper(ViewPumpContextWrapper.wrap(baseContext), repository)
    }

    private class TexterifyContextWrapper(
        baseContext: Context,
        private val repository: TexterifyRepository
    ) : ContextWrapper(baseContext) {

        private var texterifyResources: Resources? = null

        override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
            // todo what does this break/override?
            // needed for getString to use the right resources, will break bottom nav clicks though
            return this
        }

        override fun getResources(): Resources {
            if (texterifyResources == null) {
                synchronized(this) {
                    if (texterifyResources == null) {
                        texterifyResources = TexterifyResources(repository, super.getResources())
                    }
                }
            }
            return texterifyResources!!
        }
    }
}
