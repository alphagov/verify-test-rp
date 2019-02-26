package uk.gov.ida.rp.testrp.tokenservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenDto {
    private int epoch;
    private DateTime validUntil;
    private String issuedTo;

    // all of these annotations are required, otherwise an error won't be thrown for missing fields
    @JsonCreator
    public TokenDto(@JsonProperty(value = "epoch", required = true) int epoch,
                     @JsonProperty(value = "validUntil", required = true) DateTime validUntil,
                     @JsonProperty(value = "issuedTo", required = true) String issuedTo) {
        this.epoch = epoch;
        this.validUntil = validUntil;
        this.issuedTo = issuedTo;
    }

    public int getEpoch() {
        return epoch;
    }

    public DateTime getValidUntil() {
        return validUntil;
    }

    public String getIssuedTo() {
        return issuedTo;
    }
}
