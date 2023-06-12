package top.criwits.printcentre;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import de.gmuth.ipp.client.IppJob;
import de.gmuth.ipp.client.IppPrinter;
import de.gmuth.ipp.client.IppSubscription;
import top.criwits.printcentre.printer.PrinterController;
import top.criwits.printcentre.printer.PrinterControllerKotlinUtils;

public class MainActivity extends KioskActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final Button startPrintingButton = findViewById(R.id.startPrintingButton);
    final Button getTicketButton = findViewById(R.id.getTicketButton);

    startPrintingButton.setOnClickListener(v -> {
      try {
        onStartPrint();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    // Init printer, using AsyncTask
  }

  private void onStartPrint() throws InterruptedException {
    ProgressDialog dialog = ProgressDialog.show(this, "", "a", true);
    // Check printer status
    if (PrinterController.checkPrinterStatus() != PrinterController.PrinterStatus.CONNECTED) {
      dialog.dismiss();
      Toast.makeText(this, R.string.printer_disconnected, Toast.LENGTH_SHORT).show();
      return;
    }
    // Check server status
    dialog.dismiss();

    // Start scanning activity!
    new IntentIntegrator(this)
        .setCaptureActivity(ScanningActivity.class)
        .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        .setPrompt("")
        .setCameraId(1)
        .setBeepEnabled(false)
        .setBarcodeImageEnabled(false)
        .initiateScan();

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    if (result != null) {
      if (result.getContents() == null) {
        // cancelled
      } else {
        // scanned
        String str = result.getContents();
        // Check if str is a UUID
        if (str.length() == 36) {
          // Check if str is a valid UUID
          try {
            UUID uuid = UUID.fromString(str);
            // Check if document is valid
            ProgressDialog dialog = ProgressDialog.show(this, "", getString(R.string.checking_ticket), true);
            dialog.dismiss();
            // Download document
            ProgressDialog dialog2 = ProgressDialog.show(this, "", getString(R.string.downloading_doc), true);
            // download document at http://172.16.0.100:20480/document/uuid as PDF
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
              ClassicHttpRequest request = ClassicRequestBuilder.get("http://172.16.0.100:20480/document/" + uuid.toString())
                  .build();
              httpClient.execute(request, response -> {
                // Save document to file
                final HttpEntity entity = response.getEntity();
                final InputStream is = entity.getContent();
                final FileOutputStream fos = new FileOutputStream(uuid.toString() + ".pdf");
                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = is.read(buffer)) != -1) {
                  fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();
                EntityUtils.consume(entity);
                httpClient.close();
                dialog2.dismiss();
                return null;
              });
            } catch (IOException e) {
              throw new RuntimeException(e);
            }

          } catch (IllegalArgumentException e) {
            // Show error
            Toast.makeText(this, R.string.invalid_qr_code, Toast.LENGTH_SHORT).show();
          }
        } else {
          // Show error
          Toast.makeText(this, R.string.invalid_qr_code, Toast.LENGTH_SHORT).show();
        }

      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

}