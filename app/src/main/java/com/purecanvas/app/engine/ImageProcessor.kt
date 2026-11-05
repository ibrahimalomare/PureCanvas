package com.purecanvas.app.engine

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.BlurMaskFilter
import com.purecanvas.app.domain.ProcessingSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

/** Conservative, deterministic bitmap enhancement. It never searches for or edits provenance marks. */
class ImageProcessor {
    suspend fun process(source: Bitmap, settings: ProcessingSettings, progress: (Int) -> Unit = {}): Bitmap = withContext(Dispatchers.Default) {
        require(!source.isRecycled) { "The source image is no longer available." }
        val w = source.width; val h = source.height
        require(w > 0 && h > 0)
        // Bound peak memory on devices with limited heap; preserve aspect ratio.
        val maxPixels = 24_000_000L
        val scale = if (w.toLong() * h > maxPixels) kotlin.math.sqrt(maxPixels.toDouble() / (w.toDouble() * h)).toFloat() else 1f
        val input = if (scale < 1f) Bitmap.createScaledBitmap(source, (w*scale).toInt().coerceAtLeast(1), (h*scale).toInt().coerceAtLeast(1), true) else source
        val iw=input.width; val ih=input.height
        val pixels=IntArray(iw*ih); input.getPixels(pixels,0,iw,0,0,iw,ih)
        progress(12)
        // Mild local averaging reduces sensor/JPEG noise. Blend amount is deliberately restrained.
        val radius = if (settings.denoise > .65f) 2 else 1
        if (settings.denoise > .01f) {
            val copy=pixels.copyOf()
            for (y in radius until ih-radius) for (x in radius until iw-radius) {
                val i=y*iw+x; val c=copy[i]
                var rr=0; var gg=0; var bb=0; var n=0
                for (dy in -radius..radius) for (dx in -radius..radius) { val q=copy[(y+dy)*iw+x+dx]; rr+=Color.red(q); gg+=Color.green(q); bb+=Color.blue(q); n++ }
                val a=(settings.denoise*.38f).coerceIn(0f,.38f)
                pixels[i]=Color.argb(Color.alpha(c), mix(Color.red(c),rr/n,a), mix(Color.green(c),gg/n,a), mix(Color.blue(c),bb/n,a))
            }
        }
        progress(45)
        // Global tonal and color adjustments, keeping values clipped but not posterized.
        val contrast=settings.contrast.coerceIn(.5f,1.8f); val sat=settings.saturation.coerceIn(0f,2f)
        val warmth=settings.warmth.coerceIn(-.25f,.25f); val bright=settings.brightness.coerceIn(-.25f,.25f)
        for (i in pixels.indices) {
            val c=pixels[i]; var r=Color.red(c).toFloat()/255f; var g=Color.green(c).toFloat()/255f; var b=Color.blue(c).toFloat()/255f
            val l=.2126f*r+.7152f*g+.0722f*b
            r=l+(r-l)*sat; g=l+(g-l)*sat; b=l+(b-l)*sat
            r=((r-.5f)*contrast+.5f)+bright+warmth; g=((g-.5f)*contrast+.5f)+bright; b=((b-.5f)*contrast+.5f)+bright-warmth
            pixels[i]=Color.argb(Color.alpha(c),unit(r),unit(g),unit(b))
        }
        progress(70)
        // Unsharp mask from a small neighborhood; low radius and capped gain avoid halos.
        if (settings.sharpness > .01f || settings.detail > .01f) {
            val base=pixels.copyOf(); val gain=(settings.sharpness*.45f+settings.detail*.22f).coerceIn(0f,.5f)
            for(y in 1 until ih-1) for(x in 1 until iw-1) {
                val i=y*iw+x; val c=base[i]
                for (ch in 0..2) {
                    val center=channel(c,ch)
                    val avg=(channel(base[i-1],ch)+channel(base[i+1],ch)+channel(base[i-iw],ch)+channel(base[i+iw],ch))/4f
                    val v=(center+(center-avg)*gain).toInt().coerceIn(0,255)
                    pixels[i]=setChannel(pixels[i],ch,v)
                }
            }
        }
        progress(92)
        val out=Bitmap.createBitmap(iw,ih,Bitmap.Config.ARGB_8888); out.setPixels(pixels,0,iw,0,0,iw,ih)
        if (input !== source) input.recycle()
        progress(100); out
    }
    private fun mix(a:Int,b:Int,t:Float)= (a+(b-a)*t).toInt().coerceIn(0,255)
    private fun unit(v:Float)=(v.coerceIn(0f,1f)*255f).toInt()
    private fun channel(c:Int,ch:Int)=when(ch){0->Color.red(c);1->Color.green(c);else->Color.blue(c)}
    private fun setChannel(c:Int,ch:Int,v:Int)=when(ch){0->Color.argb(Color.alpha(c),v,Color.green(c),Color.blue(c));1->Color.argb(Color.alpha(c),Color.red(c),v,Color.blue(c));else->Color.argb(Color.alpha(c),Color.red(c),Color.green(c),v)}
}
