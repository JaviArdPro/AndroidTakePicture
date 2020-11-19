package com.example.tomafotoenviafoto;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Preview;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.lang.Object;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

class Mail extends javax.mail.Authenticator {
    private String _user;
    private String _pass;

    private String[] _to;
    private String _from;

    private String _port;
    private String _sport;

    private String _host;

    private String _subject;
    private String _body;

    private boolean _auth;

    private boolean _debuggable;

    private Multipart _multipart;


    public Mail() {
        _host = "smtp.gmail.com"; // default smtp server
        _port = "465"; // default smtp port
        _sport = "465"; // default socketfactory port

        _user = ""; // username
        _pass = ""; // password
        _from = ""; // email sent from
        _subject = ""; // email subject
        _body = ""; // email body

        _debuggable = false; // debug mode on or off - default off
        _auth = true; // smtp authentication - default on

        _multipart = new MimeMultipart();

        // There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added.
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    public Mail(String user, String pass) {
        this();

        _user = user;
        _pass = pass;
        Log.i("APLICACION_JAVI","asignado usuario y contraseña");
    }

    public boolean send() throws Exception {
        Properties props = _setProperties();
        Log.i("APLICACION_JAVI","users es " + _user);
        Log.i("APLICACION_JAVI","longitud de to es " + _to.length);
        Log.i("APLICACION_JAVI","from es " + _from);
        Log.i("APLICACION_JAVI","subject es " + _subject);
        Log.i("APLICACION_JAVI","body es " + _body);
        if(!_user.equals("") && !_pass.equals("") && _to.length > 0 && !_from.equals("") && !_subject.equals("") && !_body.equals("")) {
            Log.i("APLICACION_JAVI","SEND: hemos pasado el if principal");
            Session session = Session.getInstance(props, this);

            MimeMessage msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(_from));

            InternetAddress[] addressTo = new InternetAddress[_to.length];
            for (int i = 0; i < _to.length; i++) {
                addressTo[i] = new InternetAddress(_to[i]);
            }
            msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

            msg.setSubject(_subject);
            msg.setSentDate(new Date());

            // setup message body
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(_body);
            _multipart.addBodyPart(messageBodyPart);

            // Put parts in message
            msg.setContent(_multipart);
            Log.i("APLICACION_JAVI","SEND: vamos a usar Transport.send");
            // send email
            Transport.send(msg);

            return true;
        } else {
            Log.i("APLICACION_JAVI","SEND: NO hemos pasado el if principal");
            return false;
        }
    }

    public void addAttachment(String filename) throws Exception {
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);

        _multipart.addBodyPart(messageBodyPart);
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(_user, _pass);
    }

    private Properties _setProperties() {
        Properties props = new Properties();

        props.put("mail.smtp.host", _host);

        if(_debuggable) {
            props.put("mail.debug", "true");
        }

        if(_auth) {
            props.put("mail.smtp.auth", "true");
        }

        props.put("mail.smtp.port", _port);
        props.put("mail.smtp.socketFactory.port", _sport);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

        return props;
    }

    // the getters and setters
    public String getBody() {
        return _body;
    }

    public void setBody(String _body) {
        this._body = _body;
        Log.i("APLICACION_JAVI","asignado body");
    }
   /*
   *  private String[] _to;
    private String _from;

    private String _port;
    private String _sport;

    private String _host;

    private String _subject;
   * */
    public void setTo(String[] _to) {
        this._to = _to;
        Log.i("APLICACION_JAVI","asignado destinatario");
    }
    public void setFrom(String _from) {
        this._from = _from;
        Log.i("APLICACION_JAVI","asignado remitente from");
    }
    public void setSubject(String _subject) {
        this._subject = _subject;
        Log.i("APLICACION_JAVI","asignado asunto");
    }
    // more of the getters and setters …..
}

public class MainActivity extends  AppCompatActivity { //AppCompatActivity,

    static final int REQUEST_IMAGE_CAPTURE = 1;//coidgo para el resultado-tomar foto

    static final int REQUEST_TAKE_PHOTO = 2; //coidgo para el resultado-coger foto galeriaa
    //static final int RESULT_OK = 1;
    static final String TAGAPP = "JAVI_APLICACION";
    ImageView imageView = null;
    private static final int CAMERA_REQUEST = 1888;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    private File filep,storageDir;
    static Camera mCamera = null;
    private static final int CAMERA_REQUEST_CODE=777;
    private static final int BOOT_REQUEST_CODE=888;

