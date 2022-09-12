package bfst22.vector.model;

public class AddressBuilder {
        private String street, house, floor, side, postcode, city;
        
        public void street(String _street) {
            street = _street;
        }

        public void house(String _house) {
            house = _house;
        }

        public void floor(String _floor) {
            floor = _floor;
        }

        public void side(String _side) {
            side = _side;
        }

        public void postcode(String _postcode) {
            postcode = _postcode;
        }

        public void city(String _city) {
            city = _city;
        }
        
        public Address build() {
            return new Address(street, house, floor, side, postcode, city);
        }
    
}
