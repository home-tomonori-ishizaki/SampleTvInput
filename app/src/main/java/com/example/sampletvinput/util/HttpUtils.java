package com.example.sampletvinput.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {
    public static class UnauthorizedException extends Exception {
        public UnauthorizedException() {
            super("Invalid api key");
        }
    }

    public static class BadRequestException extends Exception {
        public BadRequestException() {
            super("Invalid parameters");
        }
    }

    public static String get(String urlStr) throws UnauthorizedException, BadRequestException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int status = conn.getResponseCode();
            switch (status) {
                case HttpURLConnection.HTTP_OK:              // 200
                    return readStream(conn.getInputStream());
                case HttpURLConnection.HTTP_BAD_REQUEST:   // 400
                    throw new BadRequestException();
                case HttpURLConnection.HTTP_UNAUTHORIZED : // 401
                    throw new UnauthorizedException();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    private static String readStream(InputStream stream) throws IOException {
        StringBuffer sb = new StringBuffer();
        String line = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        while((line = br.readLine()) != null){
            sb.append(line);
        }
        try {
            stream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static byte[] getBytes(String urlStr) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            //int resp = conn.getResponseCode();
            return readStreamBytes(conn.getInputStream());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    private static byte[] readStreamBytes(InputStream is) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStream os = new BufferedOutputStream(b);

        byte[] buf = new byte[1024];
        while (true) {
            int ret = is.read(buf);
            if (ret <= 0) {
                break;
            }
            os.write(buf, 0, ret);
        }

        os.flush();
        os.close();

        return b.toByteArray();
    }
}
