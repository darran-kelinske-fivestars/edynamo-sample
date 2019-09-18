package com.fivestars.edynamosample;

/**
 * This class holds the card holder information which we can get from the card.
 */
public class CardHolderInfo {
    // The card holder's name
    private String name;
    // The first six of the card (BIN/IIN)
    private String iin;
    // The last four of the card
    private String lastFour;
    // The expiration date of the card with format YYMM
    private String expirationDate;
    // The service code from the issuer.  See https://en.wikipedia.org/wiki/Magnetic_stripe_card
    private String serviceCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIin() {
        return iin;
    }

    public void setIin(String iin) {
        this.iin = iin;
    }

    public String getLastFour() {
        return lastFour;
    }

    public void setLastFour(String lastFour) {
        this.lastFour = lastFour;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    /**
     * Determine is a card is chip enabled based on service code.
     * @return true or false as to whether or not the card is chip enabled
     */
    public boolean chipEnabled() {
        return ((serviceCode != null) && (serviceCode.startsWith("2") || serviceCode.startsWith("6")));
    }
}
