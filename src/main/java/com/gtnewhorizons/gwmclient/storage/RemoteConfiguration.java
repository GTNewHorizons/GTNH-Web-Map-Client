package com.gtnewhorizons.gwmclient.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

public class RemoteConfiguration {

    public static String baseUrl = "http://127.0.0.1:8123";

    ArrayList<RemoteWorld> worlds = new ArrayList<>();
    public final ArrayList<PerMapTileDataBase> allMaps = new ArrayList<>();

    public void load() {
        try {
            URL imageURL = new URL(baseUrl + "/up/configuration");
            InputStream is = imageURL.openConnection()
                .getInputStream();

            JsonObject jsonObject = (JsonObject) Jsoner.deserialize(new InputStreamReader(is));

            JsonArray jsonWorlds = (JsonArray) jsonObject.get("worlds");

            for (Object obj : jsonWorlds) {
                if (obj instanceof JsonObject worldObj) {
                    String worldName = (String) worldObj.get("name");
                    String worldTitle = (String) worldObj.get("title");

                    JsonArray mapsArr = (JsonArray) worldObj.get("maps");

                    RemoteWorld rw = new RemoteWorld(worldName, worldTitle);
                    worlds.add(rw);

                    for (Object tmpMapObj : mapsArr) {
                        if (tmpMapObj instanceof JsonObject mapObj) {
                            String mapName = (String) mapObj.get("name");
                            String mapTitle = (String) mapObj.get("title");
                            String mapPrefix = (String) mapObj.get("prefix");

                            double[] mapToWorldArr = toDoubleArr((JsonArray) mapObj.get("maptoworld"));
                            double[] worldToMapArr = toDoubleArr((JsonArray) mapObj.get("worldtomap"));

                            RemoteMap rm = new RemoteMap(rw, mapName, mapTitle, mapPrefix);

                            rm.setMatrices(mapToWorldArr, worldToMapArr);

                            allMaps.add(rm.getTileDataBase());
                            rw.addMap(rm);
                        }
                    }
                }
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JsonException e) {
            throw new RuntimeException(e);
        }
    }

    double[] toDoubleArr(JsonArray arr) {
        double[] ret = new double[arr.size()];

        for (int i = 0; i < arr.size(); i++) ret[i] = arr.getDouble(i);

        return ret;
    }

    public ArrayList<RemoteWorld> getWorlds() {
        return worlds;
    }
}
