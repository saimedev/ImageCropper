package com.saimedevs.compose.imagecropper.easycrop

import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.saimedevs.compose.imagecropper.easycrop.images.getDecodeParams
import com.saimedevs.compose.imagecropper.easycrop.utils.ViewMat
import com.saimedevs.compose.imagecropper.easycrop.utils.atOrigin
import com.saimedevs.compose.imagecropper.easycrop.utils.coerceAtMost
import com.saimedevs.compose.imagecropper.easycrop.utils.roundUp
import com.saimedevs.compose.imagecropper.easycrop.utils.times
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Creates an [ImageBitmap] using the parameters in [CropState].
 * If [maxSize] is not null, the result will be scaled down to match it.
 * Returns null if the image could not be created.
 */
public suspend fun CropState.createResult(
    maxSize: IntSize?
): ImageBitmap? = withContext(Dispatchers.Default) {
    runCatching { doCreateResult(maxSize) }
        .onFailure { it.printStackTrace() }
        .getOrNull()
}

private suspend fun CropState.doCreateResult(maxSize: IntSize?): ImageBitmap? {
    val finalSize = region.size
        .coerceAtMost(maxSize?.toSize())
        .roundUp()
    val result = ImageBitmap(finalSize.width, finalSize.height)
    val canvas = Canvas(result)
    val viewMat = ViewMat()
    viewMat.snapFit(region, finalSize.toSize().toRect())
    val imgMat = transform.asMatrix(src.size)
    val totalMat = imgMat * viewMat.matrix

    canvas.clipPath(shape.asPath(region.atOrigin()))
    canvas.concat(totalMat)
    val inParams = getDecodeParams(view = finalSize, img = src.size, totalMat)
        ?: return null
    val decoded = src.open(inParams) ?: return null
    val paint = Paint().apply { filterQuality = FilterQuality.High }
    canvas.drawImageRect(
        image = decoded.bmp, paint = paint,
        dstOffset = decoded.params.subset.topLeft,
        dstSize = decoded.params.subset.size,
    )
    return result
}
