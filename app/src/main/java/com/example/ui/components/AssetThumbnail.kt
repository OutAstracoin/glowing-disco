package com.example.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioCyanContainer
import com.example.ui.theme.StudioSurfaceSolid
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object AssetBitmapCache {
    private val cache = LruCache<String, ImageBitmap>(120)

    fun get(path: String): ImageBitmap? = cache.get(path)

    fun put(path: String, bmp: ImageBitmap) {
        cache.put(path, bmp)
    }

    suspend fun load(context: Context, assetPath: String): ImageBitmap? {
        get(assetPath)?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open(assetPath).use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()?.also { bmp ->
                put(assetPath, bmp)
            }
        }
    }
}

@Composable
fun AssetThumbnailImage(
    assetPath: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val bitmapState = produceState<ImageBitmap?>(
        initialValue = AssetBitmapCache.get(assetPath),
        key1 = assetPath
    ) {
        if (value == null) {
            value = AssetBitmapCache.load(context, assetPath)
        }
    }

    val bmp = bitmapState.value
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(StudioSurfaceSolid)
        )
    }
}

@Composable
fun StudioItemCard(
    assetPath: String,
    label: String?,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) StudioCyanContainer else StudioSurfaceSolid
        ),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) StudioCyan else StudioBorder),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AssetThumbnailImage(
                assetPath = assetPath,
                contentDescription = label ?: testTag,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            if (!label.isNullOrBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) StudioCyan else TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                )
            }
        }
    }
}
