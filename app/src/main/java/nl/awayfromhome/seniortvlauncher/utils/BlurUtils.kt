package nl.awayfromhome.seniortvlauncher.utils

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

object BlurUtils {

    fun blur(context: Context, bitmap: Bitmap, radius: Int): Bitmap {
        val clampedRadius = radius.coerceIn(1, 25)
        return try {
            blurWithRenderScript(context, bitmap, clampedRadius.toFloat())
        } catch (e: Exception) {
            stackBlur(bitmap, clampedRadius)
        }
    }

    @Suppress("DEPRECATION")
    private fun blurWithRenderScript(context: Context, bitmap: Bitmap, radius: Float): Bitmap {
        val output = bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, true)
        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(rs, bitmap)
        val out = Allocation.createFromBitmap(rs, output)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setRadius(radius)
        script.setInput(input)
        script.forEach(out)
        out.copyTo(output)
        rs.destroy()
        return output
    }

    private fun stackBlur(sentBitmap: Bitmap, radius: Int): Bitmap {
        val bitmap = sentBitmap.copy(sentBitmap.config ?: Bitmap.Config.ARGB_8888, true)
        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rSum: Int
        var gSum: Int
        var bSum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        val vmin = IntArray(maxOf(w, h))

        var divSum = (div + 1) shr 1
        divSum *= divSum
        val dv = IntArray(256 * divSum)
        i = 0
        while (i < 256 * divSum) {
            dv[i] = i / divSum
            i++
        }

        yi = 0
        var yw = 0

        val stack = Array(div) { IntArray(3) }
        var stackPointer: Int
        var stackStart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routSum: Int
        var goutSum: Int
        var boutSum: Int
        var rinSum: Int
        var ginSum: Int
        var binSum: Int

        y = 0
        while (y < h) {
            bSum = 0
            gSum = bSum
            rSum = gSum
            boutSum = rSum
            goutSum = boutSum
            routSum = goutSum
            binSum = routSum
            ginSum = binSum
            rinSum = ginSum

            i = -radius
            while (i <= radius) {
                p = pix[yi + minOf(wm, maxOf(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - kotlin.math.abs(i)
                rSum += sir[0] * rbs
                gSum += sir[1] * rbs
                bSum += sir[2] * rbs
                if (i > 0) {
                    rinSum += sir[0]
                    ginSum += sir[1]
                    binSum += sir[2]
                } else {
                    routSum += sir[0]
                    goutSum += sir[1]
                    boutSum += sir[2]
                }
                i++
            }
            stackPointer = radius

            x = 0
            while (x < w) {
                r[yi] = dv[rSum]
                g[yi] = dv[gSum]
                b[yi] = dv[bSum]
                rSum -= routSum
                gSum -= goutSum
                bSum -= boutSum
                stackStart = stackPointer - radius + div
                sir = stack[stackStart % div]
                routSum -= sir[0]
                goutSum -= sir[1]
                boutSum -= sir[2]
                if (y == 0) vmin[x] = minOf(x + radius + 1, wm)
                p = pix[yw + vmin[x]]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rinSum += sir[0]
                ginSum += sir[1]
                binSum += sir[2]
                rSum += rinSum
                gSum += ginSum
                bSum += binSum
                stackPointer = (stackPointer + 1) % div
                sir = stack[stackPointer % div]
                routSum += sir[0]
                goutSum += sir[1]
                boutSum += sir[2]
                rinSum -= sir[0]
                ginSum -= sir[1]
                binSum -= sir[2]
                yi++
                x++
            }
            yw += w
            y++
        }

        x = 0
        while (x < w) {
            bSum = 0
            gSum = bSum
            rSum = gSum
            boutSum = rSum
            goutSum = boutSum
            routSum = goutSum
            binSum = routSum
            ginSum = binSum
            rinSum = ginSum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = maxOf(0, yp) + x
                sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - kotlin.math.abs(i)
                rSum += r[yi] * rbs
                gSum += g[yi] * rbs
                bSum += b[yi] * rbs
                if (i > 0) {
                    rinSum += sir[0]
                    ginSum += sir[1]
                    binSum += sir[2]
                } else {
                    routSum += sir[0]
                    goutSum += sir[1]
                    boutSum += sir[2]
                }
                if (i < hm) yp += w
                i++
            }
            yi = x
            stackPointer = radius
            y = 0
            while (y < h) {
                pix[yi] = -0x1000000 or (dv[rSum] shl 16) or (dv[gSum] shl 8) or dv[bSum]
                rSum -= routSum
                gSum -= goutSum
                bSum -= boutSum
                stackStart = stackPointer - radius + div
                sir = stack[stackStart % div]
                routSum -= sir[0]
                goutSum -= sir[1]
                boutSum -= sir[2]
                if (x == 0) vmin[y] = minOf(y + r1, hm) * w
                p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                rinSum += sir[0]
                ginSum += sir[1]
                binSum += sir[2]
                rSum += rinSum
                gSum += ginSum
                bSum += binSum
                stackPointer = (stackPointer + 1) % div
                sir = stack[stackPointer % div]
                routSum += sir[0]
                goutSum += sir[1]
                boutSum += sir[2]
                rinSum -= sir[0]
                ginSum -= sir[1]
                binSum -= sir[2]
                yi += w
                y++
            }
            x++
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }
}
