package com.example.jacekmichalik.idomapp.FloorMapPackage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloorItemsList implements Comparator<FloorItemsList.SecurItemData>{

    public String  floorName = "";
    public List<SecurItemData> siList = new ArrayList<SecurItemData>();

    public FloorItemsList(String floorName){
        this.floorName = floorName;
    }

    public void addItem(SecurItemData item) {
        siList.add(item);
    }

    public SecurItemData get(int position){
        return siList.get(position);
    }

    public int getSize(){
        return siList.size();
    }

    @Override
    public int compare(SecurItemData o1, SecurItemData o2) {

        int c;

        if ( o1 == o2 )
            return 0;
        if ( null == o1 )
            return -1;
        if ( null == o2 )
            return 1;

        // są różne pokoje
        c = o1.roomName.compareToIgnoreCase(o2.roomName) ;
        if ( c != 0 )
            return c;

        // pokoje są te same, sortój po typie
        c = o1.type.compareToIgnoreCase(o2.type) ;
        if ( c != 0 )
            return -c;

        // ten sam typ, sortuj po nazwie
        return  o1.name.compareToIgnoreCase(o2.name) ;
    }

    public void sortMe(){
        Collections.sort(siList,this);
    }

    public static class SecurItemData {
        public String securID;
        public String type;
        public String name;
        public String roomName;
        public String state;

        public SecurItemData(String securID, String type, String name, String roomName, String state) {
            this.securID = securID;
            this.type = type;
            this.name = name;
            this.roomName = roomName;
            this.state = state;
        }

        @Override
        public String toString() {
            return securID + "/"+type+": "  + name + "["+state+"]";
        }
    }

}
