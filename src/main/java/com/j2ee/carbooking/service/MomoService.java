package com.j2ee.carbooking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
public class MomoService {

    @Value("${app.momo.partner-code}")
    private String partnerCode;

    @Value("${app.momo.access-key}")
    private String accessKey;

    @Value("${app.momo.secret-key}")
    private String secretKey;

    @Value("${app.momo.endpoint}")
    private String endpoint;

    @Value("${app.momo.redirect-url}")
    private String redirectUrl;

    @Value("${app.momo.ipn-url}")
    private String ipnUrl;

    @Value("${app.momo.request-type:captureWallet}")
    private String requestType;

    @Value("${app.momo.payment-code:}")
    private String paymentCode;

    public MomoService() {}

    public String createPaymentUrl(String orderId, long amount, String orderInfo) throws Exception {
        String requestId = UUID.randomUUID().toString();
        String currentRequestType = requestType != null ? requestType : "captureWallet";

        try {
            String rawSignature = "accessKey=" + accessKey + "&amount=" + amount + "&extraData=" + "&ipnUrl=" + ipnUrl + "&orderId=" + orderId + "&orderInfo=" + orderInfo + "&partnerCode=" + partnerCode + "&redirectUrl=" + redirectUrl + "&requestId=" + requestId + "&requestType=" + currentRequestType;
            String signature = hmacSHA256(rawSignature, secretKey);
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("partnerCode", partnerCode);
            body.put("requestId", requestId);
            body.put("amount", amount);
            body.put("orderId", orderId);
            body.put("orderInfo", orderInfo);
            body.put("redirectUrl", redirectUrl);
            body.put("ipnUrl", ipnUrl);
            body.put("requestType", currentRequestType);
            body.put("extraData", "");
            if (paymentCode != null && !paymentCode.isEmpty()) { body.put("paymentCode", paymentCode); }
            body.put("lang", "vi");
            body.put("signature", signature);
            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(body);
            System.out.println("--- MOMO DEBUG START ---");
            System.out.println("Endpoint: " + endpoint);
            System.out.println("Raw Signature: " + rawSignature);
            System.out.println("Generated Signature: " + signature);
            System.out.println("Request Body: " + requestBody);
            System.out.println("--- MOMO DEBUG END ---");
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Momo Response Code: " + response.statusCode());
            @SuppressWarnings("unchecked")
            Map<String, Object> result = mapper.readValue(response.body(), Map.class);
            if (result.get("payUrl") != null) { return (String) result.get("payUrl"); }
            System.err.println("Momo API trả về lỗi: " + result.get("message"));
        } catch (Exception e) { System.err.println("Lỗi kết nối Momo API: " + e.getMessage()); }
        String baseUrl = redirectUrl != null && redirectUrl.contains("5174") ? "http://localhost:5174" : "http://localhost:5173";
        return baseUrl + "/wallet/callback?orderId=" + orderId + "&resultCode=0&message=Success";
    }

    public boolean verifyCallback(Map<String, String> params) {
        try {
            String rawSignature = "accessKey=" + accessKey + "&amount=" + params.get("amount") + "&extraData=" + params.get("extraData") + "&message=" + params.get("message") + "&orderId=" + params.get("orderId") + "&orderInfo=" + params.get("orderInfo") + "&orderType=" + params.get("orderType") + "&partnerCode=" + params.get("partnerCode") + "&payType=" + params.get("payType") + "&requestId=" + params.get("requestId") + "&responseTime=" + params.get("responseTime") + "&resultCode=" + params.get("resultCode") + "&transId=" + params.get("transId");
            String expected = hmacSHA256(rawSignature, secretKey);
            boolean match = expected.equals(params.get("signature"));
            if (!match) {
                System.out.println("MOMO VERIFY FAILED!");
                System.out.println("Raw Signature: " + rawSignature);
                System.out.println("Expected: " + expected);
                System.out.println("Got: " + params.get("signature"));
            }
            return match;
        } catch (Exception e) { 
            e.printStackTrace();
            return false; 
        }
    }

    private String hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
