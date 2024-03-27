package com.prem.ylan.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.DrmConfiguration
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashChunkSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.dash.DefaultDashChunkSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.trackselection.MappingTrackSelector.MappedTrackInfo
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.prem.ylan.R
import com.prem.ylan.model.PathManager.getPathInstance
import com.prem.ylan.player.helpers.CustomMethods.errorAlert
import com.prem.ylan.player.helpers.CustomMethods.getFileName
import com.prem.ylan.player.helpers.DoubleClickListener
import java.io.File
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@UnstableApi
class Player : AppCompatActivity() {
    private var playerView: PlayerView? = null
    private var bufferProgressbar: ProgressBar? = null
    private var exoPlayer: ExoPlayer? = null
    private var defaultTrackSelector: DefaultTrackSelector? = null
    private var videoQualities: ArrayList<String>? = null
    private var fileNameTV: TextView? = null
    private var brightVolumeTV: TextView? = null
    private var brightnessVolumeContainer: LinearLayout? = null
    private var volumeIcon: ImageView? = null
    private var brightnessIcon: ImageView? = null
    private var screenRotateBtn: ImageButton? = null
    private var qualitySelectionBtn: ImageButton? = null
    private var backButton: ImageButton? = null
    private var fitScreenBtn: ImageButton? = null
    private var subtitleBtn: ImageButton? = null
    private var backward10: ImageButton? = null
    private var forward10: ImageButton? = null
    private var doubleTapSkipBackwardIcon: Button? = null
    private var doubleTapSkipForwardIcon: Button? = null
    private var touchPositionX = 0
    private var gestureDetectorCompat: GestureDetectorCompat? = null
    private var brightness = 0
    private var volume = 0
    private var audioManager: AudioManager? = null
    private val SHOW_MAX_BRIGHTNESS = 100
    private val SHOW_MAX_VOLUME = 100
    private var shouldShowController = true
    private var selectedQualityIndex = 0
    private var drmScheme: UUID? = null
    private var playWhenReady = true
    private var hasRetried = false
    private var playbackPosition = C.TIME_UNSET
    private val requestProperties = HashMap<String, String>()
    private var mediaFileUrlOrPath: String? = null
    private val drmLicenceUrl: String? = null
    private val refererValue: String? = null
    private val userAgent: String? = null
    private var isLocalFile = false
    private var mediaFileName: String? = ""

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        this.enableEdgeToEdge()
        super.onCreate(savedInstanceState)



//        ----------------------- Remove system bar -------------------
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            view.onApplyWindowInsets(windowInsets)
        }
//        -------------------------------------------------------------



        if (CookieHandler.getDefault() !== DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER)
        }
        setContentView(R.layout.activity_player)
        val intent = intent
        val path = intent.getStringExtra("path")
        mediaFileUrlOrPath =
            "http://" + getPathInstance().ipAddress + ":8080/stream/url?path=/" + path
        if (intent.hasExtra("mediaFileName")) mediaFileName = intent.getStringExtra("mediaFileName")
        if (intent.hasExtra("selectedDrmScheme")) {
            val selectedDrmScheme = intent.getIntExtra("selectedDrmScheme", 0)
            drmScheme = when (selectedDrmScheme) {
                0 -> {
                    C.WIDEVINE_UUID
                }
                1 -> {
                    C.PLAYREADY_UUID
                }
                2 -> {
                    C.CLEARKEY_UUID
                }
                else -> {
                    C.WIDEVINE_UUID
                }
            }
        }
        if (intent.hasExtra("isLocalFile")) {
            isLocalFile = intent.getBooleanExtra("isLocalFile", true)
        }
        initVars()
        initializePlayer()

//        ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        /* This block of codes set the current device volume and brightness to the video on startup */brightness =
            (currentScreenBrightness * 100).toInt()
        setVolumeVariable()

//        ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        screenRotateBtn!!.setOnClickListener { view: View? ->
            val currentOrientation = getResources().configuration.orientation
            if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }

//        ..........................................................................................
        qualitySelectionBtn!!.setOnClickListener { view: View? ->
            if (videoQualities != null) {
                if (!videoQualities!!.isEmpty()) {
                    getQualityChooserDialog(this, videoQualities!!)
                } else {
                    Toast.makeText(this, "No video quality found.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Wait until video start.", Toast.LENGTH_SHORT).show()
            }
        }
        //        ..........................................................................................
        fitScreenBtn!!.setOnClickListener { v: View? ->
            if (playerView!!.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                playerView!!.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
                fitScreenBtn!!.setImageResource(R.drawable.icon_crop)
                //                Toast.makeText(this, "ZOOM", Toast.LENGTH_SHORT).show();
            } else if (playerView!!.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
                playerView!!.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL)
                fitScreenBtn!!.setImageResource(R.drawable.icon_fit_screen)
                //                Toast.makeText(this, "FILL", Toast.LENGTH_SHORT).show();
            } else if (playerView!!.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_FILL) {
                playerView!!.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
                fitScreenBtn!!.setImageResource(R.drawable.icon_crop_free)
                //                Toast.makeText(this, "FIT", Toast.LENGTH_SHORT).show();
            }
        }

        //        ..........................................................................................
        backButton!!.setOnClickListener {
            if (exoPlayer != null) {
                exoPlayer!!.stop()
                exoPlayer!!.release()
            }
            onBackPressed()
        }
        //        ..........................................................................................
        backward10!!.setOnClickListener {
            exoPlayer!!.seekTo(
                exoPlayer!!.currentPosition - 10000
            )
        }
        forward10!!.setOnClickListener {
            exoPlayer!!.seekTo(
                exoPlayer!!.currentPosition + 10000
            )
        }
        //        ..........................................................................................
        playerView!!.setOnTouchListener { _: View?, motionEvent: MotionEvent ->
            gestureDetectorCompat!!.onTouchEvent(motionEvent)
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                brightnessVolumeContainer!!.visibility = View.GONE
                if (!shouldShowController) {
                    playerView!!.setUseController(false)
                    Handler().postDelayed({
                        shouldShowController = true
                        playerView!!.setUseController(true)
                    }, 500)
                }
            }
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                touchPositionX = motionEvent.x.toInt()
            }
            false
        }


        //        ..........................................................................................
        playerView!!.setOnClickListener(
            DoubleClickListener(500
            ) {
                playerView!!.setUseController(false)
                Handler()
                    .postDelayed({ playerView!!.setUseController(true) }, 500)
                val deviceWidth =
                    Resources.getSystem().displayMetrics.widthPixels
                if (touchPositionX < deviceWidth / 2) {
                    doubleTapSkipBackwardIcon!!.visibility = View.VISIBLE
                    Handler().postDelayed({
                        doubleTapSkipBackwardIcon!!.visibility = View.GONE
                    }, 500)
                    exoPlayer!!.seekTo(exoPlayer!!.currentPosition - 10000)
                } else {
                    doubleTapSkipForwardIcon!!.visibility = View.VISIBLE
                    Handler().postDelayed({
                        doubleTapSkipForwardIcon!!.visibility = View.GONE
                    }, 500)
                    exoPlayer!!.seekTo(exoPlayer!!.currentPosition + 10000)
                }
            }
        )
        //        ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //Gesture Detect Section//
//        ******************************************************************************************
        gestureDetectorCompat =
            GestureDetectorCompat(this, object : GestureDetector.OnGestureListener {
                override fun onDown(motionEvent: MotionEvent): Boolean {
                    return false
                }

                override fun onShowPress(motionEvent: MotionEvent) {}
                override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
                    return false
                }

                override fun onLongPress(motionEvent: MotionEvent) {}
                override fun onFling(
                    motionEvent: MotionEvent?,
                    motionEvent1: MotionEvent,
                    v: Float,
                    v1: Float
                ): Boolean {
                    return false
                }

                override fun onScroll(
                    motionEvent: MotionEvent?,
                    motionEvent1: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    val deviceWidth = Resources.getSystem().displayMetrics.widthPixels
                    if (abs(distanceY.toDouble()) > abs(distanceX.toDouble())) {
                        brightnessVolumeContainer!!.visibility = View.VISIBLE
                        shouldShowController = false
                        if (motionEvent!!.x < deviceWidth.toFloat() / 2) {
                            volumeIcon!!.setVisibility(View.GONE)
                            brightnessIcon!!.setVisibility(View.VISIBLE)
                            val increase = distanceY > 0
                            val newValue = if (increase) brightness + 1 else brightness - 1
                            if (newValue >= 0 && newValue <= SHOW_MAX_BRIGHTNESS) {
                                brightness = newValue
                            }
                            brightVolumeTV!!.text = brightness.toString()
                            setScreenBrightness(brightness)
                        } else {
                            if (audioManager != null) {
                                volumeIcon!!.setVisibility(View.VISIBLE)
                                brightnessIcon!!.setVisibility(View.GONE)
                                val increase = distanceY > 0
                                val newValue = if (increase) volume + 1 else volume - 1
                                if (newValue >= 0 && newValue <= SHOW_MAX_VOLUME) {
                                    volume = newValue
                                }
                                brightVolumeTV!!.text = volume.toString()
                                setVolume(volume)
                            }
                        }
                    }
                    return true
                }
            })

