package singh.saurabh.godogs.mailSendingTask;

/**
 * Created by ${SAURBAH} on ${10/29/14}.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.Security;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.sql.DataSource;

import singh.saurabh.godogs.R;

public class GMailSender extends javax.mail.Authenticator {
    private String mailhost = "smtp.gmail.com";
    private String user;
    private String password;
    private Session session;
    private Context mContext;

    private mailSendingTask mailObject = null;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public GMailSender(String user, String password, Context context) {
        this.user = user;
        this.password = password;
        this.mContext = context;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients, File file) throws Exception {

        mailObject = new mailSendingTask(subject,body,sender,recipients, file);
        mailObject.execute((Void) null);

    }

    public class ByteArrayDataSource implements DataSource, javax.activation.DataSource {

        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {

            return null;
        }

        @Override
        public int getLoginTimeout() throws SQLException {

            return 0;
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {


        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {


        }

        @Override
        public boolean isWrapperFor(Class<?> arg0) throws SQLException {

            return false;
        }

        @Override
        public <T> T unwrap(Class<T> arg0) throws SQLException {

            return null;
        }

        @Override
        public Connection getConnection() throws SQLException {

            return null;
        }

        @Override
        public Connection getConnection(String theUsername, String thePassword)
                throws SQLException {

            return null;
        }
    }

    public class mailSendingTask extends AsyncTask<Void, Void, Boolean> {

        private final String mSubject, mBody, mSender, mRecipients;
        private File mAttachment;

        mailSendingTask (String subject, String body, String sender, String recipients, File file) {
            mSubject = subject;
            mBody = body;
            mSender = sender;
            mRecipients = recipients;
            mAttachment = file;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                MimeMessage msg = new MimeMessage(session);
                DataHandler handler = new DataHandler(new ByteArrayDataSource(mBody.getBytes(), "text/plain"));
                msg.setFrom(new InternetAddress(mSender, "Team GoDogs"));
                msg.setRecipients(Message.RecipientType.TO, mRecipients);
                msg.setSubject(mSubject);
                msg.setSentDate(new Date());
                msg.setDataHandler(handler);
//                msg.setText(mBody);
                if (mAttachment != null) {
                    MimeBodyPart mbp1 = new MimeBodyPart();
                    mbp1.setText(mBody);
                    MimeBodyPart mbp2 = new MimeBodyPart();
                    FileDataSource fds = new FileDataSource(mAttachment);
                    mbp2.setDataHandler(new DataHandler(fds));
                    mbp2.setFileName(fds.getName());
                    Multipart mp = new MimeMultipart();
                    mp.addBodyPart(mbp1);
                    mp.addBodyPart(mbp2);
                    msg.setContent(mp);
                } else {
                    msg.setText(mBody);
                }
                Transport.send(msg);
                return true;
            } catch (Exception e) {
                Log.v("send failed,", " exception: " + e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result)
                Toast.makeText(mContext, "Mail sent...", Toast.LENGTH_SHORT).show();
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Holo_Dialog));
                builder.setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle(R.string.some_error_occurred)
                        .setMessage("Please try again")
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mailObject = new mailSendingTask(mSubject, mBody, mSender, mRecipients, mAttachment);
                                mailObject.execute((Void) null);
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        }
    }
}