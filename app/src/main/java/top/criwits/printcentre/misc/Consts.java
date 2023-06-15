package top.criwits.printcentre.misc;

import android.os.Environment;

public class Consts {
  public static final String SERVER_IP = "172.16.0.100";
  public static final String PRINTER_IP = "172.16.1.60";

  public static final String PRINTER_URI = "ipp://" + PRINTER_IP + "/ipp/print";
  public static final String SERVER_URI = "http://" + SERVER_IP + ":14800";

  public static String getExternalPath(String path) {
    return Environment.getExternalStorageDirectory().getAbsolutePath() + path;
  }
}
