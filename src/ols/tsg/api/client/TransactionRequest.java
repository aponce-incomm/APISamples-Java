package ols.tsg.api.client;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "transaction")
public class TransactionRequest {
    public String api_key;
    public String type;
    public String card;
    public String csc;
    public String exp_date;
    public String amount;
    public String avs_address;
    public String avs_zip;
    public String purchase_order;
    public String invoice;
    public String email;
    public String customer_id;
    public String order_number;
    public String client_ip;
    public String description;
    public String comments;
    public Shipping shipping;
    public Billing billing;
    
    public TransactionRequest() {
        super();
    }

}


