package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Light
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.i18n.StudioStrings
import com.example.model.PropCategory
import com.example.model.PropType
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCoral
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanContainer
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceSolid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

data class PropDragSession(
    val propType: PropType,
    val screenPosition: Offset
)

@Composable
fun PropsDrawer(
    strings: StudioStrings,
    onClose: () -> Unit,
    onTapProp: (PropType) -> Unit,
    onDragPropStart: (PropType, Offset) -> Unit,
    onDragPropMove: (Offset) -> Unit,
    onDragPropEnd: (PropType, Offset) -> Unit,
    onDragPropCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filterTab by remember { mutableIntStateOf(0) } // 0 = All, 1 = Furniture, 2 = Shapes

    val visibleProps = remember(filterTab) {
        when (filterTab) {
            1 -> PropType.entries.filter { it.category == PropCategory.FURNITURE }
            2 -> PropType.entries.filter { it.category == PropCategory.SHAPE }
            else -> PropType.entries.filter { it.category == PropCategory.FURNITURE } +
                PropType.entries.filter { it.category == PropCategory.SHAPE }
        }
    }

    Surface(
        color = StudioSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, StudioBorder),
        modifier = modifier
            .widthIn(max = 680.dp)
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .testTag("props_drawer")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Header row: title + category chips + close button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.propsDrawer,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = strings.dragToDropHint,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("close_props_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_close_menu),
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Category Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabs = listOf(
                    0 to strings.all,
                    1 to strings.furniture,
                    2 to strings.shapes
                )
                tabs.forEach { (idx, label) ->
                    val selected = filterTab == idx
                    OutlinedButton(
                        onClick = { filterTab = idx },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (selected) StudioCyan else StudioBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) StudioCyanContainer else StudioSurfaceSolid,
                            contentColor = if (selected) StudioCyan else TextPrimary
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("props_filter_${idx}")
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Horizontal Draggable Props Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("props_drawer_list")
            ) {
                items(visibleProps, key = { it.key }) { prop ->
                    DraggablePropTile(
                        prop = prop,
                        label = prop.label(strings),
                        onTap = { onTapProp(prop) },
                        onDragStart = { pos -> onDragPropStart(prop, pos) },
                        onDragMove = onDragPropMove,
                        onDragEnd = { pos -> onDragPropEnd(prop, pos) },
                        onDragCancel = onDragPropCancel
                    )
                }
            }
        }
    }
}

@Composable
private fun DraggablePropTile(
    prop: PropType,
    label: String,
    onTap: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    onDragCancel: () -> Unit
) {
    var rootPos by remember { mutableStateOf(Offset.Zero) }

    Card(
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceSolid),
        border = BorderStroke(1.dp, StudioBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(92.dp)
            .onGloballyPositioned { coords ->
                rootPos = coords.positionInRoot()
            }
            .testTag("prop_drawer_item_${prop.key}")
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(prop) {
                val slop = viewConfiguration.touchSlop
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startLocal = down.position
                    var draggingToScene = false
                    var cancelledOrScrolled = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            if (draggingToScene) {
                                onDragEnd(rootPos + change.position)
                            } else if (!cancelledOrScrolled) {
                                onTap()
                            }
                            break
                        }

                        val delta = change.position - startLocal
                        if (!draggingToScene) {
                            if (abs(delta.y) > slop || abs(delta.x) > slop) {
                                // Dragging upward lifts onto the 3D scene; horizontal drag scrolls LazyRow
                                if (delta.y < -slop * 0.5f && abs(delta.y) > abs(delta.x)) {
                                    draggingToScene = true
                                    change.consume()
                                    onDragStart(rootPos + change.position)
                                } else {
                                    cancelledOrScrolled = true
                                    break
                                }
                            }
                        } else {
                            change.consume()
                            onDragMove(rootPos + change.position)
                        }
                    }

                    if (draggingToScene) {
                        // Ensure no dangling drag state if pointer loop broke unexpectedly
                        val anyPressed = currentEvent.changes.any { it.pressed }
                        if (anyPressed) onDragCancel()
                    }
                }
            }
            .clickable(onClick = onTap)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(6.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioSurfaceElevated)
            ) {
                PropPreviewGraphic(prop = prop, contentDescription = label)
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = null,
                    tint = TextSecondary.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PropPreviewGraphic(
    prop: PropType,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    if (prop.assetPath.isNotEmpty()) {
        AssetThumbnailImage(
            assetPath = prop.assetPath,
            contentDescription = contentDescription,
            modifier = modifier.size(50.dp)
        )
    } else {
        val icon = furnitureIconFor(prop)
        val tint = Color(prop.defaultColor.r, prop.defaultColor.g, prop.defaultColor.b)
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier.size(30.dp)
        )
    }
}

@Composable
fun PropDragGhost(
    session: PropDragSession,
    isOverScene: Boolean,
    strings: StudioStrings,
    modifier: Modifier = Modifier
) {
    Surface(
        color = StudioSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(2.dp, if (isOverScene) StudioCyan else StudioCoral),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            PropPreviewGraphic(
                prop = session.propType,
                contentDescription = session.propType.label(strings),
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = session.propType.label(strings),
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Text(
                    text = if (isOverScene) strings.dropOnScene else strings.dragToDropHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverScene) StudioCyan else StudioCoral
                )
            }
        }
    }
}

private fun furnitureIconFor(prop: PropType): ImageVector = when (prop) {
    PropType.CHAIR -> Icons.Filled.Chair
    PropType.STOOL -> Icons.Filled.EventSeat
    PropType.TABLE -> Icons.Filled.TableRestaurant
    PropType.SOFA -> Icons.Filled.Weekend
    PropType.LAMP -> Icons.Filled.Light
    PropType.PEDESTAL -> Icons.Filled.Layers
    else -> Icons.Filled.Layers
}
