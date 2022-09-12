package bfst22.vector;

import java.util.regex.Pattern;

public class AdressParser {
    public final String street, house, floor, side, postcode, city;

  private AdressParser(String _street, String _house, String _floor, String _side, String _postcode, String _city) {
    street = _street;
    house = _house;
    floor = _floor;
    side = _side;
    postcode = _postcode;
    city = _city;
  }

  public String toString() {
    return street + " " + house + ", " + floor + " " + side + "\n"
      + postcode + " " + city;
  }

  private final static String REGEX = "^(?<street>[A-Za-zæøåéÆØÅ ]+?) +(?<house>[0-9]+([-]?+[0-9]+)?[A-Za-z]?+)?([, ]+(?<floor>[1-124])[., ](?<side>[A-Za-zæøå]+)?)?([, ]+(?<postcode>[0-9]{4}) +(?<city>[A-Za-zæøåÆØÅ ]+))?$";
  private final static Pattern PATTERN = Pattern.compile(REGEX);

  public static AdressParser parse(String input) {
    var builder = new Builder();
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

  public String getCity() {
      return city;
  }

  public String getFloor() {
      return floor;
  }

  public String getHouse() {
      return house;
  }

  public String getPostcode() {
      return postcode;
  }

  public String getSide() {
      return side;
  }

  public String getStreet() {
      return street;
  }


  public static class Builder {
    private String street, house, floor, side, postcode, city;

    public Builder street(String _street) {
      street = _street;
      return this;
    }

    public Builder house(String _house) {
      house = _house;
      return this;
    }

    public Builder floor(String _floor) {
      floor = _floor;
      return this;
    }

    public Builder side(String _side) {
      side = _side;
      return this;
    }

    public Builder postcode(String _postcode) {
      postcode = _postcode;
      return this;
    }

    public Builder city(String _city) {
      city = _city;
      return this;
    }

    public AdressParser build() {
      return new AdressParser(street, house, floor, side, postcode, city);
    }
  }
}
