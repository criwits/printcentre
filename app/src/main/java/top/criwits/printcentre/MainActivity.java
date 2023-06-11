package top.criwits.printcentre;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import java.util.UUID;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
  }

  private void onStartPrint() throws InterruptedException {
    ProgressDialog dialog = ProgressDialog.show(this, "", "a", true);
    // Check printer status
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
            // Jump to CheckStatusActivity, TODO
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