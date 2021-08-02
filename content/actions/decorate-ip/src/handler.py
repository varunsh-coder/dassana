from json import load, dumps, loads
from typing import Dict, Any
from aws_lambda_powertools.utilities.typing import LambdaContext
from aws_lambda_powertools.utilities.validation import validator
from geoip2.database import Reader
from geoip2.errors import AddressNotFoundError

inbound_schema = load(open('input.json', 'r'))
outbound_schema = load(open('output.json', 'r'))

city_reader = Reader('/opt/dbip-city-lite-2021-07.mmdb')
country_reader = Reader('/opt/dbip-country-lite-2021-07.mmdb')
asn_reader = Reader('/opt/dbip-asn-lite-2021-07.mmdb')


class GeoDecorateObject:
    def __init__(self, ip):
        self.geo = self.Geo(ip)
        self.asn = self.Asn(ip)

    def toJSON(self):
        return dumps(self, default=lambda o: o.__dict__,
                     sort_keys=True, indent=4)

    class Geo:
        def __init__(self, ip):
            try:
                city = city_reader.city(ip)
                country = country_reader.country(ip)
                self.country = country.country.iso_code
                self.state = city.raw.get('subdivisions')[0].get('names').get(
                    'en')
                self.city = city.city.names.get('en')
            except AddressNotFoundError as e:
                self.country = None
                self.state = None
                self.city = None

        def toJSON(self):
            return dumps(self, default=lambda o: o.__dict__,
                         sort_keys=True, indent=4)

    class Asn:
        def __init__(self, ip):
            try:
                asn = asn_reader.asn(ip)
                self.org = asn.autonomous_system_organization
                self.num = asn.autonomous_system_number
            except AddressNotFoundError as e:
                self.org = None
                self.num = None

        def toJSON(self):
            return dumps(self, default=lambda o: o.__dict__,
                         sort_keys=True, indent=4)


@validator(inbound_schema=inbound_schema, outbound_schema=outbound_schema)
def handle(event: Dict[str, Any], context: LambdaContext):
    ip = event.get('ipAddress')

    return loads(GeoDecorateObject(ip).toJSON())
