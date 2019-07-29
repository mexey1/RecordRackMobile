package com.geckosolutions.recordrack.interfaces;

import org.json.JSONObject;

/**
 * Created by anthony1 on 3/1/16.
 * this interface is used to monitor changes in the units
 */
public interface NewStockFragmentInterface
{
    //void onUnitsAdded(JSONObject obj);
    void onUnitsRemoved(int position);
    void onUnitsEdited(String object, int position);
    void onItemNameEdited(String... names);
}
