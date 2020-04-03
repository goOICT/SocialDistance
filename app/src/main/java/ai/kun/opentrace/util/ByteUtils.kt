package ai.kun.opentrace.util

import android.util.Log
import androidx.annotation.Nullable
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import kotlin.experimental.and

object ByteUtils {

    private const val TAG = "ByteUtils"

    fun reverse(value: ByteArray): ByteArray {
        val length = value.size
        val reversed = ByteArray(length)
        for (i in 0 until length) {
            reversed[i] = value[length - (i + 1)]
        }
        return reversed
    }

    private val hexArray = "0123456789ABCDEF".toCharArray()

    private fun byteToHex(b: Byte): String {
        val char1 = Character.forDigit((b and 0xF0.toByte()) as Int shr 4, 16)
        val char2 = Character.forDigit((b and 0x0F).toInt(), 16)
        return String.format("0x%1\$s%2\$s", char1, char2)
    }

    fun byteArrayInHexFormat(byteArray: ByteArray?): String? {
        if (byteArray == null) {
            return null
        }
        val stringBuilder = StringBuilder()
        stringBuilder.append("{ ")
        for (i in byteArray.indices) {
            if (i > 0) {
                stringBuilder.append(", ")
            }
            val hexString = byteToHex(byteArray[i])
            stringBuilder.append(hexString)
        }
        stringBuilder.append(" }")
        return stringBuilder.toString()
    }

    fun bytesFromString(string: String): ByteArray {
        var stringBytes = ByteArray(0)
        try {
            stringBytes = string.toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, "Failed to convert message string to byte array")
        }
        return stringBytes
    }

    @Nullable
    fun stringFromBytes(bytes: ByteArray): String {
        return try {
            String(bytes, Charset.forName("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            Log.e(TAG, "Unable to convert message bytes to string")
            ""
        }
    }
}