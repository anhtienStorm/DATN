package bkav.android.btalk.messaging.util;

import android.net.Uri;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by quangnd on 23/01/2018.
 * class mo rong Json serializer de co thr dong goi duoc cac thuoc tinh Uri
 */

public class UriSerializer implements JsonSerializer<Uri> {

    @Override
    public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
