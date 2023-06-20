package top.criwits.printcentre.activities;

import static de.gmuth.ipp.client.IppTemplateAttributes.copies;
import static de.gmuth.ipp.client.IppTemplateAttributes.documentFormat;
import static de.gmuth.ipp.client.IppTemplateAttributes.jobName;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.xpath.operations.Bool;

import java.io.File;

import de.gmuth.ipp.client.IppColorMode;
import de.gmuth.ipp.client.IppJob;
import de.gmuth.ipp.client.IppPrinter;
import top.criwits.printcentre.R;
import top.criwits.printcentre.misc.Consts;
import top.criwits.printcentre.printer.PrinterHelper;
import top.criwits.printcentre.printer.PrinterHelperKotlin;

public class SimplePrintActivity extends KioskActivity {
  private String uuid;
  private int copies;
  private boolean duplex;
  private ProgressBar progressBar;
  private TextView hint;

  class PreparePrintingTask extends AsyncTask<Void, String, Boolean> {
    private boolean doPrint(File file) {
      IppPrinter printer = PrinterHelper.getPrinter();
      IppJob job = printer.createJob(
          documentFormat("image/urf"),
          jobName(file.getName()),
          copies(copies),
          IppColorMode.Monochrome
      );
      try {
        publishProgress(getString(R.string.sending_document), "true");
        job.sendDocument(file);
      } catch (Exception e) {
        // In case of SocketTimeoutException, the printer is still printing
        // so it's nothing!
      }

      IppPrinter tmpPrinter = PrinterHelper.getPrinter();
      while (true) {
        tmpPrinter.updatePrinterStateAttributes();
        PrinterHelper.PrinterStatus status = PrinterHelper.checkPrinterStatus(tmpPrinter);
        if (status == PrinterHelper.PrinterStatus.BUSY) {
          publishProgress(getString(R.string.printing_ongoing), "true");
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          continue;
        }

        if (status == PrinterHelper.PrinterStatus.CONNECTED) {
          return true; // successfully finished
        }

        if (status == PrinterHelper.PrinterStatus.DISCONNECTED) {
          return false;
        }

        if (status == PrinterHelper.PrinterStatus.STOPPED) {
          // Stuck or out of paper
          publishProgress(getString(R.string.stuck_hint), "false");
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          continue;
        }
      }
    }

    private Activity activity;
    private boolean duplex;
    private File file;

    public PreparePrintingTask(Activity activity, File file) {
      this.activity = activity;
      this.file = file;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(String... values) {
      hint.setText(values[0]);
      if (Boolean.parseBoolean(values[1])) {
        progressBar.setEnabled(true);
        progressBar.setIndeterminate(true);
      } else {
        progressBar.setEnabled(false);
      }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
      return doPrint(file);
    }

    @Override
    protected void onPostExecute(Boolean result) {
      if (!result) {
        // TODO: print failed
      }
      if (file.getName().contains("-odd")) {
        // Show a dialog with 1 button to let user flip the paper
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.redelivery_the_paper);
        builder.setMessage(R.string.duplex_instruction);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.i_finished, (dialog, which) -> {
          new PreparePrintingTask(activity, new File(file.getParentFile(), file.getName().replace("-odd", "-even"))).execute();
        });
        builder.create().show();

      } else {
        activity.finish();
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_simple_print);

    uuid = getIntent().getStringExtra("uuid");
    copies = getIntent().getIntExtra("copies", 1);
    duplex = getIntent().getBooleanExtra("duplex", false);

    progressBar = findViewById(R.id.progressBar);
    progressBar.setIndeterminate(true);
    hint = findViewById(R.id.textView7);

    File file;
    if (duplex) {
      file = new File(Consts.getExternalPath("/print/" + uuid + "-odd.urf"));
    } else {
      file = new File(Consts.getExternalPath("/print/" + uuid + ".urf"));
    }
    new PreparePrintingTask(this, file).execute();
  }

  @Override
  public void onBackPressed() {
    // DO NOTHING
  }
}