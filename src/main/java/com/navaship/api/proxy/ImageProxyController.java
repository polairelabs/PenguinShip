package com.navaship.api.proxy;

import com.navaship.api.jwt.JwtService;
import com.navaship.api.shipment.Shipment;
import com.navaship.api.shipment.ShipmentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/proxy")
public class ImageProxyController {
    private ShipmentService shipmentService;
    private JwtService jwtService;


    @GetMapping("/{shipmentId}")
    public ResponseEntity<byte[]> proxyImage(JwtAuthenticationToken principal, @PathVariable Long shipmentId) {
        Shipment myShipment = shipmentService.retrieveShipment(shipmentId);
        jwtService.checkResourceBelongsToUser(principal, myShipment);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(myShipment.getPostageLabelUrl(), HttpMethod.GET, entity, byte[].class);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "image/png");
        // responseHeaders.set("Access-Control-Allow-Origin", "*");

        return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
    }
}
