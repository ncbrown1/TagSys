package com.ucsbeci.app;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class HttpConnector {

    private String url = ""; // deleted for security purposes (private REST api)
    private String authString = "user:password"; // generalized for security purposes
    private byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
    private String encodedCredentials = new String(authEncBytes);

    public HttpConnector() {}

    private List<Integer> getIds(String table)
    {
        List<Integer> keys = new ArrayList<Integer>();
        try {
            List<JSONObject> items = sendGet(table);
            for(JSONObject jo : items) {
                keys.add(jo.getInt("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keys;
    }

    private int getMax(List<Integer> list) {
        int winner = Integer.MIN_VALUE;
        for(Integer x : list) {
            if(x > winner) {
                winner = x;
            }
        }
        if(winner != Integer.MIN_VALUE)
            return winner;
        else
            return 0;
    }

    private List<JSONObject> sendGet(String table) throws Exception
    {
        String target = url + "" + table + "/";
        List<JSONObject> list = new ArrayList<JSONObject>();

        URL obj = new URL(target);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "Basic " + encodedCredentials);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        con.disconnect();

        JSONObject source = new JSONObject(response.toString());
        JSONArray objectList = source.getJSONArray("results");
        //JSONArray objectList = new JSONArray(response.toString());


        for(int i = 0; i < objectList.length(); i++) {
            list.add((JSONObject)objectList.get(i));
        }
        return list;
    }

    public List<MyTag> getTags()
    {
        List<MyTag> result = null;
        try {
            List<JSONObject> list = sendGet("tags");
            result = new ArrayList<MyTag>();
            for (JSONObject aList : list) {
                result.add(new MyTag(aList));
            }
            sortList(0, (ArrayList) result);
        } catch (Exception e) {
            System.out.println("Could not fetch table information.");
            e.printStackTrace();
        }
        return result;
    }

    public List<Check> getChecks()
    {
        List<Check> result = null;
        try {
            List<JSONObject> list = sendGet("checks");
            result = new ArrayList<Check>();
            for (JSONObject aList : list) {
                result.add(new Check(aList));
            }
            sortList(0, (ArrayList) result);
        } catch (Exception e) {
            System.out.println("Could not fetch table information.");
            e.printStackTrace();
        }
        return result;
    }

    public List<Device> getDevices()
    {
        List<Device> result = null;
        try {
            List<JSONObject> list = sendGet("devices");
            result = new ArrayList<Device>();
            for (JSONObject aList : list) {
                result.add(new Device(aList));
            }
            sortList(0, (ArrayList) result);
        } catch (Exception e) {
            System.out.println("Could not fetch table information.");
            e.printStackTrace();
        }
        return result;
    }

    public List<Loc> getLocations()
    {
        List<Loc> result = null;
        try {
            List<JSONObject> list = sendGet("locations");
            result = new ArrayList<Loc>();
            for (JSONObject aList : list) {
                result.add(new Loc(aList));
            }
            sortList(0, (ArrayList) result);
        } catch (Exception e) {
            System.out.println("Could not fetch table information.");
            e.printStackTrace();
        }
        return result;
    }

    private void sendPut(String table, String json) throws Exception
    {
        System.out.println(json);
        String target = url + "" + table;
        URL obj = new URL(target);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        con.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(json);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        System.out.println(response.toString());
        in.close();
        con.disconnect();

    }

    public void updateTag(MyTag item)
    {
        try {
            sendPut("tags/" + item.getId() + "/", item.toString());
        } catch (Exception e) {
            System.out.println("Error in posting.");
            e.printStackTrace();
        }
    }

    public void updateDevice(Device item)
    {
        try {
            sendPut("devices/" + item.getId() + "/", item.toString());
        } catch (Exception e) {
            System.out.println("Error in posting.");
            e.printStackTrace();
        }
    }

    private void sendPost(String table, String json) throws Exception
    {
        System.out.println(json);
        String target = url + "" + table + "/";
        URL obj = new URL(target);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        con.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(json);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        System.out.println(response.toString());
        in.close();
        con.disconnect();

    }

    public void postTag(MyTag item)
    {
        int newId = getMax(getIds("tags")) + 1;
        item.setId(newId);
        try {
            sendPost("tags", item.toString());
        } catch (Exception e) {
            System.out.println("Error in posting.");
            e.printStackTrace();
        }
    }

    public void postCheck(Check item)
    {
        item.setId(getMax(getIds("checks")) + 1);
        try {
            sendPost("checks", item.toString());

        } catch (Exception e) {
            System.out.println("Error in posting.");
            e.printStackTrace();
        }
    }

    private Session emailSession() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "false");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "<your-smtp-server>"); // generalized for security purposes
        properties.put("mail.smtp.port", "587");
        return Session.getInstance(properties);
    }

    private Message createMessage(String email, String subject, String messageBody, Session session) throws MessagingException, UnsupportedEncodingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("tagsys-rounds@domain.com", "Android Bot")); // generalized for security purposes
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email, email));
        message.setSubject(subject);
        message.setText(messageBody);
        return message;
    }

    public void sendMail(Check item, MyTag tag) {
        Session session = emailSession();
        String email = "administrator@domain.com"; // generalized for security purposes
        String subject = "TagSys Rounds Check - " + tag.getLocation() + " - " + tag.getDescription();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String messageBody = "Tag " + tag.getId() + " has been checked in to with the following information:\n" +
                "\tTag Info:\n" +
                "\t\tid: " + tag.getId() + "\n" +
                "\t\tLocation: " + tag.getLocation() + "\n" +
                "\t\tDescription: " + tag.getDescription() + "\n\n" +
                "\tCheck Info:\n" +
                "\t\tStatus: " + item.getStatus() + "\n" +
                "\t\tNotes: " + item.getNotes() + "\n" +
                "\t\tUser: " + item.getUser() + "\n" +
                "\t\tTime: " + dateFormat.format(date);
        try {
            Message message = createMessage(email, subject, messageBody, session);
            Transport.send(message);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void postDevice(Device item)
    {
        item.setId(getMax(getIds("devices")) + 1);
        try {
            sendPost("devices", item.toString());
        } catch (Exception e) {
            System.out.println("Error in posting.");
            e.printStackTrace();
        }
    }

    public void postLocation(Loc loc)
    {
        loc.setId(getMax(getIds("locations")) + 1);
        try {
            sendPost("locations", loc.toString());
        } catch (Exception e) {
            System.out.println("Error in posting.");
            e.printStackTrace();
        }
    }

    private void sortList(int startIndex, List<ECIobj> list) {
        int maxIndex = getMaxIndex(startIndex, list);
        if(maxIndex != startIndex) {
            ECIobj temp = list.get(maxIndex);
            list.set(maxIndex, list.get(startIndex));
            list.set(startIndex, temp);
        }
        startIndex++;
        if(startIndex < list.size()) {
            sortList(startIndex, list);
        }
    }

    private int getMaxIndex(int startIndex, List<ECIobj> list) {
        int max = list.get(startIndex).getId();
        int maxIndex = startIndex;

        for(int i = startIndex+1; i < list.size(); i++) {
            if(max < list.get(i).getId()) {
                max = list.get(i).getId();
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private String delete(String table, int primary_id) throws Exception
    {
        String target = url + table + "/" + primary_id + "/";

        URL obj = new URL(target);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Basic " + encodedCredentials);
        con.setRequestMethod("DELETE");

        String response = con.getResponseMessage();
        con.disconnect();
        return response;
    }

    public void deleteTag(MyTag item)
    {
        try {
            delete("tags", item.getId());
        } catch (Exception e) {
            System.out.println("Error in deleting.");
            e.printStackTrace();
        }
    }

    public void deleteDevice(Device item)
    {
        try {
            delete("devices", item.getId());
        } catch (Exception e) {
            System.out.println("Error in deleting.");
            e.printStackTrace();
        }
    }

    public void deleteCheck(Check item)
    {
        try {
            delete("checks", item.getId());
        } catch (Exception e) {
            System.out.println("Error in deleting.");
            e.printStackTrace();
        }
    }

    public void deleteLocation(Loc item)
    {
        try {
            delete("locations", item.getId());
        } catch (Exception e) {
            System.out.println("Error in deleting.");
            e.printStackTrace();
        }
    }
}
