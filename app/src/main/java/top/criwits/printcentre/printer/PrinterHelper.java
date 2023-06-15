package top.criwits.printcentre.printer;

import android.util.Log;

import de.gmuth.ipp.client.IppPrinter;
import de.gmuth.log.Logging;
import top.criwits.printcentre.misc.Consts;

public class PrinterHelper {
  static {
    Logging.INSTANCE.disable();
  }

  public enum PrinterStatus {
    DISCONNECTED,
    CONNECTED,
    BUSY,
    STOPPED
  }

  public static IppPrinter getPrinter() {
    return new IppPrinter(Consts.PRINTER_URI);
  }

  public static PrinterStatus checkPrinterStatus() {
    try {
      Log.i("PrinterController", "Printer state: " + getPrinter().getState());
      switch (getPrinter().getState().getCode()) {
        case 3:
          return PrinterStatus.CONNECTED;
        case 4:
          return PrinterStatus.BUSY;
        case 5:
          return PrinterStatus.STOPPED;
        default:
          return PrinterStatus.DISCONNECTED;
      }
    } catch (Exception e) {
      return PrinterStatus.DISCONNECTED;
    }
  }
}
