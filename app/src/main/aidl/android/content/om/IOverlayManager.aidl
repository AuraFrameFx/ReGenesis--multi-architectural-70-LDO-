package android.content.om;

import android.content.om.OverlayInfo;
import android.content.om.OverlayManagerTransaction;

/**
 * AIDL interface for IOverlayManager
 * Hidden system service for managing runtime resource overlays
 */
interface IOverlayManager {
    void setEnabled(String packageName, boolean enable, int userId);
    OverlayInfo[] getOverlayInfosForTarget(String targetPackageName, int userId);
    void commit(in OverlayManagerTransaction transaction);
}
