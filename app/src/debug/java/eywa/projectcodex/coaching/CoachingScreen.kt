package eywa.projectcodex.coaching

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import eywa.projectcodex.common.sharedUi.CodexButton
import eywa.projectcodex.common.sharedUi.CodexFloatingActionButton
import eywa.projectcodex.common.sharedUi.CodexIconInfo
import eywa.projectcodex.common.sharedUi.codexTheme.CodexTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToLong

@Composable
fun CoachingScreen(
        viewModel: CoachingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val listener = { it: CoachingIntent -> viewModel.handle(it) }
    Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                    .fillMaxSize()
                    .background(CodexTheme.colors.appBackground)
    ) {
        when {
            state.selectedImage == null -> CoachingScreenSelectVideo(listener)
            else -> CoachingScreen(state, viewModel.player, listener)
        }
    }
}

@Composable
fun CoachingScreenSelectVideo(
        listener: (CoachingIntent) -> Unit,
) {
    val picker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { listener(CoachingIntent.VideoSelected(it)) }
    )

    CodexButton(
            text = "Select Video",
            onClick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            },
    )
}

@Composable
fun CoachingScreen(
        state: CoachingState,
        player: Player,
        listener: (CoachingIntent) -> Unit,
) {
    var isPaused by remember { mutableStateOf(false) }
    var videoDuration by remember { mutableStateOf(0f) }
    var circleXPercentage by remember { mutableStateOf(0f) }
    var dragInProgress by remember { mutableStateOf(false) }

    val durationIsValid = videoDuration > 0L
    val runLoop = !durationIsValid || !isPaused || dragInProgress
    val videoTime = ((videoDuration * circleXPercentage) / 1000).roundToLong()

    LaunchedEffect(player, runLoop) {
        if (runLoop) {
            while (true) {
                if (!durationIsValid) {
                    videoDuration = player.contentDuration.toFloat()
                }
                else if (dragInProgress) {
                    player.seekTo((videoDuration * circleXPercentage).roundToLong())
                }
                else {
                    circleXPercentage = (player.contentPosition.toFloat() / videoDuration)
                }
                delay(200)
            }
        }
    }

    var lifecycle by remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> lifecycle = event }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
                factory = { context ->
                    PlayerView(context).also {
                        it.player = player
                        it.useController = false
                    }
                },
                update = {
                    player.contentPosition
                    when (lifecycle) {
                        Lifecycle.Event.ON_PAUSE -> {
                            it.onPause()
                            it.player?.pause()
                        }

                        Lifecycle.Event.ON_RESUME -> {
                            it.onResume()
                        }

                        else -> Unit
                    }
                },
                modifier = Modifier.weight(1f)
        )

        Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CodexFloatingActionButton(
                    icon = CodexIconInfo.VectorIcon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Play/Pause"
                    ),
                    onClick = {
                        if (!isPaused) {
                            player.pause()
                        }
                        else {
                            videoDuration = player.contentDuration.toFloat()
                            player.play()
                        }
                        isPaused = !isPaused
                    },
                    modifier = Modifier,
            )
            CodexFloatingActionButton(
                    icon = CodexIconInfo.VectorIcon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Complete"
                    ),
                    onClick = { },
                    modifier = Modifier,
            )
        }

        Text(text = "Time: ${videoTime / 60}:${videoTime % 60}")
        TimelineSlider(
                durationIsValid = durationIsValid,
                circleXPercentage = { circleXPercentage },
                seek = { player.seekTo((it * videoDuration).roundToLong()) },
                onUpdatePosition = { circleXPercentage = it },
                onUpdateDragInProgress = { dragInProgress = it },
                modifier = Modifier.padding(bottom = 10.dp)
        )
    }
}

@Composable
private fun TimelineSlider(
        durationIsValid: Boolean,
        circleXPercentage: () -> Float,
        seek: (Float) -> Unit,
        onUpdatePosition: (Float) -> Unit,
        onUpdateDragInProgress: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
) {
    var barWidth = 0f
    val distanceFromSide = 50f

    fun updateCircleXPercentage(value: Float) {
        val newValue = when {
            (value < distanceFromSide) -> 0f
            (value > distanceFromSide + barWidth) -> 1f
            else -> (value - distanceFromSide) / barWidth
        }
        seek(newValue)
        onUpdatePosition(newValue)
    }

    Canvas(
            modifier = modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .pointerInput(durationIsValid) {
                        if (durationIsValid) {
                            detectTapGestures { updateCircleXPercentage(it.x) }
                        }
                    }
                    .pointerInput(durationIsValid) {
                        if (durationIsValid) {
                            detectDragGestures(
                                    onDragStart = {
                                        onUpdateDragInProgress(true)
                                        updateCircleXPercentage(it.x)
                                    },
                                    onDrag = { change, dragAmount ->
                                        val move = dragAmount.x / barWidth
                                        onUpdatePosition((circleXPercentage() + move).coerceIn(0f, 1f))
                                        change.consume()
                                    },
                                    onDragEnd = { onUpdateDragInProgress(false) },
                                    onDragCancel = { onUpdateDragInProgress(false) },
                            )
                        }
                    }
    ) {
        val verticalCentre = size.height / 2
        barWidth = size.width - distanceFromSide
        val circleX = if (durationIsValid) distanceFromSide + barWidth * circleXPercentage() else distanceFromSide
        drawLine(
                color = Color.Red,
                start = Offset(distanceFromSide, verticalCentre),
                end = Offset(circleX, verticalCentre),
                cap = StrokeCap.Round,
                strokeWidth = 5f
        )
        drawLine(
                color = Color.Red.copy(alpha = 0.5f),
                start = Offset(circleX, verticalCentre),
                end = Offset(size.width - distanceFromSide, verticalCentre),
                cap = StrokeCap.Round,
                strokeWidth = 5f
        )
        drawCircle(
                color = Color.Red,
                radius = 20f.coerceAtMost(distanceFromSide / 2),
                center = Offset(circleX, verticalCentre),
        )
    }
}
