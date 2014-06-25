/*
 * EntityGroup.java
 *
 *  DMXControl for Android
 *
 *  Copyright (c) 2012 DMXControl-For-Android. All rights reserved.
 *
 *      This software is free software; you can redistribute it and/or
 *      modify it under the terms of the GNU General Public License
 *      as published by the Free Software Foundation; either
 *      version 3, june 2007 of the License, or (at your option) any later version.
 *
 *      This software is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *      General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public
 *      License (gpl.txt) along with this software; if not, write to the Free Software
 *      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *      For further information, please contact info [(at)] dmxcontrol.de
 *
 * 
 */

package de.dmxcontrol.device;

import android.util.Log;

import org.json.JSONObject;

import de.dmxcontrol.app.DMXControlApplication;
import de.dmxcontrol.device.EntityManager.Type;

public class EntityGroup extends Entity {
    public final static String defaultDeviceGroupIcon = "device_group_new.png";
    public static String NetworkID = "DeviceGroup";
    
    @Override
    public String getNetworkID() {
        return NetworkID;
    }

    @Override
    public void Send() {

    }

    public EntityGroup(int id) {
        super(id, NetworkID + ": " + id, Type.GROUP);
        mImage = defaultDeviceGroupIcon;
    }

    public EntityGroup(int id, String name) {
        super(id, name, Type.GROUP);
        mImage = defaultDeviceGroupIcon;
    }

    public EntityGroup(int id, String name, String image) {
        super(id, name, Type.GROUP);
        mImage = image;
    }

    public static EntityGroup Receive(JSONObject o) {
        EntityGroup entity = null;
        try {
            if (o.getString("Type").equals(NetworkID)) {
                entity = new EntityGroup(o.getInt("Number"), o.getString("Name"), o.getString("Image"));
                entity.guid = o.getString("GUID");
            }
        }
        catch(Exception e)
        {
            Log.e("UDP Listener", e.getMessage());
            DMXControlApplication.SaveLog();
        }
        return entity;
    }
}
