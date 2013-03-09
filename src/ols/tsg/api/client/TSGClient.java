package ols.tsg.api.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ols.tsg.api.client.TransactionRequest;


public class TSGClient {

    /**
     * @param args
     */
    public static void main(String[] args) {
        //Settings 
        String APIURL = "";
        String APIKEY = "";
        int TIMEOUT = 15000; //Milliseconds
        String LANG_TYPE = ""; //"xml" or "json"

        //Transaction info
        TransactionRequest transaction_request = new TransactionRequest();
        transaction_request.api_key = APIKEY;
        transaction_request.type = "SALE";
        transaction_request.card = "4111111111111111";
        transaction_request.csc = "123";
        transaction_request.exp_date = "1121";
        transaction_request.amount = "10.00";
        transaction_request.avs_address = "112 N. Orion Court";
        transaction_request.avs_zip = "20210";
        transaction_request.purchase_order = "10";
        transaction_request.invoice = "100";
        transaction_request.email = "email@tsg.com";
        transaction_request.customer_id = "25";
        transaction_request.order_number = "1000";
        transaction_request.client_ip = "";
        transaction_request.description = "Cel Phone";
        transaction_request.comments = "Electronic Product";

        transaction_request.billing = new Billing();
        transaction_request.billing.first_name = "Joe";
        transaction_request.billing.last_name = "Smith";
        transaction_request.billing.company = "Company Inc.";
        transaction_request.billing.street = "Street 1";
        transaction_request.billing.street2 = "Street 2";
        transaction_request.billing.city = "Jersey City";
        transaction_request.billing.state = "NJ";
        transaction_request.billing.zip = "07097";
        transaction_request.billing.country = "USA";
        transaction_request.billing.phone = "123456789";
        
        transaction_request.shipping = new Shipping();
        transaction_request.shipping.first_name = "Joe";
        transaction_request.shipping.last_name = "Smith";
        transaction_request.shipping.company = "Company 2 Inc.";
        transaction_request.shipping.street = "Street 1 2";
        transaction_request.shipping.street2 = "Street 2 2";
        transaction_request.shipping.city = "Colorado City";
        transaction_request.shipping.state = "TX";
        transaction_request.shipping.zip = "79512";
        transaction_request.shipping.country = "USA";
        transaction_request.shipping.phone = "123456789";
        
        String request;
        if("xml".equals(LANG_TYPE)){ //if XML is chosen, build a XML formatted transaction 
        	request = marshalJAXBObjectToXml(TransactionRequest.class,transaction_request);
        }else{ //if JSON is chosen, build a JSON formatted transaction 
        	//Server doesn't process next fields for JSON requests, so they are excluded from the request
        	transaction_request.avs_address=null;
        	transaction_request.avs_zip=null;
        	
        	Gson gson = new Gson();
        	request = gson.toJson(transaction_request);
        }   
        
        try  
        {
        	          
            //Execute request to gateway
            System.out.println("-----------------------------------------------------");
            System.out.println("REQUEST TO URL: " + APIURL);
            System.out.println("POST DATA: \n" + request);
            
            //Trust All certificates
            trustAllSSL();
            
            URL url = new URL(APIURL);  
            URLConnection uc = url.openConnection();  
            HttpsURLConnection conn = (HttpsURLConnection) uc;  
            conn.setDoInput(true);  
            conn.setDoOutput(true);  
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestMethod("POST");  
            conn.setRequestProperty("Content-type", "application/"+LANG_TYPE);          
            PrintWriter pw = new PrintWriter(conn.getOutputStream());  
            pw.write(request);  
            pw.close();  
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
            String response = "";
            String inputLine;
            while ((inputLine = in.readLine()) != null) 
                response += inputLine;
            in.close();  
            
      //formatting response
            String formattedResponse;
            if("xml".equals(LANG_TYPE)){ // if the chosen language was XML, then the server will respond back with XML
            	formattedResponse = formatXml(response);   //Format string to xml indented         	
            }else{   // if the chosen language was JSON, then the server will respond back with JSON
            	formattedResponse = response;				//no need to format string to json indented
            }     
            
            System.out.println("-----------------------------------------------------");
            System.out.println("RESPONSE DATA: \n" + formattedResponse);
            
          //parse response according to its content
            if("xml".equals(LANG_TYPE)){
            	parseXMLResponse(conn, response);   //parse XML response          	
            }else{ 
            	parseJSONResponse(conn, response);	//parse JSON response
            }         
        }catch (Exception e){  
            System.out.println("-----------------------------------------------------");
            System.out.println("EXCEPTION: " + e.getMessage());
        }  
    }

