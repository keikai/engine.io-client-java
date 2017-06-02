package kk.socket.engineio.client;

import kk.json.JSONArray;
import kk.json.JSONObject;
import kk.json.JSONValue;

public class HandshakeData {

    public String sid;
    public String[] upgrades;
    public long pingInterval;
    public long pingTimeout;

    /*package*/ HandshakeData(String data) {
        this((JSONObject) JSONValue.parse(data));
    }

    /*package*/ HandshakeData(JSONObject data) {
        JSONArray<String> upgrades = (JSONArray<String>) data.get("upgrades");
        int length = upgrades.size();
        String[] tempUpgrades = new String[length];
        for (int i = 0; i < length; i ++) {
            tempUpgrades[i] = upgrades.get(i);
        }

        this.sid = (String) data.get("sid");
        this.upgrades = tempUpgrades;
        this.pingInterval = ((Number)data.get("pingInterval")).longValue();
        this.pingTimeout = ((Number) data.get("pingTimeout")).longValue();
    }
}
