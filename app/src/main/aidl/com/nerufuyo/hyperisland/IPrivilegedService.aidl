package com.nerufuyo.hyperisland;

import com.nerufuyo.hyperisland.IPrivilegedLogCallback;

interface IPrivilegedService {
    void setLogCallback(IPrivilegedLogCallback callback);
    boolean setPackageNetworkingEnabled(int uid, boolean enabled);
}
