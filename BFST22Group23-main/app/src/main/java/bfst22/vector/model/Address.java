package bfst22.vector.model;

import java.io.Serializable;
import java.util.regex.Pattern;

public class Address implements Serializable {

    public final String finalStreet, finalHouse, finalFloor, finalSide, finalPostcode, finalCity;

    public Address(String _street, String _house, String _floor, String _side, String _postcode, String _city) {
        finalStreet = _street;
        finalHouse = _house;
        finalFloor = _floor;
        finalSide = _side;
        finalPostcode = _postcode;
        finalCity = _city;
    }

    public String getStreet(){
        return finalStreet;
    }

    public String getHousenumber(){
        return finalHouse;
    }

    public String getFloor(){
        return finalFloor;
    }

    public String getSide(){
        return finalSide;
    }

    public String getPostcode(){
        return finalPostcode;
    }

    public String getCity(){
        return finalCity;
    }
    
    public String toString() {
        return finalStreet + " " + finalHouse + ", " + finalFloor + " " + finalSide + ", "
        + finalPostcode + " " + finalCity;
    }


    private final static String REGEX = "^(?<street>[A-Za-zæøåéÆØÅ ]+?) +(?<house>[0-9]+([-]?+[0-9]+)?[A-Za-z]?+)?([, ]+(?<floor>[1-124])[., ](?<side>[A-Za-zæøå]+)?)?([, ]+(?<postcode>[0-9]{4}) +(?<city>[A-Za-zæøåÆØÅ ]+))?$";
    private final static Pattern PATTERN = Pattern.compile(REGEX);

    public static Address parse(String input) {
        var builder = new AddressBuilder();
        var matcher = PATTERN.matcher(input);
        if (matcher.matches()) {
            builder.street(matcher.group("street"));
            builder.house(matcher.group("house"));
            builder.postcode(matcher.group("postcode"));
            builder.city(matcher.group("city"));
            builder.floor(matcher.group("floor"));
            builder.side(matcher.group("side"));
        }
        return builder.build();
    }
}