    Preview preview;
    private Camera.Parameters parameters;
    private SurfaceHolder sHolder;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("APLICACION_JAVI", "Iniciamos la captura de la imagen");

        //android.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED)
        {Log.i("APLICACION_JAVI", "Permiso CAMARA concedido");
            //takePictureNoPreview(this.getApplicationContext());
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED)== PackageManager.PERMISSION_GRANTED)
            {   Log.i("APLICACION_JAVI", "Permiso DE INICIO concedido");

                takePictureNoPreview(this.getApplicationContext());
                //<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
            }
        }
        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.i("APLICACION_JAVI", "Permiso CAMARA NO concedido");
            Log.i("APLICACION_JAVI", "sOLICITANDO PERMISO...");

            requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED)!= PackageManager.PERMISSION_GRANTED){
            Log.i("APLICACION_JAVI", "Permiso INICIO SISTEMA NO concedido");
            Log.i("APLICACION_JAVI", "sOLICITANDO PERMISO...");
            requestPermissions(new String[] {Manifest.permission.RECEIVE_BOOT_COMPLETED}, BOOT_REQUEST_CODE);

        }
       else if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED)== PackageManager.PERMISSION_GRANTED)
        {
            takePictureNoPreview(this.getApplicationContext());


        }
        Log.i("APLICACION_JAVI", "Terminamos la aplicacion");

        // this.finish();
        //System.exit(0);
    }

    public void onRequestPermissionsResults(int requestCode, String[] permissions,
                                            int[] grantResults) {
        boolean okActivity = false;
        Log.i("APLICACION_JAVI", "onRequestPermissionResult entrando...");
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                   okActivity = true;
                }
                else
                    okActivity = false;
            case BOOT_REQUEST_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    okActivity = true;
                }
                else
                    okActivity = false;
                return;
        }
        if(okActivity){
            takePictureNoPreview(this.getApplicationContext());
        }
        // Other 'case' lines to check for other
        // permissions this app might request.

}
    private void releaseCameraAndPreview() {
        //myCameraPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            Log.i("APLICACION_JAVI", "Finalizando cámara");
        }
    }
    private int findFrontFacingCamera() {
       int cameraId=0;
      boolean cameraFront;

        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }


    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d(TAGAPP, "error al abrir la cámara: NO EXISTE O ESTA EN USO");
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    public void takePictureNoPreview(Context context) {
        // open back facing camera by default
     if (checkCameraHardware(context)) {
         Log.d("APLICACION_JAVI", "ESTE DISPOSITIVO TIENE UNA CAMARA");

     }
        //Camera myCamera;
        try {
            releaseCameraAndPreview();
            Log.d("APLICACION_JAVI", "parametros numero camaras " + Camera.getNumberOfCameras());

           // mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);


                mCamera = getCameraInstance();

        } catch (Exception e) {
            Log.e("APLICACION_JAVI", "error al abrir la cámara");
            e.printStackTrace();
        }
        Log.i("APLICACION_JAVI", "inciando takePictureNoPreview ");
        if (mCamera != null) {
            try {
                Log.i("APLICACION_JAVI", "takePictureNoPreview - Camara detectada ");

                //set camera parameters if you want to
                //...
                mCamera.lock();

                    // Hack for no preview

                    SurfaceTexture surfaceTexture = new SurfaceTexture(0);

                    mCamera.setPreviewTexture(surfaceTexture);


                Log.i("JAVI_APLICACION", "esperamos 5 segundos..");

                // Preview needs time to start
                Thread.sleep(5000);
                Log.i("APLICACION_JAVI", "takePictureNoPreview..iniciando preview ");
                parameters = mCamera.getParameters();
                mCamera.setParameters(parameters);
                mCamera.startPreview();
                //mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);

                filep = createImageFile();
                Log.i("APLICACION_JAVI","Ruta donde se pretende grabar fichero "+filep.getAbsolutePath());

                Thread.sleep(5000);
                Camera.PictureCallback jpegCallback = new Camera.PictureCallback()
                {
                    //Log.d("FO", "folder" );

                    public void onPictureTaken(final byte[] data, Camera camera)
                    {

                        FileOutputStream outStream = null;
                        try{

                             if(!filep.exists()) {
                                filep.mkdirs();
                                Log.i("FO", "folder" + Environment.getExternalStorageDirectory());
                            }

                            //Calendar cal = Calendar.getInstance();
                            //SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                            //tring tar = (sdf.format(cal.getTime()));

                            outStream = new FileOutputStream(filep.getAbsoluteFile());
                            outStream.write(data);  outStream.close();

                            Log.i("APLICACION_JAVI", data.length + " byte written to:"+ filep.getAbsolutePath());


                                Log.i("APLICACION_JAVI", "intentamos enviar email");
                                new emailSendBackground().execute();


                        } catch (FileNotFoundException e){
                            Log.d("APLICACION_JAVI", e.getMessage());
                        } catch (IOException e){
                            Log.d("APLICACION_JAVI", e.getMessage());
                        }}
                };
                Log.i("APLICACION_JAVI", "jpegCallback es " + jpegCallback);

                mCamera.takePicture(null, null, jpegCallback);

                Log.i("APLICACION_JAVI", "takePictureNoPreview..tomando foto ");

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        } else {
            //booo, failed!
            Log.i("APLICACION_JAVI", "Algo ha fallado");


        }
    }
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            Log.d("APLICACION JAVI", "onShutter'd");
        }
    };



    /** Handles data for raw picture */
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("APLICACION JAVI", "onPictureTaken - raw");
        }
    };

    //PictureCallback jpegCallback = new PictureCallback() {
    // public void onPictureTaken(byte[] data, Camera camera) {
    //private Camera.PictureCallback getJpegCallback() {

        //return jpeg;
    //}



    /* Photo album for this application */
    private String getAlbumName() {
        return currentPhotoPath;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;




   public static String getCurrentPathFile()
   {
          return currentPhotoPath;
   }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("APLICACION_JAVI", "onactivity result: entrando...");
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Image saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
                Log.i("APLICACION_JAVI", "imagen slavada en: " + data.getData());
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
                Toast.makeText(this, "Cancelado", Toast.LENGTH_LONG).show();
                Log.i("APLICACION_JAVI", "onactivity result: cancelado por usuario");
            } else {
                // Image capture failed, advise user
                Toast.makeText(this, "FALLO EN LA CAPTURA", Toast.LENGTH_LONG).show();
                Log.i("APLICACION_JAVI", "onactivity result: fallo en la captura...");
            }
        }


    }
    private static String currentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}


