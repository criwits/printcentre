package top.criwits.printcentre.network;

import org.htmlunit.org.apache.http.HttpResponse;
import org.htmlunit.org.apache.http.HttpStatus;
import org.htmlunit.org.apache.http.client.HttpClient;
import org.htmlunit.org.apache.http.client.methods.HttpGet;
import org.htmlunit.org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class NetworkHelper {
  public static boolean downloadFile(String uri, File file) {
    HttpGet get = new HttpGet(uri);
    try {
      HttpClient client = HttpClientBuilder.create().build();
      HttpResponse response = client.execute(get);

      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        return false;
      }

      InputStream inputStream = response.getEntity().getContent();
      FileOutputStream fileOutputStream = new FileOutputStream(file);

      byte[] buffer = new byte[1024];
      int len;
      while ((len = inputStream.read(buffer)) != -1) {
        fileOutputStream.write(buffer, 0, len);
      }
      fileOutputStream.close();
      inputStream.close();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static String getString(String uri) {
    HttpGet get = new HttpGet(uri);
    try {
      HttpClient client = HttpClientBuilder.create().build();
      HttpResponse response = client.execute(get);

      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        return null;
      }
      InputStream is = response.getEntity().getContent();
      Scanner s = new Scanner(is).useDelimiter("\\A");
      String result = s.hasNext() ? s.next() : null;
      is.close();

      return result;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