//        ******************************************************************************************
    }



    //______________________________________________________________________________________________
    private fun initializePlayer() {
        @SuppressLint("UnsafeOptInUsageError") val videoTrackSelectionFactory: ExoTrackSelection.Factory =
            AdaptiveTrackSelection.Factory()
        defaultTrackSelector = DefaultTrackSelector(this, videoTrackSelectionFactory)
        defaultTrackSelector!!.setParameters(
            defaultTrackSelector!!.parameters.buildUpon()
                .setPreferredTextLanguage("en")
                .build()
        )
        val renderersFactory = DefaultRenderersFactory(this)
            .forceEnableMediaCodecAsynchronousQueueing()
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
            )
            .build()

        //*********************************************************************************

        exoPlayer = ExoPlayer.Builder(this, renderersFactory)
            .setTrackSelector(defaultTrackSelector!!)
            .setLoadControl(loadControl)
            .build()

        if (isLocalFile) {
            exoPlayer!!.setMediaItem(MediaItem.fromUri(Uri.fromFile(mediaFileUrlOrPath?.let {
                File(
                    it
                )
            })))
        } else {
            if (mediaFileUrlOrPath!!.lowercase(Locale.getDefault())
                    .contains(".m3u8") || mediaFileUrlOrPath!!.lowercase(
                    Locale.getDefault()
                ).contains(".ts") || mediaFileUrlOrPath!!.lowercase(Locale.getDefault())
                    .contains(".playlist")
            ) {
                val mediaSource: MediaSource =
                    buildHlsMediaSource(Uri.parse(mediaFileUrlOrPath), userAgent, drmLicenceUrl)
                exoPlayer!!.setMediaSource(mediaSource)
            } else if (mediaFileUrlOrPath!!.lowercase(Locale.getDefault()).contains(".mpd")) {
                val mediaSource: MediaSource =
                    buildDashMediaSource(Uri.parse(mediaFileUrlOrPath), userAgent, drmLicenceUrl)
                exoPlayer!!.setMediaSource(mediaSource, true)
            } else {
                Thread {
                    try {
                        val url = URL(mediaFileUrlOrPath)
                        val conn =
                            url.openConnection() as HttpURLConnection
                        conn.setRequestMethod("HEAD") // Use HEAD request to check content type
                        conn.setRequestProperty("User-Agent", userAgent)
                        conn.setRequestProperty("Referer", refererValue)

                        // Enable following redirects
                        conn.instanceFollowRedirects = true
                        if (conn.getResponseCode() == 200) {
                            val contentType = conn.contentType
                            if (contentType.equals("application/x-mpegURL", ignoreCase = true)
                                || contentType.equals(
                                    "application/vnd.apple.mpegurl",
                                    ignoreCase = true
                                )
                                || contentType.equals("video/mp2t", ignoreCase = true)
                            ) {
                                val mediaSource: MediaSource = buildHlsMediaSource(
                                    Uri.parse(mediaFileUrlOrPath),
                                    userAgent,
                                    drmLicenceUrl
                                )
                                Handler(Looper.getMainLooper()).post {
                                    exoPlayer!!.setMediaSource(
                                        mediaSource
                                    )
                                }
                            } else if (contentType.equals(
                                    "application/dash+xml",
                                    ignoreCase = true
                                )
                            ) {
                                val mediaSource: MediaSource = buildDashMediaSource(
                                    Uri.parse(mediaFileUrlOrPath),
                                    userAgent,
                                    drmLicenceUrl
                                )
                                Handler(Looper.getMainLooper()).post {
                                    exoPlayer!!.setMediaSource(
                                        mediaSource
                                    )
                                }
                            } else {
                                Handler(Looper.getMainLooper()).post {
                                    exoPlayer!!.setMediaItem(
                                        MediaItem.fromUri(
                                            Uri.parse(mediaFileUrlOrPath)
                                        )
                                    )
                                }
                            }
                        }
                        conn.disconnect()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Handler(Looper.getMainLooper()).post {
                            exoPlayer!!.setMediaItem(
                                MediaItem.fromUri(
                                    Uri.parse(mediaFileUrlOrPath)
                                )
                            )
                        }
                    }
                }.start()
            }
        }
        exoPlayer!!.prepare()
        exoPlayer!!.playWhenReady = playWhenReady
        if (playbackPosition != C.TIME_UNSET) {
            exoPlayer!!.seekTo(playbackPosition)
        }
        exoPlayer!!.addListener(object : Player.Listener {
            @SuppressLint("SetTextI18n")
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_BUFFERING) {
                    bufferProgressbar!!.visibility = View.VISIBLE
                }
                if (playbackState == Player.STATE_READY) {
                    playerView!!.setVisibility(View.VISIBLE)
                    bufferProgressbar!!.visibility = View.GONE
                    if (mediaFileName != "") {
                        fileNameTV!!.text = mediaFileName
                    } else {
                        fileNameTV!!.text = getFileName(mediaFileUrlOrPath)
                    }
                    videoQualities = videoQualitiesTracks
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                if (error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED && !hasRetried) {
                    val trackSelectionParameters = exoPlayer!!.trackSelectionParameters
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                        .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                        .build()
                    exoPlayer!!.trackSelectionParameters = trackSelectionParameters
                    exoPlayer!!.prepare()
                    Toast.makeText(
                        this@Player,
                        "Trying again because playback error.",
                        Toast.LENGTH_SHORT
                    ).show()
                    hasRetried = true
                } else {
                    playerView!!.setVisibility(View.GONE)
                    bufferProgressbar!!.visibility = View.GONE
                    exoPlayer!!.stop()
                    exoPlayer!!.release()
                    errorAlert(
                        this@Player, "Error", """
     ${error.message}
     ${error.errorCodeName}
     """.trimIndent(), "Ok", true
                    )
                }
            }
        })
        playerView!!.setPlayer(exoPlayer)
        playerView!!.setShowNextButton(false)
        playerView!!.setShowPreviousButton(false)
        playerView!!.setControllerShowTimeoutMs(2500)
    }

    //______________________________________________________________________________________________
    private fun buildDrmSessionManager(
        uuid: UUID?,
        userAgent: String?,
        drmLicenseUrl: String?
    ): DefaultDrmSessionManager {
        val licenseDataSourceFactory: HttpDataSource.Factory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent(userAgent)
            .setDefaultRequestProperties(requestProperties)
        val drmCallback = HttpMediaDrmCallback(drmLicenseUrl, true, licenseDataSourceFactory)
        return DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(uuid!!, FrameworkMediaDrm.DEFAULT_PROVIDER)
            .build(drmCallback)
    }

    //    ----------------------------------------------------------------------------------------------
    private fun buildDashMediaSource(
        uri: Uri,
        userAgent: String?,
        drmLicenseUrl: String?
    ): DashMediaSource {
        val defaultHttpDataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(requestProperties)
            .setTransferListener(
                DefaultBandwidthMeter.Builder(this@Player)
                    .setResetOnNetworkTypeChange(false)
                    .build()
            )
        val dashChunkSourceFactory: DashChunkSource.Factory =
            DefaultDashChunkSource.Factory(defaultHttpDataSourceFactory)
        val manifestDataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setDefaultRequestProperties(requestProperties)
            .setAllowCrossProtocolRedirects(true)
        return DashMediaSource.Factory(dashChunkSourceFactory, manifestDataSourceFactory)
            .createMediaSource(
                MediaItem.Builder()
                    .setUri(uri) // DRM Configuration
                    .setDrmConfiguration(
                        DrmConfiguration.Builder(drmScheme!!)
                            .setLicenseUri(Uri.parse(drmLicenseUrl))
                            .build()
                    )
                    .setMimeType(MimeTypes.APPLICATION_MPD)
                    .setTag(null)
                    .build()
            )
    }

    //    ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private fun buildHlsMediaSource(
        uri: Uri,
        userAgent: String?,
        drmLicenceUrl: String?
    ): HlsMediaSource {
        val drmSchemeUuid = Util.getDrmUuid(drmScheme.toString())
        val drmSessionManager: DrmSessionManager =
            buildDrmSessionManager(drmSchemeUuid, drmLicenceUrl, userAgent)
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
            this, DefaultHttpDataSource.Factory()
                .setUserAgent(userAgent)
                .setDefaultRequestProperties(requestProperties)
                .setAllowCrossProtocolRedirects(true)
        )
        return HlsMediaSource.Factory(dataSourceFactory)
            .setDrmSessionManagerProvider { mediaItem: MediaItem? -> drmSessionManager }
            .setAllowChunklessPreparation(true)
            .createMediaSource(
                MediaItem.Builder()
                    .setUri(uri)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()
            )
    }

    private val videoQualitiesTracks: ArrayList<String>
        //______________________________________________________________________________________________
        get() {
            val videoQualities = ArrayList<String>()
            val renderTrack = defaultTrackSelector!!.currentMappedTrackInfo!!
            val renderCount = renderTrack.rendererCount
            for (rendererIndex in 0 until renderCount) {
                if (isSupportedFormat(renderTrack, rendererIndex)) {
                    val trackGroupType = renderTrack.getRendererType(rendererIndex)
                    val trackGroups = renderTrack.getTrackGroups(rendererIndex)
                    val trackGroupsCount = trackGroups.length
                    if (trackGroupType == C.TRACK_TYPE_VIDEO) {
                        for (groupIndex in 0 until trackGroupsCount) {
                            val videoQualityTrackCount = trackGroups[groupIndex].length
                            for (trackIndex in 0 until videoQualityTrackCount) {
                                val isTrackSupported = renderTrack.getTrackSupport(
                                    rendererIndex,
                                    groupIndex,
                                    trackIndex
                                ) == C.FORMAT_HANDLED
                                if (isTrackSupported) {
                                    val track = trackGroups[groupIndex]
                                    val videoWidth = track.getFormat(trackIndex).width
                                    val videoHeight = track.getFormat(trackIndex).height
                                    val quality = videoWidth.toString() + "x" + videoHeight
                                    videoQualities.add(quality)
                                }
                            }
                        }
                    }
                }
            }
            return videoQualities
        }

    private fun isSupportedFormat(mappedTrackInfo: MappedTrackInfo?, rendererIndex: Int): Boolean {
        val trackGroupArray = mappedTrackInfo!!.getTrackGroups(rendererIndex)
        return if (trackGroupArray.length == 0) {
            false
        } else {
            mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO
        }
    }

    //______________________________________________________________________________________________
    private fun getQualityChooserDialog(context: Context, arrayList: ArrayList<String>) {
        val charSequences = arrayOfNulls<CharSequence>(arrayList.size + 1)
        charSequences[0] = "Auto"
        for (i in arrayList.indices) {
            charSequences[i + 1] = arrayList[i] //.split("x")[1] + "p";
        }
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle("Select video quality:")
        builder.setSingleChoiceItems(
            charSequences, selectedQualityIndex
        ) { dialogInterface: DialogInterface?, which: Int ->
            selectedQualityIndex = which
        }
        builder.setPositiveButton(
            "Ok"
        ) { dialogInterface: DialogInterface?, i: Int ->
            if (selectedQualityIndex == 0) {
                Toast.makeText(
                    context,
                    context.getText(R.string.app_name)
                        .toString() + " will choose video resolution automatically.",
                    Toast.LENGTH_SHORT
                ).show()
                defaultTrackSelector!!.setParameters(
                    defaultTrackSelector!!.buildUponParameters().setMaxVideoSizeSd()
                )
            } else {
                val videoQualityInfo =
                    arrayList[selectedQualityIndex - 1].split("x".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                Toast.makeText(
                    context,
                    "Video will be played with " + videoQualityInfo[1] + "p resolution.",
                    Toast.LENGTH_SHORT
                ).show()
                val videoWidth = videoQualityInfo[0].toInt()
                val videoHeight = videoQualityInfo[1].toInt()
                defaultTrackSelector!!.setParameters(
                    defaultTrackSelector!!
                        .buildUponParameters()
                        .setMaxVideoSize(videoWidth, videoHeight)
                        .setMinVideoSize(videoWidth, videoHeight)
                )
            }
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
        builder.show()
    }
    //______________________________________________________________________________________________
    /**
     * Save and Restore Playback State:
     * To maintain the playback state across different app states (minimized, restored),
     * you can save and restore the playback state. You can do this using the
     * onSaveInstanceState and onRestoreInstanceState methods. Here's an example:
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("playWhenReady", exoPlayer!!.playWhenReady)
        outState.putLong("playbackPosition", exoPlayer!!.currentPosition)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        playWhenReady = savedInstanceState.getBoolean("playWhenReady")
        playbackPosition = savedInstanceState.getLong("playbackPosition", C.TIME_UNSET)
    }

    //______________________________________________________________________________________________
    override fun onResume() {
        super.onResume()
        if (brightness > 0) {
            setScreenBrightness(brightness)
        }
        setVolume(volume)
    }

    override fun onPause() {
        super.onPause()
        exoPlayer!!.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer!!.release()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        exoPlayer!!.stop()
        exoPlayer!!.release()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            setVolumeVariable()
        }
        return super.onKeyUp(keyCode, event)
    }

    //______________________________________________________________________________________________
    private fun setScreenBrightness(brightness1: Int) {
        val d = 1.0f / SHOW_MAX_BRIGHTNESS
        val lp = window.attributes
        lp.screenBrightness = d * brightness1
        window.setAttributes(lp)
    }

    private val currentScreenBrightness: Float
        get() {
            // Get the current screen brightness value
            var currentBrightness = 0
            try {
                currentBrightness = Settings.System.getInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS
                )
            } catch (e: SettingNotFoundException) {
                e.printStackTrace()
            }

            // Get the maximum brightness value supported by the device's screen
            val maxBrightness =
                255 // Default value; you can get the actual maximum brightness using system APIs

            // Calculate the brightness value in the range [0, 1.0]
            var brightnessValue = currentBrightness.toFloat() / maxBrightness

            // Clamp the brightnessValue to the range [0, 1.0]
            brightnessValue =
                max(0.0, min(1.0, brightnessValue.toDouble())).toFloat()
            return brightnessValue
        }

    private fun setVolume(volume1: Int) {
        val maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val d = maxVolume * 1.0f / SHOW_MAX_VOLUME
        var newVolume = (d * volume1).toInt()
        if (newVolume > maxVolume) {
            newVolume = maxVolume
        }
        if (volume1 == SHOW_MAX_VOLUME && newVolume < maxVolume) {
            newVolume = maxVolume
        }
        audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
    }

    private fun setVolumeVariable() {
        volume =
            (audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC) * 1.0f / audioManager!!.getStreamMaxVolume(
                AudioManager.STREAM_MUSIC
            ) * SHOW_MAX_VOLUME).toInt()
        if (volume > SHOW_MAX_VOLUME) {
            volume = SHOW_MAX_VOLUME
        }
    }


    //______________________________________________________________________________________________
    private fun initVars() {
        playerView = findViewById(R.id.exo_player_view)
        bufferProgressbar = findViewById(R.id.buffer_progressbar)
        fileNameTV = findViewById(R.id.file_name_tv)
        qualitySelectionBtn = findViewById(R.id.quality_selection_btn)
        brightnessVolumeContainer = findViewById(R.id.brightness_volume_container)
        brightnessIcon = findViewById(R.id.brightness_icon)
        volumeIcon = findViewById(R.id.volume_icon)
        brightVolumeTV = findViewById(R.id.brightness_volume_tv)
        backButton = findViewById(R.id.back_button)
        fitScreenBtn = findViewById(R.id.fit_screen_btn)
        subtitleBtn = findViewById(androidx.media3.ui.R.id.exo_subtitle)
        doubleTapSkipBackwardIcon = findViewById(R.id.double_tap_skip_backward_icon)
        doubleTapSkipForwardIcon = findViewById(R.id.double_tab_skip_forward_icon)
        backward10 = findViewById(R.id.backward_10)
        forward10 = findViewById(R.id.forward_10)
        screenRotateBtn = findViewById(R.id.screen_rotate_btn)
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
    } //  ------------------------------------------------------------------------------------------------

    companion object {
        private var DEFAULT_COOKIE_MANAGER: CookieManager? = null

        init {
            DEFAULT_COOKIE_MANAGER = CookieManager()
            DEFAULT_COOKIE_MANAGER!!.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        }
    }
}