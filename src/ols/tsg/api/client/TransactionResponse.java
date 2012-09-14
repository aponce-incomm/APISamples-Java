package ols.tsg.api.client;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "transaction")
public class TransactionResponse {
    public String id;
    public String result;
    public String display_message;
    public String result_code;
    public String avs_result_code;
    public String csc_result_code;
    public String error_code;
    public String authorization_code;
    
    public TransactionResponse() {
        super();
    }

}
