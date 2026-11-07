package com.purecanvas.app.data

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.graphics.ImageDecoder
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.IOException

class ImageRepository(private val context: Context) {
    fun load(uri: Uri): Bitmap {
        val resolver=context.contentResolver
        val bounds=BitmapFactory.Options().apply { inJustDecodeBounds=true }
        val boundsStream = resolver.openInputStream(uri) ?: throw IOException("Unable to open image")
        boundsStream.use { BitmapFactory.decodeStream(it, null, bounds) }
        require(bounds.outWidth>0 && bounds.outHeight>0) { "Unsupported or invalid image" }
        val maxSide=5000
        var sample=1; while(bounds.outWidth/sample>maxSide || bounds.outHeight/sample>maxSide) sample*=2
        val opts=BitmapFactory.Options().apply { inSampleSize=sample; inPreferredConfig=Bitmap.Config.ARGB_8888 }
        val bitmap=resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it,null,opts) } ?: throw IOException("Unable to decode image")
        return try {
            val orientation=resolver.openInputStream(uri)?.use { ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL) } ?: 1
            val degrees=when(orientation){3->180f;6->90f;8->270f;else->0f}
            if(degrees==0f) bitmap else Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,Matrix().apply{postRotate(degrees)},true).also { if(it!==bitmap) bitmap.recycle() }
        } catch (_: Exception) { bitmap }
    }
    fun save(bitmap: Bitmap, format: String, quality: Int): Uri {
        val mime=if(format.equals("PNG",true)) "image/png" else "image/jpeg"
        val ext=if(mime=="image/png") "png" else "jpg"
        val values=ContentValues().apply { put(MediaStore.Images.Media.DISPLAY_NAME,"PureCanvas_${System.currentTimeMillis()}.$ext"); put(MediaStore.Images.Media.MIME_TYPE,mime); if(Build.VERSION.SDK_INT>=29) put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/PureCanvas") }
        val uri=context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values) ?: throw IOException("Could not create gallery item")
        try { context.contentResolver.openOutputStream(uri)?.use { out -> val ok=bitmap.compress(if(ext=="png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,quality.coerceIn(1,100),out); if(!ok) throw IOException("Image encoding failed") } ?: throw IOException("Could not write image") }
        catch(e:Exception){context.contentResolver.delete(uri,null,null); throw e}
        return uri
    }
}
