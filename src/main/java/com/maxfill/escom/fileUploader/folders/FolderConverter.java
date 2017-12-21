package com.maxfill.escom.fileUploader.folders;

import com.google.gson.*;

import java.lang.reflect.Type;

public class FolderConverter implements JsonSerializer<Folder>, JsonDeserializer<Folder>{

    //To JSON
    public JsonElement serialize(Folder folder, Type type,
                                 JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("id", folder.getId());
        object.addProperty("name", folder.getName());
        object.addProperty("parent", folder.getParent().getId());
        return object;
    }

    //From JSON
    public Folder deserialize(JsonElement json, Type type,
                              JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        Integer id = new Integer(object.get("id").getAsInt());
        String name = new String(object.get("name").getAsString());
        Boolean readonly = new Boolean(object.get("readonly").getAsBoolean());
        return new Folder(id, name, readonly);
    }
}
