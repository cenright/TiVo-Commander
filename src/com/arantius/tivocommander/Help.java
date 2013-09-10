package com.arantius.tivocommander;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Help extends Activity {
  public final void customSettings(View v) {
    Intent intent = new Intent(this, Settings.class);
    startActivity(intent);
    finish();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.help);

    Bundle bundle = getIntent().getExtras();
    if (bundle != null) {
      ((TextView) findViewById(R.id.note)).setText(bundle.getString("note"));
      findViewById(R.id.note).setVisibility(View.VISIBLE);
    }

    Utils.activateHomeButton(this);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return Utils.onOptionsItemSelected(item, this, true);
  }

  @Override
  protected void onPause() {
    super.onPause();
    Utils.log("Activity:Pause:Help");
  }

  @Override
  protected void onResume() {
    super.onResume();
    Utils.log("Activity:Resume:Help");
  }

  public final void sendReport(View v) {
    String buildProp = "unknown";
    try {
      InputStream propStream = Runtime.getRuntime().exec("/system/bin/getprop")
          .getInputStream();

      StringBuffer buildPropBuf = new StringBuffer("");
      byte[] buffer = new byte[1024];
      while (propStream.read(buffer) != -1) {
        buildPropBuf.append(new String(buffer));
      }
      buildProp = buildPropBuf.toString();
    } catch (IOException e) {
      // Ignore.
      buildProp = "Error:\n" + e.toString();
    }

    Intent i = new Intent(Intent.ACTION_SEND);
    i.setType("message/rfc822");
    i.putExtra(Intent.EXTRA_EMAIL, new String[] { "arantius+tivo@gmail.com" });
    i.putExtra(Intent.EXTRA_SUBJECT, "Error Log -- DVR Commander for TiVo");

    final String error_text =
        "Log data for the developer:\n\n"
            + "Version: " + Utils.getVersion(this) + "\n\n"
            + "Raw logs:\n" + Utils.logBufferAsString() + "\n\n"
            + "build.prop:\n" + buildProp;

    final String error_file_name = "error.txt";
    try {
      @SuppressWarnings("deprecation")
      FileOutputStream outs = openFileOutput(
          error_file_name, MODE_WORLD_READABLE);
      outs.write(error_text.getBytes(Charset.forName("UTF-8")));
      outs.close();
      // http://stackoverflow.com/a/11955326/91238
      String sdCard =
          Environment.getExternalStorageDirectory().getAbsolutePath();
      Uri uri = Uri.fromFile(new File(sdCard +
          new String(new char[sdCard.replaceAll("[^/]", "").length()])
              .replace("\0", "/..") + getFilesDir() + "/" + error_file_name));
      i.putExtra(android.content.Intent.EXTRA_STREAM, uri);
    } catch (IOException e) {
      Utils.logError("could not write error text", e);
      i.putExtra(
          Intent.EXTRA_TEXT,
          error_text + "\n\nWrite error:\n" + e.toString()
          );
    }

    try {
      this.startActivity(Intent.createChooser(i, "Send mail..."));
    } catch (android.content.ActivityNotFoundException ex) {
      Utils.toast(this, "There are no email clients installed.",
          Toast.LENGTH_SHORT);
    }

    finish();
  }
}
