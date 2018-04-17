package com.example.jacekmichalik.idomapp.FloorMapPackage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FloorItemsList implements Comparator<FloorItemsList.SecurItemData> {

    final public static String TYPE_LIGHT = "light";
    final public static String TYPE_HEATER = "heater";
    final public static String TYPE_ROOM = "room";

    public String floorName;
    private LinkedList<SecurItemData> siList = new LinkedList<>();

    public FloorItemsList(String floorName) {
        this.floorName = floorName;
    }

    public void addItem(SecurItemData item) {
//        if (item.type.equals(TYPE_LIGHT))
            siList.add(item);
    }

    public SecurItemData get(int position) {
        return siList.get(position);
    }

    public int getSize() {
        return siList.size();
    }

    public void clearAll() {
        siList.clear();
    }

    private int subCompare(SecurItemData o1, SecurItemData o2) {

        int c;

        if (o1 == o2)
            return 0;
        if (null == o1)
            return -1;
        if (null == o2)
            return 1;

        // są różne pokoje
        c = o1.roomName.compareToIgnoreCase(o2.roomName);
        if (c != 0)
            return c;

        // pokoje są te same, sortój po typie
        c = o1.type.compareToIgnoreCase(o2.type);
        if (c != 0)
            return -c;

        // ten sam typ, sortuj po nazwie
        return o1.name.compareToIgnoreCase(o2.name);
    }

    @Override
    public int compare(SecurItemData o1, SecurItemData o2) {
        return -subCompare(o1, o2);
    }

    public void orderMe() {
        Collections.sort(siList, this);
        // a teraz powstawiam "pokoje" do listy

//        if ( siList != null)            return;

        String lastRoomName = "";
        SecurItemData securItemData;
        int cur_idx = 0;
        int startsiz = siList.size();

        while (cur_idx < siList.size()) {
            securItemData = siList.get(cur_idx);
            if (lastRoomName.compareToIgnoreCase(securItemData.roomName) != 0) {
                lastRoomName = securItemData.roomName;
                // wstaw nowy obiekt - pokój
                siList.add(cur_idx, new SecurItemData(
                        "0", TYPE_ROOM, lastRoomName, lastRoomName, "",
                        getRoomTemps(lastRoomName)));
            }
            cur_idx++;

        }

        // na koniec usuń obiekty typu HEATER
        for (int i = 0; i < siList.size(); i++) {
            if (siList.get(i).type.equals(TYPE_HEATER))
                siList.remove(i);
        }

    }

    public String getRoomTemps(String roomName) {
        for (SecurItemData si : siList) {
            if (si.type.equals(TYPE_HEATER) && si.roomName.equals(roomName))
                return si.addInfo;
        }
        return "";
    }

    public static class SecurItemData {
        public String securID;
        public String type;
        public String name;
        public String roomName;
        public String state;
        public String addInfo;

        public SecurItemData(String securID, String type, String name, String roomName, String state, String addInfo) {
            this.securID = securID;
            this.type = type;
            this.name = name;
            this.roomName = roomName;
            this.state = state;
            this.addInfo = addInfo;
        }

        @Override
        public String toString() {
            return securID + "/" + type + ": " + name + "[" + state + "]";
        }
    }

}
