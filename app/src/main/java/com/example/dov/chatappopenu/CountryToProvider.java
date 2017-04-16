package com.example.dov.chatappopenu;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dov on 09/04/2017.
 */

public class CountryToProvider{

    public static String[] getProvider(String country){
        String emptyArr[] = {""};
        String provs[] = mMap.get(country) != null ? mMap.get(country) : emptyArr;
        return provs;
    }

    private static final Map<String, String[]> mMap;
    static{
        mMap = new HashMap<String, String[]>();
        String []israrlCells = {"050", "052", "053", "054", "055", "058"};
        mMap.put("+972", israrlCells);
    }
}