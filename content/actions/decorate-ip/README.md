## Decorate IP with geolocation data

This action returns geo location data of an IP address.

The action expects one input:

```json
{
  "ipAddress": ""
}
```

which returns geolocation data decorated from an MMDB source:

```json
{
  "geo": {
    "country": "US",
    "state": "California",
    "city": "San Francisco"
  },
  "asn": {
    "org": "Level Jummie Communications, Inc.",
    "num": 333
  }
}
```

If the IP is a private domain or is not discoverable in the MMDB source, then all fields will be 
returned as null:

```json
{
  "geo": {
    "country": null,
    "state": null,
    "city": null
  },
  "asn": {
    "org": null,
    "num": null
  }
}
```

Data curated from [db-ip](https://db-ip.com/db/download).