class emailSendBackground extends AsyncTask  {
      //@Override
      protected Object doInBackground(Object[] objects) {
        //TODO código del doInBackground (Hilo en Segundo Plano)
        Mail m = new Mail("****@gmail.com", "*****");

        String[] toArr = {"franciscoj.velasco@gmail.com"};

        m.setTo(toArr);
        m.setFrom("***gmail.com);
        m.setSubject("Asunto de prueba.");
        m.setBody("Email body.");

        try {
            m.addAttachment(MainActivity.getCurrentPathFile());
            if (m.send()) {
                //Toast.makeText(tomarenviarfoto.this, "Email was sent successfully.", Toast.LENGTH_LONG).show();
                Log.i("APLICACION_JAVI", "Se supone que se ha debido enviar el email");
            } else {
                Log.i("APLICACION_JAVI", "No se ha enviado, algo ha fallado");

                //Toast.makeText(MailApp.this, "Email was not sent.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            Log.e("APLICACION JAVI", "EXCEPCION ERROR NO SE PUDO ENVIAR EL EMAIL", exception);
        }

        cancel(true); //Opcional. Para cancelar en vez de ejecutar al terminar onPostExecute(), ejecutar onCancelled(). Comprobar si se ha cancelado con isCancelled()
        //...


        return null;
    }

    protected void onPostExecute() {
        //TODO código del onPostExecute (Hilo Principal)

        //this.finish();
        System.exit(0);

    }

    /* @Override
    protected void onProgressUpdate(Tipo_duranteBackground... varDuranteBackground) {
        //TODO código del onProgressUpdate (Hilo Principal)
    }

    @Override
    protected void onPostExecute(Tipo_terminarBackground varTerminarBackground) {
        //TODO código del onPostExecute (Hilo Principal)
    }

    @Override
    protected void onCancelled (Tipo_terminarBackground varTerminarBackground) {
        //TODO código del onCancelled (Hilo Principal)
    }
   */
}





