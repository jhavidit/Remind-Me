package tech.jhavidit.remindme.room

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream
import java.util.*

//class
//Converters {
//    @TypeConverter
//    fun fromBitmap(bitmap: Bitmap?): ByteArray {
//        val outputStream = ByteArrayOutputStream()
//        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//        return outputStream.toByteArray()
//    }
//
//    @TypeConverter
//    fun toBitmap(byteArray: ByteArray): Bitmap?{
//        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
//    }
//}

//class Converters {
//    @TypeConverter
fun bitmapToString(bitmap: Bitmap?): String? {
    val baos = ByteArrayOutputStream()
    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val byte = baos.toByteArray()
    val temp = Base64.encodeToString(byte, Base64.DEFAULT)
    temp?.let {
        return temp
    } ?: run {
        return null
    }

}

fun stringToUri(path: String): Uri? {
    return Uri.parse(path)
}


fun stringToBitmap(encodedString: String?): Bitmap? {
    try {
        val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        bitmap?.let {
            return bitmap
        } ?: run {
            return null
        }
    } catch (e: Exception) {
        e.message
        return null
    }
}
//}
//
//
//
////public class ImageBitmapString {
////    @TypeConverter
////    public static String BitMapToString(Bitmap bitmap)
////    {
////        ByteArrayOutputStream baos = new ByteArrayOutputStream();
////        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
////        byte[] b = baos . toByteArray ();
////        String temp = Base64 . encodeToString (b, Base64.DEFAULT);
////        if (temp == null) {
////            return null;
////        } else
////            return temp;
////    }
////
////    @TypeConverter
////    public static Bitmap StringToBitMap(String encodedString)
////    {
////        try {
////            byte[] encodeByte = Base64 . decode (encodedString, Base64.DEFAULT);
////            Bitmap bitmap = BitmapFactory . decodeByteArray (encodeByte, 0, encodeByte.length);
////            if (bitmap == null) {
////                return null;
////            } else {
////                return bitmap;
////            }
////
////        } catch (Exception e) {
////            e.getMessage();
////            return null;
////        }
////    }