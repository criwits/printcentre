package top.criwits.printcentre.printer;

import java.util.concurrent.Executor;

import de.gmuth.ipp.client.IppPrinter;
import de.gmuth.log.Logging;

public class PrinterController {
  static {
    Logging.INSTANCE.disable();
  }

  public enum PrinterStatus {
    DISCONNECTED,
    CONNECTED,
    BUSY,
    STOPPED
  }

  private static String printerURI = "ipp://172.16.2.206/ipp/print";



  public static IppPrinter getPrinter() {
    return new IppPrinter(printerURI);
  }


  public static PrinterStatus checkPrinterStatus() {
    try {
      class PrinterStatusRunnable implements Runnable {
        private int status;

        public PrinterStatusRunnable(int status) {
          this.status = status;
        }

        @Override
        public void run() {
          this.status = getPrinter().getState().getCode();
        }
      }
      PrinterStatusRunnable runnable = new PrinterStatusRunnable(0);
      Thread thread = new Thread(runnable);
      thread.start();
      thread.join();
      switch (runnable.status) {
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

  public static void runInThreadAndBlock(Runnable r) {

  }

}