    public static void trustAllSSL(){
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }
    
    //Generates XML representation of an Object
    public static String marshalJAXBObjectToXml(Class objectClass,Object object){
        StringWriter writer = new StringWriter();
        
        try {
            JAXBContext context = JAXBContext.newInstance(objectClass);
            if(context != null){
                Marshaller m = context.createMarshaller();
                m.marshal(object, writer);

                return formatXml(writer.toString());
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        
        return "";
    }
    
    //Construct Object from XML representation
    public static Object unmarshalJAXBObjectToXml(Class objectClass,String xml){
        try {
            JAXBContext context = JAXBContext.newInstance(objectClass);
            if(context != null){
                Unmarshaller u = context.createUnmarshaller ();
                return u.unmarshal (new StreamSource( new StringReader( xml ) ));
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    //Format string to xml indented
    public static String formatXml(String xml){
         try{
             Transformer serializer= SAXTransformerFactory.newInstance().newTransformer();
             serializer.setOutputProperty(OutputKeys.INDENT, "yes");
             serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
             Source xmlSource=new SAXSource(new InputSource(new ByteArrayInputStream(xml.getBytes())));
             StreamResult res =  new StreamResult(new ByteArrayOutputStream());            
             serializer.transform(xmlSource, res);
             return new String(((ByteArrayOutputStream)res.getOutputStream()).toByteArray());
         }catch(Exception e){
             return xml;
         }
    }
    
    //parse XML response according to its content
    public static void parseXMLResponse(HttpsURLConnection conn, String xml_response){ 	
    	try {
			if (conn.getResponseCode() == 200 && xml_response.contains("<transaction>")) //http status 200
			{
			    TransactionResponse transaction_response = (TransactionResponse)unmarshalJAXBObjectToXml(TransactionResponse.class, xml_response);
			    
			    if (transaction_response.result_code != null && transaction_response.result_code.equals("0000"))
			    {
			        System.out.println("-----------------------------------------------------");
			        System.out.println("TRANSACTION APPROVED: " + transaction_response.authorization_code);
			    }
			    else
			    {
			        String code = "";
			        if (transaction_response.error_code != null)
			            code = transaction_response.error_code;
			        if (transaction_response.result_code != null)
			            code = transaction_response.result_code;
			        System.out.println("-----------------------------------------------------");
			        System.out.println("TRANSACTION ERROR: Code=" + code + " Message=" + transaction_response.display_message);
			    }
			}
			else{
			    System.out.println("-----------------------------------------------------");
			    System.out.println("INVALID RESPONSE");
			}
		} catch (IOException e) {
			System.out.println("-----------------------------------------------------");
            System.out.println("EXCEPTION: " + e.getMessage());
		}
    	
    }
    
   //parse JSON response according to its content
    public static void parseJSONResponse(HttpsURLConnection conn, String json_response){ 	
    	try {
			if (conn.getResponseCode() == 200 && json_response.contains("transaction")) //http status 200
			{
				Gson gson = new Gson();
			    TransactionResponse transaction_response = gson.fromJson(json_response, TransactionResponse.class);
			    
			    if (transaction_response.result_code != null && transaction_response.result_code.equals("0000"))
			    {
			        System.out.println("-----------------------------------------------------");
			        System.out.println("TRANSACTION APPROVED: " + transaction_response.authorization_code);
			    }
			    else
			    {
			        String code = "";
			        if (transaction_response.error_code != null)
			            code = transaction_response.error_code;
			        if (transaction_response.result_code != null)
			            code = transaction_response.result_code;
			        System.out.println("-----------------------------------------------------");
			        System.out.println("TRANSACTION ERROR: Code=" + code + " Message=" + transaction_response.display_message);
			    }
			}
			else{
			    System.out.println("-----------------------------------------------------");
			    System.out.println("INVALID RESPONSE");
			}
		} catch (IOException e) {
			System.out.println("-----------------------------------------------------");
            System.out.println("EXCEPTION: " + e.getMessage());
		}
    	
    }
   
}
