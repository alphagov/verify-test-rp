package uk.gov.ida.rp.testrp.tokenservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BadTokenDto {
    private int epoch;
    private String issuedTo;

    // all of these annotations are required, otherwise an error won't be thrown for missing fields
    @JsonCreator
    public BadTokenDto(@JsonProperty(value = "epoch", required = true) int epoch,
                       @JsonProperty(value = "issuedTo", required = true) String issuedTo) {
        this.epoch = epoch;
        this.issuedTo = issuedTo;
    }

    public int getEpoch() {
        return epoch;
    }

    public String getIssuedTo() {
        return issuedTo;
    }
}
